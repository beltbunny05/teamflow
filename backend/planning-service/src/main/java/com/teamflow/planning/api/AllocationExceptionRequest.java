package com.teamflow.planning.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record AllocationExceptionRequest(@NotNull UUID employeeId, @NotBlank String kind,
                                         @NotNull LocalDate startDate, @NotNull LocalDate endDate,
                                         UUID targetProjectId, LocalDate targetStartDate,
                                         LocalDate targetEndDate, String reason) {
}
