# TeamFlow — Development Status

**Data de referência:** 2026-11-24  
**Escopo:** MVP incremental  
**Status:** Team Service e Project Service com CRUD inicial validado

## 1. Objetivo e visão

O TeamFlow é uma plataforma web SaaS B2B para organizar equipes inteiras em projetos simultâneos, considerando projetos, localidades, serviços, dias de serviço e, futuramente, alocações, deslocamentos e conflitos de planejamento.

O MVP não cadastra nem aloca funcionários individualmente. O gestor permanece responsável pelas decisões de planejamento.

## 2. Arquitetura aprovada

O backend utiliza quatro microsserviços independentes:

- Identity Service: usuários, autenticação e organizações.
- Team Service: equipes, composição agregada, competências, qualificações e disponibilidade cadastrada.
- Project Service: projetos, serviços e localidades.
- Planning Service: alocações, transporte, conflitos e planejamento.

Os serviços possuem bancos lógicos separados no PostgreSQL e não acessam diretamente as tabelas uns dos outros. A comunicação REST é síncrona nesta etapa. Não há mensageria nem Micro Frontends implementados.

## 3. Stack tecnológica

- Frontend: React, JavaScript e CSS.
- Backend: Java, Spring Boot e APIs REST.
- Persistência: PostgreSQL.
- Execução local: Docker e Docker Compose.
- Migrações dos serviços funcionais: Flyway.

TypeScript não faz parte da implementação.

## 4. Estado da infraestrutura

Verificado com Docker Compose:

- PostgreSQL está executando com persistência local.
- `identity_db`, `team_db`, `project_db` e `planning_db` existem e são acessíveis.
- Os quatro serviços backend e o frontend possuem containers independentes.
- Os containers estavam saudáveis na última execução de `docker compose ps`.
- O Project Service aplica a migração Flyway `V1__create_projects.sql` e cria a tabela `projects`.
- O Team Service aplica sua migração `V1__create_teams.sql` e possui a tabela `teams`.
- O frontend é servido em `http://localhost:5173`.
- O Team Service é exposto em `http://localhost:8082`.
- O Project Service é exposto em `http://localhost:8083`.

O contexto de organização usado localmente é temporário e enviado pelo cabeçalho `X-Organization-Id`. Isso não representa autenticação ou segurança de produção.

## 5. Estado do Identity Service

O serviço está presente no ambiente Docker Compose e possui health check funcional em `/health`.

Autenticação real, autorização e gestão completa de organizações ainda não foram implementadas.

## 6. Estado do Team Service

Implementado e testado:

- Cadastro de equipes.
- Listagem paginada.
- Consulta individual.
- Edição.
- `organization_id` em todas as operações.
- Composição agregada por funções e quantidades.
- Competências, qualificações, disponibilidade e situação como dados cadastráveis.
- Validação e tratamento padronizado de erros.
- Persistência PostgreSQL com migração Flyway.
- CORS local para integração com o frontend.

Não foram implementados funcionários individuais, alocações, transporte, conflitos ou cálculos de produtividade.

## 7. Estado do Project Service

Implementado e testado nesta sessão:

- Cadastro de projetos.
- Listagem paginada.
- Consulta individual.
- Edição.
- Identificação, cliente e descrição.
- Datas previstas de início e término como campos independentes.
- Duração estimada informada manualmente, sem cálculo automático.
- Competências e qualificações necessárias.
- Localidades agregadas vinculadas ao projeto.
- Serviços agregados vinculados ao projeto.
- Dias de serviço e duração estimada informados manualmente por serviço.
- `organization_id` em todas as operações.
- Validação e tratamento padronizado de erros.
- Persistência PostgreSQL com migração Flyway.
- CORS local para integração com o frontend.

As listas de localidades e serviços não definem regras de múltiplas etapas, alocações ou compatibilidade. Essas decisões continuam pendentes na documentação de negócio.

## 8. Estado do Planning Service

O serviço está presente no ambiente Docker Compose e possui health check funcional em `/health`.

Cadastro de alocações, eventos de transporte, motor de conflitos, disponibilidade derivada e sugestões de alocação ainda não foram implementados.

## 9. Estado do frontend

