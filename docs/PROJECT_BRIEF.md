TeamFlow — Project Brief

Status: Validado para consolidação das regras de negócio
Data: 2026-11-24
Repositório: TeamFlow
Prazo-alvo do MVP e da primeira documentação: 2026-12-31

1. Propósito deste documento

Este documento registra o contexto, as decisões de produto e as premissas iniciais do TeamFlow. Ele orienta a elaboração dos requisitos funcionais e não funcionais, das regras de negócio, da arquitetura e da implementação do MVP.

O documento deve ser atualizado conforme as decisões forem validadas. Decisões ainda não confirmadas devem permanecer identificadas como pendências.

Validação desta versão: o planejamento é orientado por projetos, com equipes e funcionários como recursos que podem participar do planejamento. Serviços permanecem informações auxiliares dos projetos. O calendário em dias inteiros, o transporte manual, a direção tecnológica e a arquitetura inicial baseada em microsserviços continuam alinhados. As questões pendentes da seção 25 não devem ser tratadas como decisões implícitas nos requisitos.

2. Contexto do produto

TeamFlow é uma plataforma web SaaS B2B para planejamento e alocação de equipes e funcionários entre múltiplos projetos simultâneos.

O produto busca atender empresas que precisam organizar a participação de equipes e funcionários em diferentes projetos, clientes e localidades, considerando disponibilidade, competências, duração informada dos projetos e deslocamentos.

O primeiro cenário de validação considera uma empresa sazonal com aproximadamente 40 integrantes, distribuídos em equipes que executam projetos em três ou quatro cidades simultaneamente.

Esse cenário serve como referência inicial, mas o produto não deve ficar limitado a um setor específico.

3. Problema prioritário

Gestores enfrentam dificuldades para organizar equipes entre projetos simultâneos.

Entre os principais problemas estão:

* Falta de visibilidade sobre a disponibilidade das equipes.
* Sobreposição de alocações.
* Dificuldade para planejar a duração dos serviços.
* Deslocamentos entre localidades.
* Incompatibilidade entre as competências das equipes e as exigências dos projetos.
* Dificuldade para visualizar conflitos e alternativas de planejamento.

4. Objetivo do produto

Permitir que gestores cadastrem equipes e projetos, planejem alocações em um calendário e identifiquem conflitos antes de confirmar o planejamento.

O sistema deve oferecer informações claras para apoiar decisões, mantendo o gestor responsável pela aprovação das alocações.

5. Usuários e perfis iniciais

O usuário principal do MVP será o gestor responsável pelo planejamento.

O sistema deverá permitir autenticação e controle básico de acesso.

Os perfis e permissões definitivos ainda precisam ser detalhados. Não implementar uma estrutura complexa de privilégios sem necessidade comprovada.

6. Decisões de produto confirmadas

* O planejamento é orientado por projetos, não por serviços individuais.
* Serviços permanecem informações auxiliares dos projetos.
* Equipes e funcionários podem ser considerados na análise de participação em projetos.
* Um funcionário pode participar de múltiplas equipes.
* Um funcionário pode precisar de alocação individual em um projeto.
* O deslocamento de um funcionário pode ocorrer independentemente do deslocamento da equipe.
* Projetos podem acontecer simultaneamente.
* Uma equipe não pode estar alocada em dois compromissos incompatíveis no mesmo período.
* O planejamento será realizado em dias inteiros.
* Todos os dias do calendário serão considerados igualmente.
* O gestor será responsável por cadastrar e atualizar os dados.
* As alocações serão manuais, com apoio do sistema para identificar conflitos.
* O sistema poderá apresentar sugestões explicáveis, mas o gestor deverá aprová-las.
* Nenhuma alteração de planejamento será executada automaticamente sem confirmação.
* O deslocamento será cadastrado manualmente.
* O MVP não dependerá de integrações externas.

7. Modelo de equipes

As equipes continuam sendo um recurso importante do planejamento, mas não são a única unidade possível. O planejamento principal é orientado por projetos e pode considerar equipes e funcionários individualmente.

O cadastro deverá permitir registrar:

