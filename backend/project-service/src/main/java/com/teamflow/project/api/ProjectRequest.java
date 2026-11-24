package com.teamflow.project.api;

import com.teamflow.project.domain.ProjectLocation;
import com.teamflow.project.domain.ProjectServiceItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record ProjectRequest(
        @NotBlank @Size(max = 150) String identification,
        @Size(max = 150) String client,
        @Size(max = 2000) String description,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate,
        @Min(0) Integer estimatedDurationDays,
        List<@NotBlank @Size(max = 100) String> competencies,
        List<@NotBlank @Size(max = 100) String> qualifications,
        @Valid List<ProjectLocation> localities,
        @Valid List<ProjectServiceItem> services
) {
}
