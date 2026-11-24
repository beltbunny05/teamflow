# TeamFlow — Project Instructions

## Project context

TeamFlow is a web platform for planning and allocating whole teams across multiple simultaneous projects, potentially in different cities and locations.

The system helps managers:

- Manage teams, projects, schedules and locations.
- Manage aggregated team composition, skills, qualifications and availability.
- Allocate complete teams to projects.
- Register manual transportation events between locations.
- Detect scheduling, availability, transportation and qualification conflicts.
- Present explainable alternative allocations for manager approval.

This project is also the practical implementation of an MBA Software Engineering thesis at USP.

The MVP does not include individual employee records, individual employee availability, individual employee allocation or individual employee conflicts. Team composition may be recorded by roles and quantities without identifying individual people.

## Product principles

- The product must be generic and adaptable to companies from different industries.
- Do not restrict the domain to events, Christmas projects or public procurement.
- Simultaneous projects are allowed.
- The whole team is the primary planning and allocation unit.
- Teams are not automatically divided between projects in the MVP.
- Planning uses service days and whole calendar days.
- Weekdays, weekends and holidays count equally in the MVP.
- Service duration is informed manually by the manager and is not inferred automatically from unrelated operational indicators.
- Transportation is a separate planning event with duration informed manually in whole days.
- A team is unavailable during transportation and cannot begin another service before completing the displacement and arriving at the destination.
- The system identifies conflicts and explains their causes.
- Allocation suggestions are assistive only; managers retain final approval.
- No planning change may be applied automatically without explicit manager confirmation.
- Do not invent rules for unresolved questions. Keep pending decisions explicit and ask before changing an agreed business rule or expanding scope.

## Architecture and technology direction

The TeamFlow backend must use a microservices-based architecture from the beginning, in accordance with the academic objective and the current project documentation.

- Frontend: React, JavaScript and CSS.
- Backend: Java and Spring Boot.
- APIs: REST.
- Database: PostgreSQL.
- Local execution and orchestration: Docker and Docker Compose.

The project must not start with a monolith or establish a modular monolith as a mandatory migration stage.

Microservices must be defined from business domains and responsibilities. Do not choose the number of services arbitrarily, and avoid both overly broad services and unjustified fragmentation.

The architecture must document and consider:

- clear domain boundaries and service responsibilities;
- explicit communication contracts;
- data ownership and persistence responsibilities;
- authentication and authorization between services;
- failure and unavailability handling;
- observability and traceability;
- unit, integration and service-communication tests;
- Docker-based local execution.

Micro Frontends are a separate future architectural investigation. They are not approved as a mandatory MVP implementation. Evaluate them independently, considering functional boundaries, development independence, shared dependencies, module communication, interface composition, build and deployment complexity, user experience and maintenance.

Do not introduce technologies, protocols, services or infrastructure that have not been justified and documented.

## Docker and development environment

- Docker must be part of the development environment from the beginning.
- Docker Compose must support the local services and dependencies required by the documented MVP.
- Document container communication, dependencies, environment variables and PostgreSQL data persistence.
- Keep the initial local environment simple to configure, reproduce, start and stop.
- Do not anticipate complex production infrastructure during the first development stage.

## Development principles

- The first priority is a working and testable MVP.
- Prefer simple, maintainable solutions over premature abstraction.
- Explain important technical decisions before making large architectural changes.
- Do not introduce dependencies without explaining why they are needed.
- Keep business rules separate from presentation logic.
- Keep presentation, business rules and persistence responsibilities separate.
- Maintain low coupling between services and explicit communication contracts.
- Write tests for critical business rules, especially calendar periods, transportation, scheduling conflicts and allocation confirmation.
- Use clear, consistent and idiomatic naming.
- Keep responsibilities, directories and files coherent.
- Avoid duplicated logic, unnecessary abstractions, oversized files and artificial fragmentation.
- Write objective and useful documentation.
- The code should look like software carefully developed by an engineering team, not structures generated only to satisfy architectural patterns.
- Never invent requirements that have not been discussed.
- Do not change an agreed business rule or expand scope without approval.

## Implementation gate

- Do not start application implementation without explicit authorization.
- Before implementing a feature, check `docs/PROJECT_BRIEF.md`, `docs/REQUIREMENTS.md` and `docs/BUSINESS_RULES.md`.
- Do not implement unresolved business rules implicitly.
- Keep decisions about conflict blocking versus alerts, date precedence, arrival-day availability, multiple teams/stages/locations, permissions and service boundaries documented as pending until approved.
- Do not create database models or service boundaries merely to fill documentation gaps.
- Do not generate the entire application in one step; propose small, reviewable implementation steps when implementation is authorized.

## Collaboration with the developer

The developer is learning software engineering and wants to understand the implementation, not just receive generated code.

When working:

1. Explain the objective of the task.
2. Propose a small implementation step.
3. Explain important files and decisions.
4. Run relevant tests when implementation is authorized.
5. Summarize what changed and what should happen next.

## Documentation

Keep project documentation in the `docs/` directory.

When requirements or architectural decisions change, update the relevant documentation.

Do not modify existing documentation when a task explicitly limits the allowed files. Preserve historical decisions and mark superseded decisions clearly when appropriate.
