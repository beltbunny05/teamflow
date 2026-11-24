package com.teamflow.project.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectLocation(
        @NotBlank @Size(max = 150) String identification
) {
}
