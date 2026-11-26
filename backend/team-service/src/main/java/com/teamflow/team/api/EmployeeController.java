package com.teamflow.team.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.teamflow.team.service.EmployeeService;
import java.util.UUID;

@RestController @RequestMapping({"/employees","/team-employees"}) @Validated
public class EmployeeController {
    private final EmployeeService service; private final UUID defaultOrg;
    public EmployeeController(EmployeeService s,@Value("${teamflow.organization.default-id}") UUID d){service=s;defaultOrg=d;}
    @PostMapping public EmployeeResponse create(@RequestHeader(value="X-Organization-Id",required=false) UUID o,@Valid @RequestBody EmployeeRequest r){return service.create(org(o),r);}
    @GetMapping public EmployeePageResponse list(@RequestHeader(value="X-Organization-Id",required=false) UUID o,@RequestParam(defaultValue="0")@Min(0)int page,@RequestParam(defaultValue="20")@Min(1)@Max(100)int size){return service.list(org(o),page,size);}
    @GetMapping("/{id}") public EmployeeResponse find(@RequestHeader(value="X-Organization-Id",required=false)UUID o,@PathVariable UUID id){return service.find(org(o),id);}
    @PutMapping("/{id}") public EmployeeResponse update(@RequestHeader(value="X-Organization-Id",required=false)UUID o,@PathVariable UUID id,@Valid@RequestBody EmployeeRequest r){return service.update(org(o),id,r);}
    @DeleteMapping("/{id}") public EmployeeResponse deactivate(@RequestHeader(value="X-Organization-Id",required=false)UUID o,@PathVariable UUID id){return service.deactivate(org(o),id);}
    @PutMapping("/{employeeId}/teams/{teamId}") public void add(@RequestHeader(value="X-Organization-Id",required=false)UUID o,@PathVariable UUID employeeId,@PathVariable UUID teamId,@Valid@RequestBody TeamMembershipRequest r){service.membership(org(o),teamId,employeeId,r);}
    @DeleteMapping("/{employeeId}/teams/{teamId}") public void remove(@RequestHeader(value="X-Organization-Id",required=false)UUID o,@PathVariable UUID employeeId,@PathVariable UUID teamId){service.removeMembership(org(o),teamId,employeeId);}
    @GetMapping("/{employeeId}/teams") public java.util.List<EmployeeTeamResponse> teams(@RequestHeader(value="X-Organization-Id",required=false)UUID o,@PathVariable UUID employeeId){return service.teams(org(o),employeeId);}
    @GetMapping("/teams/{teamId}/employees") public java.util.List<EmployeeTeamResponse> employees(@RequestHeader(value="X-Organization-Id",required=false)UUID o,@PathVariable UUID teamId){return service.employees(org(o),teamId);}
    @PostMapping("/{employeeId}/unavailability") public EmployeeUnavailabilityResponse addUnavailability(@RequestHeader(value="X-Organization-Id",required=false)UUID o,@PathVariable UUID employeeId,@Valid@RequestBody EmployeeUnavailabilityRequest r){return service.addUnavailability(org(o),employeeId,r);}
    @GetMapping("/{employeeId}/unavailability") public java.util.List<EmployeeUnavailabilityResponse> unavailability(@RequestHeader(value="X-Organization-Id",required=false)UUID o,@PathVariable UUID employeeId){return service.unavailability(org(o),employeeId);}
    private UUID org(UUID o){return o==null?defaultOrg:o;}
}