* Nome ou identificação da equipe.
* Quantidade total de integrantes.
* Composição opcional por funções e respectivas quantidades.
* Competências e qualificações.
* Disponibilidade e situação atual.

A composição por funções será informativa. O MVP não fará cálculos automáticos de produtividade com base na quantidade ou função dos integrantes.

As seguintes capacidades estão confirmadas como necessárias, mas dependem de detalhamento antes da implementação:

* Cadastro e identificação de funcionários.
* Disponibilidade individual.
* Vínculo de um funcionário com múltiplas equipes.
* Alocação individual em projetos.
* Deslocamento individual independente do deslocamento da equipe.
* Conflitos envolvendo pessoas, equipes e projetos.

O sistema não deve dividir automaticamente uma equipe sem uma regra aprovada para esse comportamento.

8. Modelo de projetos

Cada projeto deverá possuir informações suficientes para permitir seu planejamento.

Informações previstas:

* Nome ou identificação.
* Cliente, quando aplicável.
* Localidade.
* Data prevista de início.
* Data prevista de término ou duração estimada.
* Descrição do serviço.
* Competências ou qualificações necessárias.
* Quantidade de dias de serviço necessários.

As informações de serviços e seus dias permanecem auxiliares ao cadastro do projeto; não definem, por si só, a unidade principal de planejamento.

O modelo deverá permitir cadastrar projetos simultâneos em diferentes localidades.

9. Duração informada dos projetos

O planejamento é orientado por projetos. Serviços podem continuar registrados como informações auxiliares, mas não constituem a unidade principal de planejamento.

O gestor informará manualmente a duração ou o período relevante do projeto conforme as regras aprovadas.

Exemplo:

* Projeto A: duração informada de 5 dias.
* Projeto B: duração informada de 3 dias.
* Equipe ou funcionário disponível: recurso a ser analisado pelo gestor.

Ao alocar uma equipe ou funcionário no Projeto A durante cinco dias, esses dias deverão ser considerados ocupados no calendário.

O sistema não calculará produtividade diária, quantidade de instalações, produção por integrante ou capacidade produtiva.

A duração do projeto será informada pelo gestor com base em sua avaliação operacional.

10. Unidade de planejamento e calendário

O planejamento será feito em dias inteiros.

Todos os dias serão considerados equivalentes:

* Segunda-feira conta como um dia.
* Sábado conta como um dia.
* Domingo conta como um dia.
* Feriados contam como um dia.

Não haverá diferenciação entre dias úteis, finais de semana e feriados no MVP.

As regras serão aplicadas igualmente a projetos, alocações, transporte e disponibilidade.

As datas inicial e final de uma alocação deverão representar o período em que o recurso estará comprometido com o projeto. A inclusão das datas no intervalo ainda depende de decisão.

11. Alocações

Uma alocação representa o compromisso de uma equipe ou funcionário com um projeto durante determinado período.

O gestor deverá conseguir:

* Selecionar um projeto.
* Selecionar uma equipe, um funcionário ou ambos conforme o caso de uso aprovado.
* Definir o período da alocação.
* Visualizar a duração em dias.
* Consultar a disponibilidade da equipe.
* Identificar conflitos antes de confirmar.

Uma equipe ou funcionário não poderá receber alocações incompatíveis em períodos sobrepostos, conforme as regras de conflito que ainda serão aprovadas.

O sistema deverá considerar também os períodos de transporte como compromissos da equipe.

12. Transporte e deslocamento

O transporte será tratado como um evento próprio no planejamento.

Cada deslocamento deverá registrar:

* Equipe ou funcionário envolvido.
* Localidade de origem.
* Localidade de destino.
* Data de início.
* Duração estimada em dias inteiros.

A duração será informada manualmente pelo gestor.

Durante o deslocamento, a equipe ou funcionário envolvido ficará indisponível para compromissos incompatíveis.

O recurso envolvido não poderá iniciar um compromisso incompatível antes de concluir o deslocamento e chegar ao destino, conforme regra de chegada ainda pendente.

Equipes diferentes poderão ter durações de deslocamento diferentes para trajetos semelhantes.

O MVP não calculará rotas, distâncias ou tempos automaticamente e não dependerá de serviços de mapas.

13. Conflitos e compatibilidade

