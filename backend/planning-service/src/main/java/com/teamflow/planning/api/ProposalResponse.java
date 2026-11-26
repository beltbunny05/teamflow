package com.teamflow.planning.api;
import java.time.OffsetDateTime; import java.util.UUID;
public record ProposalResponse(UUID id,UUID organizationId,long version,String status,String explanation,OffsetDateTime createdAt) {}
