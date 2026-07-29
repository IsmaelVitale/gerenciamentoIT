# Portal HTML local do GerenciamentoIT

Central estática para testar os módulos e os endpoints da API sem Vite, npm ou Postman.

## Como executar

1. Inicie a API pelo IntelliJ.
2. Confirme no console que ela está disponível em `http://localhost:8080/api`.
3. Abra `ABRIR_PORTAL.bat` ou dê duplo clique em `index.html`.
4. Clique em **Entrar na API**.
5. Use a matrícula `ADMIN-LOCAL` no ambiente local padrão.

Não é necessário executar nenhum comando no terminal para abrir o portal.

## Páginas disponíveis

| Arquivo | Funcionalidades |
| --- | --- |
| `index.html` | Central de navegação e teste rápido da API |
| `apps/hub-pdas.html` | Contextos, abertura de conferência, leituras, itens e conclusão |
| `apps/gestao-operacional.html` | Setores, turnos, usuários e atribuições de acesso |
| `apps/gestao-ativos.html` | Tipos de ativo, inventário, liberação, alocação e correção |
| `apps/atendimento-itsm.html` | Abertura, listagem e consulta de chamados |
| `apps/planner-ti.html` | Protótipo local do Planner salvo no navegador |
| `apps/console-api.html` | Requisições livres para qualquer endpoint |

## Sessão compartilhada

O endereço da API, o token e os dados da sessão ficam no armazenamento local do navegador. Após entrar em uma página, a mesma sessão é reutilizada nas demais.

A API continua sendo a responsável por validar as permissões. Um botão visível no portal não contorna uma resposta `403`.

## Execução por `file://`

Arquivos abertos diretamente no navegador utilizam a origem `null`. A configuração CORS local padrão da API inclui essa origem para permitir os testes.

Ao publicar a API, configure `CORS_ALLOWED_ORIGINS` explicitamente com os domínios autorizados. A origem `null` não deve ser usada no ambiente de produção.

## Planner

A API ainda não possui endpoints do Planner. Por isso, essa página funciona como protótipo local usando o armazenamento do navegador e informa claramente que as tarefas não são salvas no MySQL.
