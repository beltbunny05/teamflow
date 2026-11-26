package com.teamflow.planning.api;

import java.time.LocalDate;
import java.util.UUID;

public record AvailabilityCommitment(UUID id, String kind, UUID projectId, LocalDate startDate,
                                     LocalDate endDate, String status, boolean fixed,
                                     String explanation) {
}
