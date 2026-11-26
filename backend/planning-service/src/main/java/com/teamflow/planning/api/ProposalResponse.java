package com.teamflow.planning.api;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProposalResponse(UUID id, UUID organizationId, long version, String status,
                               String explanation, OffsetDateTime createdAt, OffsetDateTime updatedAt,
                               long baseRevision, JsonNode payload) {
    public ProposalResponse(UUID id, UUID organizationId, long version, String status,
                            String explanation, OffsetDateTime createdAt) {
        this(id, organizationId, version, status, explanation, createdAt, createdAt, 0, null);
    }
}
