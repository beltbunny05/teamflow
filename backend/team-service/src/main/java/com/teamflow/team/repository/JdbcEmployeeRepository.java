package com.teamflow.team.repository;

import com.teamflow.team.domain.Employee;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcEmployeeRepository {
    private final JdbcTemplate jdbc;
    public JdbcEmployeeRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public Employee save(Employee e) {
        jdbc.update("INSERT INTO employees (id,organization_id,identification,primary_role,active,initial_location,created_at,updated_at) VALUES (?,?,?,?,?,?,?,?)",
                e.id(),e.organizationId(),e.identification(),e.primaryRole(),e.active(),e.initialLocation(),e.createdAt(),e.updatedAt()); return e;
    }
    public List<Employee> findAll(UUID org,int page,int size) { return jdbc.query("SELECT * FROM employees WHERE organization_id=? ORDER BY identification,id LIMIT ? OFFSET ?", (r,n)->new Employee(r.getObject("id",UUID.class),r.getObject("organization_id",UUID.class),r.getString("identification"),r.getString("primary_role"),r.getBoolean("active"),r.getString("initial_location"),r.getObject("created_at",OffsetDateTime.class),r.getObject("updated_at",OffsetDateTime.class)),org,size,page*size); }
    public long count(UUID org) { Long n=jdbc.queryForObject("SELECT count(*) FROM employees WHERE organization_id=?",Long.class,org); return n==null?0:n; }
    public Optional<Employee> find(UUID org,UUID id) { return jdbc.query("SELECT * FROM employees WHERE organization_id=? AND id=?",(r,n)->new Employee(r.getObject("id",UUID.class),r.getObject("organization_id",UUID.class),r.getString("identification"),r.getString("primary_role"),r.getBoolean("active"),r.getString("initial_location"),r.getObject("created_at",OffsetDateTime.class),r.getObject("updated_at",OffsetDateTime.class)),org,id).stream().findFirst(); }
    public Optional<Employee> update(Employee e) { int n=jdbc.update("UPDATE employees SET identification=?,primary_role=?,active=?,initial_location=?,updated_at=? WHERE organization_id=? AND id=?",e.identification(),e.primaryRole(),e.active(),e.initialLocation(),e.updatedAt(),e.organizationId(),e.id()); return n==0?Optional.empty():Optional.of(e); }
    public void addMembership(UUID org,UUID team,UUID employee,String role) { jdbc.update("INSERT INTO team_memberships (organization_id,team_id,employee_id,role,created_at) VALUES (?,?,?,?,current_timestamp) ON CONFLICT (team_id,employee_id) DO UPDATE SET role=excluded.role",org,team,employee,role); }
    public void removeMembership(UUID org,UUID team,UUID employee) { jdbc.update("DELETE FROM team_memberships WHERE organization_id=? AND team_id=? AND employee_id=?",org,team,employee); }
    public EmployeeUnavailabilityRow addUnavailability(UUID org,UUID employee,java.time.LocalDate start,java.time.LocalDate end,String reason) { UUID id=UUID.randomUUID(); jdbc.update("INSERT INTO employee_unavailability (id,organization_id,employee_id,start_date,end_date,reason) VALUES (?,?,?,?,?,?)",id,org,employee,start,end,reason); return new EmployeeUnavailabilityRow(id,employee,start,end,reason); }
    public List<EmployeeUnavailabilityRow> unavailability(UUID org,UUID employee) { return jdbc.query("SELECT id,employee_id,start_date,end_date,reason FROM employee_unavailability WHERE organization_id=? AND employee_id=? ORDER BY start_date",(r,n)->new EmployeeUnavailabilityRow(r.getObject("id",UUID.class),r.getObject("employee_id",UUID.class),r.getObject("start_date",java.time.LocalDate.class),r.getObject("end_date",java.time.LocalDate.class),r.getString("reason")),org,employee); }
    public List<EmployeeTeamRow> teamsForEmployee(UUID org,UUID employee) { return jdbc.query("SELECT team_id,employee_id,role FROM team_memberships WHERE organization_id=? AND employee_id=? ORDER BY team_id",(r,n)->new EmployeeTeamRow(r.getObject("team_id",UUID.class),r.getObject("employee_id",UUID.class),r.getString("role")),org,employee); }
    public List<EmployeeTeamRow> employeesForTeam(UUID org,UUID team) { return jdbc.query("SELECT team_id,employee_id,role FROM team_memberships WHERE organization_id=? AND team_id=? ORDER BY employee_id",(r,n)->new EmployeeTeamRow(r.getObject("team_id",UUID.class),r.getObject("employee_id",UUID.class),r.getString("role")),org,team); }
    public record EmployeeTeamRow(UUID teamId,UUID employeeId,String role) {}
    public record EmployeeUnavailabilityRow(UUID id,UUID employeeId,java.time.LocalDate startDate,java.time.LocalDate endDate,String reason) {}
}
