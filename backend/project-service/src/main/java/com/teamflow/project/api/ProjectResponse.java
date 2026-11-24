package com.teamflow.project.api;

import com.teamflow.project.domain.ProjectLocation;
import com.teamflow.project.domain.ProjectServiceItem;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        UUID organizationId,
        String identification,
        String client,
        String description,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate,
        Integer estimatedDurationDays,
        List<String> competencies,
        List<String> qualifications,
        List<ProjectLocation> localities,
        List<ProjectServiceItem> services,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
