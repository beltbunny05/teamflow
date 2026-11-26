package com.teamflow.team.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.teamflow.team.domain.CompositionItem;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record TeamResponse(
        UUID id,
        UUID organizationId,
        String identification,
        int totalMembers,
        List<CompositionItem> composition,
        List<String> competencies,
        List<String> qualifications,
        JsonNode availability,
        String status,
        String initialLocation,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