O sistema deverá identificar situações que impeçam ou comprometam uma alocação.

Exemplos:

* Equipe ou funcionário já comprometido com outro projeto no período.
* Equipe ou funcionário em deslocamento.
* Equipe ou funcionário sem disponibilidade no período.
* Competências necessárias não atendidas.
* Conflito entre datas de serviço e transporte.

As regras exatas de compatibilidade deverão ser documentadas em BUSINESS_RULES.md.

O sistema deverá explicar o motivo de cada incompatibilidade identificada.

14. Sugestões de alocação

O TeamFlow poderá apresentar sugestões para ajudar o gestor a encontrar equipes disponíveis.

As sugestões deverão considerar informações reais cadastradas no sistema, como:

* Disponibilidade.
* Período solicitado.
* Competências e qualificações.
* Compromissos existentes.
* Deslocamentos planejados.

As sugestões deverão apresentar justificativas compreensíveis.

O gestor continuará responsável por selecionar e confirmar a alocação.

Não haverá otimização automática complexa no MVP.

15. Métricas e visualizações

O sistema deverá oferecer visualizações que ajudem o gestor a compreender o planejamento.

Possibilidades iniciais:

* Calendário de alocações.
* Equipes disponíveis e ocupadas.
* Projetos em andamento.
* Períodos de transporte.
* Alocações de equipes e funcionários por projeto.
* Conflitos identificados.

As métricas definitivas deverão ser escolhidas durante a definição dos requisitos.

Não serão utilizadas métricas de produtividade ou quantidade diária produzida.

16. Segurança, privacidade e auditoria

O MVP deverá contemplar:

* Autenticação.
* Controle básico de acesso.
* Validação de dados.
* Proteção das APIs.
* Registro de alterações relevantes.
* Identificação do responsável pela alteração.
* Registro de data e horário das alterações.

O desenvolvimento deverá observar os princípios aplicáveis da LGPD.

Não coletar dados pessoais desnecessários; os dados individuais de funcionários devem ser limitados ao necessário para o planejamento aprovado.

17. Organização e multi-tenancy

O modelo comercial previsto é SaaS B2B, com possibilidade de atender múltiplas organizações no futuro.

O MVP será validado prioritariamente com uma organização.

Ainda deverá ser decidido se a estrutura de organization_id será implementada desde o início nos serviços e modelos de dados.

A decisão deverá considerar a arquitetura distribuída e a evolução futura do produto.

18. Direção tecnológica

Stack definida:

Frontend

* React.
* JavaScript.
* CSS.

Backend

* Java.
* Spring Boot.
* APIs REST.

Banco de dados

* PostgreSQL.

Ambiente

* Docker.
* Docker Compose.

TypeScript não faz parte da direção tecnológica definida para o projeto.

19. Arquitetura baseada em microsserviços

A arquitetura do TeamFlow será baseada em microsserviços desde o início.

Não será iniciado como monólito para posterior separação.

A definição dos serviços deverá partir do mapeamento dos domínios e das responsabilidades do negócio.

Não definir arbitrariamente uma quantidade de microsserviços antes dessa análise.

A arquitetura deverá considerar:

* Limites claros de domínio.
* Responsabilidades bem definidas.
* Baixo acoplamento.
* Contratos explícitos entre serviços.
* Persistência e propriedade dos dados.
* Comunicação entre serviços.
* Tratamento de falhas.
* Autenticação e autorização.
* Observabilidade.
* Testes de integração.

Evitar fragmentação artificial em serviços pequenos demais.

As decisões arquiteturais deverão ser justificadas e documentadas, considerando também o objetivo acadêmico do projeto.

20. Micro Frontends

Micro Frontends serão investigados separadamente.

Não são uma decisão de implementação obrigatória para o MVP.

A escolha deverá considerar a complexidade real do frontend e os benefícios em relação à manutenção e à evolução do produto.

21. Docker e ambiente de desenvolvimento

Docker será utilizado desde o início do desenvolvimento.

Docker Compose deverá facilitar a execução local dos serviços e das dependências necessárias.

O ambiente inicial deverá ser simples de configurar e reproduzir.

Não antecipar infraestrutura complexa de produção durante a primeira etapa de desenvolvimento.

