package com.teamflow.team.api;

import com.teamflow.team.service.TeamService;
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
@RequestMapping("/teams")
@Validated
public class TeamController {
    private final TeamService service;
    private final UUID defaultOrganizationId;

    public TeamController(TeamService service,
                          @Value("${teamflow.organization.default-id}") UUID defaultOrganizationId) {
        this.service = service;
        this.defaultOrganizationId = defaultOrganizationId;
    }

    @PostMapping
    public TeamResponse create(@RequestHeader(value = "X-Organization-Id", required = false) UUID organizationId,
                               @Valid @RequestBody TeamRequest request) {
        return service.create(resolveOrganization(organizationId), request);
    }

    @GetMapping
    public TeamPageResponse list(@RequestHeader(value = "X-Organization-Id", required = false) UUID organizationId,
                                 @RequestParam(defaultValue = "0") @Min(0) int page,
                                 @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(resolveOrganization(organizationId), page, size);
    }

    @GetMapping("/{id}")
    public TeamResponse findById(@RequestHeader(value = "X-Organization-Id", required = false) UUID organizationId,
                                 @PathVariable UUID id) {
        return service.findById(resolveOrganization(organizationId), id);
    }

    @PutMapping("/{id}")
    public TeamResponse update(@RequestHeader(value = "X-Organization-Id", required = false) UUID organizationId,
                               @PathVariable UUID id,
                               @Valid @RequestBody TeamRequest request) {
        return service.update(resolveOrganization(organizationId), id, request);
    }

    private UUID resolveOrganization(UUID organizationId) {
        return organizationId == null ? defaultOrganizationId : organizationId;
    }
}
