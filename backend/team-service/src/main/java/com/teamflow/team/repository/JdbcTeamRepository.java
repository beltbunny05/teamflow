package com.teamflow.team.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamflow.team.domain.CompositionItem;
import com.teamflow.team.domain.Team;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcTeamRepository implements TeamRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    private final RowMapper<Team> rowMapper = this::mapRow;

    public JdbcTeamRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Team save(Team team) {
        jdbcTemplate.update("""
                INSERT INTO teams (id, organization_id, identification, total_members, composition,
                    competencies, qualifications, availability, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, CAST(? AS jsonb), CAST(? AS jsonb), CAST(? AS jsonb), CAST(? AS jsonb), ?, ?, ?)
                """,
                team.id(), team.organizationId(), team.identification(), team.totalMembers(),
                json(team.composition()), json(team.competencies()), json(team.qualifications()),
                json(team.availability()), team.status(), team.createdAt(), team.updatedAt());
        return team;
    }

    @Override
    public List<Team> findAll(UUID organizationId, int page, int size) {
        return jdbcTemplate.query("""
                SELECT id, organization_id, identification, total_members, composition,
                    competencies, qualifications, availability, status, created_at, updated_at
                FROM teams WHERE organization_id = ?
                ORDER BY identification, id LIMIT ? OFFSET ?
                """, rowMapper, organizationId, size, page * size);
    }

    @Override
    public long count(UUID organizationId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM teams WHERE organization_id = ?", Long.class, organizationId);
        return count == null ? 0 : count;
    }

    @Override
    public Optional<Team> findById(UUID organizationId, UUID id) {
        return jdbcTemplate.query("""
                SELECT id, organization_id, identification, total_members, composition,
                    competencies, qualifications, availability, status, created_at, updated_at
                FROM teams WHERE organization_id = ? AND id = ?
                """, rowMapper, organizationId, id).stream().findFirst();
    }

    @Override
    public Optional<Team> update(Team team) {
        int updated = jdbcTemplate.update("""
                UPDATE teams SET identification = ?, total_members = ?, composition = CAST(? AS jsonb),
                    competencies = CAST(? AS jsonb), qualifications = CAST(? AS jsonb),
                    availability = CAST(? AS jsonb), status = ?, updated_at = ?
                WHERE organization_id = ? AND id = ?
                """,
                team.identification(), team.totalMembers(), json(team.composition()),
                json(team.competencies()), json(team.qualifications()), json(team.availability()),
                team.status(), team.updatedAt(), team.organizationId(), team.id());
        return updated == 0 ? Optional.empty() : Optional.of(team);
    }

    private Team mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return new Team(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("organization_id", UUID.class),
                resultSet.getString("identification"),
                resultSet.getInt("total_members"),
                readList(resultSet.getString("composition"), objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, CompositionItem.class)),
                readList(resultSet.getString("competencies"), objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, String.class)),
                readList(resultSet.getString("qualifications"), objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, String.class)),
                readJson(resultSet.getString("availability")),
                resultSet.getString("status"),
                resultSet.getObject("created_at", OffsetDateTime.class),
                resultSet.getObject("updated_at", OffsetDateTime.class));
    }

    private <T> List<T> readList(String value, com.fasterxml.jackson.databind.JavaType type) {
        if (value == null) return null;
        try {
            return objectMapper.readValue(value, type);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored team JSON is invalid", exception);
        }
    }

    private JsonNode readJson(String value) {
        if (value == null) return null;
        try {
            return objectMapper.readTree(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored team availability is invalid", exception);
        }
    }

    private String json(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Team data cannot be serialized", exception);
        }
    }
}
