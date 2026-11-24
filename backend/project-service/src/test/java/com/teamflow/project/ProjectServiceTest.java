package com.teamflow.project;

import com.teamflow.project.api.ProjectRequest;
import com.teamflow.project.domain.Project;
import com.teamflow.project.repository.ProjectRepository;
import com.teamflow.project.service.ProjectService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProjectServiceTest {
    private final ProjectRepository repository = mock(ProjectRepository.class);
    private final ProjectService service = new ProjectService(repository);

    @Test
    void createsProjectWithoutInferringDatesOrDurations() {
        UUID organizationId = UUID.randomUUID();
        ProjectRequest request = new ProjectRequest("Projeto Norte", null, null,
                LocalDate.of(2026, 12, 1), null, 5, null, null, null, null);
        when(repository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(organizationId, request);

        assertThat(response.identification()).isEqualTo("Projeto Norte");
        assertThat(response.plannedStartDate()).isEqualTo(request.plannedStartDate());
        assertThat(response.plannedEndDate()).isNull();
        assertThat(response.estimatedDurationDays()).isEqualTo(5);
        verify(repository).save(any(Project.class));
    }
}
