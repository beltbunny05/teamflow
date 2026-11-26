package com.teamflow.planning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.teamflow.planning.api.*;
import com.teamflow.planning.client.*;
import com.teamflow.planning.repository.PlanningRepository;
import com.teamflow.planning.service.PlanningService;
import org.junit.jupiter.api.Test;
import java.time.*; import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class PlanningServiceTest {
    private final PlanningRepository repository = mock(PlanningRepository.class);
    private final PlanningService service = new PlanningService(repository, objectMapper());
    private final UUID organization = UUID.randomUUID();
    private final UUID resource = UUID.randomUUID();

    @Test
    void detectsOverlappingAllocationAsBlocking() {
        UUID project = UUID.randomUUID();
        when(repository.allocations(any(), isNull(), eq("EMPLOYEE"), eq(resource), any(), any()))
                .thenReturn(List.of(new AllocationResponse(UUID.randomUUID(), organization, project, "EMPLOYEE", resource,
                        LocalDate.of(2026, 1, 3), LocalDate.of(2026, 1, 5), false, "CONFIRMED", OffsetDateTime.now(), OffsetDateTime.now())));
        var conflicts = service.conflicts(organization, new AllocationRequest(project, "EMPLOYEE", resource,
                LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 7), false));
        assertThat(conflicts).anyMatch(c -> c.type().equals("OVERLAPPING_ALLOCATION") && c.severity().equals("BLOCKING"));
    }

    @Test
    void rejectsReversedCalendarPeriod() {
        var request = new AllocationRequest(UUID.randomUUID(), "TEAM", resource,
                LocalDate.of(2026, 2, 2), LocalDate.of(2026, 2, 1), false);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.create(organization, request))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repository);
    }

    @Test
    void fractionalExecutionProducesOnlyEffectiveServiceIntervals() {
        UUID employee = UUID.randomUUID();
        UUID team = UUID.randomUUID();
        RemoteProject project = project("fractional", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 8), 5, false, null);
        PlanningService planning = integrated(project, team, employee, List.of(new RemoteUnavailability(UUID.randomUUID(), employee,
                LocalDate.of(2026, 1, 4), LocalDate.of(2026, 1, 6), "reserved")));

        ProposalResponse proposal = planning.generateProposal(organization);
        List<Map<String, String>> creates = items(proposal).stream().filter(i -> "CREATE".equals(i.get("operation"))).toList();

        assertThat(creates).hasSize(2);
        assertThat(creates).extracting(i -> i.get("startDate") + "/" + i.get("endDate"))
                .containsExactly("2026-01-01/2026-01-03", "2026-01-07/2026-01-08");
        assertThat(creates.stream().mapToInt(this::inclusiveDays).sum()).isEqualTo(5);
    }

    @Test
    void continuousExecutionDoesNotJoinSeparatedAvailability() {
        UUID employee = UUID.randomUUID();
        UUID team = UUID.randomUUID();
        RemoteProject project = project("continuous", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 8), 5, true, null);
        PlanningService planning = integrated(project, team, employee, List.of(new RemoteUnavailability(UUID.randomUUID(), employee,
                LocalDate.of(2026, 1, 4), LocalDate.of(2026, 6, 1), "reserved")));

        ProposalResponse proposal = planning.generateProposal(organization);

        assertThat(items(proposal)).noneMatch(i -> "CREATE".equals(i.get("operation")));
        assertThat(proposal.payload().path("unattendedProjects")).hasSize(1);
    }

    @Test
    void laterProjectCannotUseResourceAlreadyPlannedByEarlierProject() {
        UUID employee = UUID.randomUUID();
        UUID team = UUID.randomUUID();
        RemoteProject first = project("first", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 3), 3, true, null);
        RemoteProject second = project("second", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 3), 3, true, null);
        PlanningService planning = integrated(List.of(first, second), team, employee, List.of());

        ProposalResponse proposal = planning.generateProposal(organization);

        assertThat(items(proposal)).filteredOn(i -> "CREATE".equals(i.get("operation")))
                .extracting(i -> i.get("projectId")).containsExactly(first.id().toString());
        assertThat(proposal.payload().path("unattendedProjects")).hasSize(1);
    }

    @Test
    void individualAlternativeIsEvaluatedWithoutClaimingProjectSufficiency() {
        UUID employee = UUID.randomUUID();
        UUID emptyTeam = UUID.randomUUID();
        RemoteProject project = project("individual", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), 2, true, null);
        PlanningService planning = integrated(List.of(project), List.of(), employee, List.of());

        ProposalResponse proposal = planning.generateProposal(organization);

        assertThat(items(proposal)).filteredOn(i -> "CREATE".equals(i.get("operation"))).singleElement()
                .satisfies(i -> assertThat(i.get("resourceType")).isEqualTo("EMPLOYEE"))
                .satisfies(i -> assertThat(i.get("status")).isEqualTo("ALERT_INDIVIDUAL_REVIEW"));
        assertThat(proposal.payload().get("fullyViable").asBoolean()).isFalse();
    }

    @Test
    void sharedEmployeePreventsSecondTeamAlternative() {
        UUID employee = UUID.randomUUID();
        UUID firstTeam = UUID.randomUUID();
        UUID secondTeam = UUID.randomUUID();
        RemoteProject first = project("first", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), 2, true, null);
        RemoteProject second = project("second", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), 2, true, null);
        PlanningService planning = integrated(List.of(first, second), List.of(firstTeam, secondTeam), employee, List.of());

        ProposalResponse proposal = planning.generateProposal(organization);

        assertThat(items(proposal)).filteredOn(i -> "CREATE".equals(i.get("operation"))).hasSize(1);
        assertThat(proposal.payload().path("unattendedProjects")).hasSize(1);
    }

    @Test
    void fixedCommitmentAndCompositionSnapshotArePreserved() {
        UUID employee = UUID.randomUUID();
        UUID team = UUID.randomUUID();
        UUID fixedAllocation = UUID.randomUUID();
        RemoteProject project = project("snapshot", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), 2, true, null);
        PlanningService planning = integrated(project, team, employee, List.of());
        when(repository.allocations(any(), any(), any(), any(), any(), any())).thenReturn(List.of(
                new AllocationResponse(fixedAllocation, organization, UUID.randomUUID(), "TEAM", team,
                        LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 11), true, "CONFIRMED", OffsetDateTime.now(), OffsetDateTime.now())));
        when(repository.allocationMemberIds(any(), eq(fixedAllocation))).thenReturn(Set.of(employee));

        ProposalResponse proposal = planning.generateProposal(organization);

        assertThat(items(proposal)).anyMatch(i -> "RETAIN".equals(i.get("operation")) && "FIXED".equals(i.get("status")));
        assertThat(proposal.payload().path("teamCompositionSnapshot").path(team.toString()).toString()).contains(employee.toString());
    }

    @Test
    void proposalGenerationIsDeterministicAndDeadlineWinsBeforePriority() {
        UUID employee = UUID.randomUUID();
        UUID team = UUID.randomUUID();
        RemoteProject urgent = new RemoteProject(UUID.randomUUID(), organization, "urgent", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), 2, List.of(), "LOW", true, null);
        RemoteProject later = new RemoteProject(UUID.randomUUID(), organization, "later", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 3), 2, List.of(), "CRITICAL", true, null);
        PlanningService planning = integrated(List.of(urgent, later), team, employee, List.of());

        ProposalResponse first = planning.generateProposal(organization);
        ProposalResponse second = planning.generateProposal(organization);

        assertThat(items(first)).isEqualTo(items(second));
        assertThat(items(first)).filteredOn(i -> "CREATE".equals(i.get("operation"))).singleElement()
                .extracting(i -> i.get("projectId")).isEqualTo(urgent.id().toString());
    }

    @Test
    void approvedFractionalIntervalsArePersistedSeparately() {
        UUID employee = UUID.randomUUID();
        UUID team = UUID.randomUUID();
        RemoteProject project = project("persisted", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 8), 5, false, null);
        PlanningService planning = integrated(project, team, employee, List.of(new RemoteUnavailability(UUID.randomUUID(), employee,
                LocalDate.of(2026, 1, 4), LocalDate.of(2026, 1, 6), "reserved")));
        ProposalResponse generated = planning.generateProposal(organization);
        when(repository.proposal(organization, generated.id())).thenReturn(Optional.of(generated));

        planning.approve(organization, generated.id(), null);

        var captor = org.mockito.ArgumentCaptor.forClass(AllocationRequest.class);
        verify(repository, times(2)).saveAllocation(eq(organization), captor.capture(), eq(Set.of(employee)));
        assertThat(captor.getAllValues()).extracting(r -> r.startDate() + "/" + r.endDate())
                .containsExactly("2026-01-01/2026-01-03", "2026-01-07/2026-01-08");
    }

    @Test
    void proposalTransportCanBeDefinedAndApprovedWithoutPrecreatingConfirmedTransport() {
        UUID employee = UUID.randomUUID(); UUID team = UUID.randomUUID();
        RemoteProject project = new RemoteProject(UUID.randomUUID(), organization, "remote", LocalDate.of(2026, 1, 3), LocalDate.of(2026, 1, 5), 2,
                List.of(new RemoteLocality("Destination")), "NORMAL", true, null);
        PlanningService planning = integrated(project, team, employee, List.of());
        ProposalResponse generated = planning.generateProposal(organization);
        List<ProposalItem> original = new ObjectMapper().findAndRegisterModules().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .convertValue(generated.payload().path("items"), new ObjectMapper().getTypeFactory().constructCollectionType(List.class, ProposalItem.class));
        ProposalItem transport = original.stream().filter(i -> "TRANSPORT".equals(i.operation())).findFirst().orElseThrow();
        ProposalItem create = original.stream().filter(i -> "CREATE".equals(i.operation())).findFirst().orElseThrow();
        ProposalItem confirmedTransport = new ProposalItem(transport.itemId(), transport.operation(), transport.projectId(), transport.projectIdentification(), transport.resourceType(), transport.resourceId(), transport.resourceIdentification(), transport.startDate(), transport.endDate(), false, "CONFIRMED_TRANSPORT", transport.explanation(), transport.conflictIds(), null, 1, transport.startDate().plusDays(1), transport.origin(), transport.destination());
        ProposalItem movedCreate = new ProposalItem(create.itemId(), create.operation(), create.projectId(), create.projectIdentification(), create.resourceType(), create.resourceId(), create.resourceIdentification(), LocalDate.of(2026, 1, 4), LocalDate.of(2026, 1, 5), false, create.status(), create.explanation(), create.conflictIds());
        List<ProposalItem> revisedItems = original.stream().map(i -> i.itemId().equals(transport.itemId()) ? confirmedTransport : i.itemId().equals(create.itemId()) ? movedCreate : i).toList();
        ProposalResponse revised = proposalWithPayload(generated, revisedItems);
        when(repository.proposal(organization, generated.id())).thenReturn(Optional.of(generated), Optional.of(revised), Optional.of(revised));
        when(repository.updateProposal(eq(organization), eq(generated.id()), any(ProposalUpdateRequest.class))).thenReturn(revised);

        ProposalResponse returned = planning.updateProposal(organization, generated.id(), new ProposalUpdateRequest(revisedItems, generated.explanation()));

        assertThat(returned.payload().path("items").toString()).contains("CONFIRMED_TRANSPORT");
        assertThatThrownBy(() -> planning.approve(organization, generated.id(), new ProposalApprovalRequest(List.of(create.itemId()))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("transport");
        planning.approve(organization, generated.id(), new ProposalApprovalRequest(revisedItems.stream().map(ProposalItem::itemId).toList()));
        verify(repository).saveTransport(eq(organization), any(TransportRequest.class));
        verify(repository).saveAllocation(eq(organization), any(AllocationRequest.class), eq(Set.of(employee)));
    }

    @Test
    void pendingTransportCannotBeApprovedAsZeroDuration() {
        UUID employee = UUID.randomUUID(); UUID team = UUID.randomUUID();
        RemoteProject project = new RemoteProject(UUID.randomUUID(), organization, "pending transport", LocalDate.of(2026, 1, 3), LocalDate.of(2026, 1, 5), 2,
                List.of(new RemoteLocality("Destination")), "NORMAL", true, null);
        PlanningService planning = integrated(project, team, employee, List.of());
        ProposalResponse generated = planning.generateProposal(organization);
        when(repository.proposal(organization, generated.id())).thenReturn(Optional.of(generated));

        assertThatThrownBy(() -> planning.approve(organization, generated.id(), null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Pending transport");
        verify(repository, never()).saveTransport(any(), any());
        verify(repository, never()).saveAllocation(any(), any(), any());
    }

    @Test
    void individualTransportBlocksCollectiveAllocationOfItsTeamMember() {
        UUID employee = UUID.randomUUID(); UUID team = UUID.randomUUID();
        RemoteProject project = project("transport conflict", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), 2, true, null);
        PlanningService planning = integrated(project, team, employee, List.of());
        when(repository.transports(organization)).thenReturn(List.of(new TransportResponse(UUID.randomUUID(), organization, "EMPLOYEE", employee,
                "Origin", "Destination", LocalDate.of(2026, 1, 1), 1, LocalDate.of(2026, 1, 2), "CONFIRMED", OffsetDateTime.now(), OffsetDateTime.now())));

        List<ConflictResponse> conflicts = planning.conflicts(organization, new AllocationRequest(project.id(), "TEAM", team,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), false));

        assertThat(conflicts).anyMatch(c -> "ALLOCATION_DURING_TRANSPORT".equals(c.type()) && "BLOCKING".equals(c.severity()));
    }

    @Test
    void replanRepresentsReplacementOfFlexibleCommitmentWithoutChangingItAutomatically() {
        UUID employee = UUID.randomUUID(); UUID team = UUID.randomUUID(); UUID allocationId = UUID.randomUUID(); UUID projectId = UUID.randomUUID();
        RemoteProject project = new RemoteProject(projectId, organization, "replan", LocalDate.of(2026, 1, 3), LocalDate.of(2026, 1, 4), 2, List.of(), "NORMAL", true, null);
        PlanningService planning = integrated(project, team, employee, List.of());
        AllocationResponse flexible = new AllocationResponse(allocationId, organization, projectId, "TEAM", team, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), false, "CONFIRMED", OffsetDateTime.now(), OffsetDateTime.now());
        when(repository.allocations(any(), any(), any(), any(), any(), any())).thenReturn(List.of(flexible));
        when(repository.allocationMemberIds(any(), eq(allocationId))).thenReturn(Set.of(employee));

        ProposalResponse proposal = planning.generateProposal(organization);

        assertThat(items(proposal)).anyMatch(i -> "CANCEL".equals(i.get("operation")) && i.get("itemId").equals(allocationId.toString()));
        assertThat(items(proposal)).anyMatch(i -> "CREATE".equals(i.get("operation")) && i.get("startDate").equals("2026-01-03"));
        verify(repository, never()).cancel(any(), any(), any());
    }

    @Test
    void editingProposalCannotRemoveFixedCommitment() {
        UUID employee = UUID.randomUUID(); UUID team = UUID.randomUUID(); UUID fixedId = UUID.randomUUID();
        RemoteProject project = project("fixed edit", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), 2, true, null);
        PlanningService planning = integrated(project, team, employee, List.of());
        when(repository.allocations(any(), any(), any(), any(), any(), any())).thenReturn(List.of(new AllocationResponse(fixedId, organization, project.id(), "TEAM", team, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 11), true, "CONFIRMED", OffsetDateTime.now(), OffsetDateTime.now())));
        ProposalResponse proposal = planning.generateProposal(organization);
        when(repository.proposal(organization, proposal.id())).thenReturn(Optional.of(proposal));

        assertThatThrownBy(() -> planning.updateProposal(organization, proposal.id(), new ProposalUpdateRequest(List.of(), "changed")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Fixed");
    }

    @Test
    void staleProposalIsRejectedBeforePersistingAnySelectedItem() {
        UUID employee = UUID.randomUUID(); UUID team = UUID.randomUUID();
        RemoteProject project = project("stale", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), 2, true, null);
        PlanningService planning = integrated(project, team, employee, List.of());
        ProposalResponse generated = planning.generateProposal(organization);
        when(repository.proposal(organization, generated.id())).thenReturn(Optional.of(generated));
        when(repository.currentRevision(organization)).thenReturn(generated.baseRevision() + 1);

        assertThatThrownBy(() -> planning.approve(organization, generated.id(), null))
                .isInstanceOf(org.springframework.dao.OptimisticLockingFailureException.class)
                .hasMessageContaining("stale");
        verify(repository, never()).saveAllocation(any(), any(), any());
        verify(repository, never()).saveTransport(any(), any());
    }

    @Test
    void repeatedApprovalDoesNotDuplicateConfirmation() {
        UUID employee = UUID.randomUUID(); UUID team = UUID.randomUUID();
        RemoteProject project = project("duplicate", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), 2, true, null);
        PlanningService planning = integrated(project, team, employee, List.of());
        ProposalResponse generated = planning.generateProposal(organization);
        ProposalResponse approved = new ProposalResponse(generated.id(), generated.organizationId(), generated.version(), "APPROVED",
                generated.explanation(), generated.createdAt(), generated.updatedAt(), generated.baseRevision(), generated.payload());
        when(repository.proposal(organization, generated.id())).thenReturn(Optional.of(generated), Optional.of(approved), Optional.of(approved));

        planning.approve(organization, generated.id(), null);

        assertThatThrownBy(() -> planning.approve(organization, generated.id(), null))
                .isInstanceOf(org.springframework.dao.OptimisticLockingFailureException.class)
                .hasMessageContaining("no longer draft");
        verify(repository, times(1)).saveAllocation(eq(organization), any(AllocationRequest.class), eq(Set.of(employee)));
        verify(repository, times(3)).lockOrganization(organization);
    }

    @Test
    void organizationContextIsPassedToRemotePlanningData() {
        UUID otherOrganization = UUID.randomUUID();
        UUID employee = UUID.randomUUID(); UUID team = UUID.randomUUID();
        ProjectClient projects = mock(ProjectClient.class); TeamClient teams = mock(TeamClient.class);
        when(projects.list(any())).thenAnswer(invocation -> List.of(projectFor(invocation.getArgument(0), "isolated")));
        when(teams.listTeams(any())).thenReturn(List.of(new RemoteTeam(team, organization, "team", 1, "ACTIVE", "City")));
        when(teams.listEmployees(any())).thenReturn(List.of(new RemoteEmployee(employee, organization, "employee", "role", true, "City")));
        when(teams.employeesForTeam(any(), eq(team))).thenReturn(List.of(new RemoteMembership(team, employee, "role")));
        when(teams.getTeam(any(), eq(team))).thenReturn(new RemoteTeam(team, organization, "team", 1, "ACTIVE", "City"));
        when(teams.getEmployee(any(), eq(employee))).thenReturn(new RemoteEmployee(employee, organization, "employee", "role", true, "City"));
        when(teams.unavailability(any(), eq(employee))).thenReturn(List.of());
        when(teams.teamsForEmployee(any(), eq(employee))).thenReturn(List.of());
        PlanningService planning = new PlanningService(repository, objectMapper(), projects, teams);
        stubRepository();

        planning.generateProposal(otherOrganization);

        verify(projects).list(otherOrganization);
        verify(teams).listTeams(otherOrganization);
        verify(teams).listEmployees(otherOrganization);
    }

    private PlanningService integrated(RemoteProject project, UUID team, UUID employee, List<RemoteUnavailability> unavailable) {
        return integrated(List.of(project), List.of(team), employee, unavailable);
    }

    private PlanningService integrated(List<RemoteProject> projects, UUID team, UUID employee, List<RemoteUnavailability> unavailable) {
        return integrated(projects, List.of(team), employee, unavailable);
    }

    private PlanningService integrated(List<RemoteProject> projects, List<UUID> teamIds, UUID employee, List<RemoteUnavailability> unavailable) {
        ProjectClient projectClient = mock(ProjectClient.class); TeamClient teamClient = mock(TeamClient.class);
        when(projectClient.list(organization)).thenReturn(projects);
        when(projectClient.get(eq(organization), any())).thenAnswer(invocation -> projects.stream().filter(project -> project.id().equals(invocation.getArgument(1))).findFirst().orElseThrow());
        List<RemoteTeam> teams = teamIds.stream().map(id -> new RemoteTeam(id, organization, id.toString(), 1, "ACTIVE", "City")).toList();
        when(teamClient.listTeams(organization)).thenReturn(teams);
        when(teamClient.listEmployees(organization)).thenReturn(List.of(new RemoteEmployee(employee, organization, "employee", "role", true, "City")));
        for (RemoteTeam team : teams) {
            when(teamClient.employeesForTeam(organization, team.id())).thenReturn(List.of(new RemoteMembership(team.id(), employee, "role")));
            when(teamClient.getTeam(organization, team.id())).thenReturn(team);
        }
        when(teamClient.getEmployee(organization, employee)).thenReturn(new RemoteEmployee(employee, organization, "employee", "role", true, "City"));
        when(teamClient.unavailability(organization, employee)).thenReturn(unavailable);
        when(teamClient.teamsForEmployee(organization, employee)).thenReturn(teamIds.stream().map(id -> new RemoteMembership(id, employee, "role")).toList());
        stubRepository();
        return new PlanningService(repository, objectMapper(), projectClient, teamClient);
    }

    private void stubRepository() {
        when(repository.allocations(any(), any(), any(), any(), any(), any())).thenReturn(List.of());
        when(repository.transports(any())).thenReturn(List.of());
        when(repository.exceptions(any(), any())).thenReturn(List.of());
        when(repository.allocationMemberIds(any(), any())).thenReturn(Set.of());
        when(repository.currentRevision(any())).thenReturn(0L);
        when(repository.nextProposalVersion(any())).thenReturn(1L);
        when(repository.saveProposal(any(), anyLong(), anyLong(), anyString(), any())).thenAnswer(invocation -> {
            return new ProposalResponse(UUID.randomUUID(), invocation.getArgument(0), invocation.getArgument(1), "DRAFT",
                    invocation.getArgument(3), OffsetDateTime.now(), OffsetDateTime.now(), invocation.getArgument(2), invocation.getArgument(4));
        });
    }

    private RemoteProject project(String name, LocalDate start, LocalDate end, int duration, boolean continuous, Integer desired) {
        return new RemoteProject(UUID.randomUUID(), organization, name, start, end, duration, List.of(), "NORMAL", continuous, desired);
    }

    private RemoteProject projectFor(UUID org, String name) {
        return new RemoteProject(UUID.randomUUID(), org, name, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), 2, List.of(), "NORMAL", true, null);
    }

    private ProposalResponse proposalWithPayload(ProposalResponse original, List<ProposalItem> items) {
        ObjectNode payload = original.payload().deepCopy();
        payload.set("items", objectMapper().valueToTree(items));
        return new ProposalResponse(original.id(), original.organizationId(), original.version(), original.status(), original.explanation(), original.createdAt(), original.updatedAt(), original.baseRevision(), payload);
    }

    private List<Map<String, String>> items(ProposalResponse proposal) {
        List<Map<String, String>> result = new ArrayList<>();
        proposal.payload().path("items").forEach(node -> {
            Map<String, String> item = new HashMap<>();
            node.fields().forEachRemaining(entry -> item.put(entry.getKey(), entry.getValue().asText()));
            result.add(item);
        });
        return result;
    }

    private int inclusiveDays(Map<String, String> item) {
        return (int) (LocalDate.parse(item.get("endDate")).toEpochDay() - LocalDate.parse(item.get("startDate")).toEpochDay() + 1);
    }

    private ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
