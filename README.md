# TeamFlow

Estrutura inicial executável do MVP do TeamFlow.

## Stack

- Frontend: React, JavaScript e CSS com Vite.
- Backend: Java 21, Spring Boot e APIs REST.
- Persistência: PostgreSQL, com um banco lógico por serviço.
- Execução local: Docker Compose.

## Serviços

| Serviço | Porta | Banco |
|---|---:|---|
| Identity Service | 8081 | `identity_db` |
| Team Service | 8082 | `team_db` |
| Project Service | 8083 | `project_db` |
| Planning Service | 8084 | `planning_db` |
| Frontend | 5173 | — |

Cada backend é uma aplicação Spring Boot independente. Nesta etapa, os serviços possuem apenas configuração, conectividade com o PostgreSQL e health check; as funcionalidades de negócio serão implementadas em incrementos posteriores.

## Executar localmente

Pré-requisitos:

- Docker com Docker Compose;
- Node.js apenas se o frontend for executado fora do Compose;
- Java e Maven apenas se os backends forem executados fora do Compose.

1. Copie `.env.example` para `.env` e ajuste os valores locais, se necessário.
2. Suba o ambiente:

```bash
docker compose up --build
```

3. Acesse o frontend em <http://localhost:5173>.

Health checks básicos:

```text
http://localhost:8081/health
http://localhost:8082/health
http://localhost:8083/health
http://localhost:8084/health
```

Para parar os serviços:

```bash
docker compose down
```

Para remover também os dados locais do PostgreSQL:

```bash
docker compose down -v
```

O comando `down -v` remove somente o volume nomeado do ambiente local e deve ser usado quando os dados de desenvolvimento puderem ser descartados.

## Documentação

- [Project Brief](docs/PROJECT_BRIEF.md)
- [Requirements](docs/REQUIREMENTS.md)
- [Business Rules](docs/BUSINESS_RULES.md)
- [Architecture](docs/ARCHITECTURE.md)

## Núcleo operacional disponível

Além dos CRUDs de equipes e projetos, a etapa atual disponibiliza cadastro de funcionários e memberships no Team Service e endpoints persistentes de alocações, transportes, conflitos e propostas no Planning Service. O frontend possui telas simples de Funcionários e Planejamento; o dashboard visual está deliberadamente fora do escopo.

Para testar manualmente, envie `X-Organization-Id` nas chamadas. Exemplos: `POST /employees`, `PUT /employees/{employeeId}/teams/{teamId}`, `POST /planning/allocations`, `POST /planning/transports`, `POST /planning/conflicts`, `POST /planning/proposals` e `POST /planning/proposals/{id}/approve`. Uma proposta é criada como `DRAFT` e não altera alocações automaticamente.