22. Princípios de qualidade e organização do código

O código deverá seguir boas práticas de desenvolvimento profissional.

Princípios:

* Nomes claros e expressivos.
* Responsabilidades bem definidas.
* Componentes e serviços com escopo coerente.
* Arquivos com tamanho e responsabilidade controlados.
* Organização natural do projeto.
* Tratamento adequado de erros.
* Validação de entradas.
* Testes úteis e relevantes.
* Documentação objetiva.
* Evitar abstrações desnecessárias.
* Evitar fragmentação excessiva.
* Evitar soluções artificiais ou complexidade sem justificativa.

A prioridade será produzir código legível, sustentável, testável e compreensível por outros desenvolvedores.

23. Funcionalidades futuras

As seguintes ideias poderão ser consideradas depois do MVP:

* Assistente de IA integrado por API.
* Consultas em linguagem natural sobre disponibilidade e planejamento.
* Sugestões de alocação mais avançadas.
* Integrações com sistemas corporativos.
* Notificações avançadas.
* Aplicativo mobile.
* Recursos adicionais de gestão organizacional.

Um futuro assistente de IA deverá consultar informações reais do sistema, explicar suas respostas e nunca alterar o planejamento sem confirmação explícita do gestor.

Essas funcionalidades não fazem parte do escopo atual do MVP.

24. Fora do escopo do MVP

* Folha de pagamento.
* Recrutamento.
* Gestão financeira.
* Folha de pagamento e gestão trabalhista individual.
* Cálculo de produtividade diária.
* Quantidade de instalações ou produção por dia.
* Cálculo automático de capacidade produtiva.
* Otimização automática complexa.
* Cálculo automático de rotas e deslocamentos.
* Aplicativo mobile.
* Integrações corporativas complexas.
* Assistente de IA.

25. Decisões e questões pendentes

As seguintes questões deverão ser resolvidas antes ou durante a documentação dos requisitos:

1. Quais informações serão obrigatórias no cadastro de equipes?
2. Como serão cadastradas e verificadas as competências e qualificações?
3. Como representar serviços que exigem mais de uma equipe?
4. Como tratar projetos com mais de uma etapa ou localidade?
5. Como definir as regras exatas de sobreposição de datas?
6. Como representar a disponibilidade inicial de uma equipe?
7. Quais perfis mínimos de acesso serão necessários?
8. Como será implementada a separação entre organizações?
9. Quais serão os limites e responsabilidades de cada microsserviço?
10. Como os serviços irão se comunicar?
11. Como será a autenticação entre serviços?
12. Como serão organizados os dados e os bancos de cada serviço?
13. Como identificar e cadastrar funcionários?
14. Um funcionário pode pertencer a várias equipes simultaneamente, inclusive com funções diferentes?
15. Como representar a disponibilidade individual?
16. Como funciona a alocação individual em relação à alocação da equipe?
17. Uma alocação individual pode existir sem alocação prévia da equipe?
18. Como representar o deslocamento individual?
19. Como identificar conflitos quando uma pessoa participa de múltiplas equipes?
20. Como tratar conflitos entre datas, localidades, projetos e deslocamentos?
21. Quando um conflito bloqueia uma operação e quando gera apenas alerta?

As decisões deverão ser registradas e atualizadas conforme o projeto evoluir.

26. Próximos passos

A sequência de trabalho prevista é:

1. Manter as pendências deste Project Brief explícitas durante o refinamento.
2. Elaborar e revisar REQUIREMENTS.md com requisitos funcionais e não funcionais.
3. Elaborar BUSINESS_RULES.md com regras de negócio detalhadas, quando autorizado.
4. Definir os principais modelos de domínio e dados.
5. Mapear os domínios e definir as fronteiras dos microsserviços.
6. Documentar contratos e comunicação entre serviços.
7. Preparar a estrutura inicial do projeto com Docker Compose, somente após as decisões documentais necessárias.
8. Iniciar a implementação do MVP somente após autorização explícita e conclusão das etapas anteriores.

A implementação deverá começar somente depois que as decisões fundamentais de produto, regras de negócio e arquitetura estiverem suficientemente claras.
