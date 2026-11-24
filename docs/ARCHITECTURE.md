# TeamFlow — Initial Architecture

**Status:** Approved initial direction; implementation scaffold
**Scope:** MVP foundation
**Date:** 2026-11-23

## 1. Architectural decisions

- The backend starts as four independent Spring Boot services.
- The services communicate synchronously through REST initially.
- No messaging infrastructure is introduced in the MVP foundation.
- Each service has a separate PostgreSQL database and owns its migrations and data.
- The frontend is an independent React application using JavaScript and CSS.
- Docker Compose runs the local environment.
- Micro Frontends are not implemented in this stage.
- No application business rules are implemented in this scaffold.

## 2. Service responsibilities

| Service | Responsibility | Own database |
|---|---|---|
| Identity Service | Users, authentication and organizations. | `identity_db` |
| Team Service | Teams, aggregated composition, competencies and registered availability. | `team_db` |
| Project Service | Projects, services and locations. | `project_db` |
| Planning Service | Allocations, transportation, conflicts and planning. | `planning_db` |

The service boundaries are intentionally limited to the approved responsibilities. No domain entities or business workflows are created in this scaffold.

## 3. Data ownership

Each service owns its database and migration responsibility. Services must not read or write another service's tables directly.

Cross-service data access will use REST contracts and only the information necessary for the requested operation. Shared database tables, cross-service SQL joins and direct mutation of another service's data are not part of the approved foundation.

`organization_id` is considered from the beginning in the service configuration and future domain design, but the detailed multi-organization rules remain pending in the product documentation.

## 4. Communication

The initial communication style is synchronous REST:

- the frontend calls the service that owns the requested operation;
- Planning Service will later consult Team Service and Project Service for planning validations;
- Identity Service will later support authentication and organization context;
- contracts and authorization between services must be documented before business workflows are implemented.

Asynchronous communication may be evaluated later for derived views, notifications, or long-running work. It is deliberately not included in the MVP foundation.

## 5. Local execution

Docker Compose provides:

- one PostgreSQL container;
- four logical databases created by the PostgreSQL initialization script;
- one container for each backend service;
- one frontend container running the JavaScript development server;
- service-specific environment variables and ports;
- health checks for PostgreSQL and the application containers.

The single PostgreSQL container is a local infrastructure choice. Logical database ownership remains separated by service. A production topology has not been decided.

## 6. Initial dependency direction

```mermaid
flowchart LR
    UI[React Frontend]
    IAM[Identity Service]
    TEAM[Team Service]
    PROJECT[Project Service]
    PLAN[Planning Service]
    PG[(PostgreSQL container)]

    UI -->|REST| IAM
    UI -->|REST| TEAM
    UI -->|REST| PROJECT
    UI -->|REST| PLAN

    PLAN -.->|future REST validation calls| TEAM
    PLAN -.->|future REST validation calls| PROJECT

    IAM -->|identity_db| PG
    TEAM -->|team_db| PG
    PROJECT -->|project_db| PG
    PLAN -->|planning_db| PG
```

The dashed calls are future business integrations. This scaffold does not implement them.

## 7. Pending decisions

- Exact domain contracts between Planning, Team and Project services.
- Authentication and authorization flows between services.
- Organization isolation and the final `organization_id` model.
- Migration tooling and migration execution policy per service.
- Conflict and availability rules that remain pending in `BUSINESS_RULES.md`.
- Production deployment and PostgreSQL topology.
- Whether a separate read/reporting service is needed.
- Whether Micro Frontends provide enough benefit to justify their complexity.
