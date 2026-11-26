package com.teamflow.planning.api;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate; import java.util.UUID;
public record AllocationRequest(@NotNull UUID projectId,@NotNull String resourceType,@NotNull UUID resourceId,@NotNull LocalDate startDate,@NotNull LocalDate endDate,Boolean fixed) {}
