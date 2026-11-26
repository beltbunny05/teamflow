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

`POST /proposals` apenas gera DRAFT. `PUT /proposals/{id}` edita itens antes da aprovação; transportes podem receber `durationDays`, `arrivalDate` derivada, `origin`, `destination` e status `CONFIRMED_TRANSPORT`, sem criar ou alterar transporte confirmado durante a edição. Itens fixados devem permanecer inalterados.

Os itens da proposta podem ter as operações `CREATE`, `RETAIN`, `CANCEL`, `UPDATE` e `TRANSPORT`. `relatedId` identifica o compromisso substituído quando aplicável. Um item `TRANSPORT` pendente usa `status: PENDING_TRANSPORT` e `durationDays: null`; ele não é duração zero e bloqueia a aprovação de alocações dependentes. Transportes confirmados na proposta são persistidos junto com as alocações dependentes durante a aprovação local.

`POST /proposals/{id}/approve` aceita `itemIds` para aprovação seletiva e revalida a proposta; transporte pendente, dependência não selecionada, conflito bloqueante, revisão alterada ou composição de equipe modificada impedem confirmação. Aprovação parcial usa `status: PARTIALLY_APPROVED`, registra `approvedItemIds` e `unapprovedItemIds` e mantém `approvalComplete: false`. `POST /proposals/{id}/reject` e `/discard` encerram o ciclo sem alterar compromissos.

Os períodos múltiplos de projetos fracionáveis são representados por múltiplas alocações do mesmo projeto; a integração visual deve agrupá-las por `projectId`, sem presumir que o intervalo entre elas seja execução. No replanejamento, uma mudança de recurso ou período é representada por `CANCEL` do compromisso flexível anterior e `CREATE` do novo compromisso; compromissos fixados só podem ser `RETAIN`.

As operações de escrita do Planning Service serializam mutações por organização com bloqueio transacional do estado de planejamento. Isso protege o ciclo local contra duas aprovações simultâneas ou uma alocação manual concorrente; não cria atomicidade distribuída com Team Service ou Project Service.
