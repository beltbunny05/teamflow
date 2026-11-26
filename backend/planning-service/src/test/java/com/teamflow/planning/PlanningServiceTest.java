package com.teamflow.planning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamflow.planning.api.AllocationRequest;
import com.teamflow.planning.api.AllocationResponse;
import com.teamflow.planning.api.TransportResponse;
import com.teamflow.planning.repository.PlanningRepository;
import com.teamflow.planning.service.PlanningService;
import org.junit.jupiter.api.Test;
import java.time.*; import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PlanningServiceTest {
    private final PlanningRepository repository = mock(PlanningRepository.class);
    private final PlanningService service = new PlanningService(repository, new ObjectMapper());
    private final UUID organization = UUID.randomUUID();
    private final UUID resource = UUID.randomUUID();

    @Test
    void detectsOverlappingAllocationAsBlocking() {
        UUID project = UUID.randomUUID();
        when(repository.allocations(any(), isNull(), eq("EMPLOYEE"), eq(resource), any(), any()))
                .thenReturn(List.of(new AllocationResponse(UUID.randomUUID(), organization, project, "EMPLOYEE", resource,
                        LocalDate.of(2026, 1, 3), LocalDate.of(2026, 1, 5), false, "CONFIRMED", OffsetDateTime.now(), OffsetDateTime.now())));
        var conflicts = service.conflicts(organization, new AllocationRequest(project, "EMPLOYEE", resource,
                LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 7), false));
        assertThat(conflicts).anyMatch(c -> c.type().equals("OVERLAPPING_ALLOCATION") && c.severity().equals("BLOCKING"));
    }

    @Test
    void rejectsReversedCalendarPeriod() {
        var request = new AllocationRequest(UUID.randomUUID(), "TEAM", resource,
                LocalDate.of(2026, 2, 2), LocalDate.of(2026, 2, 1), false);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.create(organization, request))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repository);
    }
}
