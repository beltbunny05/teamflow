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
public class ProjectClient {
    private final RestClient client;

    public ProjectClient(@Value("${PROJECT_SERVICE_URL:http://project-service:8083}") String baseUrl,
                         @Value("${TEAMFLOW_CLIENT_CONNECT_TIMEOUT_MS:1500}") int connectTimeout,
                         @Value("${TEAMFLOW_CLIENT_READ_TIMEOUT_MS:3000}") int readTimeout) {
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory(connectTimeout, readTimeout)).build();
    }

    public RemoteProject get(UUID organizationId, UUID projectId) {
        try {
            RemoteProject project = client.get().uri("/projects/{id}", projectId)
                    .header("X-Organization-Id", organizationId.toString()).retrieve().body(RemoteProject.class);
            if (project == null || !organizationId.equals(project.organizationId())) {
                throw new RemoteReferenceNotFoundException("Project is not available in the organization");
            }
            return project;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new RemoteReferenceNotFoundException("Project not found: " + projectId);
            }
            throw dependency("Project Service returned " + exception.getStatusCode(), exception);
        } catch (RestClientException exception) {
            throw dependency("Project Service is unavailable", exception);
        }
    }

    public List<RemoteProject> list(UUID organizationId) {
        try {
            ProjectPage page = client.get().uri(uriBuilder -> uriBuilder.path("/projects")
                            .queryParam("page", 0).queryParam("size", 100).build())
                    .header("X-Organization-Id", organizationId.toString()).retrieve()
                    .body(new ParameterizedTypeReference<ProjectPage>() {});
            if (page == null || page.content() == null) return List.of();
            if (page.content().stream().anyMatch(project -> !organizationId.equals(project.organizationId()))) {
                throw new RemoteReferenceNotFoundException("Project response crossed organization boundary");
            }
            return page.content();
        } catch (RestClientException exception) {
            throw dependency("Project Service is unavailable", exception);
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

    private record ProjectPage(List<RemoteProject> content) {
    }
}
