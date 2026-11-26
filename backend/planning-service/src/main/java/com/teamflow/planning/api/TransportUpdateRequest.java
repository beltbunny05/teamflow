package com.teamflow.planning.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TransportUpdateRequest(@NotNull UUID resourceId, @NotNull String resourceType,
                                     @NotBlank String origin, @NotBlank String destination,
                                     @NotNull LocalDate startDate, @Min(1) Integer durationDays,
                                     OffsetDateTime expectedUpdatedAt) {
}
