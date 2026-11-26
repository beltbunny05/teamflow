package com.teamflow.planning.api;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProposalItem(UUID itemId, String operation, UUID projectId, String projectIdentification,
                           String resourceType, UUID resourceId, String resourceIdentification,
                           LocalDate startDate, LocalDate endDate, boolean fixed, String status,
                           String explanation, List<UUID> conflictIds) {
}
