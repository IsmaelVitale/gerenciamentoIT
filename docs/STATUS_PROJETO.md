# Estado do projeto GerenciamentoIT

Atualizado em 29 de julho de 2026.

## Visao atual

O repositorio possui uma fundacao executavel para a API central, inventario inicial de ativos, abertura de chamados e o primeiro fluxo real do Hub de PDAs. O bot do WhatsApp possui contrato real com a API, embora continue desativado por padrao por meio de `SIMULATION_MODE=true`.

## Entregas concluidas

### Fundacao da API

- Java 21 e Spring Boot 4.1;
- API REST sob `/api/v1`;
- sessao opaca provisoria por matricula;
- papeis, permissoes, setores e turnos;
- auditoria e controle otimista;
- MySQL, Docker Compose, OpenAPI e health check;
- CI da API com Maven.

### Ativos

- tipos de ativo;
- cadastro inicial;
- numero de serie obrigatorio e unico;
- patrimonio opcional e unico quando preenchido;
- liberacao e correcao de identificacao;
- historico inicial de movimentacoes.

### Chamados e WhatsApp

- entidade e persistencia de chamados;
- protocolo, status inicial `ABERTO` e solicitante autenticado;
- abertura idempotente por origem e identificador externo;
- consulta dos proprios chamados;
- origem `WHATSAPP` incorporada ao modelo de sessao;
- bot com fluxo `descricao -> matricula -> chamado`;
- revogacao da sessao temporaria apos a abertura;
- recusa de chamados em grupos;
- CI de sintaxe para o bot.

### Hub de PDAs

- SPA/PWA desktop-first com React, TypeScript e Vite;
- identificacao real por matricula com origem `HUB_PDA`;
- token opaco mantido durante a sessao do navegador;
- restauracao e revogacao da sessao;
- endpoint universal `GET /api/v1/me/contextos-operacionais`;
- selecao de setor e turno autorizados;
- estados de carregamento, erro, API indisponivel e ausencia de contexto;
- modo simulado removido do Hub.

## Decisoes preservadas

- a API e a fonte de verdade das regras e da autorizacao;
- o aplicativo de origem e metadado de auditoria, nao autorizacao;
- a identidade do chamado vem da sessao, nao do telefone;
- o numero de telefone e somente contato;
- mensagens repetidas do WhatsApp nao devem criar chamados duplicados;
- o banco e o schema continuam sendo criados automaticamente nesta fase;
- Flyway e Liquibase ainda nao foram introduzidos;
- endpoints representam recursos de negocio e nao recebem o nome de um frontend;
- o frontend organiza o fluxo, mas a API preserva regras, autorizacao e estado real.

## Proximos incrementos recomendados

1. **Conferencia online de PDAs:** abrir conferencia, carregar o pool esperado, registrar leituras e concluir com validacao da API.
2. **Acoes rapidas de PDAs:** consulta, emprestimo, recebimento, devolucao, indisponibilidade e manutencao.
3. **Operacao offline do Hub:** IndexedDB, fila idempotente, sincronizacao e conflitos.
4. **ITSM basico:** triagem, categoria, prioridade, atribuicao, mensagens publicas, resolucao e fechamento.
5. **Vinculacao segura do WhatsApp:** substituir matricula isolada por OTP, senha, SSO, cracha ou outro fator e persistir a associacao autorizada entre usuario e telefone.
6. **Portal de Chamados:** adaptar a interface do solicitante aos novos endpoints reais.
7. **Planner:** quadros, tarefas e sincronismo entre chamado e tarefa.
8. **Evolucao de banco:** introduzir migracoes versionadas antes de ambientes produtivos compartilhados.

## Riscos e dividas tecnicas

- autenticacao apenas por matricula e provisoria e inadequada para producao;
- `whatsapp-web.js` automatiza o WhatsApp Web e nao e a API oficial da Meta;
- o estado conversacional pendente do bot fica somente em memoria;
- `ddl-auto=update` nao substitui migracoes controladas;
- os frontends antigos ainda sao referencia visual e nao representam integralmente os contratos atuais;
- conferencia, acoes rapidas e fila offline do Hub ainda nao foram implementadas;
- monitoramento, rate limiting, politicas de retencao e observabilidade do bot ainda precisam ser definidos.

## Orientacao de trabalho

A frente ativa e o **Hub de PDAs**. O proximo incremento recomendado e a conferencia online, usando os contratos universais da API e mantendo o frontend responsavel apenas pelo fluxo e pela apresentacao.
