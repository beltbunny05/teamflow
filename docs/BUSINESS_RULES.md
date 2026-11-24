# TeamFlow — Business Rules

**Status:** Draft inicial para validação  
**Data:** 2026-11-21  
**Escopo:** MVP  
**Documentos de origem:** `PROJECT_BRIEF.md` e `REQUIREMENTS.md`  
**Implementação:** não iniciada

## 1. Identificação e conceitos fundamentais

### 1.1 Regras confirmadas

**RN-BR-001 — Equipe como unidade de alocação**  
A equipe inteira é a unidade principal de planejamento e alocação.

**RN-BR-002 — Ausência de alocação individual**  
O MVP não possui alocação, disponibilidade ou conflito operacional de funcionários individuais.

**RN-BR-003 — Projetos simultâneos**  
Projetos podem ocorrer simultaneamente, inclusive em localidades diferentes.

**RN-BR-004 — Responsabilidade do gestor**  
O gestor cadastra e atualiza os dados operacionais e mantém a decisão final sobre as alocações.

### 1.2 Regras que precisam de detalhamento

- O conjunto exato de entidades, estados e transições do planejamento ainda não foi fechado.
- Os perfis mínimos e as permissões correspondentes ainda precisam ser definidos.
- A representação de múltiplas organizações ainda depende de decisão sobre `organization_id` e segregação dos dados.

### 1.3 Decisões dependentes de aprovação

- Aprovar os perfis e permissões mínimos do MVP.
- Aprovar a representação de organizações no modelo operacional.

## 2. Regras de cadastro de equipes

### 2.1 Regras confirmadas

**RN-BR-005 — Identificação da equipe**  
Uma equipe deve possuir nome ou identificação.

**RN-BR-006 — Quantidade agregada**  
Uma equipe deve possuir a quantidade total de integrantes, sem exigir o cadastro dos nomes ou registros individuais.

**RN-BR-007 — Composição opcional**  
A composição por funções e respectivas quantidades pode ser registrada de forma agregada. Esse detalhamento é informativo e opcional.

**RN-BR-008 — Dados operacionais da equipe**  
Uma equipe pode possuir competências, qualificações, disponibilidade e situação atual cadastradas.

**RN-BR-009 — Proibição de divisão automática**  
O MVP não divide automaticamente uma equipe entre projetos.

### 2.2 Regras que precisam de detalhamento

- Quais campos são obrigatórios além do nome ou identificação.
- Quais valores e estados são válidos para a situação atual da equipe.
- Como registrar disponibilidade parcial, indisponibilidade ou alterações futuras da equipe.
- Como tratar a edição de uma equipe que já possui alocações ou transportes.

### 2.3 Decisões dependentes de aprovação

- Aprovar o conjunto mínimo de informações obrigatórias do cadastro.
- Aprovar os estados permitidos para a situação da equipe.

## 3. Regras de competências e qualificações

### 3.1 Regras confirmadas

**RN-BR-010 — Associação à equipe**  
Competências e qualificações podem ser associadas a uma equipe.

**RN-BR-011 — Compatibilidade com o projeto**  
As competências e qualificações cadastradas para a equipe devem ser consideradas ao avaliar sua compatibilidade com as exigências de um projeto.

**RN-BR-012 — Composição não altera cálculo**  
O detalhamento opcional da composição por funções não cria, por si só, qualquer cálculo automático adicional.

### 3.2 Regras que precisam de detalhamento

- Como competências e qualificações serão cadastradas, nomeadas e mantidas.
- Se uma exigência de projeto será obrigatória, recomendada ou apenas informativa.
- Como será feita a comparação entre uma exigência e uma competência ou qualificação da equipe.
- Como tratar níveis, validade, equivalência ou ausência de uma qualificação.

### 3.3 Decisões dependentes de aprovação

- Aprovar o vocabulário e a estrutura mínima de competências e qualificações.
- Aprovar os critérios que definem atendimento ou não atendimento de uma exigência.

## 4. Regras de cadastro de projetos

### 4.1 Regras confirmadas

**RN-BR-013 — Identificação e contexto do projeto**  
Um projeto pode possuir nome ou identificação, cliente quando aplicável, localidade e descrição do serviço.

