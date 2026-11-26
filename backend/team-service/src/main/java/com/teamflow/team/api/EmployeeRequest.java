package com.teamflow.team.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmployeeRequest(
        @NotBlank @Size(max = 150) String identification,
        @NotBlank @Size(max = 100) String primaryRole,
        Boolean active,
        @Size(max = 150) String initialLocation) {}
