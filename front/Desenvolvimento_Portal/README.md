# Desenvolvimento Portal do GerenciamentoIT

Central técnica estática para testar módulos e endpoints da API sem Vite, npm ou Postman.

Esta pasta é destinada ao desenvolvimento e à administração técnica. As interfaces que simulam a experiência final dos usuários ficam em `front/Aplicacoes`.

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

O endereço da API, o token e os dados da sessão ficam no armazenamento local do navegador. Como alguns navegadores isolam esse armazenamento por arquivo em `file://`, os links do menu transportam a sessão para a próxima página e a gravam novamente no destino.

Evite copiar ou compartilhar o endereço exibido na barra durante a troca de página, pois ele pode conter temporariamente o token local. O portal remove esse trecho do endereço após importá-lo quando o navegador permite.

A API continua sendo a responsável por validar as permissões. Um botão visível no portal não contorna uma resposta `403`.

## Execução por `file://`

Arquivos abertos diretamente no navegador utilizam a origem `null`. A configuração CORS local padrão da API inclui essa origem para permitir os testes.

Ao publicar a API, configure `CORS_ALLOWED_ORIGINS` explicitamente com os domínios autorizados. A origem `null` não deve ser usada no ambiente de produção.

## Planner

A API ainda não possui endpoints do Planner. Por isso, essa página funciona como protótipo local usando o armazenamento do navegador e informa claramente que as tarefas não são salvas no MySQL.
