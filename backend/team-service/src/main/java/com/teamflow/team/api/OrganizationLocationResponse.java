package com.teamflow.team.api;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrganizationLocationResponse(UUID organizationId, String baseLocation, OffsetDateTime updatedAt) {}
