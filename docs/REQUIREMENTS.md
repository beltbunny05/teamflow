# TeamFlow — Requirements

**Status:** Draft revisado derivado do `PROJECT_BRIEF.md`  
**Data:** 2026-11-19  
**Escopo:** MVP  
**Implementação:** incremental iniciada; Team Service e Project Service possuem CRUD inicial

## 1. Objetivo

Este documento organiza os requisitos funcionais e não funcionais iniciais do TeamFlow a partir do `PROJECT_BRIEF.md` validado.

Ele não substitui o Project Brief nem define regras de negócio detalhadas. Regras ainda pendentes permanecem identificadas como pendências e não devem ser inferidas durante a implementação.

## 2. Convenções

- **Confirmado:** decisão suficientemente estabelecida no Project Brief para orientar o refinamento do requisito.
- **Pendente:** depende de decisão adicional antes de ser considerado fechado.
- **Fora do MVP:** explicitamente excluído nesta etapa.
- **MUST:** requisito necessário para o escopo atual.
- **SHOULD:** funcionalidade prevista e importante, mas cuja prioridade/forma final ainda depende de refinamento.

Os critérios de aceitação descrevem comportamentos observáveis. A ausência de um critério fechado indica que a decisão correspondente ainda precisa ser tomada.

## 3. Conceitos e limites do planejamento

- **Projeto:** unidade principal de planejamento. Serviços são informações auxiliares do projeto.
- **Período de alocação:** intervalo de datas em que uma equipe ou funcionário fica comprometido com o projeto.
- **Dias de transporte:** intervalo de datas em que uma equipe ou funcionário fica indisponível por deslocamento entre localidades.
- O planejamento usa dias inteiros de calendário; dias úteis, fins de semana e feriados têm o mesmo peso.
- O planejamento é orientado por projetos e pode considerar equipes e funcionários individualmente.
- Um funcionário pode participar de múltiplas equipes, mas a forma de identificar, cadastrar e relacionar esses registros ainda é pendente.
- O sistema deve permitir analisar alocações e conflitos envolvendo projetos, equipes e funcionários.
- O gestor cadastra e atualiza os dados operacionais e mantém a decisão final sobre alocações.
- O deslocamento é cadastrado manualmente como evento próprio do planejamento.
- O MVP não dependerá de integrações externas.
- Este documento não introduz métricas ou cálculos adicionais além do planejamento por dias e compromissos.

## 4. Requisitos funcionais

### 4.1 Autenticação e acesso

| ID | Requisito | Status | Origem |
|---|---|---|---|
| RF-001 | O sistema deve autenticar usuários antes de permitir acesso às funcionalidades protegidas. | Confirmado | Brief, seções 5 e 16 |
| RF-002 | O sistema deve aplicar controle básico de acesso às funcionalidades e aos dados. | Confirmado | Brief, seções 5 e 16 |
| RF-003 | Os perfis e permissões devem permanecer simples até que uma necessidade mais complexa seja confirmada. | Pendente | Brief, seção 5 |

### 4.2 Equipes

| ID | Requisito | Status | Origem |
|---|---|---|---|
| RF-004 | O gestor deve poder cadastrar uma equipe com nome ou identificação. | Confirmado | Brief, seções 6 e 7 |
| RF-005 | O gestor deve poder informar a quantidade total de integrantes sem que o cadastro agregado da equipe dependa do cadastro individual de funcionários. | Confirmado | Brief, seção 7 |
| RF-006 | O sistema deve permitir registrar composição agregada por funções e quantidades, quando o gestor optar por detalhá-la. | Confirmado | Brief, seções 6 e 7 |
| RF-007 | O sistema deve permitir registrar competências e qualificações associadas à equipe. | Confirmado | Brief, seção 7 |
| RF-008 | O sistema deve permitir registrar e atualizar a disponibilidade e a situação atual da equipe. | Confirmado | Brief, seção 7 |
| RF-009 | O gestor deve poder editar os dados da equipe. | Confirmado | Brief, seção 6 |
| RF-010 | O cadastro agregado de uma equipe não deve exigir que todos os funcionários estejam cadastrados individualmente. | Confirmado | Brief, seção 7 |

### 4.2.1 Funcionários e vínculos com equipes

