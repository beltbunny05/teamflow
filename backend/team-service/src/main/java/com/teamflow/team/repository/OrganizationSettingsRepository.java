package com.teamflow.team.repository;

import com.teamflow.team.api.OrganizationLocationResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Repository
public class OrganizationSettingsRepository {
    private final JdbcTemplate jdbc;

    public OrganizationSettingsRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public OrganizationLocationResponse save(UUID organizationId, String baseLocation) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        jdbc.update("INSERT INTO organization_settings (organization_id, base_location, updated_at) VALUES (?, ?, ?) ON CONFLICT (organization_id) DO UPDATE SET base_location=EXCLUDED.base_location, updated_at=EXCLUDED.updated_at",
                organizationId, normalize(baseLocation), now);
        return find(organizationId);
    }

    public OrganizationLocationResponse find(UUID organizationId) {
        return jdbc.query("SELECT organization_id, base_location, updated_at FROM organization_settings WHERE organization_id=?",
                (rs, row) -> new OrganizationLocationResponse(rs.getObject("organization_id", UUID.class), rs.getString("base_location"), rs.getObject("updated_at", OffsetDateTime.class)), organizationId)
                .stream().findFirst().orElse(new OrganizationLocationResponse(organizationId, null, null));
    }

    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
