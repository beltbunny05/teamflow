package com.teamflow.project.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProjectServiceItem(
        @NotBlank @Size(max = 150) String identification,
        @Size(max = 2000) String description,
        @Min(0) Integer serviceDays,
        @Min(0) Integer estimatedDurationDays,
        List<@NotBlank @Size(max = 100) String> competencies,
        List<@NotBlank @Size(max = 100) String> qualifications
) {
}
