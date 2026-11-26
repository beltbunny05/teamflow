package com.teamflow.planning.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.teamflow.planning.api.*;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Repository
public class PlanningRepository {
    private final JdbcTemplate jdbc;

    public PlanningRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public AllocationResponse saveAllocation(UUID org, AllocationRequest r) {
        UUID id = UUID.randomUUID(); OffsetDateTime now = now();
        jdbc.update("""
                INSERT INTO allocations (id, organization_id, project_id, resource_type, resource_id,
                    start_date, end_date, fixed, status, created_at, updated_at, revision)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'CONFIRMED', ?, ?, 0)
                """, id, org, r.projectId(), r.resourceType(), r.resourceId(), r.startDate(), r.endDate(),
                Boolean.TRUE.equals(r.fixed()), now, now);
        bumpRevision(org); return allocation(org, id).orElseThrow();
    }

    public List<AllocationResponse> allocations(UUID org, UUID project, String type, UUID resource, LocalDate from, LocalDate to) {
        StringBuilder sql = new StringBuilder("SELECT * FROM allocations WHERE organization_id=?");
        List<Object> p = new ArrayList<>(List.of(org));
        if (project != null) { sql.append(" AND project_id=?"); p.add(project); }
        if (type != null) { sql.append(" AND resource_type=?"); p.add(type); }
        if (resource != null) { sql.append(" AND resource_id=?"); p.add(resource); }
        if (from != null) { sql.append(" AND end_date>=?"); p.add(from); }
        if (to != null) { sql.append(" AND start_date<=?"); p.add(to); }
        sql.append(" ORDER BY start_date, id");
        return jdbc.query(sql.toString(), this::allocationRow, p.toArray());
    }

    public Optional<AllocationResponse> allocation(UUID org, UUID id) {
        return jdbc.query("SELECT * FROM allocations WHERE organization_id=? AND id=?", this::allocationRow, org, id).stream().findFirst();
    }

    public AllocationResponse updateAllocation(UUID org, UUID id, AllocationUpdateRequest r, OffsetDateTime expected) {
        OffsetDateTime now = now();
        String sql = "UPDATE allocations SET project_id=?, resource_type=?, resource_id=?, start_date=?, end_date=?, fixed=?, updated_at=?, revision=revision+1 WHERE organization_id=? AND id=? AND status <> 'CANCELLED'" + (expected == null ? "" : " AND updated_at=?");
        List<Object> p = new ArrayList<>(List.of(r.projectId(), r.resourceType(), r.resourceId(), r.startDate(), r.endDate(), Boolean.TRUE.equals(r.fixed()), now, org, id));
        if (expected != null) p.add(expected);
        if (jdbc.update(sql, p.toArray()) == 0) throw new OptimisticLockingFailureException("Allocation was changed or cancelled");
        bumpRevision(org); return allocation(org, id).orElseThrow();
    }

    public AllocationResponse setFixed(UUID org, UUID id, boolean fixed, OffsetDateTime expected) {
        String sql = "UPDATE allocations SET fixed=?, updated_at=?, revision=revision+1 WHERE organization_id=? AND id=? AND status <> 'CANCELLED'" + (expected == null ? "" : " AND updated_at=?");
        List<Object> p = new ArrayList<>(List.of(fixed, now(), org, id)); if (expected != null) p.add(expected);
        if (jdbc.update(sql, p.toArray()) == 0) throw new OptimisticLockingFailureException("Allocation was changed or cancelled");
        bumpRevision(org); return allocation(org, id).orElseThrow();
    }

    public void cancel(UUID org, UUID id, OffsetDateTime expected) {
        String sql = "UPDATE allocations SET status='CANCELLED', updated_at=?, revision=revision+1 WHERE organization_id=? AND id=? AND status <> 'CANCELLED'" + (expected == null ? "" : " AND updated_at=?");
        List<Object> p = new ArrayList<>(List.of(now(), org, id)); if (expected != null) p.add(expected);
        if (jdbc.update(sql, p.toArray()) == 0) throw new OptimisticLockingFailureException("Allocation was changed or cancelled");
        bumpRevision(org);
    }

    public TransportResponse saveTransport(UUID org, TransportRequest r) {
        UUID id = UUID.randomUUID(); OffsetDateTime now = now();
        LocalDate arrival = r.durationDays() == null ? null : r.startDate().plusDays(r.durationDays());
        String status = r.durationDays() == null ? "PENDING" : "CONFIRMED";
        jdbc.update("""
                INSERT INTO transports (id, organization_id, resource_type, resource_id, origin, destination,
                    start_date, duration_days, arrival_date, status, created_at, updated_at, revision)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                """, id, org, r.resourceType(), r.resourceId(), r.origin(), r.destination(), r.startDate(), r.durationDays(), arrival, status, now, now);
        bumpRevision(org); return transport(org, id).orElseThrow();
    }

