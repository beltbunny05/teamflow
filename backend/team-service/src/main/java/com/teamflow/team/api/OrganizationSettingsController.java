package com.teamflow.team.api;

import com.teamflow.team.repository.OrganizationSettingsRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/organization-settings")
public class OrganizationSettingsController {
    private final OrganizationSettingsRepository repository;
    private final UUID defaultOrganizationId;

    public OrganizationSettingsController(OrganizationSettingsRepository repository,
                                          @Value("${teamflow.organization.default-id}") UUID defaultOrganizationId) {
        this.repository = repository;
        this.defaultOrganizationId = defaultOrganizationId;
    }

    @GetMapping("/base-location")
    public OrganizationLocationResponse get(@RequestHeader(value = "X-Organization-Id", required = false) UUID organizationId) {
        return repository.find(resolve(organizationId));
    }

    @PutMapping("/base-location")
    public OrganizationLocationResponse put(@RequestHeader(value = "X-Organization-Id", required = false) UUID organizationId,
                                             @Valid @RequestBody OrganizationLocationRequest request) {
        return repository.save(resolve(organizationId), request.baseLocation());
    }

    private UUID resolve(UUID organizationId) { return organizationId == null ? defaultOrganizationId : organizationId; }
}
