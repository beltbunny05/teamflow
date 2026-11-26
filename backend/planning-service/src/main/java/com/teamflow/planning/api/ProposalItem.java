package com.teamflow.planning.api;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProposalItem(UUID itemId, String operation, UUID projectId, String projectIdentification,
                           String resourceType, UUID resourceId, String resourceIdentification,
                           LocalDate startDate, LocalDate endDate, boolean fixed, String status,
                           String explanation, List<UUID> conflictIds, UUID relatedId,
                           Integer durationDays, LocalDate arrivalDate, String origin, String destination) {
    public ProposalItem(UUID itemId, String operation, UUID projectId, String projectIdentification,
                        String resourceType, UUID resourceId, String resourceIdentification,
                        LocalDate startDate, LocalDate endDate, boolean fixed, String status,
                        String explanation, List<UUID> conflictIds) {
        this(itemId, operation, projectId, projectIdentification, resourceType, resourceId,
                resourceIdentification, startDate, endDate, fixed, status, explanation,
                conflictIds, null, null, null, null, null);
    }
}