| ID | Requisito | Status | Origem |
|---|---|---|---|
| RF-050 | O sistema deve permitir considerar funcionários individualmente no planejamento orientado por projetos. | Confirmado | Brief, seção 6 |
| RF-051 | Um funcionário pode participar de múltiplas equipes. | Confirmado; detalhamento pendente | Brief, seção 6 |
| RF-052 | O sistema deve permitir analisar a necessidade de alocação individual de um funcionário em um projeto. | Confirmado; detalhamento pendente | Brief, seção 6 |
| RF-053 | O deslocamento de um funcionário pode ser independente do deslocamento da equipe. | Confirmado; detalhamento pendente | Brief, seção 6 |
| RF-054 | O sistema deve identificar conflitos que envolvam projetos, equipes e funcionários. | Confirmado; regras pendentes | Brief, seção 6 |
| RF-055 | Nenhuma alocação individual ou alteração de planejamento deve ser aplicada sem confirmação explícita do gestor. | Confirmado | Brief, seção 6 |

Os requisitos acima confirmam a necessidade do domínio, mas não fecham o cadastro, os vínculos, a disponibilidade, a precedência entre alocações ou o comportamento dos conflitos.

### 4.3 Projetos

| ID | Requisito | Status | Origem |
|---|---|---|---|
| RF-011 | O gestor deve poder cadastrar e editar projetos. | Confirmado | Brief, seções 6 e 8 |
| RF-012 | Um projeto deve poder registrar nome ou identificação, cliente quando aplicável, localidade, descrição do serviço e competências ou qualificações necessárias. | Confirmado | Brief, seção 8 |
| RF-013 | Um projeto deve permitir registrar separadamente a data prevista de início. | Confirmado | Brief, seção 8 |
| RF-014 | Um projeto deve permitir registrar separadamente a data prevista de término. | Confirmado | Brief, seção 8 |
| RF-015 | Um projeto deve permitir registrar separadamente uma duração estimada. | Confirmado | Brief, seção 8 |
| RF-016 | Um projeto pode manter informações auxiliares de serviços e seus dias informados manualmente. | Confirmado | Brief, seção 8 |
| RF-017 | O sistema deve permitir cadastrar projetos simultâneos em diferentes localidades. | Confirmado | Brief, seções 2, 6 e 8 |
| RF-018 | A regra de precedência entre datas previstas e duração estimada, inclusive em caso de divergência, deve ser definida antes da implementação correspondente. | Pendente | Brief, seções 8, 9 e 25 |

### 4.4 Calendário e alocações

| ID | Requisito | Status | Origem |
|---|---|---|---|
| RF-019 | O sistema deve utilizar dias inteiros de calendário como unidade de planejamento. | Confirmado | Brief, seções 6, 9 e 10 |
| RF-020 | O sistema deve considerar dias úteis, fins de semana e feriados como dias equivalentes no planejamento do MVP. | Confirmado | Brief, seção 10 |
| RF-021 | O gestor deve poder analisar e, após confirmação, criar uma alocação de equipe ou funcionário em um projeto, com data de início e data de término ou duração do período. | Confirmado; detalhamento pendente | Brief, seções 6, 10 e 11 |
| RF-022 | O sistema deve exibir a duração do período de alocação em dias inteiros. | Confirmado | Brief, seção 11 |
| RF-023 | O gestor deve poder editar uma alocação existente. | Confirmado | Brief, seção 6 |
| RF-024 | O sistema deve permitir visualizar a disponibilidade dos recursos envolvidos antes da confirmação da alocação. | Confirmado; disponibilidade individual pendente | Brief, seção 11 |
| RF-025 | O sistema deve identificar uma alocação incompatível com outro compromisso do mesmo projeto, equipe ou funcionário no período sobreposto. | Confirmado; regras pendentes | Brief, seções 6, 11 e 13 |
| RF-026 | A regra que define quais incompatibilidades bloqueiam a confirmação e quais permitem prosseguir mediante confirmação do gestor permanece pendente. | Pendente | Brief, seções 13 e 25 |
| RF-027 | O sistema não deve dividir automaticamente uma equipe entre projetos no MVP. | Confirmado | Brief, seções 6, 7 e 24 |

### 4.5 Transporte e deslocamento

