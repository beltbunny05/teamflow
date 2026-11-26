package com.teamflow.planning.api;
import java.time.LocalDate; import java.time.OffsetDateTime; import java.util.UUID;
public record AllocationResponse(UUID id,UUID organizationId,UUID projectId,String resourceType,UUID resourceId,LocalDate startDate,LocalDate endDate,boolean fixed,String status,OffsetDateTime createdAt,OffsetDateTime updatedAt) {}
