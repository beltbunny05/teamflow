package com.teamflow.planning.api;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AllocationExceptionResponse(UUID id, UUID organizationId, UUID allocationId,
                                          UUID employeeId, String kind, LocalDate startDate,
                                          LocalDate endDate, UUID targetProjectId,
                                          LocalDate targetStartDate, LocalDate targetEndDate,
                                          String reason, OffsetDateTime createdAt) {
}
