package com.teamflow.project.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamflow.project.domain.Project;
import com.teamflow.project.domain.ProjectLocation;
import com.teamflow.project.domain.ProjectServiceItem;
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
public class JdbcProjectRepository implements ProjectRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final RowMapper<Project> rowMapper = this::mapRow;

    public JdbcProjectRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Project save(Project project) {
        jdbcTemplate.update("""
                INSERT INTO projects (id, organization_id, identification, client, description,
                    planned_start_date, planned_end_date, estimated_duration_days, competencies,
                    qualifications, localities, services, priority, continuous_execution, desired_people, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS jsonb), CAST(? AS jsonb),
                    CAST(? AS jsonb), CAST(? AS jsonb), ?, ?, ?, ?, ?)
                """,
                project.id(), project.organizationId(), project.identification(), project.client(),
                project.description(), project.plannedStartDate(), project.plannedEndDate(),
                project.estimatedDurationDays(), json(project.competencies()), json(project.qualifications()),
                json(project.localities()), json(project.services()), project.priority(), project.continuousExecution(), project.desiredPeople(), project.createdAt(), project.updatedAt());
        return project;
    }

    @Override
    public List<Project> findAll(UUID organizationId, int page, int size) {
        return jdbcTemplate.query("""
                SELECT id, organization_id, identification, client, description, planned_start_date,
                    planned_end_date, estimated_duration_days, competencies, qualifications,
                    localities, services, priority, continuous_execution, desired_people, created_at, updated_at
                FROM projects WHERE organization_id = ?
                ORDER BY identification, id LIMIT ? OFFSET ?
                """, rowMapper, organizationId, size, page * size);
    }

    @Override
    public long count(UUID organizationId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM projects WHERE organization_id = ?", Long.class, organizationId);
        return count == null ? 0 : count;
    }

    @Override
    public Optional<Project> findById(UUID organizationId, UUID id) {
        return jdbcTemplate.query("""
                SELECT id, organization_id, identification, client, description, planned_start_date,
                    planned_end_date, estimated_duration_days, competencies, qualifications,
                    localities, services, priority, continuous_execution, desired_people, created_at, updated_at
                FROM projects WHERE organization_id = ? AND id = ?
                """, rowMapper, organizationId, id).stream().findFirst();
    }

    @Override
    public Optional<Project> update(Project project) {
        int updated = jdbcTemplate.update("""
                UPDATE projects SET identification = ?, client = ?, description = ?,
                    planned_start_date = ?, planned_end_date = ?, estimated_duration_days = ?,
                    competencies = CAST(? AS jsonb), qualifications = CAST(? AS jsonb),
                    localities = CAST(? AS jsonb), services = CAST(? AS jsonb), priority = ?, continuous_execution = ?, desired_people = ?, updated_at = ?
                WHERE organization_id = ? AND id = ?
                """,
                project.identification(), project.client(), project.description(), project.plannedStartDate(),
                project.plannedEndDate(), project.estimatedDurationDays(), json(project.competencies()),
                json(project.qualifications()), json(project.localities()), json(project.services()), project.priority(), project.continuousExecution(), project.desiredPeople(),
                project.updatedAt(), project.organizationId(), project.id());
        return updated == 0 ? Optional.empty() : Optional.of(project);
    }

    private Project mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return new Project(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("organization_id", UUID.class),
                resultSet.getString("identification"),
                resultSet.getString("client"),
                resultSet.getString("description"),
                resultSet.getObject("planned_start_date", java.time.LocalDate.class),
                resultSet.getObject("planned_end_date", java.time.LocalDate.class),
                (Integer) resultSet.getObject("estimated_duration_days"),
                readList(resultSet.getString("competencies"), objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, String.class)),
                readList(resultSet.getString("qualifications"), objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, String.class)),
                readList(resultSet.getString("localities"), objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, ProjectLocation.class)),
                readList(resultSet.getString("services"), objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, ProjectServiceItem.class)),
                resultSet.getString("priority"), resultSet.getBoolean("continuous_execution"),
                (Integer) resultSet.getObject("desired_people"),
                resultSet.getObject("created_at", OffsetDateTime.class),
                resultSet.getObject("updated_at", OffsetDateTime.class));
    }

    private <T> List<T> readList(String value, com.fasterxml.jackson.databind.JavaType type) {
        if (value == null) return null;
        try {
            return objectMapper.readValue(value, type);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored project JSON is invalid", exception);
        }
    }

    private String json(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Project data cannot be serialized", exception);
        }
    }
}
