# GerenciamentoIT API

Fundacao da nova API central do GerenciamentoIT.

## Escopo desta entrega

- Java 21 e Spring Boot 4.1;
- Spring Web, Security, Data JPA e Hibernate;
- MySQL com criacao automatica do banco logico;
- `ddl-auto=update`, sem Flyway ou Liquibase;
- sessao opaca autenticada por matricula;
- papeis e escopos de acesso;
- setores e turnos;
- tipos de ativo;
- cadastro e liberacao de ativos;
- numero de serie obrigatorio e unico;
- patrimonio opcional e unico quando preenchido;
- historico inicial de movimentacoes e auditoria;
- Swagger/OpenAPI;
- bootstrap automatico do primeiro gestor e dos tipos basicos de ativo.

WhatsApp, chamados, Planner, ITSM, conferencia de PDA, emprestimos e divergencias ficam fora desta primeira etapa.

## Criacao automatica do banco

A URL JDBC usa:

```text
createDatabaseIfNotExist=true
```

O fluxo de primeira execucao e:

1. o servidor MySQL precisa estar ativo;
2. o usuario configurado precisa possuir permissao `CREATE DATABASE` e permissoes de DDL/DML;
3. o MySQL Connector/J cria `gerenciamento_it` quando o banco nao existe;
4. o Hibernate cria e atualiza tabelas, indices, restricoes e relacionamentos.

A aplicacao nao instala nem inicia o servidor MySQL. O `compose.yml` da raiz oferece um ambiente completo para desenvolvimento.

## Execucao com Docker

Na raiz do repositorio:

```bash
docker compose up --build
```

O `compose.yml` nao declara `MYSQL_DATABASE`; portanto, o banco `gerenciamento_it` e criado pela conexao da propria API.

Servicos:

```text
API:      http://localhost:8080/api
Swagger:  http://localhost:8080/api/swagger-ui.html
Health:   http://localhost:8080/api/actuator/health
MySQL:    localhost:3306
```

## Execucao local

Com MySQL ativo:

```bash
cp .env.example .env
./mvnw spring-boot:run
```

Variaveis principais:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD
DB_SSL_MODE
BOOTSTRAP_ADMIN_MATRICULA
BOOTSTRAP_ADMIN_NOME
CORS_ALLOWED_ORIGINS
SESSION_DURATION
```

## Primeiro acesso

Na primeira inicializacao, o sistema cria automaticamente:

- os tipos `PDA`, `NOTEBOOK`, `CELULAR`, `IMPRESSORA` e `PERIFERICO`;
- o usuario gestor informado por `BOOTSTRAP_ADMIN_MATRICULA`;
- uma atribuicao global `GESTOR_TI` para esse usuario.

O valor local padrao e `ADMIN-LOCAL`. Ele deve ser substituido em qualquer ambiente publicado.
Depois da primeira configuracao administrativa, defina `BOOTSTRAP_ENABLED=false` para impedir que uma reinicializacao recrie uma atribuicao removida intencionalmente.

Criacao da sessao:

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

As chamadas autenticadas usam:

```http
Authorization: Bearer <token>
```

A autenticacao somente por matricula e deliberadamente provisoria. O token de sessao impede que os comandos de negocio aceitem uma matricula arbitraria como autor da operacao.

## Cadastro de ativo

Liste os tipos para obter o UUID de `PDA`:

```http
GET /api/v1/tipos-ativo
```

Cadastre um ativo sem patrimonio:

```http
POST /api/v1/ativos
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "tipoAtivoId": "UUID-DO-TIPO-PDA",
  "numeroSerie": "SN-CT40-000187",
  "patrimonio": null,
  "fabricante": "Honeywell",
  "modelo": "CT40",
  "observacao": "Recebido sem etiqueta patrimonial"
}
```

O ativo nasce como:

```text
situacaoPatrimonial = EM_PREPARACAO
disponibilidade     = INDISPONIVEL
localizacaoAtual    = TI
```

Liberacao:

```http
POST /api/v1/ativos/{id}/liberacoes
```

Correcao ou inclusao de patrimonio:

```http
POST /api/v1/ativos/{id}/correcoes-identificacao
```

```json
{
  "numeroSerie": "SN-CT40-000187",
  "patrimonio": "PDA-047",
  "removerPatrimonio": false,
  "motivo": "Etiqueta patrimonial aplicada pela T.I."
}
```

## Testes

```bash
./mvnw verify
```

Os testes usam H2 em memoria no modo de compatibilidade MySQL; a aplicacao normal continua usando MySQL.