    public List<TransportResponse> transports(UUID org) { return jdbc.query("SELECT * FROM transports WHERE organization_id=? ORDER BY start_date, id", this::transportRow, org); }
    public Optional<TransportResponse> transport(UUID org, UUID id) { return jdbc.query("SELECT * FROM transports WHERE organization_id=? AND id=?", this::transportRow, org, id).stream().findFirst(); }

    public TransportResponse updateTransport(UUID org, UUID id, TransportUpdateRequest r, OffsetDateTime expected) {
        LocalDate arrival = r.durationDays() == null ? null : r.startDate().plusDays(r.durationDays());
        String status = r.durationDays() == null ? "PENDING" : "CONFIRMED";
        String sql = "UPDATE transports SET resource_type=?, resource_id=?, origin=?, destination=?, start_date=?, duration_days=?, arrival_date=?, status=?, updated_at=?, revision=revision+1 WHERE organization_id=? AND id=? AND status <> 'CANCELLED'" + (expected == null ? "" : " AND updated_at=?");
        List<Object> p = new ArrayList<>(List.of(r.resourceType(), r.resourceId(), r.origin(), r.destination(), r.startDate(), r.durationDays(), arrival, status, now(), org, id)); if (expected != null) p.add(expected);
        if (jdbc.update(sql, p.toArray()) == 0) throw new OptimisticLockingFailureException("Transport was changed or cancelled");
        bumpRevision(org); return transport(org, id).orElseThrow();
    }

    public void cancelTransport(UUID org, UUID id, OffsetDateTime expected) {
        String sql = "UPDATE transports SET status='CANCELLED', updated_at=?, revision=revision+1 WHERE organization_id=? AND id=? AND status <> 'CANCELLED'" + (expected == null ? "" : " AND updated_at=?");
        List<Object> p = new ArrayList<>(List.of(now(), org, id)); if (expected != null) p.add(expected);
        if (jdbc.update(sql, p.toArray()) == 0) throw new OptimisticLockingFailureException("Transport was changed or cancelled");
        bumpRevision(org);
    }

