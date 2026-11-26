package com.teamflow.team.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TeamMembershipRequest(@NotBlank @Size(max = 100) String role) {}
