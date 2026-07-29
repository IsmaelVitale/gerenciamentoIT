# Aplicações finais em HTML

Protótipos funcionais das interfaces finais do GerenciamentoIT, construídos com HTML, CSS e JavaScript nativos.

Estas páginas representam o que cada público verá no produto. Ferramentas técnicas, testes livres de endpoint e respostas JSON ficam separadas em `front/Desenvolvimento_Portal`.

## Executar

1. Inicie a API pelo IntelliJ em `http://localhost:8080/api`.
2. Abra `ABRIR_APLICACOES.bat` ou `index.html`.
3. Escolha uma aplicação.
4. Entre com a matrícula disponível no ambiente local.

No bootstrap padrão, use `ADMIN-LOCAL`.

## Aplicações

- `hub-pdas`: conferência operacional para Líder, Supervisor e T.I.
- `gestao-operacional`: usuários, setores, turnos e acessos.
- `gestao-ativos`: inventário e ciclo de vida dos ativos.
- `atendimento-itsm`: portal do usuário para chamados.
- `planner-ti`: quadro visual local enquanto o backend do Planner não existe.

## Limites atuais

- A API ainda não oferece endpoints para o Planner. Seus dados são armazenados somente no navegador.
- A API ainda não possui atualização ou exclusão para vários cadastros. As interfaces apresentam apenas ações realmente suportadas.
- Como os arquivos são abertos por `file://`, a configuração local da API permite a origem CORS `null`. Essa origem não deve ser usada em produção.
