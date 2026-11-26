package com.teamflow.planning.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.teamflow.planning.api.*;
import com.teamflow.planning.client.*;
import com.teamflow.planning.repository.PlanningRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PlanningService {
    private final PlanningRepository repo;
    private final ObjectMapper mapper;
    private final ProjectClient projects;
    private final TeamClient teams;

    public PlanningService(PlanningRepository repo, ObjectMapper mapper) {
        this(repo, mapper, null, null);
    }

    @Autowired
    public PlanningService(PlanningRepository repo, ObjectMapper mapper, ProjectClient projects, TeamClient teams) {
        this.repo = repo;
        this.mapper = mapper;
        this.projects = projects;
        this.teams = teams;
    }

    @Transactional
    public AllocationResponse create(UUID org, AllocationRequest request) {
        AllocationRequest normalized = normalize(request);
        validateDates(normalized.startDate(), normalized.endDate());
        repo.lockOrganization(org);
        validateReferences(org, normalized);
        ensureNoUnresolved(conflicts(org, normalized));
        return repo.saveAllocation(org, normalized, snapshotMembers(org, normalized));
    }

    public List<AllocationResponse> list(UUID org, UUID project, String type, UUID resource, LocalDate from, LocalDate to) {
        return repo.allocations(org, project, normalizeType(type), resource, from, to);
    }

    @Transactional
    public AllocationResponse update(UUID org, UUID id, AllocationUpdateRequest request) {
        AllocationRequest normalized = normalize(new AllocationRequest(request.projectId(), request.resourceType(), request.resourceId(), request.startDate(), request.endDate(), request.fixed()));
        validateDates(normalized.startDate(), normalized.endDate());
        repo.lockOrganization(org);
        validateReferences(org, normalized);
        ensureNoUnresolved(conflictsExcluding(org, normalized, id));
        return repo.updateAllocation(org, id, new AllocationUpdateRequest(normalized.projectId(), normalized.resourceType(), normalized.resourceId(), normalized.startDate(), normalized.endDate(), normalized.fixed(), request.expectedUpdatedAt()), request.expectedUpdatedAt());
    }

    @Transactional
    public AllocationResponse fixed(UUID org, UUID id, boolean value, OffsetDateTime expectedUpdatedAt) {
        repo.lockOrganization(org);
        return repo.setFixed(org, id, value, expectedUpdatedAt);
    }

    @Transactional
    public void cancel(UUID org, UUID id, OffsetDateTime expectedUpdatedAt) { repo.lockOrganization(org); repo.cancel(org, id, expectedUpdatedAt); }

    @Transactional
    public TransportResponse transport(UUID org, TransportRequest request) {
        validateResource(request.resourceType());
        validateDates(request.startDate(), request.startDate());
        if (request.durationDays() != null && request.durationDays() < 1) throw new IllegalArgumentException("durationDays must be positive");
        repo.lockOrganization(org);
        validateResourceReference(org, request.resourceType(), request.resourceId());
        if (request.origin().trim().equalsIgnoreCase(request.destination().trim())) throw new IllegalArgumentException("origin and destination must differ");
        return repo.saveTransport(org, new TransportRequest(normalizeType(request.resourceType()), request.resourceId(), request.origin().trim(), request.destination().trim(), request.startDate(), request.durationDays()));
    }

    public List<TransportResponse> transports(UUID org) { return repo.transports(org); }

    @Transactional
    public TransportResponse updateTransport(UUID org, UUID id, TransportUpdateRequest request) {
        validateResource(request.resourceType());
        validateDates(request.startDate(), request.startDate());
        if (request.durationDays() != null && request.durationDays() < 1) throw new IllegalArgumentException("durationDays must be positive");
        repo.lockOrganization(org);
        validateResourceReference(org, request.resourceType(), request.resourceId());
        if (request.origin().trim().equalsIgnoreCase(request.destination().trim())) throw new IllegalArgumentException("origin and destination must differ");
        return repo.updateTransport(org, id, new TransportUpdateRequest(request.resourceId(), normalizeType(request.resourceType()), request.origin().trim(), request.destination().trim(), request.startDate(), request.durationDays(), request.expectedUpdatedAt()), request.expectedUpdatedAt());
    }

    @Transactional
    public void cancelTransport(UUID org, UUID id, OffsetDateTime expectedUpdatedAt) { repo.lockOrganization(org); repo.cancelTransport(org, id, expectedUpdatedAt); }

    @Transactional
    public AllocationExceptionResponse exception(UUID org, UUID allocationId, AllocationExceptionRequest request) {
        repo.lockOrganization(org);
        AllocationResponse allocation = repo.allocation(org, allocationId).orElseThrow(() -> new NoSuchElementException("Allocation not found"));
        if (!"TEAM".equals(allocation.resourceType())) throw new IllegalArgumentException("Exceptions apply only to team allocations");
        if (request.endDate().isBefore(request.startDate())) throw new IllegalArgumentException("endDate must be on or after startDate");
        RemoteEmployee employee = requireTeams().getEmployee(org, request.employeeId());
        if (!employee.active()) throw new IllegalArgumentException("Inactive employees cannot be allocated");
        List<RemoteMembership> members = requireTeams().employeesForTeam(org, allocation.resourceId());
        if (members.stream().noneMatch(m -> m.employeeId().equals(employee.id()))) throw new IllegalArgumentException("Employee is not part of the allocated team");
        if ("REALLOCATE".equalsIgnoreCase(request.kind()) && request.targetProjectId() == null) throw new IllegalArgumentException("targetProjectId is required for REALLOCATE");
        return repo.saveException(org, allocationId, new AllocationExceptionRequest(request.employeeId(), request.kind().toUpperCase(), request.startDate(), request.endDate(), request.targetProjectId(), request.targetStartDate(), request.targetEndDate(), request.reason()));
    }

    public List<AllocationExceptionResponse> exceptions(UUID org, UUID allocationId) { return repo.exceptions(org, allocationId); }

    public List<ConflictResponse> conflicts(UUID org, AllocationRequest request) {
        AllocationRequest normalized = normalize(request);
        validateDates(normalized.startDate(), normalized.endDate());
        return conflictsExcluding(org, normalized, Set.of());
    }

    public AvailabilityResponse availability(UUID org, String resourceType, UUID resourceId, LocalDate from, LocalDate to) {
        validateDates(from, to); String type = normalizeType(resourceType); validateResource(type);
        List<ConflictResponse> conflicts = conflicts(org, new AllocationRequest(UUID.randomUUID(), type, resourceId, from, to, false));
        List<AvailabilityCommitment> commitments = conflicts.stream().filter(c -> c.commitmentIds() != null).flatMap(c -> c.commitmentIds().stream()).distinct().map(id -> new AvailabilityCommitment(id, "ALLOCATION", null, from, to, "CONFLICTING", false, cDescription(conflicts, id))).toList();
        List<String> reasons = conflicts.stream().map(ConflictResponse::description).distinct().toList();
        String status = conflicts.stream().anyMatch(c -> "BLOCKING".equals(c.severity())) ? "UNAVAILABLE" : conflicts.isEmpty() ? "AVAILABLE" : "PENDING";
        return new AvailabilityResponse(org, type, resourceId, from, to, status, reasons, commitments);
    }

    public List<ProposalResponse> proposals(UUID org) { return repo.proposals(org); }
    public ProposalResponse proposal(UUID org, UUID id) { return repo.proposal(org, id).orElseThrow(() -> new NoSuchElementException("Proposal not found")); }

    @Transactional
    public ProposalResponse generateProposal(UUID org) {
        requireClients();
        repo.lockOrganization(org);
        long baseRevision = repo.currentRevision(org);
        List<RemoteProject> projectList = projects.list(org).stream().sorted(projectComparator()).toList();
        List<RemoteTeam> teamList = teams.listTeams(org).stream().filter(RemoteTeam::active).sorted(Comparator.comparing(RemoteTeam::identification).thenComparing(RemoteTeam::id)).toList();
        List<RemoteEmployee> employeeList = teams.listEmployees(org).stream().filter(RemoteEmployee::active)
                .sorted(Comparator.comparing(RemoteEmployee::identification).thenComparing(RemoteEmployee::id)).toList();
        ObjectNode compositionSnapshot = mapper.createObjectNode();
        Map<UUID, Set<UUID>> teamMembers = new LinkedHashMap<>();
        for (RemoteTeam team : teamList) {
            ArrayNode members = compositionSnapshot.putArray(team.id().toString());
            Set<UUID> memberIds = requireTeams().employeesForTeam(org, team.id()).stream().map(RemoteMembership::employeeId)
                    .collect(Collectors.toCollection(TreeSet::new));
            teamMembers.put(team.id(), memberIds);
            memberIds.forEach(id -> members.add(id.toString()));
        }
        ArrayNode items = mapper.createArrayNode(); ArrayNode unattended = mapper.createArrayNode();
        List<AllocationResponse> existing = repo.allocations(org, null, null, null, null, null).stream().filter(a -> !"CANCELLED".equals(a.status())).toList();
        existing.stream().filter(AllocationResponse::fixed).forEach(a -> items.add(itemNode(new ProposalItem(a.id(), "RETAIN", a.projectId(), a.projectId().toString(), a.resourceType(), a.resourceId(), a.resourceId().toString(), a.startDate(), a.endDate(), true, "FIXED", "Compromisso fixado preservado.", List.of()))));
        Set<UUID> flexibleIds = existing.stream().filter(a -> !a.fixed()).map(AllocationResponse::id).collect(Collectors.toSet());
        List<Occupancy> plannedOccupancies = new ArrayList<>();
        existing.stream().filter(AllocationResponse::fixed).forEach(a -> plannedOccupancies.add(new Occupancy(a.resourceType(), a.resourceId(), effectiveMembers(org, a), List.of(new Period(a.startDate(), a.endDate())))));
        int planned = 0;
        for (RemoteProject project : projectList) {
            PlanningWindow window = planningWindow(project);
            if (window == null) { unattended.add(unattendedNode(project, "Dados de início, duração ou prazo estão incompletos.", List.of("Completar o cadastro do projeto."))); continue; }
            String locality = locality(project);
            List<Candidate> selected = selectTeams(org, project, window, locality, teamList, teamMembers, plannedOccupancies, flexibleIds);
            boolean individualAlternative = selected.isEmpty();
            if (individualAlternative) {
                selected = selectIndividuals(org, project, window, locality, employeeList, plannedOccupancies, flexibleIds);
            }
            if (selected.isEmpty()) {
                unattended.add(unattendedNode(project, "Nenhuma equipe ou funcionário ativo está disponível sem conflito bloqueante.", List.of("Realocar recursos", "Incluir funcionários", "Revisar duração", "Revisar prazo")));
                continue;
            }
            int selectedPeople = selected.stream().flatMap(candidate -> candidate.members().stream()).collect(Collectors.toSet()).size();
            boolean capacityAlert = project.desiredPeople() != null && selectedPeople < project.desiredPeople();
            for (Candidate candidate : selected) {
                String status = capacityAlert ? "ALERT_CAPACITY" : candidate.individual() ? "ALERT_INDIVIDUAL_REVIEW" : "VIABLE";
                String itemExplanation = capacityAlert ? "Quantidade desejada não atendida; revisar duração ou complementar a equipe."
                        : candidate.individual() ? "Alternativa individual assistiva; a alocação de um funcionário não comprova sozinha a execução do projeto." : "Alocação coletiva selecionada pela heurística determinística.";
                for (Period period : candidate.periods()) {
                    items.add(itemNode(new ProposalItem(stableItemId("CREATE", project.id(), candidate.resourceType(), candidate.resourceId(), period), "CREATE", project.id(), project.identification(), candidate.resourceType(), candidate.resourceId(), candidate.identification(), period.start(), period.end(), false, status, itemExplanation, List.of())));
                }
                plannedOccupancies.add(new Occupancy(candidate.resourceType(), candidate.resourceId(), candidate.members(), candidate.periods()));
                planned++;
                addTransportItems(org, project, locality, candidate, items);
            }
        }
        reconcileFlexibleAllocations(items, existing, projectList);
        ObjectNode payload = mapper.createObjectNode(); payload.put("strategy", "deadline-priority-feasible-intervals-v2"); payload.put("baseRevision", baseRevision); payload.put("explanation", "Heurística determinística lexicográfica: prazo, prioridade, antecipação, ociosidade e deslocamento; intervalos contínuos ou fracionados são avaliados sem alterar compromissos fixados."); payload.set("teamCompositionSnapshot", compositionSnapshot); payload.set("items", items); payload.set("unattendedProjects", unattended); payload.put("fullyViable", unattended.isEmpty() && items.findValuesAsText("status").stream().noneMatch(status -> status.equals("PENDING_TRANSPORT") || status.equals("ALERT_CAPACITY") || status.equals("ALERT_INDIVIDUAL_REVIEW"))); payload.put("plannedProjectAllocations", planned);
        return repo.saveProposal(org, repo.nextProposalVersion(org), baseRevision, payload.get("explanation").asText(), payload);
    }

    @Transactional
    public ProposalResponse updateProposal(UUID org, UUID id, ProposalUpdateRequest request) {
        repo.lockOrganization(org);
        ProposalResponse current = proposal(org, id);
        if (!"DRAFT".equals(current.status())) throw new OptimisticLockingFailureException("Only draft proposals can be edited");
        List<ProposalItem> normalized = validateProposalItems(current.payload(), request.items());
        return repo.updateProposal(org, id, new ProposalUpdateRequest(normalized, request.explanation()));
    }

    @Transactional
    public ProposalResponse approve(UUID org, UUID id, ProposalApprovalRequest request) {
        repo.lockOrganization(org);
        ProposalResponse proposal = proposal(org, id);
        if (!"DRAFT".equals(proposal.status())) throw new OptimisticLockingFailureException("Proposal is no longer draft");
        if (repo.currentRevision(org) != proposal.baseRevision()) throw new OptimisticLockingFailureException("Proposal is stale; generate a new proposal");
        ensureCompositionUnchanged(org, proposal.payload());
        List<ProposalItem> items = readItems(proposal.payload());
        Set<UUID> selected = request == null || request.itemIds() == null || request.itemIds().isEmpty() ? items.stream().map(ProposalItem::itemId).collect(Collectors.toSet()) : new HashSet<>(request.itemIds());
        List<ProposalItem> chosen = items.stream().filter(i -> selected.contains(i.itemId())).toList();
        if (chosen.size() != selected.size()) throw new IllegalArgumentException("Proposal selection contains unknown items");
        if (chosen.stream().anyMatch(i -> "PENDING_TRANSPORT".equals(i.status()))) throw new IllegalArgumentException("Pending transport must be defined before approval");
        if (chosen.stream().anyMatch(i -> "BLOCKING".equals(i.status()))) throw new IllegalArgumentException("Blocking proposal items cannot be approved");
        validateSelectedDependencies(items, chosen);

        List<ProposalItem> ordered = chosen.stream().sorted(Comparator.comparingInt(this::approvalOrder)).toList();
        for (ProposalItem item : ordered) {
            if (!"TRANSPORT".equals(item.operation())) continue;
            if (!"CONFIRMED_TRANSPORT".equals(item.status()) || item.durationDays() == null || item.origin() == null || item.destination() == null) throw new IllegalArgumentException("Transport duration and route must be defined before approval");
            repo.saveTransport(org, new TransportRequest(item.resourceType(), item.resourceId(), item.origin(), item.destination(), item.startDate(), item.durationDays()));
        }
        for (ProposalItem item : ordered) {
            if (!"CANCEL".equals(item.operation())) continue;
            repo.cancel(org, item.relatedId() == null ? item.itemId() : item.relatedId(), null);
        }
        for (ProposalItem item : ordered) {
            if (!"UPDATE".equals(item.operation())) continue;
            UUID allocationId = item.relatedId() == null ? item.itemId() : item.relatedId();
            AllocationRequest allocation = new AllocationRequest(item.projectId(), item.resourceType(), item.resourceId(), item.startDate(), item.endDate(), item.fixed());
            validateReferences(org, allocation); ensureNoUnresolved(conflictsExcluding(org, allocation, allocationId));
            repo.updateAllocation(org, allocationId, new AllocationUpdateRequest(allocation.projectId(), allocation.resourceType(), allocation.resourceId(), allocation.startDate(), allocation.endDate(), allocation.fixed(), null), null);
        }
        for (ProposalItem item : ordered) {
            if (!"CREATE".equals(item.operation())) continue;
            AllocationRequest allocation = new AllocationRequest(item.projectId(), item.resourceType(), item.resourceId(), item.startDate(), item.endDate(), item.fixed());
            validateReferences(org, allocation); ensureNoUnresolved(conflicts(org, allocation));
            repo.saveAllocation(org, allocation, snapshotMembers(org, allocation));
        }
        boolean completeSelection = items.stream().filter(this::actionable).allMatch(i -> selected.contains(i.itemId()));
        String proposalStatus = completeSelection ? "APPROVED" : "PARTIALLY_APPROVED";
        ObjectNode payload = proposal.payload() == null ? mapper.createObjectNode() : proposal.payload().deepCopy();
        payload.put("approvalComplete", completeSelection && payload.path("fullyViable").asBoolean(false));
        payload.set("approvedItemIds", mapper.valueToTree(chosen.stream().map(ProposalItem::itemId).toList()));
        payload.set("unapprovedItemIds", mapper.valueToTree(items.stream().filter(i -> actionable(i) && !selected.contains(i.itemId())).map(ProposalItem::itemId).toList()));
        repo.markProposal(org, id, proposalStatus, payload);
        repo.log(org, "APPROVE", "PROPOSAL", id, mapper.createObjectNode().put("selectedItems", chosen.size()));
        return proposal(org, id);
    }

    @Transactional
    public ProposalResponse reject(UUID org, UUID id) { repo.lockOrganization(org); proposal(org, id); repo.markProposal(org, id, "REJECTED"); return proposal(org, id); }
    @Transactional
    public ProposalResponse discard(UUID org, UUID id) { repo.lockOrganization(org); proposal(org, id); repo.markProposal(org, id, "DISCARDED"); return proposal(org, id); }

    private List<ConflictResponse> conflictsExcluding(UUID org, AllocationRequest r, UUID excluded) {
        return conflictsExcluding(org, r, excluded == null ? Set.of() : Set.of(excluded));
    }

    private List<ConflictResponse> conflictsExcluding(UUID org, AllocationRequest r, Collection<UUID> excluded) {
        String type = normalizeType(r.resourceType()); validateResource(type); List<ConflictResponse> result = new ArrayList<>();
        for (AllocationResponse a : repo.allocations(org, null, type, r.resourceId(), r.startDate(), r.endDate())) {
            if (!excluded.contains(a.id()) && !"CANCELLED".equals(a.status()) && overlaps(a.startDate(), a.endDate(), r.startDate(), r.endDate())) result.add(conflict("OVERLAPPING_ALLOCATION", "BLOCKING", type, r.resourceId(), r.startDate(), r.endDate(), "O recurso já possui uma alocação sobreposta.", "Escolha outro recurso ou período.", List.of(a.id())));
        }
        for (TransportResponse t : repo.transports(org)) {
            if (!type.equals(t.resourceType()) || !r.resourceId().equals(t.resourceId()) || "CANCELLED".equals(t.status())) continue;
            LocalDate end = t.arrivalDate() == null ? t.startDate() : t.arrivalDate();
            if (t.arrivalDate() == null && !r.endDate().isBefore(t.startDate())) result.add(conflict("TRANSPORT_PENDING", "PLANNING_PENDING", type, r.resourceId(), t.startDate(), r.endDate(), "Há deslocamento sem duração definida.", "Informe a duração e confirme o transporte.", List.of()));
            else if (overlaps(t.startDate(), end, r.startDate(), r.endDate())) result.add(conflict("ALLOCATION_DURING_TRANSPORT", "BLOCKING", type, r.resourceId(), r.startDate(), r.endDate(), "A alocação coincide com o deslocamento ou a chegada do recurso.", "Antecipe ou adie a alocação.", List.of()));
        }
        if (projects != null && teams != null && "EMPLOYEE".equals(type)) result.addAll(employeeConflicts(org, r, excluded));
        if (projects != null && teams != null && "TEAM".equals(type)) result.addAll(teamConflicts(org, r, excluded));
        return distinctConflicts(result);
    }

    private List<ConflictResponse> employeeConflicts(UUID org, AllocationRequest r, Collection<UUID> excluded) {
        List<ConflictResponse> result = new ArrayList<>(); RemoteEmployee employee = requireTeams().getEmployee(org, r.resourceId());
        if (!employee.active()) result.add(conflict("INACTIVE_RESOURCE", "BLOCKING", "EMPLOYEE", employee.id(), r.startDate(), r.endDate(), "O funcionário está inativo.", "Ative o funcionário ou selecione outro recurso.", List.of()));
        for (RemoteUnavailability u : requireTeams().unavailability(org, employee.id())) if (overlaps(u.startDate(), u.endDate(), r.startDate(), r.endDate())) result.add(conflict("EMPLOYEE_UNAVAILABLE", "BLOCKING", "EMPLOYEE", employee.id(), u.startDate(), u.endDate(), "O funcionário possui indisponibilidade cadastrada.", "Escolha outro período ou funcionário.", List.of()));
        for (RemoteMembership membership : requireTeams().teamsForEmployee(org, employee.id())) {
            for (AllocationResponse a : repo.allocations(org, null, "TEAM", membership.teamId(), r.startDate(), r.endDate())) if (!"CANCELLED".equals(a.status()) && !excluded.contains(a.id()) && overlaps(a.startDate(), a.endDate(), r.startDate(), r.endDate()) && effectiveMembers(org, a).contains(employee.id())) result.add(conflict("MULTIPLE_TEAM_COMMITMENT", "BLOCKING", "EMPLOYEE", employee.id(), r.startDate(), r.endDate(), "O funcionário está comprometido por outra equipe no mesmo período.", "Realocar o funcionário ou ajustar as datas.", List.of(a.id())));
        }
        return result;
    }

    private List<ConflictResponse> teamConflicts(UUID org, AllocationRequest r, Collection<UUID> excluded) {
        List<ConflictResponse> result = new ArrayList<>(); RemoteTeam team = requireTeams().getTeam(org, r.resourceId());
        if (!team.active()) result.add(conflict("INACTIVE_RESOURCE", "BLOCKING", "TEAM", team.id(), r.startDate(), r.endDate(), "A equipe está inativa.", "Ative a equipe ou selecione outra.", List.of()));
        List<RemoteMembership> memberships = requireTeams().employeesForTeam(org, team.id()); Set<UUID> seen = new HashSet<>();
        for (RemoteMembership membership : memberships) if (seen.add(membership.employeeId())) {
            RemoteEmployee employee = requireTeams().getEmployee(org, membership.employeeId());
            if (!employee.active()) result.add(conflict("INACTIVE_RESOURCE", "BLOCKING", "EMPLOYEE", employee.id(), r.startDate(), r.endDate(), "A equipe contém funcionário inativo.", "Atualize a composição ou use outra equipe.", List.of()));
            for (RemoteUnavailability u : requireTeams().unavailability(org, employee.id())) if (overlaps(u.startDate(), u.endDate(), r.startDate(), r.endDate())) result.add(conflict("EMPLOYEE_UNAVAILABLE", "BLOCKING", "EMPLOYEE", employee.id(), u.startDate(), u.endDate(), "Um integrante possui indisponibilidade cadastrada.", "Exclua temporariamente o integrante ou ajuste o período.", List.of()));
            for (TransportResponse transport : requireTeamsTransports(org, employee.id())) {
                LocalDate end = transport.arrivalDate() == null ? transport.startDate() : transport.arrivalDate();
                if (transport.arrivalDate() == null && !r.endDate().isBefore(transport.startDate())) result.add(conflict("TRANSPORT_PENDING", "PLANNING_PENDING", "EMPLOYEE", employee.id(), transport.startDate(), r.endDate(), "Um integrante possui deslocamento sem duração definida.", "Informe a duração e confirme o transporte.", List.of()));
                else if (overlaps(transport.startDate(), end, r.startDate(), r.endDate())) result.add(conflict("ALLOCATION_DURING_TRANSPORT", "BLOCKING", "EMPLOYEE", employee.id(), r.startDate(), r.endDate(), "Um integrante está em deslocamento ou chegada durante a alocação.", "Antecipe ou adie a alocação.", List.of()));
            }
            for (RemoteMembership other : requireTeams().teamsForEmployee(org, employee.id())) for (AllocationResponse a : repo.allocations(org, null, "TEAM", other.teamId(), r.startDate(), r.endDate())) if (!excluded.contains(a.id()) && !"CANCELLED".equals(a.status()) && !a.resourceId().equals(team.id()) && overlaps(a.startDate(), a.endDate(), r.startDate(), r.endDate()) && effectiveMembers(org, a).contains(employee.id())) result.add(conflict("MULTIPLE_TEAM_COMMITMENT", "BLOCKING", "EMPLOYEE", employee.id(), r.startDate(), r.endDate(), "Um integrante está comprometido por outra equipe.", "Realocar o integrante ou ajustar as datas.", List.of(a.id())));
        }
        return result;
    }

    private List<Candidate> selectTeams(UUID org, RemoteProject project, PlanningWindow window, String locality, List<RemoteTeam> candidates, Map<UUID, Set<UUID>> teamMembers, List<Occupancy> occupancies, Collection<UUID> excluded) {
        int desired = project.desiredPeople() == null ? 0 : project.desiredPeople(); List<Candidate> selected = new ArrayList<>(); Set<UUID> employeeIds = new HashSet<>();
        for (RemoteTeam team : candidates) {
            if (!selected.isEmpty() && desired == 0) break;
            Set<UUID> members = teamMembers.getOrDefault(team.id(), Set.of()); Set<UUID> newMembers = members.stream().filter(employeeIds::add).collect(Collectors.toSet());
            List<Period> periods = feasiblePeriods(org, project, window, "TEAM", team.id(), members, occupancies, excluded);
            if (members.isEmpty() || newMembers.isEmpty() || periods.isEmpty()) continue;
            selected.add(new Candidate("TEAM", team.id(), team.identification(), members, periods, false));
            if (desired > 0 && employeeIds.size() >= desired) break;
        }
        return selected;
    }

    private List<Candidate> selectIndividuals(UUID org, RemoteProject project, PlanningWindow window, String locality, List<RemoteEmployee> candidates, List<Occupancy> occupancies, Collection<UUID> excluded) {
        int desired = project.desiredPeople() == null ? 1 : project.desiredPeople(); List<Candidate> selected = new ArrayList<>();
        for (RemoteEmployee employee : candidates) {
            Set<UUID> members = Set.of(employee.id()); List<Period> periods = feasiblePeriods(org, project, window, "EMPLOYEE", employee.id(), members, occupancies, excluded);
            if (periods.isEmpty()) continue;
            selected.add(new Candidate("EMPLOYEE", employee.id(), employee.identification(), members, periods, true));
            if (selected.size() >= desired) break;
        }
        return selected;
    }

    private List<Period> feasiblePeriods(UUID org, RemoteProject project, PlanningWindow window, String resourceType, UUID resourceId, Set<UUID> members, List<Occupancy> occupancies, Collection<UUID> excluded) {
        List<LocalDate> available = new ArrayList<>();
        for (LocalDate day = window.start(); !day.isAfter(window.end()); day = day.plusDays(1)) {
            LocalDate candidateDay = day;
            AllocationRequest request = new AllocationRequest(project.id(), resourceType, resourceId, day, day, false);
            boolean blocked = conflictsExcluding(org, request, excluded).stream().anyMatch(c -> "BLOCKING".equals(c.severity())) || occupancies.stream().anyMatch(o -> o.overlaps(resourceType, resourceId, members, candidateDay));
            if (!blocked) available.add(day);
        }
        if (project.continuousExecution()) {
            for (int i = 0; i + window.duration() <= available.size(); i++) {
                LocalDate first = available.get(i), last = first.plusDays(window.duration() - 1);
                if (last.equals(available.get(i + window.duration() - 1))) {
                    return List.of(new Period(first, last));
                }
            }
            return List.of();
        }
        if (available.size() < window.duration()) return List.of();
        List<Period> result = new ArrayList<>(); LocalDate start = null, previous = null; int count = 0;
        for (LocalDate day : available.subList(0, window.duration())) {
            if (count == 0) { start = day; previous = day; count = 1; continue; }
            if (!day.equals(previous.plusDays(1))) { result.add(new Period(start, previous)); start = day; }
            previous = day; count++;
        }
        if (count > 0) result.add(new Period(start, previous));
        return result;
    }

    private void addTransportItems(UUID org, RemoteProject project, String locality, Candidate candidate, ArrayNode items) {
        if (locality == null || candidate.periods().isEmpty()) return;
        String origin = candidate.individual() ? teams.getEmployee(org, candidate.resourceId()).initialLocation() : teams.getTeam(org, candidate.resourceId()).initialLocation();
        if (origin == null || origin.isBlank()) origin = requireTeams().organizationLocation(org).baseLocation();
        if (origin != null && !origin.equalsIgnoreCase(locality)) {
            for (Period period : candidate.periods()) {
                Period transportPeriod = new Period(period.start().minusDays(1), period.start().minusDays(1));
                items.add(itemNode(new ProposalItem(stableItemId("TRANSPORT", project.id(), candidate.resourceType(), candidate.resourceId(), transportPeriod), "TRANSPORT", project.id(), project.identification(), candidate.resourceType(), candidate.resourceId(), candidate.identification(), transportPeriod.start(), transportPeriod.end(), false, "PENDING_TRANSPORT", "Origem " + origin + " e destino " + locality + "; duração deve ser informada.", List.of(), null, null, null, origin, locality)));
            }
        }
    }

    private void reconcileFlexibleAllocations(ArrayNode items, List<AllocationResponse> existing, List<RemoteProject> projects) {
        List<AllocationResponse> flexible = existing.stream().filter(a -> !a.fixed()).toList();
        List<ProposalItem> generated = mapper.convertValue(items, mapper.getTypeFactory().constructCollectionType(List.class, ProposalItem.class));
        Set<UUID> matched = new HashSet<>();
        Set<UUID> projectsWithReplacement = generated.stream().filter(i -> "CREATE".equals(i.operation())).map(ProposalItem::projectId).collect(Collectors.toSet());
        ArrayNode reconciled = mapper.createArrayNode();
        for (ProposalItem item : generated) {
            if ("CREATE".equals(item.operation())) {
                AllocationResponse match = flexible.stream().filter(a -> !matched.contains(a.id()) && sameAllocation(a, item)).findFirst().orElse(null);
                if (match != null) {
                    matched.add(match.id());
                    reconciled.add(itemNode(new ProposalItem(match.id(), "RETAIN", match.projectId(), item.projectIdentification(), match.resourceType(), match.resourceId(), item.resourceIdentification(), match.startDate(), match.endDate(), false, "FLEXIBLE", "Compromisso flexível mantido no replanejamento.", List.of(), match.id(), null, null, null, null)));
                    continue;
                }
            }
            reconciled.add(itemNode(item));
        }
        for (AllocationResponse allocation : flexible) {
            if (matched.contains(allocation.id())) continue;
            if (projectsWithReplacement.contains(allocation.projectId())) {
                reconciled.add(itemNode(new ProposalItem(allocation.id(), "CANCEL", allocation.projectId(), allocation.projectId().toString(), allocation.resourceType(), allocation.resourceId(), allocation.resourceId().toString(), allocation.startDate(), allocation.endDate(), false, "REPLAN_REQUIRED", "Compromisso flexível será cancelado porque o replanejamento propõe outro recurso ou período.", List.of(), allocation.id(), null, null, null, null)));
            } else {
                reconciled.add(itemNode(new ProposalItem(allocation.id(), "RETAIN", allocation.projectId(), allocation.projectId().toString(), allocation.resourceType(), allocation.resourceId(), allocation.resourceId().toString(), allocation.startDate(), allocation.endDate(), false, "FLEXIBLE", "Compromisso flexível preservado porque não foi encontrada substituição viável.", List.of(), allocation.id(), null, null, null, null)));
            }
        }
        items.removeAll(); items.addAll(reconciled);
    }

    private boolean sameAllocation(AllocationResponse allocation, ProposalItem item) {
        return allocation.projectId().equals(item.projectId()) && allocation.resourceType().equals(item.resourceType()) && allocation.resourceId().equals(item.resourceId())
                && allocation.startDate().equals(item.startDate()) && allocation.endDate().equals(item.endDate());
    }

    private void validateReferences(UUID org, AllocationRequest r) { requireProjects().get(org, r.projectId()); validateResourceReference(org, r.resourceType(), r.resourceId()); }
    private Set<UUID> snapshotMembers(UUID org, AllocationRequest r) {
        if ("EMPLOYEE".equals(r.resourceType())) return Set.of(r.resourceId());
        return requireTeams().employeesForTeam(org, r.resourceId()).stream().map(RemoteMembership::employeeId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
    private void ensureCompositionUnchanged(UUID org, JsonNode payload) {
        JsonNode snapshot = payload.path("teamCompositionSnapshot");
        if (!snapshot.isObject()) return;
        Iterator<String> names = snapshot.fieldNames();
        while (names.hasNext()) {
            UUID teamId = UUID.fromString(names.next());
            List<String> current = requireTeams().employeesForTeam(org, teamId).stream().map(RemoteMembership::employeeId).map(UUID::toString).sorted().toList();
            List<String> generated = mapper.convertValue(snapshot.path(teamId.toString()), mapper.getTypeFactory().constructCollectionType(List.class, String.class));
            if (!current.equals(generated)) throw new OptimisticLockingFailureException("Proposal is stale; team composition changed");
        }
    }
    private Set<UUID> effectiveMembers(UUID org, AllocationResponse allocation) {
        Set<UUID> members = repo.allocationMemberIds(org, allocation.id());
        if (members.isEmpty() && "TEAM".equals(allocation.resourceType())) {
            try { members = requireTeams().employeesForTeam(org, allocation.resourceId()).stream().map(RemoteMembership::employeeId)
                    .collect(Collectors.toCollection(LinkedHashSet::new)); }
            catch (RemoteReferenceNotFoundException ignored) { return Set.of(); }
        }
        for (AllocationExceptionResponse exception : repo.exceptions(org, allocation.id())) {
            if (overlaps(exception.startDate(), exception.endDate(), allocation.startDate(), allocation.endDate())) members.remove(exception.employeeId());
        }
        return members;
    }
    private List<TransportResponse> requireTeamsTransports(UUID org, UUID employeeId) {
        return repo.transports(org).stream().filter(t -> "EMPLOYEE".equals(t.resourceType()) && employeeId.equals(t.resourceId()) && !"CANCELLED".equals(t.status())).toList();
    }
    private void validateResourceReference(UUID org, String type, UUID id) { if ("TEAM".equals(normalizeType(type))) { if (!requireTeams().getTeam(org, id).active()) throw new IllegalArgumentException("Inactive team cannot be allocated"); } else { if (!requireTeams().getEmployee(org, id).active()) throw new IllegalArgumentException("Inactive employee cannot be allocated"); } }
    private void ensureNoBlocking(List<ConflictResponse> conflicts) { if (conflicts.stream().anyMatch(c -> "BLOCKING".equals(c.severity()))) throw new IllegalArgumentException(conflicts.stream().filter(c -> "BLOCKING".equals(c.severity())).map(ConflictResponse::description).distinct().collect(Collectors.joining(" "))); }
    private void ensureNoUnresolved(List<ConflictResponse> conflicts) {
        if (conflicts.stream().anyMatch(c -> "BLOCKING".equals(c.severity()) || "PLANNING_PENDING".equals(c.severity())))
            throw new IllegalArgumentException(conflicts.stream().filter(c -> "BLOCKING".equals(c.severity()) || "PLANNING_PENDING".equals(c.severity())).map(ConflictResponse::description).distinct().collect(Collectors.joining(" ")));
    }
    private boolean actionable(ProposalItem item) { return Set.of("CREATE", "TRANSPORT", "CANCEL", "UPDATE").contains(item.operation()); }
    private int approvalOrder(ProposalItem item) { return switch (item.operation()) { case "TRANSPORT" -> 0; case "CANCEL" -> 1; case "UPDATE" -> 2; case "CREATE" -> 3; default -> 4; }; }
    private void validateSelectedDependencies(List<ProposalItem> items, List<ProposalItem> chosen) {
        Set<UUID> selectedIds = chosen.stream().map(ProposalItem::itemId).collect(Collectors.toSet());
        for (ProposalItem allocation : chosen.stream().filter(i -> "CREATE".equals(i.operation()) || "UPDATE".equals(i.operation())).toList()) {
            List<ProposalItem> dependencies = items.stream().filter(i -> "TRANSPORT".equals(i.operation()) && dependencyKey(i).equals(dependencyKey(allocation))).toList();
            if (dependencies.stream().anyMatch(i -> !selectedIds.contains(i.itemId()) || !"CONFIRMED_TRANSPORT".equals(i.status())))
                throw new IllegalArgumentException("A aprovação desta alocação exige a seleção de todos os transportes confirmados relacionados");
        }
    }
    private List<ProposalItem> validateProposalItems(JsonNode currentPayload, List<ProposalItem> requested) {
        if (requested == null) throw new IllegalArgumentException("Proposal items are required");
        Set<UUID> ids = new HashSet<>(); List<ProposalItem> normalized = new ArrayList<>();
        for (ProposalItem item : requested) {
            if (item.itemId() == null || !ids.add(item.itemId())) throw new IllegalArgumentException("Proposal item IDs must be unique");
            if (item.operation() == null || !Set.of("CREATE", "TRANSPORT", "RETAIN", "CANCEL", "UPDATE").contains(item.operation())) throw new IllegalArgumentException("Unsupported proposal operation");
            if (item.startDate() == null || item.endDate() == null || item.endDate().isBefore(item.startDate())) throw new IllegalArgumentException("Proposal item dates are invalid");
            if (item.fixed() && !"RETAIN".equals(item.operation())) throw new IllegalArgumentException("Fixed commitments can only be retained");
            if ("TRANSPORT".equals(item.operation())) {
                if (item.durationDays() != null && item.durationDays() < 1) throw new IllegalArgumentException("Transport duration must be positive");
                String status = item.durationDays() == null ? "PENDING_TRANSPORT" : "CONFIRMED_TRANSPORT";
                LocalDate arrival = item.durationDays() == null ? null : item.startDate().plusDays(item.durationDays());
                normalized.add(new ProposalItem(item.itemId(), item.operation(), item.projectId(), item.projectIdentification(), item.resourceType(), item.resourceId(), item.resourceIdentification(), item.startDate(), item.endDate(), item.fixed(), status, item.explanation(), item.conflictIds(), item.relatedId(), item.durationDays(), arrival, item.origin(), item.destination()));
            } else normalized.add(item);
        }
        ensureFixedItemsUnchanged(currentPayload, normalized);
        return normalized;
    }
    private void ensureFixedItemsUnchanged(JsonNode currentPayload, List<ProposalItem> requested) {
        List<ProposalItem> current = readItems(currentPayload);
        for (ProposalItem fixed : current.stream().filter(ProposalItem::fixed).toList()) {
            if (requested.stream().noneMatch(item -> item.itemId().equals(fixed.itemId()) && item.fixed() && "RETAIN".equals(item.operation()) && Objects.equals(item.startDate(), fixed.startDate()) && Objects.equals(item.endDate(), fixed.endDate()) && Objects.equals(item.resourceId(), fixed.resourceId())))
                throw new IllegalArgumentException("Fixed commitments must be preserved");
        }
    }
    private AllocationRequest normalize(AllocationRequest r) { return new AllocationRequest(r.projectId(), normalizeType(r.resourceType()), r.resourceId(), r.startDate(), r.endDate(), r.fixed()); }
    private String normalizeType(String type) { return type == null ? null : type.trim().toUpperCase(); }
    private void validateResource(String type) { if (!("TEAM".equals(normalizeType(type)) || "EMPLOYEE".equals(normalizeType(type)))) throw new IllegalArgumentException("resourceType must be TEAM or EMPLOYEE"); }
    private void validateDates(LocalDate start, LocalDate end) { if (start == null || end == null || end.isBefore(start)) throw new IllegalArgumentException("endDate must be on or after startDate"); }
    private boolean overlaps(LocalDate aStart, LocalDate aEnd, LocalDate bStart, LocalDate bEnd) { return !aEnd.isBefore(bStart) && !bEnd.isBefore(aStart); }
    private ConflictResponse conflict(String type, String severity, String resourceType, UUID resourceId, LocalDate start, LocalDate end, String description, String alternative, List<UUID> ids) { return new ConflictResponse(type, severity, resourceType, resourceId, start, end, description, alternative, ids); }
    private List<ConflictResponse> distinctConflicts(List<ConflictResponse> values) { return values.stream().collect(Collectors.toMap(c -> c.type()+c.resourceType()+c.resourceId()+c.startDate()+c.endDate()+c.description(), Function.identity(), (a,b)->a, LinkedHashMap::new)).values().stream().toList(); }
    private String cDescription(List<ConflictResponse> conflicts, UUID id) { return conflicts.stream().filter(c -> c.commitmentIds().contains(id)).map(ConflictResponse::description).findFirst().orElse("Compromisso"); }
    private void requireClients() { if (projects == null || teams == null) throw new IllegalStateException("Planning remote clients are not configured"); }
    private ProjectClient requireProjects() { requireClients(); return projects; }
    private TeamClient requireTeams() { requireClients(); return teams; }

    private Comparator<RemoteProject> projectComparator() {
        // RN-DEC-16 is lexicographic: protect the earliest deadlines before
        // using priority as a tie-breaker. Sorting by priority first can make
        // a high-priority project consume the only resource needed by an
        // earlier deadline, so it is not considered an optimization strategy.
        return Comparator.comparing((RemoteProject p) -> p.plannedEndDate() == null ? LocalDate.MAX : p.plannedEndDate())
                .thenComparing(Comparator.comparingInt((RemoteProject p) -> priority(p.priority())).reversed())
                .thenComparing(p -> margin(p) == null ? Integer.MAX_VALUE : margin(p))
                .thenComparing(RemoteProject::identification).thenComparing(RemoteProject::id);
    }
    private int priority(String value) { return switch (value == null ? "NORMAL" : value.toUpperCase()) { case "CRITICAL" -> 4; case "HIGH" -> 3; case "LOW" -> 1; default -> 2; }; }
    private Integer margin(RemoteProject p) { if (p.plannedStartDate() == null || p.plannedEndDate() == null || p.estimatedDurationDays() == null) return null; return (int)(p.plannedEndDate().toEpochDay() - p.plannedStartDate().toEpochDay() + 1 - p.estimatedDurationDays()); }
    private PlanningWindow planningWindow(RemoteProject p) { if (p.plannedStartDate() == null || p.plannedEndDate() == null || p.plannedEndDate().isBefore(p.plannedStartDate())) return null; int duration = p.estimatedDurationDays() == null ? (int)(p.plannedEndDate().toEpochDay() - p.plannedStartDate().toEpochDay() + 1) : p.estimatedDurationDays(); if (duration < 1 || duration > p.plannedEndDate().toEpochDay() - p.plannedStartDate().toEpochDay() + 1) return null; return new PlanningWindow(p.plannedStartDate(), p.plannedEndDate(), duration); }
    private String locality(RemoteProject p) { return p.localities() == null || p.localities().isEmpty() ? null : p.localities().get(0).identification(); }
    private String explanation(List<ConflictResponse> conflicts, RemoteProject project) { return conflicts.isEmpty() ? "Alocação selecionada pela heurística; quantidade desejada não implica produtividade automática." : conflicts.stream().map(ConflictResponse::description).distinct().collect(Collectors.joining(" ")); }
    private ObjectNode unattendedNode(RemoteProject p, String reason, List<String> alternatives) { ObjectNode n=mapper.createObjectNode(); n.put("projectId", p.id().toString()); n.put("projectIdentification", p.identification()); n.put("reason", reason); ArrayNode a=mapper.createArrayNode(); alternatives.forEach(a::add); n.set("alternatives", a); return n; }
    private ObjectNode itemNode(ProposalItem item) { return mapper.valueToTree(item); }
    private UUID stableItemId(String operation, UUID projectId, String resourceType, UUID resourceId, Period period) {
        return UUID.nameUUIDFromBytes((operation + ":" + projectId + ":" + resourceType + ":" + resourceId + ":" + period.start() + ":" + period.end()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
    private String dependencyKey(ProposalItem item) { return item.projectId() + ":" + item.resourceType() + ":" + item.resourceId(); }
    private List<ProposalItem> readItems(JsonNode payload) { JsonNode items=payload.path("items"); if (!items.isArray()) return List.of(); return mapper.convertValue(items, mapper.getTypeFactory().constructCollectionType(List.class, ProposalItem.class)); }
    private record Period(LocalDate start, LocalDate end) {}
    private record PlanningWindow(LocalDate start, LocalDate end, int duration) {}
    private record Candidate(String resourceType, UUID resourceId, String identification, Set<UUID> members, List<Period> periods, boolean individual) {}
    private record Occupancy(String resourceType, UUID resourceId, Set<UUID> members, List<Period> periods) {
        private boolean overlaps(String type, UUID id, Set<UUID> candidateMembers, LocalDate day) {
            return (resourceType.equals(type) && resourceId.equals(id) || !Collections.disjoint(members, candidateMembers))
                    && periods.stream().anyMatch(period -> !day.isBefore(period.start()) && !day.isAfter(period.end()));
        }
    }
}