**RN-BR-014 — Datas e duração separadas**  
Data prevista de início, data prevista de término, duração estimada e quantidade de dias de serviço são informações distintas. O sistema não deve presumir que uma seja automaticamente calculada a partir de outra.

**RN-BR-015 — Exigências do projeto**  
Um projeto pode registrar competências e qualificações necessárias.

**RN-BR-016 — Projetos simultâneos**  
O cadastro deve permitir projetos simultâneos em localidades diferentes.

### 4.2 Regras que precisam de detalhamento

- Como tratar projetos com mais de uma equipe.
- Como tratar projetos com mais de uma etapa.
- Como tratar projetos com mais de uma localidade.
- Como relacionar a quantidade de dias de serviço ao projeto e à alocação.
- Quais validações mínimas são necessárias para criar ou editar um projeto.

### 4.3 Decisões dependentes de aprovação

- Aprovar a precedência entre datas previstas e duração estimada quando houver divergência.
- Aprovar se uma quantidade de dias de serviço pertence ao projeto, ao serviço ou à alocação.
- Aprovar a forma de representar múltiplas equipes, etapas ou localidades.

## 5. Regras de dias de serviço

### 5.1 Regras confirmadas

**RN-BR-017 — Significado dos dias de serviço**  
Dias de serviço representam a quantidade de dias necessária para executar o serviço de um projeto.

**RN-BR-018 — Informação manual**  
O gestor informa manualmente a duração estimada do serviço com base em sua avaliação operacional.

**RN-BR-019 — Dias inteiros**  
Os dias de serviço são expressos em dias inteiros.

**RN-BR-020 — Sem cálculo de produção**  
O sistema não calcula produtividade, instalações por dia, produção por integrante ou capacidade produtiva para determinar a duração do serviço.

### 5.2 Regras que precisam de detalhamento

- Como tratar a diferença entre duração estimada e quantidade de dias de serviço necessários quando ambas forem informadas.
- Como tratar alterações na duração depois de uma alocação confirmada.
- Como validar valores nulos, zero ou negativos.

### 5.3 Decisões dependentes de aprovação

- Aprovar a regra de precedência quando datas e duração estimada divergirem.
- Aprovar as regras de alteração dos dias de serviço após confirmação da alocação.

## 6. Regras de calendário

### 6.1 Regras confirmadas

**RN-BR-021 — Unidade de calendário**  
O planejamento utiliza dias inteiros de calendário.

**RN-BR-022 — Equivalência das datas**  
Segundas-feiras, sábados, domingos e feriados contam como dias normais. Não há diferenciação entre dias úteis, fins de semana e feriados no MVP.

**RN-BR-023 — Aplicação uniforme**  
A regra de dias de calendário aplica-se a projetos, alocações, transportes e disponibilidade.

**RN-BR-024 — Período comprometido**  
As datas inicial e final de uma alocação representam o período em que a equipe está comprometida com o serviço.

### 6.2 Regras que precisam de detalhamento

- Como contar o intervalo quando as datas inicial e final forem iguais.
- Como validar uma data final anterior à data inicial.
- Como exibir ou tratar datas de fusos horários diferentes, caso esse cenário seja necessário.

### 6.3 Decisões dependentes de aprovação

- Aprovar a regra de inclusão das datas inicial e final no intervalo de uma alocação.
- Aprovar o tratamento de qualquer necessidade futura de múltiplos calendários.

## 7. Regras de alocação

### 7.1 Regras confirmadas

**RN-BR-025 — Unidade da alocação**  
Uma alocação representa o compromisso de uma equipe com um projeto durante determinado período.

**RN-BR-026 — Dados mínimos**  
Uma alocação deve relacionar equipe, projeto e período de alocação.

**RN-BR-027 — Operação manual**  
O gestor cria e edita alocações manualmente, com apoio do sistema para consultar disponibilidade e identificar conflitos.

**RN-BR-028 — Equipe inteira**  
Uma alocação compromete a equipe inteira; o MVP não fraciona automaticamente a equipe.

**RN-BR-029 — Confirmação do gestor**  
Uma alocação ou alteração de planejamento só é efetivada após confirmação do gestor.

### 7.2 Regras que precisam de detalhamento

