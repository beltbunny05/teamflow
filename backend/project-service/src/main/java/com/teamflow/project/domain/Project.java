package com.teamflow.project.domain;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record Project(
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
        String priority,
        boolean continuousExecution,
        Integer desiredPeople,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
