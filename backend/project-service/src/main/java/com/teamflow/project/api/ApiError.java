package com.teamflow.project.api;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String code,
        String message,
        Map<String, String> fields
) {
}
