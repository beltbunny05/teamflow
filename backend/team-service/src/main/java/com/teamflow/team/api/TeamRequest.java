package com.teamflow.team.api;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.teamflow.team.domain.CompositionItem;

import java.util.List;

public record TeamRequest(
        @NotBlank @Size(max = 150) String identification,
        @NotNull @Min(0) Integer totalMembers,
        @Valid List<CompositionItem> composition,
        List<@NotBlank @Size(max = 100) String> competencies,
        List<@NotBlank @Size(max = 100) String> qualifications,
        JsonNode availability,
        @Size(max = 50) String status
) {
}
