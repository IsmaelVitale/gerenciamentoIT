# Estado do projeto GerenciamentoIT

Atualizado em 27 de julho de 2026.

## Visao atual

O repositorio ja possui uma fundacao executavel para a API central e iniciou duas verticais funcionais: inventario de ativos e abertura de chamados. O bot do WhatsApp deixou de ser apenas uma simulacao estrutural e agora possui contrato real com a API, embora continue desativado por padrao por meio de `SIMULATION_MODE=true`.

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

## Decisoes preservadas

- a API e a fonte de verdade das regras e da autorizacao;
- o aplicativo de origem e metadado de auditoria, nao autorizacao;
- a identidade do chamado vem da sessao, nao do telefone;
- o numero de telefone e somente contato;
- mensagens repetidas do WhatsApp nao devem criar chamados duplicados;
- o banco e o schema continuam sendo criados automaticamente nesta fase;
- Flyway e Liquibase ainda nao foram introduzidos.

## Proximos incrementos recomendados

1. **ITSM basico:** triagem, categoria, prioridade, atribuicao, mensagens publicas, resolucao e fechamento.
2. **Vinculacao segura do WhatsApp:** substituir matricula isolada por OTP, senha, SSO, cracha ou outro fator e persistir a associacao autorizada entre usuario e telefone.
3. **Portal de Chamados:** adaptar a interface do solicitante aos novos endpoints reais.
4. **Operacao de PDAs:** distribuicao permanente, conferencia por turno, emprestimos, manutencoes e divergencias.
5. **Planner:** quadros, tarefas e sincronismo entre chamado e tarefa.
6. **Evolucao de banco:** introduzir migracoes versionadas antes de ambientes produtivos compartilhados.

## Riscos e dividas tecnicas

- autenticacao apenas por matricula e provisoria e inadequada para producao;
- `whatsapp-web.js` automatiza o WhatsApp Web e nao e a API oficial da Meta;
- o estado conversacional pendente do bot fica somente em memoria;
- `ddl-auto=update` nao substitui migracoes controladas;
- os frontends antigos ainda sao referencia visual e nao representam integralmente os contratos atuais;
- monitoramento, rate limiting, politicas de retencao e observabilidade do bot ainda precisam ser definidos.

## Orientacao de trabalho

A proxima frente mais coerente e o **ITSM basico**, porque transforma o chamado aberto pelo WhatsApp ou pelo futuro portal em um fluxo atendivel pela T.I. Sem essa etapa, o sistema recebe chamados, mas ainda nao oferece triagem, atribuicao ou resolucao operacional.
