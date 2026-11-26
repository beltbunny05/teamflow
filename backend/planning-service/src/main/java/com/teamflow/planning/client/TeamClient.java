package com.teamflow.planning.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Component
public class TeamClient {
    private final RestClient client;

    public TeamClient(@Value("${TEAM_SERVICE_URL:http://team-service:8082}") String baseUrl,
                      @Value("${TEAMFLOW_CLIENT_CONNECT_TIMEOUT_MS:1500}") int connectTimeout,
                      @Value("${TEAMFLOW_CLIENT_READ_TIMEOUT_MS:3000}") int readTimeout) {
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory(connectTimeout, readTimeout)).build();
    }

    public RemoteTeam getTeam(UUID organizationId, UUID teamId) {
        try {
            RemoteTeam team = client.get().uri("/teams/{id}", teamId).header("X-Organization-Id", organizationId.toString())
                    .retrieve().body(RemoteTeam.class);
            if (team == null || !organizationId.equals(team.organizationId())) {
                throw new RemoteReferenceNotFoundException("Team is not available in the organization");
            }
            return team;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new RemoteReferenceNotFoundException("Team not found: " + teamId);
            }
            throw dependency("Team Service returned " + exception.getStatusCode(), exception);
        } catch (RestClientException exception) {
            throw dependency("Team Service is unavailable", exception);
        }
    }

    public RemoteEmployee getEmployee(UUID organizationId, UUID employeeId) {
        try {
            RemoteEmployee employee = client.get().uri("/employees/{id}", employeeId)
                    .header("X-Organization-Id", organizationId.toString()).retrieve().body(RemoteEmployee.class);
            if (employee == null || !organizationId.equals(employee.organizationId())) {
                throw new RemoteReferenceNotFoundException("Employee is not available in the organization");
            }
            return employee;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new RemoteReferenceNotFoundException("Employee not found: " + employeeId);
            }
            throw dependency("Team Service returned " + exception.getStatusCode(), exception);
        } catch (RestClientException exception) {
            throw dependency("Team Service is unavailable", exception);
        }
    }

    public List<RemoteTeam> listTeams(UUID organizationId) {
        try {
                    TeamPage page = client.get().uri(uriBuilder -> uriBuilder.path("/teams").queryParam("page", 0).queryParam("size", 100).build())
                    .header("X-Organization-Id", organizationId.toString()).retrieve()
                    .body(new ParameterizedTypeReference<TeamPage>() {});
            if (page == null || page.content() == null) return List.of();
            if (page.content().stream().anyMatch(team -> !organizationId.equals(team.organizationId()))) {
                throw new RemoteReferenceNotFoundException("Team response crossed organization boundary");
            }
            return page.content();
        } catch (RestClientException exception) {
            throw dependency("Team Service is unavailable", exception);
        }
    }

    public List<RemoteEmployee> listEmployees(UUID organizationId) {
        try {
            EmployeePage page = client.get().uri(uriBuilder -> uriBuilder.path("/employees").queryParam("page", 0).queryParam("size", 100).build())
                    .header("X-Organization-Id", organizationId.toString()).retrieve()
                    .body(new ParameterizedTypeReference<EmployeePage>() {});
            if (page == null || page.content() == null) return List.of();
            if (page.content().stream().anyMatch(employee -> !organizationId.equals(employee.organizationId()))) {
                throw new RemoteReferenceNotFoundException("Employee response crossed organization boundary");
            }
            return page.content();
        } catch (RestClientException exception) {
            throw dependency("Team Service is unavailable", exception);
        }
    }

    public List<RemoteMembership> employeesForTeam(UUID organizationId, UUID teamId) {
        try {
            List<RemoteMembership> memberships = client.get().uri("/employees/teams/{teamId}/employees", teamId)
                    .header("X-Organization-Id", organizationId.toString()).retrieve()
                    .body(new ParameterizedTypeReference<List<RemoteMembership>>() {});
            return memberships == null ? List.of() : memberships;
        } catch (RestClientException exception) {
            throw dependency("Team Service is unavailable", exception);
        }
    }

    public List<RemoteMembership> teamsForEmployee(UUID organizationId, UUID employeeId) {
        try {
            List<RemoteMembership> memberships = client.get().uri("/employees/{employeeId}/teams", employeeId)
                    .header("X-Organization-Id", organizationId.toString()).retrieve()
                    .body(new ParameterizedTypeReference<List<RemoteMembership>>() {});
            return memberships == null ? List.of() : memberships;
        } catch (RestClientException exception) {
            throw dependency("Team Service is unavailable", exception);
        }
    }

    public List<RemoteUnavailability> unavailability(UUID organizationId, UUID employeeId) {
        try {
            return client.get().uri("/employees/{employeeId}/unavailability", employeeId)
                    .header("X-Organization-Id", organizationId.toString()).retrieve()
                    .body(new ParameterizedTypeReference<List<RemoteUnavailability>>() {});
        } catch (RestClientException exception) {
            throw dependency("Team Service is unavailable", exception);
        }
    }

    private RemoteDependencyException dependency(String message, Throwable cause) {
        return new RemoteDependencyException(message, cause);
    }

    private SimpleClientHttpRequestFactory factory(int connectTimeout, int readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeout));
        factory.setReadTimeout(Duration.ofMillis(readTimeout));
        return factory;
    }

    private record TeamPage(List<RemoteTeam> content) {}
    private record EmployeePage(List<RemoteEmployee> content) {}
}
