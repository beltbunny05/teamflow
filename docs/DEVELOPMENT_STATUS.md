# TeamFlow — Development Status

**Data de referência:** 2026-11-25
**Escopo:** MVP incremental  
**Status:** Núcleo operacional inicial implementado; integração completa e algoritmo ainda parciais

## 1. Objetivo e visão

O TeamFlow é uma plataforma web SaaS B2B para organizar a participação de equipes e funcionários em projetos simultâneos, considerando projetos, localidades, durações informadas e, futuramente, alocações, deslocamentos e conflitos de planejamento.

A direção de produto agora exige considerar funcionários individualmente. O cadastro, a disponibilidade, os vínculos com equipes, a alocação individual e os deslocamentos individuais ainda não foram implementados e permanecem dependentes de decisões de negócio detalhadas. O gestor permanece responsável pelas decisões de planejamento.

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

Implementado:

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
- Funcionários individuais com função principal, situação e localização inicial opcional.
- Listagem paginada, consulta, edição e inativação lógica de funcionários.
- Memberships de funcionários em múltiplas equipes com função específica por vínculo.
- Consulta dos vínculos por funcionário e por equipe.
- Cadastro e consulta de indisponibilidades individuais.

Ainda não participam da disponibilidade derivada: indisponibilidades, composição coletiva, alocações e deslocamentos do Planning Service.

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
- Prioridade `LOW`, `NORMAL`, `HIGH` ou `CRITICAL`.
- Continuidade de execução.
- Quantidade desejada opcional.

O campo legado de localidades foi preservado para transição segura. Projetos novos devem usar uma única localidade; registros antigos com múltiplas localidades não são reduzidos arbitrariamente. Serviços continuam sendo informações auxiliares.

## 8. Estado do Planning Service

Implementado o primeiro núcleo persistente:

- alocações de equipes e funcionários, inclusive períodos inclusivos e filtros;
- fixação/liberação e cancelamento lógico de alocações;
- transportes individuais/coletivos com duração manual opcional, chegada e situação `PENDING`/`CONFIRMED`;
- conflitos iniciais de sobreposição e transporte pendente;
- propostas versionadas em estado `DRAFT` e aprovação integral;
- tratamento padronizado de validação e falhas de requisição;
- Flyway e banco próprio do serviço.

O serviço ainda não consulta Team/Project por REST, não executa a heurística completa, não deriva disponibilidade integral e não implementa aprovação seletiva/replanejamento detalhado.

## 9. Estado do frontend

O frontend React existente foi evoluído, sem criar uma aplicação separada:

- Navegação entre Equipes e Projetos.
- Listagem, cadastro, edição e detalhes de equipes.
- Listagem, cadastro, edição e detalhes de projetos.
- Cadastro de localidades e serviços dentro do formulário de projeto.
- Estados de carregamento, lista vazia e erros.
- Consumo das APIs REST usando o contexto temporário de organização.
- Interface com navegação lateral e área central de trabalho, preparada para receber futuramente uma visão operacional centralizada.
- Módulo simples de Funcionários com cadastro/edição/listagem.
- Módulo simples de Planejamento com consulta de alocações/transportes e geração de proposta.

Dashboard, calendário de planejamento, alocações, transportes e conflitos ainda não estão disponíveis.

## 10. Funcionalidades concluídas e testadas

Comandos e verificações realizados:

- `npm run build` no frontend: aprovado.
- Testes unitários do Team Service em container Maven: 2 testes, 0 falhas, 0 erros.
- Testes unitários do Project Service em container Maven: 2 testes, 0 falhas, 0 erros.
- Testes do Planning Service em container Maven: 3 testes, 0 falhas, 0 erros.
- Integrações Testcontainers Team/Project: tentadas, mas não concluídas porque o container Maven não alcançou a porta publicada do PostgreSQL de teste; não classificadas como aprovadas.
- Build Docker do Team Service: aprovado.
- Build Docker do Project Service: aprovado.
- Build Docker do Planning Service: aprovado.
- Build Docker do frontend: aprovado.
- `docker compose config --quiet`: aprovado.
- `docker compose up -d`: executado com containers saudáveis.
- Health checks dos serviços: aprovados para Identity, Team, Project e Planning.
- CRUD real do Team Service: criação, listagem, consulta, edição e isolamento organizacional verificados.
- CRUD real do Project Service: criação, listagem, consulta, edição e isolamento organizacional verificados.
- Migrações V2 de Team/Project e V1/V2 de Planning aplicadas no PostgreSQL.
- Smoke REST: funcionário, projeto, alocação fixa, conflito bloqueante, transporte pendente, proposta DRAFT e aprovação integral.