| ID | Requisito | Status | Origem |
|---|---|---|---|
| RF-028 | O gestor deve poder cadastrar um deslocamento como evento próprio do planejamento. | Confirmado | Brief, seção 12 |
| RF-029 | Um deslocamento deve registrar o recurso envolvido, localidade de origem, localidade de destino, data de início e duração estimada em dias inteiros. | Confirmado; modelo pendente | Brief, seção 12 |
| RF-030 | O sistema deve considerar o período de transporte na análise de compromissos da equipe ou funcionário. | Confirmado | Brief, seções 11–13 |
| RF-031 | O sistema deve identificar quando o início de um próximo compromisso ocorre antes da conclusão do deslocamento e da chegada ao destino. | Confirmado; regras pendentes | Brief, seção 12 |
| RF-032 | A regra exata sobre a disponibilidade da equipe no dia de chegada e sobre bloqueio ou alerta para o próximo serviço permanece pendente. | Pendente | Brief, seções 12, 13 e 25 |
| RF-033 | O sistema não deve calcular automaticamente rotas, distâncias ou tempos de deslocamento no MVP. | Confirmado | Brief, seção 12 |

### 4.6 Compatibilidade e conflitos

| ID | Requisito | Status | Origem |
|---|---|---|---|
| RF-034 | O sistema deve verificar a disponibilidade dos recursos envolvidos ao analisar uma alocação. | Confirmado; modelo pendente | Brief, seção 13 |
| RF-035 | O sistema deve considerar compromissos de projetos e eventos de transporte na análise de conflitos. | Confirmado | Brief, seções 11–13 |
| RF-036 | O sistema deve verificar se as competências e qualificações necessárias do projeto são atendidas pelos recursos avaliados. | Confirmado; critérios pendentes | Brief, seções 13 e 14 |
| RF-037 | O sistema deve identificar e diferenciar conflitos de período, indisponibilidade, competência, qualificação e transporte envolvendo projetos, equipes e funcionários. | Confirmado; regras pendentes | Brief, seção 13 |
| RF-038 | O sistema deve explicar ao gestor o motivo de cada incompatibilidade identificada. | Confirmado | Brief, seção 13 |
| RF-039 | As regras detalhadas de sobreposição, múltiplas etapas e múltiplas localidades devem ser documentadas antes da implementação correspondente. | Pendente | Brief, seções 13 e 25 |

### 4.7 Sugestões de alocação

As sugestões são uma funcionalidade prevista com prioridade **SHOULD**. Elas não representam alocação automática.

| ID | Prioridade | Requisito | Status | Origem |
|---|---|---|---|---|
| RF-040 | SHOULD | O sistema poderá apresentar sugestões de equipes ou funcionários para um período e projeto solicitados. | Previsto/Pendente | Brief, seção 14 |
| RF-041 | SHOULD | As sugestões devem considerar disponibilidade, período solicitado, competências, qualificações, compromissos e deslocamentos cadastrados dos recursos avaliados. | Previsto/Pendente | Brief, seção 14 |
| RF-042 | SHOULD | Cada sugestão deve apresentar justificativa compreensível baseada nos dados cadastrados. | Previsto/Pendente | Brief, seção 14 |
| RF-043 | SHOULD | O gestor deve selecionar e confirmar uma sugestão antes que ela altere o planejamento. | Confirmado | Brief, seções 4, 6 e 14 |
| RF-044 | MUST | O sistema não deve executar otimização automática complexa nem aplicar uma sugestão sem confirmação do gestor. | Confirmado | Brief, seções 6, 14 e 24 |

Os critérios mínimos de seleção, ordenação, explicação e comportamento quando não houver alternativa ainda precisam ser definidos.

### 4.8 Visualizações e histórico

| ID | Requisito | Status | Origem |
|---|---|---|---|
| RF-045 | O sistema deve oferecer uma visualização de calendário das alocações. | Confirmado | Brief, seção 15 |
| RF-046 | O sistema deve permitir consultar equipes e funcionários disponíveis e ocupados, conforme os dados e regras aprovados. | Confirmado; modelo pendente | Brief, seção 15 |
| RF-047 | O sistema deve permitir visualizar projetos em andamento, períodos de transporte, alocações de equipes e funcionários e conflitos identificados. | Confirmado; detalhamento pendente | Brief, seção 15 |
| RF-048 | O sistema deve registrar alterações relevantes, identificando responsável e data e horário da alteração. | Confirmado | Brief, seção 16 |
| RF-049 | As métricas definitivas e suas metas devem ser definidas antes de serem tratadas como requisitos fechados de dashboard. | Pendente | Brief, seção 15 |

