package com.teamflow.team.domain;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record Team(
        UUID id,
        UUID organizationId,
        String identification,
        int totalMembers,
        List<CompositionItem> composition,
        List<String> competencies,
        List<String> qualifications,
        JsonNode availability,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
