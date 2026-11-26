package com.teamflow.team.api;

import java.util.UUID;

public record EmployeeTeamResponse(UUID teamId, UUID employeeId, String role) {}