## 5. Critérios de aceitação funcionais

Cada requisito funcional deve possuir pelo menos um critério verificável. Os critérios abaixo não definem tecnologia ou implementação interna.

### Autenticação e acesso

- **CA-RF-001-01:** Dado um usuário válido, quando ele fornece credenciais válidas, então acessa as funcionalidades protegidas autorizadas.
- **CA-RF-001-02:** Dado um usuário não autenticado, quando tenta acessar uma funcionalidade protegida, então o acesso é recusado.
- **CA-RF-002-01:** Dado um usuário autenticado, quando acessa uma funcionalidade ou dado não autorizado para seu perfil, então a operação é recusada.
- **CA-RF-002-02:** Dado um usuário autorizado, quando acessa uma funcionalidade permitida, então a operação é disponibilizada.
- **CA-RF-003-01:** Antes de definir os requisitos de perfis, o documento de permissões deve identificar os perfis mínimos e as operações permitidas para cada um.

### Equipes

- **CA-RF-004-01:** Dado um gestor autorizado, quando informa um nome ou identificação válida e confirma o cadastro, então uma equipe é criada e pode ser consultada.
- **CA-RF-005-01:** Dado um gestor autorizado, quando informa a quantidade total de integrantes, então a equipe é salva sem exigir nomes ou registros individuais.
- **CA-RF-006-01:** Dado um gestor autorizado, quando informa funções e quantidades, então a composição agregada fica associada à equipe.
- **CA-RF-006-02:** Dado um gestor que não deseja detalhar funções, quando salva a equipe, então o cadastro é aceito sem essa informação.
- **CA-RF-007-01:** Dado um gestor autorizado, quando associa competências ou qualificações à equipe, então essas informações podem ser consultadas posteriormente.
- **CA-RF-008-01:** Dado um gestor autorizado, quando informa ou altera a disponibilidade ou situação da equipe, então o novo estado fica disponível para consulta no planejamento.
- **CA-RF-009-01:** Dado uma equipe existente e um gestor autorizado, quando altera seus dados e confirma, então a consulta posterior apresenta os dados atualizados.
- **CA-RF-010-01:** Quando uma equipe é cadastrada ou alocada, o sistema não solicita nem cria automaticamente registros individuais de funcionários.

### Funcionários e vínculos

- **CA-RF-050-01:** Quando o planejamento de um projeto exigir análise individual, o sistema deve permitir considerar um funcionário como recurso, sem aplicar alteração automaticamente.
- **CA-RF-051-01:** Antes da implementação dos vínculos, a documentação deve definir como um funcionário participa de múltiplas equipes e como suas funções são representadas.
- **CA-RF-052-01:** Antes de confirmar uma alocação individual, o sistema deve apresentar o projeto, o período e os conflitos identificados conforme as regras aprovadas.
- **CA-RF-053-01:** Dado um deslocamento individual, a análise não deve presumir que o deslocamento da equipe tenha o mesmo período ou destino.
- **CA-RF-054-01:** Quando houver conflito envolvendo projeto, equipe ou funcionário, o resultado deve identificar os recursos e compromissos relacionados.
- **CA-RF-055-01:** Quando uma alocação individual não for confirmada pelo gestor, o planejamento deve permanecer inalterado.

### Projetos

- **CA-RF-011-01:** Dado um gestor autorizado, quando informa os dados mínimos definidos para um projeto e confirma, então o projeto é criado.
- **CA-RF-011-02:** Dado um projeto existente e um gestor autorizado, quando altera seus dados e confirma, então a consulta posterior apresenta os dados atualizados.
- **CA-RF-012-01:** Dado um projeto, quando seus dados previstos são informados, então nome ou identificação, cliente quando aplicável, localidade, descrição e necessidades de competências podem ser consultados.
- **CA-RF-013-01:** Dado um projeto, quando o gestor informa uma data prevista de início, então essa data pode ser consultada independentemente das demais datas.
- **CA-RF-014-01:** Dado um projeto, quando o gestor informa uma data prevista de término, então essa data pode ser consultada independentemente da duração estimada.
- **CA-RF-015-01:** Dado um projeto, quando o gestor informa uma duração estimada, então o valor é armazenado como informação própria do projeto.
- **CA-RF-016-01:** Dado um projeto, quando o gestor informa a quantidade de dias de serviço necessários, então esse valor fica disponível para o planejamento.
- **CA-RF-017-01:** Dado um gestor autorizado, quando cadastra projetos em períodos simultâneos e localidades diferentes, então ambos podem ser consultados sem que um cadastro substitua o outro.
- **CA-RF-018-01:** Antes de tratar divergência entre data de início, data de término e duração estimada, a documentação deve definir qual informação prevalece ou como o gestor resolve a divergência.

