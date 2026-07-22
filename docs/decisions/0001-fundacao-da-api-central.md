# ADR 0001 — Fundacao da API central

- Status: aceita
- Data: 2026-07-22

## Contexto

O GerenciamentoIT sera um ecossistema com interfaces especializadas para operacao, ativos, chamados, ITSM e tarefas. Todas as interfaces devem compartilhar a mesma fonte de verdade e as mesmas regras de autorizacao.

O prototipo anterior nao sera utilizado como modelo de dominio nem como fonte de dados para a nova versao.

## Decisao

1. A solucao tera uma API central Spring Boot e um banco MySQL compartilhado.
2. O aplicativo de origem sera apenas metadado de auditoria; autorizacao dependera do usuario autenticado, papel, permissao e escopo.
3. O banco logico sera criado pelo MySQL Connector/J com `createDatabaseIfNotExist=true`.
4. O schema sera criado e atualizado pelo Hibernate com `spring.jpa.hibernate.ddl-auto=update`.
5. Flyway, Liquibase e scripts SQL obrigatorios nao serao utilizados nesta fase.
6. O numero de serie sera obrigatorio e unico para todo ativo.
7. O patrimonio sera opcional e unico quando preenchido.
8. A autenticacao inicial sera por matricula, gerando uma sessao opaca temporaria. Esse mecanismo e provisório e podera ser substituido sem alterar o dominio.
9. A integracao com WhatsApp nao faz parte desta entrega e recebera regras especificas futuramente.

## Consequencias

- Um servidor MySQL precisa existir e estar acessivel; a aplicacao cria o banco dentro dele, mas nao instala o servidor.
- O usuario MySQL precisa de permissoes para criar o banco e executar DDL/DML.
- Alteracoes destrutivas de schema exigirao disciplina, pois `ddl-auto=update` nao substitui uma estrategia formal de migracao de dados.
- Entidades mutaveis usam controle otimista de versao para reduzir sobrescritas concorrentes.
- O bootstrap cria os tipos basicos de ativo e o primeiro gestor em uma instalacao vazia.
