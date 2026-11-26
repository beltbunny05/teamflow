package com.teamflow.planning.api;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AvailabilityResponse(UUID organizationId, String resourceType, UUID resourceId,
                                   LocalDate startDate, LocalDate endDate, String status,
                                   List<String> reasons, List<AvailabilityCommitment> commitments) {
}
