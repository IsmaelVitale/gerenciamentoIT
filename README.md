# GerenciamentoIT

Fundacao da nova plataforma central de gerenciamento operacional e de T.I.

Nesta etapa, o repositorio contem a primeira versao executavel da API central. Os frontends atuais permanecem preservados, mas ainda nao foram adaptados para consumir o novo contrato.

## Fundacao implementada

- Java 21 e Spring Boot 4.1;
- API REST em `/api/v1`;
- Spring Security com sessao opaca por matricula;
- papeis, permissoes e escopos por setor e turno;
- setores e turnos configuraveis;
- inventario inicial de ativos;
- numero de serie obrigatorio e unico;
- patrimonio opcional e unico quando preenchido;
- MySQL com criacao automatica do banco logico;
- Hibernate com `ddl-auto=update` para criacao e evolucao do schema;
- auditoria e historico inicial de movimentacoes;
- Swagger/OpenAPI e health check;
- ambiente Docker Compose e CI no GitHub Actions.

## Inicializacao

```bash
cp .env.example .env
docker compose up --build
```

A API fica disponivel em:

```text
http://localhost:8080/api
```

Documentacao interativa:

```text
http://localhost:8080/api/swagger-ui.html
```

O `compose.yml` inicia somente o servidor MySQL. O banco `gerenciamento_it` e criado pela propria conexao da API usando `createDatabaseIfNotExist=true`; em seguida, o Hibernate cria as tabelas, indices, relacionamentos e restricoes.

## Primeiro acesso

Por padrao local, o bootstrap cria o gestor:

```text
Matricula: ADMIN-LOCAL
Papel: GESTOR_TI
```

Esse valor deve ser alterado pelas variaveis `BOOTSTRAP_ADMIN_MATRICULA` e `BOOTSTRAP_ADMIN_NOME` antes de publicar um ambiente.

```http
POST /api/v1/sessoes
Content-Type: application/json
```

```json
{
  "matricula": "ADMIN-LOCAL",
  "origemAplicacao": "GESTAO_ATIVOS"
}
```

Consulte [`API/README.md`](API/README.md) para os endpoints e as configuracoes detalhadas.

## Proximas etapas

A fundacao sera expandida com:

1. alocacao e ciclo completo dos ativos;
2. conferencia de pools de PDA por turno;
3. emprestimos, manutencoes e divergencias;
4. chamados e ITSM;
5. Planner e sincronismo chamado-tarefa;
6. frontends especializados;
7. integracao com WhatsApp, com regras proprias em uma etapa futura.