## 11. Limitações conhecidas

- Não há autenticação real nem autorização de produção; `X-Organization-Id` é apenas contexto de desenvolvimento.
- O Planning Service ainda não consulta Team Service e Project Service por REST e, portanto, não valida referências remotas nem compõe disponibilidade completa.
- A geração de propostas ainda é um esqueleto persistente: não materializa alocações heurísticas nem alternativas por projeto.
- Transporte possui criação e consulta, mas ainda não possui edição, cancelamento e revalidação completa após mudanças de alocação.
- Indisponibilidade individual possui persistência e endpoints de cadastro/consulta, mas ainda não participa do mecanismo de conflitos.
- Propostas ainda não têm snapshot detalhado, edição, aprovação seletiva, comparação de versões ou proteção contra obsolescência.
- Não há auditoria operacional completa com usuário autenticado.
- Dashboard operacional, calendário, Gantt, gráficos e drag-and-drop permanecem fora desta etapa.
- Dados sintéticos usados em testes locais devem permanecer identificados como dados de teste.

## 12. Próximo incremento

Adicionar clientes REST isolados para Team/Project com timeout e propagação do contexto organizacional; completar disponibilidade, revalidação, transportes e algoritmo heurístico determinístico; depois criar aprovação seletiva com controle de versão e testes de integração PostgreSQL.

## 13. Instruções para continuar

1. Ler `AGENTS.md`, `docs/PROJECT_BRIEF.md`, `docs/REQUIREMENTS.md`, `docs/BUSINESS_RULES.md` e `docs/ARCHITECTURE.md`.
2. Executar `docker compose config --quiet` e `docker compose up -d`.
3. Usar `http://localhost:5173` para acessar a interface.
4. Enviar `X-Organization-Id` como contexto temporário de desenvolvimento.
5. Executar os testes e builds do serviço alterado antes da próxima integração.
6. Não alterar automaticamente compromissos confirmados nem aplicar propostas sem aprovação.

## 14. Decisões e limites ainda abertos

- Autenticação, autorização, auditoria e observabilidade de produção.
- Política final de edição/cancelamento de transportes e disponibilidade no dia de chegada.
- Critérios detalhados para conflitos derivados de composição, múltiplas equipes e alocação individual/coletiva.
- Formato completo de alternativas, revisão de duração e aprovação seletiva de propostas.
- Escala máxima esperada do algoritmo heurístico e métricas operacionais de avaliação.

## 15. Retomada da implementação — 2026-11-25

### Concluído nesta sessão

- migração aditiva do Team Service para funcionários, memberships e indisponibilidades;
- endpoints de cadastro, consulta paginada, edição, inativação e vínculos de funcionários;
- migração aditiva do Project Service para prioridade, continuidade e quantidade desejada;
- primeiro fluxo persistente do Planning Service para alocações, fixação, cancelamento, transportes pendentes/confirmados, consulta de conflitos e propostas DRAFT/APPROVED;
- proteção de unicidade para versões de propostas e normalização de tipo de recurso na validação de conflitos;
- telas React simples para Funcionários e Planejamento, preservando a navegação e a identidade visual existente;
- Flyway habilitado no Planning Service.

### Parcial ou não concluído

- indisponibilidade individual possui migração, service e endpoints de cadastro/consulta, mas ainda não participa da disponibilidade derivada;
- transporte ainda não possui edição, cancelamento e revalidação completa após alterações;
- Planning ainda não consulta efetivamente Team/Project por REST, não possui clientes com timeout e não implementa a heurística completa de geração de alocações;
- propostas ainda persistem metadados de estratégia e não possuem snapshot detalhado, edição, aprovação seletiva ou proteção por versão;
- há testes unitários iniciais do Planning Service; ainda falta cobertura proporcional de integração para os fluxos adicionados;
- frontend permite consultar e gerar propostas, mas ainda não oferece formulários completos de alocação e transporte;
- dashboard, calendário, Gantt, gráficos e drag-and-drop continuam fora desta etapa.

### Próxima retomada

Adicionar clientes REST isolados para Team/Project com timeout e propagação de organização; completar disponibilidade, revalidação e algoritmo heurístico; depois criar testes de integração com PostgreSQL para isolamento, conflitos, transporte pendente, fixação e aprovação.
