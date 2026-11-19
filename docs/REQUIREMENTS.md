# TeamFlow — Requirements

**Status:** Draft inicial derivado do `PROJECT_BRIEF.md`  
**Data:** 2026-11-19  
**Escopo:** MVP  
**Implementação:** não iniciada

## 1. Objetivo

Este documento organiza os requisitos funcionais e não funcionais iniciais do TeamFlow a partir do `PROJECT_BRIEF.md` validado.

Ele não substitui o Project Brief nem define regras de negócio detalhadas. Regras ainda pendentes permanecem identificadas como pendências e não devem ser inferidas durante a implementação.

## 2. Convenções

- **Confirmado:** decisão suficientemente estabelecida no Project Brief para orientar o refinamento do requisito.
- **Pendente:** depende de decisão adicional antes de ser considerado fechado.
- **Fora do MVP:** explicitamente excluído nesta etapa.
- **MUST:** requisito necessário para o escopo atual.
- **SHOULD:** orientação importante, sujeita a refinamento.

Os requisitos abaixo são iniciais. Cada requisito deverá ser refinado com critérios de aceitação antes da implementação.

## 3. Premissas de escopo

- A unidade principal de planejamento e alocação é a equipe inteira.
- O MVP não terá cadastro, disponibilidade, alocação ou conflito operacional de funcionários individuais.
- O planejamento usará dias inteiros de calendário; dias úteis, fins de semana e feriados têm o mesmo peso.
- O gestor cadastra e atualiza os dados operacionais e mantém a decisão final sobre alocações.
- O deslocamento é cadastrado manualmente como evento próprio do planejamento.
- O MVP não dependerá de integrações externas.
- O MVP não calculará produtividade diária, quantidade produzida, capacidade produtiva ou rotas automaticamente.

## 4. Requisitos funcionais

### Autenticação e acesso

| ID | Requisito | Status | Origem |
|---|---|---|---|
| RF-001 | O sistema deve autenticar usuários antes de permitir acesso às funcionalidades protegidas. | Confirmado | Brief, seção 5 e seção 16 |
| RF-002 | O sistema deve aplicar controle básico de acesso às funcionalidades e dados. | Confirmado | Brief, seção 5 e seção 16 |
| RF-003 | O sistema deve manter os perfis e permissões simples, sem introduzir uma estrutura complexa antes de necessidade comprovada. | Pendente | Brief, seção 5 |

### Equipes

| ID | Requisito | Status | Origem |
|---|---|---|---|
| RF-004 | O gestor deve poder cadastrar uma equipe com nome ou identificação. | Confirmado | Brief, seções 6 e 7 |
| RF-005 | O gestor deve poder informar a quantidade total de integrantes da equipe sem cadastrar registros individuais. | Confirmado | Brief, seção 7 |
| RF-006 | O sistema deve permitir registrar composição agregada por funções e quantidades, quando o gestor optar por detalhá-la. | Confirmado | Brief, seções 6 e 7 |
| RF-007 | O sistema deve permitir registrar competências e qualificações associadas à equipe. | Confirmado | Brief, seção 7 |
| RF-008 | O sistema deve permitir registrar e atualizar a disponibilidade e a situação atual da equipe. | Confirmado | Brief, seção 7 |
| RF-009 | O gestor deve poder editar os dados da equipe. | Confirmado | Brief, seção 6 |
| RF-010 | O sistema não deve exigir cadastro individual de funcionários para cadastrar, consultar ou alocar uma equipe. | Confirmado | Brief, seção 7 |

### Projetos

| ID | Requisito | Status | Origem |
|---|---|---|---|
| RF-011 | O gestor deve poder cadastrar e editar projetos. | Confirmado | Brief, seções 6 e 8 |
| RF-012 | Um projeto deve poder registrar nome ou identificação, cliente quando aplicável, localidade, descrição do serviço e competências ou qualificações necessárias. | Confirmado | Brief, seção 8 |
| RF-013 | Um projeto deve poder registrar data prevista de início, data prevista de término ou duração estimada e quantidade de dias de serviço necessários. | Confirmado | Brief, seções 8 e 9 |
| RF-014 | O sistema deve permitir cadastrar projetos simultâneos em diferentes localidades. | Confirmado | Brief, seções 2, 6 e 8 |

### Calendário e alocações

