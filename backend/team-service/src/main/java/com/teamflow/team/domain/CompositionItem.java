package com.teamflow.team.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompositionItem(
        @NotBlank @Size(max = 100) String role,
        @Min(0) int quantity
) {
}
