package com.teamflow.planning.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ProposalUpdateRequest(@NotNull List<ProposalItem> items, @NotBlank String explanation) {
}