| ID | Requisito | Status | Origem |
|---|---|---|---|
| RF-015 | O sistema deve utilizar dias inteiros de calendário como unidade de planejamento. | Confirmado | Brief, seções 6, 9 e 10 |
| RF-016 | O sistema deve considerar segunda-feira, sábado, domingo e feriados como dias equivalentes no planejamento do MVP. | Confirmado | Brief, seção 10 |
| RF-017 | O gestor deve poder criar uma alocação selecionando equipe, projeto, data de início e data de término ou duração. | Confirmado | Brief, seções 6, 9 e 11 |
| RF-018 | O sistema deve exibir a duração da alocação em dias inteiros. | Confirmado | Brief, seção 11 |
| RF-019 | O gestor deve poder editar uma alocação existente. | Confirmado | Brief, seção 6 |
| RF-020 | O sistema deve permitir visualizar a disponibilidade da equipe antes da confirmação da alocação. | Confirmado | Brief, seção 11 |
| RF-021 | O sistema deve impedir ou sinalizar uma alocação incompatível com outro compromisso da mesma equipe no período sobreposto, conforme a regra de negócio que será detalhada. | Confirmado/Pendente | Brief, seções 6, 11 e 13 |
| RF-022 | O sistema não deve dividir automaticamente uma equipe entre projetos no MVP. | Confirmado | Brief, seções 6, 7 e 24 |

### Transporte e deslocamento

| ID | Requisito | Status | Origem |
|---|---|---|---|
| RF-023 | O gestor deve poder cadastrar um deslocamento como evento próprio do planejamento. | Confirmado | Brief, seção 12 |
| RF-024 | Um deslocamento deve registrar equipe, localidade de origem, localidade de destino, data de início e duração estimada em dias inteiros. | Confirmado | Brief, seção 12 |
| RF-025 | O sistema deve considerar a equipe indisponível durante o período do deslocamento. | Confirmado | Brief, seção 12 |
| RF-026 | O sistema deve impedir ou sinalizar o início de um próximo serviço antes da conclusão do deslocamento e da chegada ao destino. | Confirmado | Brief, seção 12 |
| RF-027 | O sistema não deve calcular automaticamente rotas, distâncias ou tempos de deslocamento no MVP. | Confirmado | Brief, seção 12 |

### Compatibilidade e conflitos

| ID | Requisito | Status | Origem |
|---|---|---|---|
| RF-028 | O sistema deve verificar disponibilidade da equipe ao analisar uma alocação. | Confirmado | Brief, seção 13 |
| RF-029 | O sistema deve considerar compromissos de projetos e eventos de transporte na análise de conflitos. | Confirmado | Brief, seções 11–13 |
| RF-030 | O sistema deve verificar se as competências e qualificações necessárias do projeto são atendidas pela equipe. | Confirmado | Brief, seções 13 e 14 |
| RF-031 | O sistema deve identificar e diferenciar conflito de período, indisponibilidade, incompatibilidade de competências e conflito com transporte. | Confirmado | Brief, seção 13 |
| RF-032 | O sistema deve explicar ao gestor o motivo de cada incompatibilidade identificada. | Confirmado | Brief, seção 13 |
| RF-033 | As regras detalhadas de sobreposição, múltiplas etapas e múltiplas localidades devem ser documentadas antes da implementação correspondente. | Pendente | Brief, seções 13 e 25 |

### Sugestões de alocação

| ID | Requisito | Status | Origem |
|---|---|---|---|
| RF-034 | O sistema poderá apresentar sugestões de equipes para um período e projeto solicitados. | Confirmado | Brief, seção 14 |
| RF-035 | As sugestões devem considerar disponibilidade, período solicitado, competências, qualificações, compromissos e deslocamentos cadastrados. | Confirmado | Brief, seção 14 |
| RF-036 | Cada sugestão deve apresentar justificativa compreensível baseada nos dados cadastrados. | Confirmado | Brief, seção 14 |
| RF-037 | O gestor deve selecionar e confirmar uma sugestão antes que ela altere o planejamento. | Confirmado | Brief, seções 4, 6 e 14 |
| RF-038 | O sistema não deve executar otimização automática complexa no MVP. | Confirmado | Brief, seções 14 e 24 |

### Visualizações e histórico

| ID | Requisito | Status | Origem |
|---|---|---|---|
| RF-039 | O sistema deve oferecer uma visualização de calendário das alocações. | Confirmado | Brief, seção 15 |
| RF-040 | O sistema deve permitir consultar equipes disponíveis e ocupadas. | Confirmado | Brief, seção 15 |
| RF-041 | O sistema deve permitir visualizar projetos em andamento, períodos de transporte, dias de serviço alocados por equipe e conflitos identificados. | Confirmado | Brief, seção 15 |
| RF-042 | O sistema deve registrar alterações relevantes, identificando responsável e data e horário da alteração. | Confirmado | Brief, seção 16 |
| RF-043 | As métricas definitivas e suas metas devem ser definidas antes de serem tratadas como requisitos fechados de dashboard. | Pendente | Brief, seção 15 |