### Calendário e alocações

- **CA-RF-019-01:** Dado um intervalo entre duas datas, quando o sistema exibe sua duração de planejamento, então utiliza dias inteiros de calendário.
- **CA-RF-020-01:** Dado um planejamento que atravessa dias úteis, fins de semana ou feriados, então cada data é considerada como um dia de calendário, sem exclusão automática.
- **CA-RF-021-01:** Dado um gestor autorizado, quando seleciona uma equipe, um projeto e o período da alocação e confirma dados válidos, então uma alocação é criada.
- **CA-RF-022-01:** Dado uma alocação existente, quando o gestor consulta seu período, então o sistema exibe sua duração em dias inteiros.
- **CA-RF-023-01:** Dado uma alocação existente e um gestor autorizado, quando ele altera o período ou os dados permitidos e confirma, então a alocação é atualizada.
- **CA-RF-024-01:** Antes de confirmar uma alocação, o gestor consegue consultar os compromissos e a disponibilidade cadastrada da equipe para o período.
- **CA-RF-025-01:** Dado um compromisso sobreposto da mesma equipe, quando o gestor analisa uma nova alocação, então o sistema identifica a incompatibilidade e informa o compromisso relacionado.
- **CA-RF-026-01:** A documentação de regras deve classificar cada tipo de incompatibilidade como bloqueio ou alerta com possibilidade de confirmação antes que o comportamento final seja implementado.
- **CA-RF-027-01:** Quando uma alocação é criada ou editada, o sistema mantém a equipe como unidade inteira e não cria uma divisão automática entre projetos.

### Transporte e deslocamento

- **CA-RF-028-01:** Dado um gestor autorizado, quando informa os dados de um deslocamento e confirma, então o deslocamento aparece como evento próprio no planejamento.
- **CA-RF-029-01:** Dado um deslocamento cadastrado, quando seus dados são consultados, então equipe, origem, destino, data de início e duração em dias inteiros são apresentados.
- **CA-RF-030-01:** Dado um deslocamento cadastrado, quando o gestor analisa compromissos no seu intervalo, então o transporte é apresentado como compromisso da equipe.
- **CA-RF-031-01:** Dado um deslocamento que termina depois do início de um serviço, quando o gestor analisa o planejamento, então o sistema identifica a ordem temporal incompatível.
- **CA-RF-032-01:** Antes de bloquear ou permitir o próximo serviço no dia de chegada, a documentação deve definir se a equipe fica disponível nesse dia e se a situação será bloqueio ou alerta.
- **CA-RF-033-01:** Quando um deslocamento é cadastrado, o sistema não exige cálculo automático de rota, distância ou tempo.

### Compatibilidade e conflitos

- **CA-RF-034-01:** Dado um período informado, quando o gestor analisa uma alocação, então o sistema apresenta a disponibilidade cadastrada da equipe para esse período.
- **CA-RF-035-01:** Dado um projeto ou transporte existente no período, quando o gestor analisa uma nova alocação, então o compromisso é considerado na verificação.
- **CA-RF-036-01:** Dado um projeto com competências ou qualificações necessárias, quando uma equipe é avaliada, então o sistema informa se os requisitos cadastrados são atendidos.
- **CA-RF-037-01:** Quando uma incompatibilidade é identificada, o resultado classifica o motivo entre conflito de período, indisponibilidade, competência ou transporte, conforme as regras definidas.
- **CA-RF-038-01:** Para cada incompatibilidade exibida, o gestor consegue consultar uma explicação relacionada aos dados que originaram o resultado.
- **CA-RF-039-01:** Antes da implementação de casos com múltiplas etapas, localidades ou sobreposições especiais, as regras correspondentes devem estar documentadas e aprovadas.

### Sugestões de alocação

