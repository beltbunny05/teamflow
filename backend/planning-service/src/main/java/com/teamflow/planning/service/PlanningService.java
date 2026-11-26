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
        validateReferences(org, normalized);
        ensureNoBlocking(conflicts(org, normalized));
        return repo.saveAllocation(org, normalized);
    }

    public List<AllocationResponse> list(UUID org, UUID project, String type, UUID resource, LocalDate from, LocalDate to) {
        return repo.allocations(org, project, normalizeType(type), resource, from, to);
    }

    @Transactional
    public AllocationResponse update(UUID org, UUID id, AllocationUpdateRequest request) {
        AllocationRequest normalized = normalize(new AllocationRequest(request.projectId(), request.resourceType(), request.resourceId(), request.startDate(), request.endDate(), request.fixed()));
        validateDates(normalized.startDate(), normalized.endDate());
        validateReferences(org, normalized);
        ensureNoBlocking(conflictsExcluding(org, normalized, id));
        return repo.updateAllocation(org, id, new AllocationUpdateRequest(normalized.projectId(), normalized.resourceType(), normalized.resourceId(), normalized.startDate(), normalized.endDate(), normalized.fixed(), request.expectedUpdatedAt()), request.expectedUpdatedAt());
    }

    @Transactional
    public AllocationResponse fixed(UUID org, UUID id, boolean value, OffsetDateTime expectedUpdatedAt) {
        return repo.setFixed(org, id, value, expectedUpdatedAt);
    }

    @Transactional
    public void cancel(UUID org, UUID id, OffsetDateTime expectedUpdatedAt) { repo.cancel(org, id, expectedUpdatedAt); }

    @Transactional
    public TransportResponse transport(UUID org, TransportRequest request) {
        validateResource(request.resourceType());
        validateDates(request.startDate(), request.startDate());
        if (request.durationDays() != null && request.durationDays() < 1) throw new IllegalArgumentException("durationDays must be positive");
        validateResourceReference(org, request.resourceType(), request.resourceId());
        if (request.origin().trim().equalsIgnoreCase(request.destination().trim())) throw new IllegalArgumentException("origin and destination must differ");
        return repo.saveTransport(org, new TransportRequest(normalizeType(request.resourceType()), request.resourceId(), request.origin().trim(), request.destination().trim(), request.startDate(), request.durationDays()));
    }

    public List<TransportResponse> transports(UUID org) { return repo.transports(org); }

    @Transactional
    public TransportResponse updateTransport(UUID org, UUID id, TransportUpdateRequest request) {
        validateResource(request.resourceType());
        if (request.durationDays() != null && request.durationDays() < 1) throw new IllegalArgumentException("durationDays must be positive");
        validateResourceReference(org, request.resourceType(), request.resourceId());
        if (request.origin().trim().equalsIgnoreCase(request.destination().trim())) throw new IllegalArgumentException("origin and destination must differ");
        return repo.updateTransport(org, id, new TransportUpdateRequest(request.resourceId(), normalizeType(request.resourceType()), request.origin().trim(), request.destination().trim(), request.startDate(), request.durationDays(), request.expectedUpdatedAt()), request.expectedUpdatedAt());
    }

    @Transactional
    public void cancelTransport(UUID org, UUID id, OffsetDateTime expectedUpdatedAt) { repo.cancelTransport(org, id, expectedUpdatedAt); }

    @Transactional
    public AllocationExceptionResponse exception(UUID org, UUID allocationId, AllocationExceptionRequest request) {
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
        return conflictsExcluding(org, normalized, null);
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
        long baseRevision = repo.currentRevision(org);
        List<RemoteProject> projectList = projects.list(org).stream().sorted(projectComparator()).toList();
        List<RemoteTeam> teamList = teams.listTeams(org).stream().filter(RemoteTeam::active).sorted(Comparator.comparing(RemoteTeam::identification).thenComparing(RemoteTeam::id)).toList();
        ArrayNode items = mapper.createArrayNode(); ArrayNode unattended = mapper.createArrayNode();
        List<AllocationResponse> existing = repo.allocations(org, null, null, null, null, null).stream().filter(a -> !"CANCELLED".equals(a.status())).toList();
        existing.stream().filter(AllocationResponse::fixed).forEach(a -> items.add(itemNode(new ProposalItem(a.id(), "RETAIN", a.projectId(), a.projectId().toString(), a.resourceType(), a.resourceId(), a.resourceId().toString(), a.startDate(), a.endDate(), true, "FIXED", "Compromisso fixado preservado.", List.of()))));
        int planned = 0;
        for (RemoteProject project : projectList) {
            Period period = projectPeriod(project);
            if (period == null) { unattended.add(unattendedNode(project, "Dados de início, duração ou prazo estão incompletos.", List.of("Completar o cadastro do projeto."))); continue; }
            String locality = locality(project);
            List<RemoteTeam> selected = selectTeams(org, project, period, locality, teamList, existing);
            if (selected.isEmpty()) {
                unattended.add(unattendedNode(project, "Nenhuma equipe ativa está disponível sem conflito bloqueante.", List.of("Realocar recursos", "Incluir funcionários", "Revisar duração", "Revisar prazo")));
                continue;
            }
            int selectedPeople = selected.stream()
                    .flatMap(team -> requireTeams().employeesForTeam(org, team.id()).stream())
                    .map(RemoteMembership::employeeId).distinct().toList().size();
            boolean capacityAlert = project.desiredPeople() != null && selectedPeople < project.desiredPeople();
            for (RemoteTeam team : selected) {
                List<ConflictResponse> found = conflicts(org, new AllocationRequest(project.id(), "TEAM", team.id(), period.start(), period.end(), false));
                List<ConflictResponse> blocking = found.stream().filter(c -> "BLOCKING".equals(c.severity())).toList();
                if (!blocking.isEmpty()) continue;
                String status = capacityAlert ? "ALERT_CAPACITY" : found.isEmpty() ? "VIABLE" : "ALERT";
                String itemExplanation = capacityAlert ? "Quantidade desejada não atendida; revisar duração ou complementar a equipe." : explanation(found, project);
                items.add(itemNode(new ProposalItem(UUID.randomUUID(), "CREATE", project.id(), project.identification(), "TEAM", team.id(), team.identification(), period.start(), period.end(), false, status, itemExplanation, found.stream().flatMap(c -> c.commitmentIds().stream()).toList())));
                if (locality != null && team.initialLocation() != null && !team.initialLocation().equalsIgnoreCase(locality)) {
                    items.add(itemNode(new ProposalItem(UUID.randomUUID(), "TRANSPORT", project.id(), project.identification(), "TEAM", team.id(), team.identification(), period.start().minusDays(1), period.start().minusDays(1), false, "PENDING_TRANSPORT", "Origem " + team.initialLocation() + " e destino " + locality + "; duração deve ser informada.", List.of())));
                }
                planned++;
            }
        }
        ObjectNode payload = mapper.createObjectNode(); payload.put("strategy", "priority-deadline-greedy-v1"); payload.put("baseRevision", baseRevision); payload.put("explanation", "Proposta determinística por prioridade, prazo, margem e identificador; compromissos fixados são preservados."); payload.set("items", items); payload.set("unattendedProjects", unattended); payload.put("fullyViable", unattended.isEmpty() && items.findValuesAsText("status").stream().noneMatch(status -> status.equals("PENDING_TRANSPORT") || status.equals("ALERT_CAPACITY"))); payload.put("plannedProjectAllocations", planned);
        return repo.saveProposal(org, repo.nextProposalVersion(org), baseRevision, payload.get("explanation").asText(), payload);
    }

    @Transactional
    public ProposalResponse updateProposal(UUID org, UUID id, ProposalUpdateRequest request) {
        ProposalResponse current = proposal(org, id);
        if (!"DRAFT".equals(current.status())) throw new OptimisticLockingFailureException("Only draft proposals can be edited");
        return repo.updateProposal(org, id, request);
    }

    @Transactional
    public ProposalResponse approve(UUID org, UUID id, ProposalApprovalRequest request) {
        ProposalResponse proposal = proposal(org, id);
        if (!"DRAFT".equals(proposal.status())) throw new OptimisticLockingFailureException("Proposal is no longer draft");
        if (repo.currentRevision(org) != proposal.baseRevision()) throw new OptimisticLockingFailureException("Proposal is stale; generate a new proposal");
        List<ProposalItem> items = readItems(proposal.payload());
        Set<UUID> selected = request == null || request.itemIds() == null || request.itemIds().isEmpty() ? items.stream().map(ProposalItem::itemId).collect(Collectors.toSet()) : new HashSet<>(request.itemIds());
        List<ProposalItem> chosen = items.stream().filter(i -> selected.contains(i.itemId())).toList();
        if (chosen.stream().anyMatch(i -> "PENDING_TRANSPORT".equals(i.status()))) throw new IllegalArgumentException("Pending transport must be defined before approval");
        Set<String> pendingTransportDependencies = items.stream().filter(i -> "TRANSPORT".equals(i.operation()) && "PENDING_TRANSPORT".equals(i.status()))
                .map(this::dependencyKey).collect(Collectors.toSet());
        if (chosen.stream().filter(i -> "CREATE".equals(i.operation())).anyMatch(i -> pendingTransportDependencies.contains(dependencyKey(i)))) {
            throw new IllegalArgumentException("Allocation depends on a pending transport; define the transport before approval");
        }
        if (chosen.stream().anyMatch(i -> "BLOCKING".equals(i.status()))) throw new IllegalArgumentException("Blocking proposal items cannot be approved");
        for (ProposalItem item : chosen) {
            if (!"CREATE".equals(item.operation())) continue;
            AllocationRequest allocation = new AllocationRequest(item.projectId(), item.resourceType(), item.resourceId(), item.startDate(), item.endDate(), item.fixed());
            validateReferences(org, allocation); ensureNoBlocking(conflicts(org, allocation));
            repo.saveAllocation(org, allocation);
        }
        repo.markProposal(org, id, "APPROVED");
        repo.log(org, "APPROVE", "PROPOSAL", id, mapper.createObjectNode().put("selectedItems", chosen.size()));
        return proposal(org, id);
    }

    @Transactional
    public ProposalResponse reject(UUID org, UUID id) { proposal(org, id); repo.markProposal(org, id, "REJECTED"); return proposal(org, id); }
    @Transactional
    public ProposalResponse discard(UUID org, UUID id) { proposal(org, id); repo.markProposal(org, id, "DISCARDED"); return proposal(org, id); }

    private List<ConflictResponse> conflictsExcluding(UUID org, AllocationRequest r, UUID excluded) {
        String type = normalizeType(r.resourceType()); validateResource(type); List<ConflictResponse> result = new ArrayList<>();
        for (AllocationResponse a : repo.allocations(org, null, type, r.resourceId(), r.startDate(), r.endDate())) {
            if (!a.id().equals(excluded) && !"CANCELLED".equals(a.status()) && overlaps(a.startDate(), a.endDate(), r.startDate(), r.endDate())) result.add(conflict("OVERLAPPING_ALLOCATION", "BLOCKING", type, r.resourceId(), r.startDate(), r.endDate(), "O recurso já possui uma alocação sobreposta.", "Escolha outro recurso ou período.", List.of(a.id())));
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

    private List<ConflictResponse> employeeConflicts(UUID org, AllocationRequest r, UUID excluded) {
        List<ConflictResponse> result = new ArrayList<>(); RemoteEmployee employee = requireTeams().getEmployee(org, r.resourceId());
        if (!employee.active()) result.add(conflict("INACTIVE_RESOURCE", "BLOCKING", "EMPLOYEE", employee.id(), r.startDate(), r.endDate(), "O funcionário está inativo.", "Ative o funcionário ou selecione outro recurso.", List.of()));
        for (RemoteUnavailability u : requireTeams().unavailability(org, employee.id())) if (overlaps(u.startDate(), u.endDate(), r.startDate(), r.endDate())) result.add(conflict("EMPLOYEE_UNAVAILABLE", "BLOCKING", "EMPLOYEE", employee.id(), u.startDate(), u.endDate(), "O funcionário possui indisponibilidade cadastrada.", "Escolha outro período ou funcionário.", List.of()));
        for (RemoteMembership membership : requireTeams().teamsForEmployee(org, employee.id())) {
            for (AllocationResponse a : repo.allocations(org, null, "TEAM", membership.teamId(), r.startDate(), r.endDate())) if (!"CANCELLED".equals(a.status()) && !a.id().equals(excluded) && overlaps(a.startDate(), a.endDate(), r.startDate(), r.endDate())) result.add(conflict("MULTIPLE_TEAM_COMMITMENT", "BLOCKING", "EMPLOYEE", employee.id(), r.startDate(), r.endDate(), "O funcionário está comprometido por outra equipe no mesmo período.", "Realocar o funcionário ou ajustar as datas.", List.of(a.id())));
        }
        return result;
    }

    private List<ConflictResponse> teamConflicts(UUID org, AllocationRequest r, UUID excluded) {
        List<ConflictResponse> result = new ArrayList<>(); RemoteTeam team = requireTeams().getTeam(org, r.resourceId());
        if (!team.active()) result.add(conflict("INACTIVE_RESOURCE", "BLOCKING", "TEAM", team.id(), r.startDate(), r.endDate(), "A equipe está inativa.", "Ative a equipe ou selecione outra.", List.of()));
        List<RemoteMembership> memberships = requireTeams().employeesForTeam(org, team.id()); Set<UUID> seen = new HashSet<>();
        for (RemoteMembership membership : memberships) if (seen.add(membership.employeeId())) {
            RemoteEmployee employee = requireTeams().getEmployee(org, membership.employeeId());
            if (!employee.active()) result.add(conflict("INACTIVE_RESOURCE", "BLOCKING", "EMPLOYEE", employee.id(), r.startDate(), r.endDate(), "A equipe contém funcionário inativo.", "Atualize a composição ou use outra equipe.", List.of()));
            for (RemoteUnavailability u : requireTeams().unavailability(org, employee.id())) if (overlaps(u.startDate(), u.endDate(), r.startDate(), r.endDate())) result.add(conflict("EMPLOYEE_UNAVAILABLE", "BLOCKING", "EMPLOYEE", employee.id(), u.startDate(), u.endDate(), "Um integrante possui indisponibilidade cadastrada.", "Exclua temporariamente o integrante ou ajuste o período.", List.of()));
            for (RemoteMembership other : requireTeams().teamsForEmployee(org, employee.id())) for (AllocationResponse a : repo.allocations(org, null, "TEAM", other.teamId(), r.startDate(), r.endDate())) if (!a.id().equals(excluded) && !"CANCELLED".equals(a.status()) && !a.resourceId().equals(team.id()) && overlaps(a.startDate(), a.endDate(), r.startDate(), r.endDate())) result.add(conflict("MULTIPLE_TEAM_COMMITMENT", "BLOCKING", "EMPLOYEE", employee.id(), r.startDate(), r.endDate(), "Um integrante está comprometido por outra equipe.", "Realocar o integrante ou ajustar as datas.", List.of(a.id())));
        }
        return result;
    }

    private List<RemoteTeam> selectTeams(UUID org, RemoteProject project, Period period, String locality, List<RemoteTeam> candidates, List<AllocationResponse> existing) {
        int desired = project.desiredPeople() == null ? 0 : project.desiredPeople(); List<RemoteTeam> selected = new ArrayList<>(); Set<UUID> employeeIds = new HashSet<>();
        for (RemoteTeam team : candidates) {
            if (!selected.isEmpty() && desired == 0) break;
            List<ConflictResponse> conflicts = conflicts(org, new AllocationRequest(project.id(), "TEAM", team.id(), period.start(), period.end(), false));
            if (conflicts.stream().anyMatch(c -> "BLOCKING".equals(c.severity()))) continue;
            List<RemoteMembership> members = requireTeams().employeesForTeam(org, team.id()); Set<UUID> newMembers = members.stream().map(RemoteMembership::employeeId).filter(employeeIds::add).collect(Collectors.toSet());
            if (members.isEmpty() || newMembers.isEmpty()) continue;
            selected.add(team);
            if (desired > 0 && employeeIds.size() >= desired) break;
        }
        return selected;
    }

    private void validateReferences(UUID org, AllocationRequest r) { requireProjects().get(org, r.projectId()); validateResourceReference(org, r.resourceType(), r.resourceId()); }
    private void validateResourceReference(UUID org, String type, UUID id) { if ("TEAM".equals(normalizeType(type))) { if (!requireTeams().getTeam(org, id).active()) throw new IllegalArgumentException("Inactive team cannot be allocated"); } else { if (!requireTeams().getEmployee(org, id).active()) throw new IllegalArgumentException("Inactive employee cannot be allocated"); } }
    private void ensureNoBlocking(List<ConflictResponse> conflicts) { if (conflicts.stream().anyMatch(c -> "BLOCKING".equals(c.severity()))) throw new IllegalArgumentException(conflicts.stream().filter(c -> "BLOCKING".equals(c.severity())).map(ConflictResponse::description).distinct().collect(Collectors.joining(" "))); }
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

    private Comparator<RemoteProject> projectComparator() { return Comparator.comparingInt((RemoteProject p) -> priority(p.priority())).reversed().thenComparing(p -> p.plannedEndDate() == null ? LocalDate.MAX : p.plannedEndDate()).thenComparing(p -> margin(p) == null ? Integer.MAX_VALUE : margin(p)).thenComparing(RemoteProject::identification).thenComparing(RemoteProject::id); }
    private int priority(String value) { return switch (value == null ? "NORMAL" : value.toUpperCase()) { case "CRITICAL" -> 4; case "HIGH" -> 3; case "LOW" -> 1; default -> 2; }; }
    private Integer margin(RemoteProject p) { if (p.plannedStartDate() == null || p.plannedEndDate() == null || p.estimatedDurationDays() == null) return null; return (int)(p.plannedEndDate().toEpochDay() - p.plannedStartDate().toEpochDay() + 1 - p.estimatedDurationDays()); }
    private Period projectPeriod(RemoteProject p) { if (p.plannedStartDate() == null || p.plannedEndDate() == null) return null; int duration = p.estimatedDurationDays() == null ? (int)(p.plannedEndDate().toEpochDay() - p.plannedStartDate().toEpochDay() + 1) : p.estimatedDurationDays(); if (duration < 1) return null; LocalDate end = p.plannedStartDate().plusDays(duration - 1); if (end.isAfter(p.plannedEndDate())) return null; return new Period(p.plannedStartDate(), end); }
    private String locality(RemoteProject p) { return p.localities() == null || p.localities().isEmpty() ? null : p.localities().get(0).identification(); }
    private String explanation(List<ConflictResponse> conflicts, RemoteProject project) { return conflicts.isEmpty() ? "Alocação selecionada pela heurística; quantidade desejada não implica produtividade automática." : conflicts.stream().map(ConflictResponse::description).distinct().collect(Collectors.joining(" ")); }
    private ObjectNode unattendedNode(RemoteProject p, String reason, List<String> alternatives) { ObjectNode n=mapper.createObjectNode(); n.put("projectId", p.id().toString()); n.put("projectIdentification", p.identification()); n.put("reason", reason); ArrayNode a=mapper.createArrayNode(); alternatives.forEach(a::add); n.set("alternatives", a); return n; }
    private ObjectNode itemNode(ProposalItem item) { return mapper.valueToTree(item); }
    private String dependencyKey(ProposalItem item) { return item.projectId() + ":" + item.resourceType() + ":" + item.resourceId(); }
    private List<ProposalItem> readItems(JsonNode payload) { JsonNode items=payload.path("items"); if (!items.isArray()) return List.of(); return mapper.convertValue(items, mapper.getTypeFactory().constructCollectionType(List.class, ProposalItem.class)); }
    private record Period(LocalDate start, LocalDate end) {}
}
