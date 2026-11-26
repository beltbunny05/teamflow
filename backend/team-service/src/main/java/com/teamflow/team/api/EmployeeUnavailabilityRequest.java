package com.teamflow.team.api;
import jakarta.validation.constraints.NotNull; import java.time.LocalDate;
public record EmployeeUnavailabilityRequest(@NotNull LocalDate startDate,@NotNull LocalDate endDate,String reason) {}
