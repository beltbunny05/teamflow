package com.teamflow.team.api;

import jakarta.validation.constraints.Size;

public record OrganizationLocationRequest(@Size(max = 150) String baseLocation) {}
