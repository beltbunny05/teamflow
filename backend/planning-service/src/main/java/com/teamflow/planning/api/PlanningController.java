package com.teamflow.planning.api;
import com.teamflow.planning.service.PlanningService; import jakarta.validation.Valid; import org.springframework.beans.factory.annotation.Value; import org.springframework.web.bind.annotation.*; import java.time.LocalDate; import java.util.*;
@RestController @RequestMapping("/planning") public class PlanningController{
 private final PlanningService service;private final UUID defaultOrg;public PlanningController(PlanningService s,@Value("${teamflow.organization.default-id}")UUID d){service=s;defaultOrg=d;}private UUID org(UUID o){return o==null?defaultOrg:o;}
 @PostMapping("/allocations") public AllocationResponse create(@RequestHeader(value="X-Organization-Id",required=false)UUID o,@Valid@RequestBody AllocationRequest r){return service.create(org(o),r);}
 @GetMapping("/allocations") public List<AllocationResponse> list(@RequestHeader(value="X-Organization-Id",required=false)UUID o,@RequestParam(required=false)UUID projectId,@RequestParam(required=false)String resourceType,@RequestParam(required=false)UUID resourceId,@RequestParam(required=false)LocalDate from,@RequestParam(required=false)LocalDate to){return service.list(org(o),projectId,resourceType,resourceId,from,to);}
 @PostMapping("/allocations/{id}/fixed") public void fixed(@RequestHeader(value="X-Organization-Id",required=false)UUID o,@PathVariable UUID id,@RequestParam boolean value){service.fixed(org(o),id,value);}
 @DeleteMapping("/allocations/{id}") public void cancel(@RequestHeader(value="X-Organization-Id",required=false)UUID o,@PathVariable UUID id){service.cancel(org(o),id);}
 @PostMapping("/transports") public TransportResponse transport(@RequestHeader(value="X-Organization-Id",required=false)UUID o,@Valid@RequestBody TransportRequest r){return service.transport(org(o),r);}
 @GetMapping("/transports") public List<TransportResponse> transports(@RequestHeader(value="X-Organization-Id",required=false)UUID o){return service.transports(org(o));}
 @PostMapping("/conflicts") public List<ConflictResponse> conflicts(@RequestHeader(value="X-Organization-Id",required=false)UUID o,@Valid@RequestBody AllocationRequest r){return service.conflicts(org(o),r);}
 @PostMapping("/proposals") public ProposalResponse proposal(@RequestHeader(value="X-Organization-Id",required=false)UUID o){return service.proposal(org(o));}
 @PostMapping("/proposals/{id}/approve") public void approve(@RequestHeader(value="X-Organization-Id",required=false)UUID o,@PathVariable UUID id){service.approve(org(o),id);}
}
