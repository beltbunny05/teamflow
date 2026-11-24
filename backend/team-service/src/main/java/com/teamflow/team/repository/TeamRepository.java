package com.teamflow.team.repository;

import com.teamflow.team.domain.Team;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository {
    Team save(Team team);

    List<Team> findAll(UUID organizationId, int page, int size);

    long count(UUID organizationId);

    Optional<Team> findById(UUID organizationId, UUID id);

    Optional<Team> update(Team team);
}
