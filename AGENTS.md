
# TeamFlow — Project Instructions

## Project context

TeamFlow is a web platform for planning and allocating employees across multiple simultaneous projects, potentially in different cities and locations.

The system helps managers:
- Manage projects, schedules and locations.
- Manage employees, skills and availability.
- Allocate employees to projects.
- Detect scheduling and resource conflicts.
- Calculate workforce capacity and utilization.
- Suggest alternative allocations for manager approval.

This project is also the practical implementation of an MBA Software Engineering thesis at USP.

## Product principles

- The product must be generic and adaptable to companies from different industries.
- Do not restrict the domain to events, Christmas projects or public procurement.
- Simultaneous projects are allowed.
- Incompatible assignments of the same employee must be detected.
- Allocation suggestions must consider availability, skills and location constraints.
- Managers retain final approval over suggested changes.

## Initial architecture direction

- Frontend: React and TypeScript.
- Microfrontend integration: Module Federation.
- Backend: Java and Spring Boot.
- APIs: REST.
- Database: PostgreSQL.
- Containers: Docker.

These are architectural targets, not permission to introduce unnecessary complexity.

## Development principles

- The first priority is a working and testable MVP.
- Do not implement all microfrontends and microservices at once.
- Prefer simple, maintainable solutions over premature abstraction.
- Explain important technical decisions before making large architectural changes.
- Do not introduce dependencies without explaining why they are needed.
- Keep business rules separate from presentation logic.
- Write tests for critical business rules, especially scheduling conflicts and allocation capacity.
- Never invent requirements that have not been discussed.
- Ask before changing an agreed business rule or expanding the scope.

## Collaboration with the developer

The developer is learning software engineering and wants to understand the implementation, not just receive generated code.

When working:
1. Explain the objective of the task.
2. Propose a small implementation step.
3. Explain important files and decisions.
4. Run relevant tests.
5. Summarize what changed and what should happen next.

Do not generate the entire application in one step.

## Documentation

Keep project documentation in the `docs/` directory.

When requirements or architectural decisions change, update the relevant documentation.

Before implementing a feature, check the existing requirements and business rules.
