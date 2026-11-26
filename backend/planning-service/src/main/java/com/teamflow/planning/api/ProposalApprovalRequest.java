package com.teamflow.planning.api;

import java.util.List;
import java.util.UUID;

public record ProposalApprovalRequest(List<UUID> itemIds) {
}
