package com.teamflow.team.service;

import com.teamflow.team.api.TeamPageResponse;
import com.teamflow.team.api.TeamRequest;
import com.teamflow.team.api.TeamResponse;
import com.teamflow.team.domain.Team;
import com.teamflow.team.exception.TeamNotFoundException;
import com.teamflow.team.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class TeamService {
    private final TeamRepository repository;

    public TeamService(TeamRepository repository) {
        this.repository = repository;
    }

    public TeamResponse create(UUID organizationId, TeamRequest request) {
        OffsetDateTime now = now();
        Team team = new Team(UUID.randomUUID(), organizationId, request.identification().trim(),
                request.totalMembers(), request.composition(), request.competencies(),
                request.qualifications(), request.availability(), normalizedStatus(request.status()),
                normalize(request.initialLocation()), now, now);
        return toResponse(repository.save(team));
    }

    public TeamPageResponse list(UUID organizationId, int page, int size) {
        long total = repository.count(organizationId);
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new TeamPageResponse(repository.findAll(organizationId, page, size).stream()
                .map(this::toResponse).toList(), page, size, total, totalPages);
    }

    public TeamResponse findById(UUID organizationId, UUID id) {
        return repository.findById(organizationId, id).map(this::toResponse)
                .orElseThrow(() -> new TeamNotFoundException(id));
    }

    public TeamResponse update(UUID organizationId, UUID id, TeamRequest request) {
        Team current = repository.findById(organizationId, id)
                .orElseThrow(() -> new TeamNotFoundException(id));
        Team updated = new Team(current.id(), current.organizationId(), request.identification().trim(),
                request.totalMembers(), request.composition(), request.competencies(),
                request.qualifications(), request.availability(), normalizedStatus(request.status()),
                normalize(request.initialLocation()), current.createdAt(), now());
        return repository.update(updated).map(this::toResponse)
                .orElseThrow(() -> new TeamNotFoundException(id));
    }

    private TeamResponse toResponse(Team team) {
        return new TeamResponse(team.id(), team.organizationId(), team.identification(), team.totalMembers(),
                team.composition(), team.competencies(), team.qualifications(), team.availability(),
                team.status(), team.initialLocation(), team.createdAt(), team.updatedAt());
    }

    private String normalizedStatus(String status) {
        return status == null || status.isBlank() ? null : status.trim();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
