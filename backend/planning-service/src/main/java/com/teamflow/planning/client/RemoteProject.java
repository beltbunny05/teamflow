package com.teamflow.planning.client;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record RemoteProject(UUID id, UUID organizationId, String identification,
                            LocalDate plannedStartDate, LocalDate plannedEndDate,
                            Integer estimatedDurationDays, List<RemoteLocality> localities,
                            String priority, boolean continuousExecution, Integer desiredPeople) {
}
