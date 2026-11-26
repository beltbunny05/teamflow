package com.teamflow.planning.client;

import java.time.LocalDate;
import java.util.UUID;

public record RemoteUnavailability(UUID id, UUID employeeId, LocalDate startDate,
                                   LocalDate endDate, String reason) {
}