- **CA-RF-040-01:** Quando a funcionalidade SHOULD estiver priorizada e houver dados suficientes, o sistema pode apresentar uma ou mais equipes candidatas para o período e projeto solicitados.
- **CA-RF-041-01:** Para cada sugestão apresentada, o resultado identifica quais dados de disponibilidade, período, competências, compromissos e transporte foram considerados.
- **CA-RF-042-01:** Para cada sugestão apresentada, o gestor consegue consultar uma justificativa compreensível.
- **CA-RF-043-01:** Quando o gestor não confirma uma sugestão, o planejamento permanece inalterado.
- **CA-RF-043-02:** Quando o gestor confirma uma sugestão autorizada, a alteração correspondente é registrada no planejamento.
- **CA-RF-044-01:** O sistema não aplica sugestões sem confirmação explícita e não executa otimização automática complexa no MVP.

### Visualizações e histórico

- **CA-RF-045-01:** Dado que existam alocações, o gestor consegue visualizá-las em um calendário.
- **CA-RF-046-01:** Dado um período consultado, o sistema diferencia os recursos disponíveis dos ocupados com base nos compromissos cadastrados e nas regras aprovadas.
- **CA-RF-047-01:** Dado um planejamento com projetos, transportes, alocações e conflitos, o gestor consegue visualizar esses elementos e as alocações de equipes e funcionários por projeto.
- **CA-RF-048-01:** Quando uma alteração relevante é confirmada, o histórico registra o responsável e a data e horário da alteração.
- **CA-RF-049-01:** Antes de fechar métricas e metas do dashboard, a documentação deve identificar cada métrica, sua definição, sua fonte e sua meta, quando aplicável.

## 6. Requisitos não funcionais

### Arquitetura e integração

| ID | Requisito | Status | Origem |
|---|---|---|---|
| RNF-001 | O backend deve ser desenvolvido desde o início como um conjunto de microsserviços, sem monólito inicial ou monólito modular como etapa obrigatória. | Confirmado | Brief, seção 19 |
| RNF-002 | Os limites e responsabilidades dos microsserviços devem ser definidos a partir dos domínios de negócio, evitando serviços abrangentes demais ou fragmentação artificial. | Confirmado/Pendente | Brief, seções 19 e 25 |
| RNF-003 | Os serviços devem possuir contratos explícitos de comunicação por APIs REST, com versionamento e responsabilidades documentados. | Confirmado/Pendente | Brief, seções 18, 19 e 25 |
| RNF-004 | A persistência e a propriedade dos dados devem ter responsabilidades claramente delimitadas entre os serviços. | Confirmado/Pendente | Brief, seção 19 |
| RNF-005 | A comunicação entre serviços deve considerar autenticação, autorização, tratamento de falhas e indisponibilidade. | Confirmado/Pendente | Brief, seção 19 |
| RNF-006 | A solução deve incluir observabilidade e rastreabilidade suficientes para acompanhar operações distribuídas. | Confirmado/Pendente | Brief, seção 19 |
| RNF-007 | Micro Frontends devem ser avaliados separadamente e não são obrigatórios para o MVP. | Confirmado | Brief, seção 20 |

### Tecnologias e ambiente

| ID | Requisito | Status | Origem |
|---|---|---|---|
| RNF-008 | O frontend deve utilizar React, JavaScript e CSS. | Confirmado | Brief, seção 18 |
| RNF-009 | O backend deve utilizar Java, Spring Boot e APIs REST. | Confirmado | Brief, seção 18 |
| RNF-010 | A persistência deve utilizar PostgreSQL. | Confirmado | Brief, seção 18 |
| RNF-011 | Os serviços e dependências locais devem ser executáveis com Docker e Docker Compose desde o início do desenvolvimento. | Confirmado | Brief, seção 21 |
| RNF-012 | O ambiente local deve ser simples de configurar, reproduzir, iniciar e interromper. | Confirmado | Brief, seção 21 |
| RNF-013 | A configuração local deve contemplar comunicação entre containers, dependências, variáveis de ambiente e persistência do PostgreSQL. | Confirmado | Brief, seção 21 |
| RNF-014 | TypeScript não deve ser introduzido como tecnologia-alvo do projeto. | Confirmado | Brief, seção 18 |

### Segurança, privacidade e auditoria

