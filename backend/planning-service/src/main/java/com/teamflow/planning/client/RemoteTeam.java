package com.teamflow.planning.client;

import java.util.UUID;

public record RemoteTeam(UUID id, UUID organizationId, String identification, int totalMembers,
                         String status, String initialLocation) {
    public boolean active() {
        return status == null || !(status.equalsIgnoreCase("INACTIVE") || status.equalsIgnoreCase("INATIVA"));
    }
}
