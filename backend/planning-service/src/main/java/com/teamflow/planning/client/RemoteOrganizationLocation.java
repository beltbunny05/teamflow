package com.teamflow.planning.client;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RemoteOrganizationLocation(UUID organizationId, String baseLocation, OffsetDateTime updatedAt) {}