- Quais dados podem ser editados depois que a alocação foi confirmada.
- Como tratar duas alocações da mesma equipe com períodos parcialmente sobrepostos.
- Quais conflitos impedem a confirmação e quais permitem prosseguir após confirmação adicional.
- Como tratar alocações de projetos com múltiplas etapas ou localidades.

### 7.3 Decisões dependentes de aprovação

- Aprovar a classificação de cada conflito como bloqueio ou alerta.
- Aprovar as regras para alteração e cancelamento de alocações confirmadas.

## 8. Regras de disponibilidade

### 8.1 Regras confirmadas

**RN-BR-030 — Disponibilidade no nível de equipe**  
A disponibilidade é controlada no nível da equipe, não no nível de funcionários individuais.

**RN-BR-031 — Consulta antes da confirmação**  
O gestor deve poder consultar a disponibilidade da equipe antes de confirmar uma alocação.

**RN-BR-032 — Compromissos considerados**  
Projetos alocados e eventos de transporte são compromissos que devem ser considerados na análise da disponibilidade.

### 8.2 Regras que precisam de detalhamento

- Como cadastrar a disponibilidade inicial de uma equipe.
- Como representar disponibilidade parcial ou mudanças futuras.
- Como representar ausências da equipe.
- Como tratar exceções autorizadas de disponibilidade.
- Como a disponibilidade é afetada por um transporte no dia de chegada.

### 8.3 Decisões dependentes de aprovação

- Aprovar o modelo de disponibilidade da equipe.
- Aprovar as exceções permitidas e quem pode autorizá-las.
- Aprovar se a equipe fica disponível no dia de chegada do transporte.

## 9. Regras de transporte e deslocamento

### 9.1 Regras confirmadas

**RN-BR-033 — Evento próprio**  
O transporte é um evento próprio do planejamento, separado da alocação do serviço.

**RN-BR-034 — Dados do transporte**  
Um deslocamento registra equipe, localidade de origem, localidade de destino, data de início e duração estimada em dias inteiros.

**RN-BR-035 — Duração manual**  
A duração do transporte é informada manualmente pelo gestor.

**RN-BR-036 — Indisponibilidade durante deslocamento**  
A equipe fica indisponível para outros projetos durante o período do deslocamento.

**RN-BR-037 — Chegada antes do próximo serviço**  
A equipe não pode iniciar outro serviço antes de concluir o deslocamento e chegar ao destino.

**RN-BR-038 — Sem cálculo de rota**  
O MVP não calcula automaticamente rotas, distâncias ou tempos de deslocamento e não depende de serviços de mapas.

### 9.2 Regras que precisam de detalhamento

- Como contar o dia de início e o dia de término do transporte.
- Se o dia de chegada deixa a equipe disponível para iniciar outro serviço.
- Como tratar múltiplos deslocamentos consecutivos.
- Como tratar deslocamentos entre etapas ou localidades do mesmo projeto.
- Como tratar alterações na duração de um transporte já relacionado a uma alocação.

### 9.3 Decisões dependentes de aprovação

- Aprovar a disponibilidade da equipe no dia de chegada.
- Aprovar a regra de bloqueio ou alerta para um serviço que começa no dia de chegada.
- Aprovar as regras para múltiplos deslocamentos e deslocamentos entre etapas.

## 10. Regras de conflitos e incompatibilidades

### 10.1 Regras confirmadas

**RN-BR-039 — Identificação de conflitos**  
O sistema identifica situações que impeçam ou comprometam uma alocação.

**RN-BR-040 — Tipos iniciais**  
As situações identificadas incluem equipe comprometida com outro projeto, equipe em deslocamento, equipe sem disponibilidade e competências necessárias não atendidas.

**RN-BR-041 — Explicação**  
O sistema apresenta ao gestor o motivo de cada incompatibilidade identificada.

**RN-BR-042 — Não confundir identificação com bloqueio**  
A identificação de um conflito não define, por si só, se a alocação será bloqueada. Essa consequência depende de regra ainda pendente.

### 10.2 Regras que precisam de detalhamento

- Como definir sobreposição entre períodos de alocação.
- Como diferenciar conflito de período, indisponibilidade, competência e transporte em todos os casos.
- Como tratar conflitos entre dois eventos de transporte.
- Como tratar múltiplas etapas ou localidades.
- Como registrar e explicar conflitos simultâneos.

