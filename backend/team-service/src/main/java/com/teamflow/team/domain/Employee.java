package com.teamflow.team.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Employee(UUID id, UUID organizationId, String identification, String primaryRole,
                       boolean active, String initialLocation, OffsetDateTime createdAt,
                       OffsetDateTime updatedAt) {}
