package com.teamflow.team.api;
import java.time.LocalDate; import java.util.UUID;
public record EmployeeUnavailabilityResponse(UUID id,UUID employeeId,LocalDate startDate,LocalDate endDate,String reason) {}