### 10.3 Decisões dependentes de aprovação

- Aprovar quais conflitos bloqueiam uma alocação.
- Aprovar quais conflitos podem ser aceitos mediante confirmação do gestor.
- Aprovar se deve existir justificativa adicional para aceitar um alerta.

## 11. Regras de sugestões de alocação

### 11.1 Regras confirmadas

**RN-BR-043 — Funcionalidade assistida**  
O sistema pode apresentar sugestões para ajudar o gestor a encontrar equipes disponíveis. A funcionalidade tem prioridade `SHOULD` no documento de requisitos.

**RN-BR-044 — Dados considerados**  
As sugestões devem considerar dados reais cadastrados, como disponibilidade, período solicitado, competências, qualificações, compromissos e deslocamentos.

**RN-BR-045 — Justificativa**  
As sugestões devem apresentar justificativas compreensíveis.

**RN-BR-046 — Confirmação manual**  
O gestor seleciona e confirma a sugestão; a sugestão não altera o planejamento automaticamente.

**RN-BR-047 — Sem otimização complexa**  
O MVP não executa otimização automática complexa.

### 11.2 Regras que precisam de detalhamento

- Critérios mínimos para uma equipe aparecer como candidata.
- Forma de ordenar ou priorizar sugestões.
- Comportamento quando nenhuma equipe atender aos critérios.
- Dados que devem aparecer na justificativa.
- Tratamento de sugestões desatualizadas depois de uma alteração no planejamento.

### 11.3 Decisões dependentes de aprovação

- Aprovar a prioridade efetiva da funcionalidade `SHOULD` no MVP.
- Aprovar os critérios mínimos de seleção e ordenação.
- Aprovar se uma sugestão parcialmente compatível pode ser exibida como alerta.

## 12. Regras de confirmação e alterações

### 12.1 Regras confirmadas

**RN-BR-048 — Confirmação explícita**  
Nenhuma alteração de planejamento deve ser executada automaticamente sem confirmação do gestor.

**RN-BR-049 — Alocação manual**  
As alocações são manuais, mesmo quando o sistema oferece apoio ou sugestão.

**RN-BR-050 — Edição pelo gestor**  
O gestor pode cadastrar e atualizar equipes, projetos e alocações conforme as permissões definidas.

### 12.2 Regras que precisam de detalhamento

- Quais alterações exigem nova verificação de conflitos.
- Se editar um projeto deve revalidar alocações já existentes.
- Como tratar uma confirmação quando existem alertas não bloqueantes.
- Como cancelar, desfazer ou corrigir uma alteração confirmada.

### 12.3 Decisões dependentes de aprovação

- Aprovar o fluxo de confirmação quando houver alertas.
- Aprovar as regras de edição, cancelamento e correção de alterações confirmadas.

## 13. Regras de histórico e auditoria

### 13.1 Regras confirmadas

**RN-BR-051 — Alterações relevantes**  
Alterações relevantes de planejamento devem ser registradas.

**RN-BR-052 — Responsável e momento**  
O registro deve identificar o responsável pela alteração e a data e horário em que ela ocorreu.

**RN-BR-053 — Validação de dados**  
Os dados informados devem ser validados antes de serem aceitos pelo sistema.

### 13.2 Regras que precisam de detalhamento

- Quais alterações são consideradas relevantes.
- Quais dados anteriores e posteriores devem ser preservados.
- Por quanto tempo o histórico deve ser mantido.
- Quem pode consultar o histórico.
- Como tratar correções administrativas e alterações rejeitadas.

### 13.3 Decisões dependentes de aprovação

- Aprovar o nível mínimo de detalhe do histórico.
- Aprovar retenção, consulta e tratamento de registros de auditoria.

## 14. Pendências e decisões necessárias

### 14.1 Pendências documentadas

As seguintes decisões continuam abertas e não devem ser inferidas:

