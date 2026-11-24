package com.teamflow.project.service;

import com.teamflow.project.api.ProjectPageResponse;
import com.teamflow.project.api.ProjectRequest;
import com.teamflow.project.api.ProjectResponse;
import com.teamflow.project.domain.Project;
import com.teamflow.project.exception.ProjectNotFoundException;
import com.teamflow.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class ProjectService {
    private final ProjectRepository repository;

    public ProjectService(ProjectRepository repository) {
        this.repository = repository;
    }

    public ProjectResponse create(UUID organizationId, ProjectRequest request) {
        OffsetDateTime now = now();
        Project project = fromRequest(UUID.randomUUID(), organizationId, request, now, now);
        return toResponse(repository.save(project));
    }

    public ProjectPageResponse list(UUID organizationId, int page, int size) {
        long total = repository.count(organizationId);
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new ProjectPageResponse(repository.findAll(organizationId, page, size).stream()
                .map(this::toResponse).toList(), page, size, total, totalPages);
    }

    public ProjectResponse findById(UUID organizationId, UUID id) {
        return repository.findById(organizationId, id).map(this::toResponse)
                .orElseThrow(() -> new ProjectNotFoundException(id));
    }

    public ProjectResponse update(UUID organizationId, UUID id, ProjectRequest request) {
        Project current = repository.findById(organizationId, id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        Project updated = fromRequest(current.id(), current.organizationId(), request,
                current.createdAt(), now());
        return repository.update(updated).map(this::toResponse)
                .orElseThrow(() -> new ProjectNotFoundException(id));
    }

    private Project fromRequest(UUID id, UUID organizationId, ProjectRequest request,
                                OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        return new Project(id, organizationId, request.identification().trim(), normalized(request.client()),
                normalized(request.description()), request.plannedStartDate(), request.plannedEndDate(),
                request.estimatedDurationDays(), request.competencies(), request.qualifications(),
                request.localities(), request.services(), createdAt, updatedAt);
    }

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(project.id(), project.organizationId(), project.identification(),
                project.client(), project.description(), project.plannedStartDate(), project.plannedEndDate(),
                project.estimatedDurationDays(), project.competencies(), project.qualifications(),
                project.localities(), project.services(), project.createdAt(), project.updatedAt());
    }

    private String normalized(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
