package com.teamflow.team.service;

import com.teamflow.team.api.*;
import com.teamflow.team.domain.Employee;
import com.teamflow.team.exception.TeamNotFoundException;
import com.teamflow.team.repository.JdbcEmployeeRepository;
import com.teamflow.team.repository.TeamRepository;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class EmployeeService {
    private final JdbcEmployeeRepository repository; private final TeamRepository teamRepository;
    public EmployeeService(JdbcEmployeeRepository repository, TeamRepository teamRepository) { this.repository=repository; this.teamRepository=teamRepository; }
    public EmployeeResponse create(UUID org,EmployeeRequest r) { var now=now(); return response(repository.save(new Employee(UUID.randomUUID(),org,r.identification().trim(),r.primaryRole().trim(),r.active()==null||r.active(),normalize(r.initialLocation()),now,now))); }
    public EmployeePageResponse list(UUID org,int page,int size) { long total=repository.count(org); return new EmployeePageResponse(repository.findAll(org,page,size).stream().map(this::response).toList(),page,size,total,total==0?0:(int)Math.ceil((double)total/size)); }
    public EmployeeResponse find(UUID org,UUID id) { return repository.find(org,id).map(this::response).orElseThrow(()->new TeamNotFoundException(id)); }
    public EmployeeResponse update(UUID org,UUID id,EmployeeRequest r) { Employee current=repository.find(org,id).orElseThrow(()->new TeamNotFoundException(id)); var e=new Employee(id,org,r.identification().trim(),r.primaryRole().trim(),r.active()==null?current.active():r.active(),normalize(r.initialLocation()),current.createdAt(),now()); return repository.update(e).map(this::response).orElseThrow(()->new TeamNotFoundException(id)); }
    public EmployeeResponse deactivate(UUID org,UUID id) { Employee e=repository.find(org,id).orElseThrow(()->new TeamNotFoundException(id)); return repository.update(new Employee(e.id(),e.organizationId(),e.identification(),e.primaryRole(),false,e.initialLocation(),e.createdAt(),now())).map(this::response).orElseThrow(()->new TeamNotFoundException(id)); }
    public void membership(UUID org,UUID team,UUID employee,TeamMembershipRequest r) { find(org,employee); teamRepository.findById(org,team).orElseThrow(()->new TeamNotFoundException(team)); repository.addMembership(org,team,employee,r.role().trim()); }
    public void removeMembership(UUID org,UUID team,UUID employee) { find(org,employee); teamRepository.findById(org,team).orElseThrow(()->new TeamNotFoundException(team)); repository.removeMembership(org,team,employee); }
    public java.util.List<EmployeeTeamResponse> teams(UUID org,UUID employee) { find(org,employee); return repository.teamsForEmployee(org,employee).stream().map(x->new EmployeeTeamResponse(x.teamId(),x.employeeId(),x.role())).toList(); }
    public java.util.List<EmployeeTeamResponse> employees(UUID org,UUID team) { teamRepository.findById(org,team).orElseThrow(()->new TeamNotFoundException(team)); return repository.employeesForTeam(org,team).stream().map(x->new EmployeeTeamResponse(x.teamId(),x.employeeId(),x.role())).toList(); }
    public EmployeeUnavailabilityResponse addUnavailability(UUID org,UUID employee,EmployeeUnavailabilityRequest r) { find(org,employee); if(r.endDate().isBefore(r.startDate())) throw new IllegalArgumentException("endDate must be on or after startDate"); var u=repository.addUnavailability(org,employee,r.startDate(),r.endDate(),r.reason()); return new EmployeeUnavailabilityResponse(u.id(),u.employeeId(),u.startDate(),u.endDate(),u.reason()); }
    public java.util.List<EmployeeUnavailabilityResponse> unavailability(UUID org,UUID employee) { find(org,employee); return repository.unavailability(org,employee).stream().map(u->new EmployeeUnavailabilityResponse(u.id(),u.employeeId(),u.startDate(),u.endDate(),u.reason())).toList(); }
    private EmployeeResponse response(Employee e){return new EmployeeResponse(e.id(),e.organizationId(),e.identification(),e.primaryRole(),e.active(),e.initialLocation(),e.createdAt(),e.updatedAt());}
    private String normalize(String s){return s==null||s.isBlank()?null:s.trim();} private OffsetDateTime now(){return OffsetDateTime.now(ZoneOffset.UTC);}
}
