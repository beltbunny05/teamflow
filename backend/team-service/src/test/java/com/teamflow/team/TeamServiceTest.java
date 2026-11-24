package com.teamflow.team;

import com.teamflow.team.api.TeamRequest;
import com.teamflow.team.api.TeamResponse;
import com.teamflow.team.domain.Team;
import com.teamflow.team.repository.TeamRepository;
import com.teamflow.team.service.TeamService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {
    @Mock
    private TeamRepository repository;

    @Test
    void createsTeamWithOrganizationContextAndNormalizedValues() {
        UUID organizationId = UUID.randomUUID();
        TeamService service = new TeamService(repository);
        when(repository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TeamResponse response = service.create(organizationId,
                new TeamRequest("  Equipe A  ", 4, null, null, null, null, "  ATIVA  "));

        assertThat(response.organizationId()).isEqualTo(organizationId);
        assertThat(response.identification()).isEqualTo("Equipe A");
        assertThat(response.status()).isEqualTo("ATIVA");
    }
}
