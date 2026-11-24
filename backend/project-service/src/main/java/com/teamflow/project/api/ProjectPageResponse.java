package com.teamflow.project.api;

import java.util.List;

public record ProjectPageResponse(
        List<ProjectResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