1. Informações obrigatórias e estados válidos no cadastro de equipes.
2. Cadastro, níveis, validade e comparação de competências e qualificações.
3. Projetos que exigem mais de uma equipe.
4. Projetos com múltiplas etapas ou localidades.
5. Precedência entre data prevista de início, data prevista de término, duração estimada e dias de serviço.
6. Inclusão das datas inicial e final no intervalo de alocação.
7. Disponibilidade parcial, ausências e alterações futuras da equipe.
8. Exceções de disponibilidade e respectivas autorizações.
9. Regra para a disponibilidade da equipe no dia de chegada do transporte.
10. Tipos de conflito que bloqueiam uma alocação.
11. Tipos de conflito que permitem prosseguir mediante confirmação.
12. Necessidade de justificativa adicional para aceitar alertas.
13. Critérios, ordenação e prioridade efetiva das sugestões `SHOULD`.
14. Perfis e permissões mínimos.
15. Representação de organizações e eventual `organization_id`.
16. Limites, responsabilidades e quantidade dos microsserviços.
17. Comunicação, autenticação e propriedade dos dados entre serviços.

### 14.2 Decisões que dependem de aprovação

O responsável pelo produto deve aprovar, no mínimo:

- a precedência entre datas e duração estimada;
- as regras de início e término dos intervalos de alocação;
- o tratamento do dia de chegada do transporte;
- a classificação de conflitos como bloqueio ou alerta;
- o fluxo de confirmação quando houver alertas;
- o tratamento de projetos com múltiplas equipes, etapas ou localidades;
- o modelo de disponibilidade e suas exceções;
- os critérios mínimos e a prioridade das sugestões;
- os perfis e permissões do MVP;
- o nível de detalhe e retenção do histórico.

### 14.3 Rastreabilidade com `REQUIREMENTS.md`

| Regra ou grupo de regras | Requisitos relacionados | Situação |
|---|---|---|
| Equipe como unidade e ausência de cadastro individual | RF-004 a RF-010, RF-027 | Correspondente |
| Competências e qualificações da equipe e do projeto | RF-007, RF-012, RF-036 | Correspondente; critérios de comparação pendentes |
| Cadastro e alterações manuais pelo gestor | RF-004, RF-009, RF-011, RF-021, RF-023, RF-028, RF-043 | Correspondente; permissões e confirmações pendentes |
| Cadastro de projetos e datas separadas | RF-011 a RF-018 | Correspondente; precedência pendente |
| Dias de serviço e duração informada | RF-015, RF-016 | Correspondente; relação entre duração e dias pendente |
| Calendário e período de alocação | RF-019 a RF-024 | Correspondente; regras de intervalo pendentes |
| Disponibilidade da equipe | RF-008, RF-024, RF-034 | Correspondente; modelo de disponibilidade pendente |
| Transporte e chegada | RF-028 a RF-033 | Correspondente; dia de chegada pendente |
| Conflitos e explicações | RF-025, RF-034 a RF-039 | Correspondente; bloqueio versus alerta pendente |
| Sugestões assistidas | RF-040 a RF-044 | Correspondente; prioridade `SHOULD` e critérios pendentes |
| Confirmação e alterações | RF-021, RF-023, RF-043, RF-044 | Correspondente; regras de confirmação com alertas pendentes |
| Histórico e auditoria | RF-048, RNF-018 | Correspondente; escopo do histórico pendente |
| Validação dos dados informados | RNF-015 | Correspondente; regras de validação pendentes |

### 14.4 Regras sem requisito correspondente completo

Não foi identificada regra operacional confirmada sem qualquer referência no `REQUIREMENTS.md`. A regra RN-BR-020 possui referência apenas como restrição de escopo nas premissas do documento, sem um requisito funcional dedicado. Alguns outros detalhes permanecem apenas parcialmente cobertos e dependem de refinamento:

- RN-BR-020: a proibição de cálculos de produtividade, instalações por dia ou capacidade produtiva está registrada como limite de escopo, não como requisito funcional independente;
- equipes diferentes podem ter durações de transporte diferentes para trajetos semelhantes;
- a forma de contar início e término de cada evento de transporte;
- a diferença entre duração estimada e quantidade de dias de serviço quando ambas forem informadas;
- retenção e nível de detalhe do histórico;
- exceções de disponibilidade.

Esses pontos estão registrados como pendências neste documento e não devem ser convertidos em comportamento implícito.
