package com.teamflow.project.api;

import com.teamflow.project.service.ProjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/projects")
@Validated
public class ProjectController {
    private final ProjectService service;
    private final UUID defaultOrganizationId;

    public ProjectController(ProjectService service,
                             @Value("${teamflow.organization.default-id}") UUID defaultOrganizationId) {
        this.service = service;
        this.defaultOrganizationId = defaultOrganizationId;
    }

    @PostMapping
    public ProjectResponse create(@RequestHeader(value = "X-Organization-Id", required = false) UUID organizationId,
                                  @Valid @RequestBody ProjectRequest request) {
        return service.create(resolveOrganization(organizationId), request);
    }

    @GetMapping
    public ProjectPageResponse list(@RequestHeader(value = "X-Organization-Id", required = false) UUID organizationId,
                                    @RequestParam(defaultValue = "0") @Min(0) int page,
                                    @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(resolveOrganization(organizationId), page, size);
    }

    @GetMapping("/{id}")
    public ProjectResponse findById(@RequestHeader(value = "X-Organization-Id", required = false) UUID organizationId,
                                    @PathVariable UUID id) {
        return service.findById(resolveOrganization(organizationId), id);
    }

    @PutMapping("/{id}")
    public ProjectResponse update(@RequestHeader(value = "X-Organization-Id", required = false) UUID organizationId,
                                 @PathVariable UUID id,
                                 @Valid @RequestBody ProjectRequest request) {
        return service.update(resolveOrganization(organizationId), id, request);
    }

    private UUID resolveOrganization(UUID organizationId) {
        return organizationId == null ? defaultOrganizationId : organizationId;
    }
}
