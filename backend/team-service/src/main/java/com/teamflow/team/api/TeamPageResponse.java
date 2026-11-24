package com.teamflow.team.api;

import java.util.List;

public record TeamPageResponse(
        List<TeamResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
