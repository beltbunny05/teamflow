package com.teamflow.planning.api;
import java.time.LocalDate; import java.util.UUID;
public record ConflictResponse(String type,String severity,String resourceType,UUID resourceId,LocalDate startDate,LocalDate endDate,String description,String alternative) {}
