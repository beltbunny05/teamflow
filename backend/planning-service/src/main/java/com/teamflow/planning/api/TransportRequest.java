package com.teamflow.planning.api;
import jakarta.validation.constraints.*; import java.time.LocalDate; import java.util.UUID;
public record TransportRequest(@NotNull String resourceType,@NotNull UUID resourceId,@NotBlank String origin,@NotBlank String destination,@NotNull LocalDate startDate,@Min(1) Integer durationDays) {}