    public AllocationExceptionResponse saveException(UUID org, UUID allocationId, AllocationExceptionRequest r) {
        UUID id = UUID.randomUUID(); OffsetDateTime created = now();
        jdbc.update("""
                INSERT INTO allocation_exceptions (id, organization_id, allocation_id, employee_id, kind, start_date,
                    end_date, target_project_id, target_start_date, target_end_date, reason, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, id, org, allocationId, r.employeeId(), r.kind(), r.startDate(), r.endDate(), r.targetProjectId(), r.targetStartDate(), r.targetEndDate(), r.reason(), created);
        return exception(org, id).orElseThrow();
    }
    public List<AllocationExceptionResponse> exceptions(UUID org, UUID allocationId) { return jdbc.query("SELECT * FROM allocation_exceptions WHERE organization_id=? AND allocation_id=? ORDER BY start_date, id", this::exceptionRow, org, allocationId); }

    public long currentRevision(UUID org) { ensureState(org); Long n = jdbc.queryForObject("SELECT allocation_revision FROM planning_state WHERE organization_id=?", Long.class, org); return n == null ? 0 : n; }
    public long nextProposalVersion(UUID org) { Long n = jdbc.queryForObject("SELECT COALESCE(MAX(version),0)+1 FROM planning_proposals WHERE organization_id=?", Long.class, org); return n == null ? 1 : n; }

    public ProposalResponse saveProposal(UUID org, long version, long baseRevision, String explanation, JsonNode payload) {
        UUID id = UUID.randomUUID(); OffsetDateTime now = now();
        jdbc.update("INSERT INTO planning_proposals (id, organization_id, version, status, payload, created_at, approved_at, base_revision, updated_at) VALUES (?, ?, ?, 'DRAFT', ?::jsonb, ?, NULL, ?, ?)", id, org, version, payload.toString(), now, baseRevision, now);
        return proposal(org, id).orElseThrow();
    }
    public List<ProposalResponse> proposals(UUID org) { return jdbc.query("SELECT * FROM planning_proposals WHERE organization_id=? ORDER BY version DESC", this::proposalRow, org); }
    public Optional<ProposalResponse> proposal(UUID org, UUID id) { return jdbc.query("SELECT * FROM planning_proposals WHERE organization_id=? AND id=?", this::proposalRow, org, id).stream().findFirst(); }
    public ProposalResponse updateProposal(UUID org, UUID id, ProposalUpdateRequest r) {
        OffsetDateTime now = now();
        String payload = "{\"explanation\":" + quote(r.explanation()) + ",\"items\":" + new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(r.items()) + "}";
        if (jdbc.update("UPDATE planning_proposals SET payload=?::jsonb, updated_at=? WHERE organization_id=? AND id=? AND status='DRAFT'", payload, now, org, id) == 0) throw new OptimisticLockingFailureException("Proposal is no longer editable");
        return proposal(org, id).orElseThrow();
    }
    public void markProposal(UUID org, UUID id, String status) {
        if (jdbc.update("UPDATE planning_proposals SET status=?, approved_at=?, updated_at=? WHERE organization_id=? AND id=? AND status='DRAFT'", status, "APPROVED".equals(status) ? now() : null, now(), org, id) == 0) throw new OptimisticLockingFailureException("Proposal is no longer editable");
    }
    public void log(UUID org, String action, String entityType, UUID entityId, JsonNode details) { jdbc.update("INSERT INTO planning_change_log (id, organization_id, action, entity_type, entity_id, details, created_at) VALUES (?, ?, ?, ?, ?, ?::jsonb, ?)", UUID.randomUUID(), org, action, entityType, entityId, details.toString(), now()); }

    private void bumpRevision(UUID org) { ensureState(org); jdbc.update("UPDATE planning_state SET allocation_revision=allocation_revision+1, updated_at=? WHERE organization_id=?", now(), org); }
    private void ensureState(UUID org) { jdbc.update("INSERT INTO planning_state (organization_id, allocation_revision, updated_at) VALUES (?, 0, ?) ON CONFLICT (organization_id) DO NOTHING", org, now()); }
    private Optional<AllocationExceptionResponse> exception(UUID org, UUID id) { return jdbc.query("SELECT * FROM allocation_exceptions WHERE organization_id=? AND id=?", this::exceptionRow, org, id).stream().findFirst(); }

    private AllocationResponse allocationRow(ResultSet rs, int ignored) throws java.sql.SQLException { return new AllocationResponse(rs.getObject("id", UUID.class), rs.getObject("organization_id", UUID.class), rs.getObject("project_id", UUID.class), rs.getString("resource_type"), rs.getObject("resource_id", UUID.class), rs.getObject("start_date", LocalDate.class), rs.getObject("end_date", LocalDate.class), rs.getBoolean("fixed"), rs.getString("status"), rs.getObject("created_at", OffsetDateTime.class), rs.getObject("updated_at", OffsetDateTime.class), rs.getLong("revision")); }
    private TransportResponse transportRow(ResultSet rs, int ignored) throws java.sql.SQLException { return new TransportResponse(rs.getObject("id", UUID.class), rs.getObject("organization_id", UUID.class), rs.getString("resource_type"), rs.getObject("resource_id", UUID.class), rs.getString("origin"), rs.getString("destination"), rs.getObject("start_date", LocalDate.class), (Integer) rs.getObject("duration_days"), rs.getObject("arrival_date", LocalDate.class), rs.getString("status"), rs.getObject("created_at", OffsetDateTime.class), rs.getObject("updated_at", OffsetDateTime.class), rs.getLong("revision")); }
    private AllocationExceptionResponse exceptionRow(ResultSet rs, int ignored) throws java.sql.SQLException { return new AllocationExceptionResponse(rs.getObject("id", UUID.class), rs.getObject("organization_id", UUID.class), rs.getObject("allocation_id", UUID.class), rs.getObject("employee_id", UUID.class), rs.getString("kind"), rs.getObject("start_date", LocalDate.class), rs.getObject("end_date", LocalDate.class), rs.getObject("target_project_id", UUID.class), rs.getObject("target_start_date", LocalDate.class), rs.getObject("target_end_date", LocalDate.class), rs.getString("reason"), rs.getObject("created_at", OffsetDateTime.class)); }
    private ProposalResponse proposalRow(ResultSet rs, int ignored) throws java.sql.SQLException { try { JsonNode payload = new com.fasterxml.jackson.databind.ObjectMapper().readTree(rs.getString("payload")); return new ProposalResponse(rs.getObject("id", UUID.class), rs.getObject("organization_id", UUID.class), rs.getLong("version"), rs.getString("status"), payload.path("explanation").asText("Proposta de planejamento"), rs.getObject("created_at", OffsetDateTime.class), rs.getObject("updated_at", OffsetDateTime.class), rs.getLong("base_revision"), payload); } catch (java.io.IOException e) { throw new IllegalStateException("Invalid proposal payload", e); } }
    private String quote(String value) { return new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(value).toString(); }
    private OffsetDateTime now() { return OffsetDateTime.now(ZoneOffset.UTC); }
}