O frontend React existente foi evoluído, sem criar uma aplicação separada:

- Navegação entre Equipes e Projetos.
- Listagem, cadastro, edição e detalhes de equipes.
- Listagem, cadastro, edição e detalhes de projetos.
- Cadastro de localidades e serviços dentro do formulário de projeto.
- Estados de carregamento, lista vazia e erros.
- Consumo das APIs REST usando o contexto temporário de organização.
- Interface preparada para receber futuramente uma visão operacional centralizada.

Dashboard, calendário de planejamento, alocações, transportes e conflitos ainda não estão disponíveis.

## 10. Funcionalidades concluídas e testadas

Comandos e verificações realizados:

- `npm run build` no frontend: aprovado.
- `mvn test` do Team Service com PostgreSQL via Testcontainers: 5 testes, 0 falhas, 0 erros.
- `mvn test` do Project Service com PostgreSQL via Testcontainers: 5 testes, 0 falhas, 0 erros.
- Build Docker do Team Service: aprovado.
- Build Docker do Project Service: aprovado.
- Build Docker do frontend: aprovado.
- `docker compose config --quiet`: aprovado.
- `docker compose up -d`: executado com containers saudáveis.
- Health checks dos serviços: aprovados para Identity, Team, Project e Planning.
- CRUD real do Team Service: criação, listagem, consulta, edição e isolamento organizacional verificados.
- CRUD real do Project Service: criação, listagem, consulta, edição e isolamento organizacional verificados.
- Migrações e tabelas de equipes e projetos verificadas no PostgreSQL.

## 11. Limitações conhecidas

- Não há autenticação real nem autorização de produção.
- `X-Organization-Id` é somente um contexto temporário de desenvolvimento.
- Os valores oficiais de situação da equipe ainda não foram definidos.
- A precedência entre datas previstas, duração estimada e dias de serviço permanece pendente.
- As regras de múltiplas equipes, etapas e localidades permanecem pendentes.
- As regras de intervalo, conflitos bloqueantes, alertas e disponibilidade no dia de chegada permanecem pendentes.
- Não há auditoria operacional completa com usuário autenticado.
- Não há dashboard operacional nem integração entre Planning, Team e Project para validações.
- Dados de teste foram criados em organizações temporárias durante as verificações locais.

## 12. Próximo incremento

O incremento originalmente previsto — implementação do Project Service integrada ao frontend React existente — foi concluído nesta sessão e está registrado na seção 7.

O próximo incremento efetivo deverá ser definido após a revisão deste estado. A direção natural é iniciar o Planning Service com um primeiro fluxo vertical aprovado, sem inferir as regras de conflitos, transporte ou confirmação que continuam pendentes.

## 13. Instruções para continuar

1. Ler `AGENTS.md`, `docs/PROJECT_BRIEF.md`, `docs/REQUIREMENTS.md`, `docs/BUSINESS_RULES.md` e `docs/ARCHITECTURE.md`.
2. Executar `docker compose config` para validar a configuração local.
3. Executar `docker compose up -d` para iniciar PostgreSQL, serviços e frontend.
4. Usar `http://localhost:5173` para acessar a interface.
5. Usar `X-Organization-Id` apenas como contexto temporário de desenvolvimento.
6. Executar os testes do serviço alterado antes de qualquer nova integração.
7. Não implementar regras pendentes implicitamente.
8. Não iniciar funcionalidades de Planning sem autorização e sem revisar as pendências de negócio correspondentes.

## 14. Decisões que ainda dependem de aprovação

- Perfis e permissões do MVP.
- Valores válidos para situação da equipe.
- Modelo detalhado de disponibilidade e suas exceções.
- Precedência entre datas, duração estimada e dias de serviço.
- Regras para múltiplas equipes, etapas e localidades.
- Inclusão das datas inicial e final nos intervalos de planejamento.
- Disponibilidade da equipe no dia de chegada do transporte.
- Conflitos que bloqueiam uma operação e conflitos que permitem confirmação.
- Critérios, prioridade e ordenação das sugestões de alocação.
- Fluxos de autenticação, autorização e comunicação entre serviços.
- Política final de auditoria, observabilidade e implantação de produção.