## 5. Requisitos não funcionais

### Arquitetura e integração

| ID | Requisito | Status | Origem |
|---|---|---|---|
| RNF-001 | O backend deve ser desenvolvido desde o início como um conjunto de microsserviços, sem monólito inicial ou monólito modular como etapa obrigatória. | Confirmado | Brief, seção 19 |
| RNF-002 | Os limites e responsabilidades dos microsserviços devem ser definidos a partir dos domínios de negócio, evitando tanto serviços abrangentes demais quanto fragmentação artificial. | Confirmado/Pendente | Brief, seções 19 e 25 |
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
| RNF-016 | O sistema deve evitar coletar dados pessoais desnecessários, especialmente dados individuais de funcionários fora do escopo. | Confirmado | Brief, seções 7, 16 e 24 |
| RNF-017 | O desenvolvimento deve observar os princípios aplicáveis da LGPD. | Confirmado | Brief, seção 16 |
| RNF-018 | As operações relevantes devem permitir identificar o responsável e o momento da alteração. | Confirmado | Brief, seção 16 |
| RNF-019 | A segregação futura entre organizações deve ser considerada antes de fechar o modelo de dados e as regras de acesso. | Pendente | Brief, seções 17 e 25 |

### Qualidade e manutenção

| ID | Requisito | Status | Origem |
|---|---|---|---|
| RNF-020 | O código deve utilizar nomenclatura clara, responsabilidades bem definidas e organização coerente. | Confirmado | Brief, seção 22 |
| RNF-021 | A solução deve manter baixo acoplamento entre serviços e separar apresentação, regras de negócio e persistência. | Confirmado | Brief, seção 22 |
| RNF-022 | Devem ser evitadas abstrações desnecessárias, fragmentação excessiva, arquivos excessivamente grandes e complexidade sem justificativa. | Confirmado | Brief, seção 22 |
| RNF-023 | Cada serviço deve possuir testes úteis e relevantes para suas responsabilidades, incluindo os testes de integração necessários. | Confirmado/Pendente | Brief, seções 19 e 22 |
| RNF-024 | A documentação técnica deve ser objetiva, útil e mantida junto das decisões relevantes. | Confirmado | Brief, seção 22 |
| RNF-025 | Não deve ser antecipada infraestrutura complexa de produção durante a primeira etapa. | Confirmado | Brief, seções 21 e 22 |

## 6. Fora do escopo do MVP

Os itens abaixo não devem ser implementados como parte do MVP:

- cadastro, disponibilidade, alocação ou conflitos individuais de funcionários;
- cálculo de produtividade diária, quantidade produzida ou capacidade produtiva;
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

## 7. Pendências que bloqueiam o fechamento dos requisitos

Antes de considerar este documento pronto para implementação, devem ser resolvidos:

1. Informações obrigatórias no cadastro de equipes.
2. Cadastro e verificação de competências e qualificações.
3. Tratamento de serviços que exigem mais de uma equipe.
4. Tratamento de projetos com mais de uma etapa ou localidade.
5. Regras exatas de sobreposição de datas.
6. Representação da disponibilidade inicial de uma equipe.
7. Perfis e permissões mínimos.
8. Inclusão ou não de `organization_id` desde o início.
9. Limites, responsabilidades e quantidade dos microsserviços.
10. Comunicação e autenticação entre serviços.
11. Organização dos dados e propriedade dos bancos por serviço.
12. Critérios de aceitação de cada requisito.

## 8. Rastreabilidade

Os requisitos foram derivados das seções correspondentes do `PROJECT_BRIEF.md`. As referências “Confirmado/Pendente” indicam que o tema foi decidido em princípio, mas ainda depende de detalhamento antes da implementação.

Nenhum requisito deste documento autoriza o início da programação. A implementação permanece condicionada à resolução das pendências, à definição das regras de negócio e à autorização explícita do responsável pelo projeto.

## 9. Próximos passos documentais

1. Revisar os requisitos com as partes interessadas.
2. Resolver as pendências do Project Brief e deste documento.
3. Criar `BUSINESS_RULES.md` somente quando essa etapa for autorizada.
4. Definir modelos de domínio, contratos e fronteiras dos microsserviços.
5. Definir critérios de aceitação.
6. Somente depois, autorizar a preparação da implementação.
