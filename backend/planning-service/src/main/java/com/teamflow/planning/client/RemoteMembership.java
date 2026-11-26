package com.teamflow.planning.client;

import java.util.UUID;

public record RemoteMembership(UUID teamId, UUID employeeId, String role) {
}
