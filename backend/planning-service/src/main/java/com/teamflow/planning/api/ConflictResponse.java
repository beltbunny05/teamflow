package com.teamflow.planning.api;
import java.time.LocalDate; import java.util.UUID;
public record ConflictResponse(String type, String severity, String resourceType, UUID resourceId,
                               LocalDate startDate, LocalDate endDate, String description,
                               String alternative, java.util.List<UUID> commitmentIds) {
    public ConflictResponse(String type, String severity, String resourceType, UUID resourceId,
                            LocalDate startDate, LocalDate endDate, String description, String alternative) {
        this(type, severity, resourceType, resourceId, startDate, endDate, description, alternative, java.util.List.of());
    }
}
