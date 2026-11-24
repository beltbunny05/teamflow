package com.teamflow.project.repository;

import com.teamflow.project.domain.Project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository {
    Project save(Project project);

    List<Project> findAll(UUID organizationId, int page, int size);

    long count(UUID organizationId);

    Optional<Project> findById(UUID organizationId, UUID id);

    Optional<Project> update(Project project);
}
