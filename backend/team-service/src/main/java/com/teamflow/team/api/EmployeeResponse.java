package com.teamflow.team.api;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EmployeeResponse(UUID id, UUID organizationId, String identification, String primaryRole,
                               boolean active, String initialLocation, OffsetDateTime createdAt,
                               OffsetDateTime updatedAt) {}