| ID | Requisito | Status | Origem |
|---|---|---|---|
| RNF-015 | As APIs devem validar entradas e proteger operações conforme o controle de acesso definido. | Confirmado/Pendente | Brief, seções 5 e 16 |
| RNF-016 | O sistema deve evitar coletar dados pessoais desnecessários e limitar os dados individuais de funcionários ao necessário para o planejamento aprovado. | Confirmado | Brief, seções 7, 16 e 24 |
| RNF-017 | O desenvolvimento deve observar os princípios aplicáveis da LGPD. | Confirmado | Brief, seção 16 |
| RNF-018 | As operações relevantes devem permitir identificar o responsável e o momento da alteração. | Confirmado | Brief, seção 16 |
| RNF-019 | A segregação futura entre organizações deve ser considerada antes de fechar o modelo de dados e as regras de acesso. | Pendente | Brief, seções 17 e 25 |

### Qualidade e manutenção

| ID | Requisito | Status | Origem |
|---|---|---|---|
| RNF-020 | O código deve utilizar nomenclatura clara, responsabilidades bem definidas e organização coerente. | Confirmado | Brief, seção 22 |
| RNF-021 | A solução deve manter baixo acoplamento entre serviços e separar apresentação, regras de negócio e persistência. | Confirmado | Brief, seção 22 |
| RNF-022 | Devem ser evitadas abstrações desnecessárias, fragmentação excessiva, arquivos excessivamente grandes e complexidade sem justificativa. | Confirmado | Brief, seção 22 |
| RNF-023 | Cada serviço deve possuir testes úteis e relevantes para suas responsabilidades, incluindo testes de integração e comunicação quando aplicáveis. | Confirmado/Pendente | Brief, seções 19 e 22 |
| RNF-024 | A documentação técnica deve ser objetiva, útil e mantida junto das decisões relevantes. | Confirmado | Brief, seção 22 |
| RNF-025 | Não deve ser antecipada infraestrutura complexa de produção durante a primeira etapa. | Confirmado | Brief, seções 21 e 22 |

## 7. Critérios de verificação não funcionais

Quando a decisão já é suficiente, os critérios abaixo permitem verificação. Quando depende de arquitetura futura, o critério permanece pendente.

- **CV-RNF-001-01:** A documentação de arquitetura registra microsserviços como arquitetura inicial e não registra monólito como etapa obrigatória.
- **CV-RNF-002-01:** Pendente até a documentação arquitetural definir os domínios, limites e responsabilidades dos serviços.
- **CV-RNF-003-01:** Pendente até serem definidos os contratos, versionamento e responsabilidades das APIs.
- **CV-RNF-004-01:** Pendente até a documentação arquitetural registrar a propriedade dos dados e as responsabilidades de persistência por serviço.
- **CV-RNF-005-01:** Pendente até serem definidas as estratégias de autenticação, autorização e tratamento de falhas entre serviços.
- **CV-RNF-006-01:** Pendente até serem definidos os requisitos de observabilidade e rastreabilidade distribuída.
- **CV-RNF-007-01:** A documentação de arquitetura apresenta a avaliação de Micro Frontends separadamente, sem tratá-los como implementação obrigatória do MVP.
- **CV-RNF-008-01:** A documentação e a estrutura frontend aprovada registram React, JavaScript e CSS, sem TypeScript como tecnologia-alvo.
- **CV-RNF-009-01:** A documentação e a estrutura backend aprovada registram Java, Spring Boot e APIs REST.
- **CV-RNF-010-01:** A persistência aprovada registra PostgreSQL como banco de dados-alvo.
- **CV-RNF-011-01:** O ambiente local consegue iniciar os serviços e dependências definidos por meio de Docker e Docker Compose.
- **CV-RNF-012-01:** Uma pessoa seguindo a documentação consegue configurar, iniciar e interromper o ambiente local sem etapas não documentadas.
- **CV-RNF-013-01:** A configuração local documenta dependências, comunicação entre containers, variáveis de ambiente e persistência do PostgreSQL.
- **CV-RNF-014-01:** Não existem referências a TypeScript como tecnologia de implementação aprovada.
- **CV-RNF-015-01:** Pendente até os perfis, permissões e regras de validação de entrada serem detalhados.
- **CV-RNF-016-01:** A revisão de escopo limita os dados individuais de funcionários ao necessário para o planejamento aprovado e não inclui dados trabalhistas ou pessoais sem finalidade definida.
- **CV-RNF-017-01:** Pendente até serem definidos os requisitos de privacidade aplicáveis ao tratamento de dados do MVP.
- **CV-RNF-018-01:** Uma alteração relevante registrada permite identificar responsável e data e horário.
- **CV-RNF-019-01:** Pendente até ser decidida a representação de organizações e a segregação dos dados.
- **CV-RNF-020-01:** A revisão de cada serviço identifica nomes, responsabilidades e organização coerentes com o domínio documentado.
- **CV-RNF-021-01:** A documentação e a revisão técnica demonstram separação entre apresentação, regras de negócio e persistência e baixo acoplamento entre serviços.
- **CV-RNF-022-01:** A revisão técnica não identifica abstrações ou fragmentação sem justificativa para atender apenas a um padrão arquitetural.
- **CV-RNF-023-01:** Pendente até serem definidos os níveis mínimos de testes por serviço e de comunicação entre serviços.
- **CV-RNF-024-01:** As decisões relevantes possuem documentação objetiva e referência ao documento de origem.
- **CV-RNF-025-01:** A primeira etapa não depende de uma infraestrutura complexa de produção não documentada.

