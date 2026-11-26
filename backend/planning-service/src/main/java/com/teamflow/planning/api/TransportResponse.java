package com.teamflow.planning.api;
import java.time.LocalDate; import java.time.OffsetDateTime; import java.util.UUID;
public record TransportResponse(UUID id,UUID organizationId,String resourceType,UUID resourceId,String origin,String destination,LocalDate startDate,Integer durationDays,LocalDate arrivalDate,String status,OffsetDateTime createdAt,OffsetDateTime updatedAt) {}
