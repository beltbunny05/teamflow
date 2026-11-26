package com.teamflow.planning.client;

import java.util.UUID;

public record RemoteEmployee(UUID id, UUID organizationId, String identification,
                             String primaryRole, boolean active, String initialLocation) {
}