## 8. Fora do escopo do MVP

Os itens abaixo não devem ser implementados como parte do MVP:

- folha de pagamento, gestão trabalhista e coleta de dados individuais sem finalidade de planejamento;
- cálculo automático de rotas, distâncias ou tempos de deslocamento;
- otimização automática complexa;
- assistente de IA;
- folha de pagamento;
- recrutamento;
- gestão financeira;
- aplicativo mobile;
- integrações corporativas complexas;
- notificações avançadas;
- Micro Frontends como decisão de implementação obrigatória;
- infraestrutura complexa de produção.

## 9. Pendências que bloqueiam o fechamento dos requisitos

Antes de considerar este documento pronto para implementação, devem ser resolvidos:

1. Informações obrigatórias no cadastro de equipes.
2. Cadastro e verificação de competências e qualificações.
3. Tratamento de serviços que exigem mais de uma equipe.
4. Tratamento de projetos com mais de uma etapa ou localidade.
5. Regra de precedência entre data prevista de início, data prevista de término e duração estimada.
6. Regras exatas de sobreposição de datas.
7. Quais conflitos bloqueiam a alocação e quais permitem prosseguir mediante confirmação.
8. Regra de disponibilidade da equipe no dia de chegada após transporte.
9. Representação da disponibilidade inicial de uma equipe.
10. Perfis e permissões mínimos.
11. Critérios mínimos, prioridade efetiva e comportamento das sugestões `SHOULD`.
12. Inclusão ou não de `organization_id` desde o início.
13. Limites, responsabilidades e quantidade dos microsserviços.
14. Comunicação e autenticação entre serviços.
15. Organização dos dados e propriedade dos bancos por serviço.
16. Critérios de aceitação finais e regras de negócio detalhadas.
17. Identificação e cadastro de funcionários.
18. Participação de um funcionário em múltiplas equipes e funções.
19. Disponibilidade individual e suas exceções.
20. Relação entre alocação individual e alocação da equipe.
21. Possibilidade de alocação individual sem alocação prévia da equipe.
22. Deslocamento individual e sua independência do deslocamento da equipe.
23. Conflitos envolvendo pessoas em múltiplas equipes, datas, localidades e projetos.
24. Classificação de conflitos como bloqueios ou alertas.

## 10. Rastreabilidade e controle de implementação

Os requisitos foram derivados das seções correspondentes do `PROJECT_BRIEF.md`. As referências “Confirmado/Pendente” indicam que o tema foi decidido em princípio, mas ainda depende de detalhamento antes da implementação.

Nenhum requisito deste documento autoriza o início da programação. A implementação permanece condicionada à resolução das pendências, à definição das regras de negócio e à autorização explícita do responsável pelo projeto.

## 11. Próximos passos documentais

1. Revisar os requisitos e critérios de aceitação com as partes interessadas.
2. Resolver as pendências do Project Brief e deste documento.
3. Criar `BUSINESS_RULES.md` somente quando essa etapa for autorizada.
4. Definir modelos de domínio, contratos e fronteiras dos microsserviços.
5. Validar os critérios de aceitação finais.
6. Somente depois, autorizar a preparação da implementação.
