# Contratos operacionais para o dashboard

O dashboard deve consumir o Planning Service como fonte das regras de negócio. Não deve recalcular conflitos, duração ou disponibilidade no frontend. Todas as chamadas devem enviar `X-Organization-Id` enquanto a autenticação definitiva não estiver implementada.

## Consultas

| Endpoint | Filtros/entrada | Resposta relevante |
|---|---|---|
| `GET /allocations` | `project`, `resourceType`, `resource`, `from`, `to` | alocações inclusivas; `memberIds` contém o snapshot efetivo quando existir; registros legados retornam lista vazia |
| `GET /transports` | organização | origem, destino, período, `durationDays`, `arrivalDate`, `status` (`PENDING`/`CONFIRMED`) |
| `GET /availability` | `resourceType`, `resourceId`, `from`, `to` | `AVAILABLE`, `UNAVAILABLE` ou `PENDING`, razões e compromissos relacionados |
| `POST /conflicts` | `AllocationRequest` | conflitos explicáveis, severidade e compromissos envolvidos |
| `GET /proposals` / `GET /proposals/{id}` | organização e id | versão, status, payload com itens, projetos não atendidos e pendências |
| `GET /organization-settings/base-location` | organização | cidade base; `baseLocation: null` representa pendência explícita |

Team Service fornece equipes, funcionários, indisponibilidades e memberships. Project Service fornece projetos, período, continuidade, prioridade, localidades e duração manual. As respostas são paginadas em `GET /teams`, `GET /employees` e `GET /projects`; o cliente deve respeitar `page`, `size`, `totalElements` e `totalPages`.

## Escrita controlada

`POST /proposals` apenas gera DRAFT. `POST /proposals/{id}/approve` aceita `itemIds` para aprovação seletiva e revalida a proposta; transporte pendente, conflito bloqueante, revisão alterada ou composição de equipe modificada impedem confirmação. `POST /proposals/{id}/reject` e `/discard` encerram o ciclo sem alterar compromissos.

Os períodos múltiplos de projetos fracionáveis são representados por múltiplas alocações do mesmo projeto; a integração visual deve agrupá-las por `projectId`, sem presumir que o intervalo entre elas seja execução.
