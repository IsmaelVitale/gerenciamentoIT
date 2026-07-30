# Aplicações finais em HTML

Protótipos funcionais das interfaces finais do GerenciamentoIT, construídos com HTML, CSS e JavaScript nativos.

Estas páginas representam o que cada público verá no produto. Ferramentas técnicas, testes livres de endpoint e respostas JSON ficam separadas em `front/Desenvolvimento_Portal`.

## Direção visual

As telas revisadas da pasta `production` são a referência de experiência:

- navegação lateral escura;
- cabeçalhos claros e contexto da sessão;
- cartões operacionais e tabelas compactas;
- fluxo orientado a scanner no Hub de PDAs;
- estados, alertas, confirmações e formulários adequados para uso em PC;
- adaptação para tablet sem transformar o tablet no formato principal.

A referência é visual e operacional. Os contratos antigos de `/pdas`, os papéis `ADMIN` e `OPERADOR` e os CRUDs que não existem na API atual não são reutilizados.

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

## Papéis atuais

- `USUARIO`: abre e acompanha os próprios chamados.
- `LIDER`: visualiza seus contextos, consulta ativos, executa conferências e administra acessos permitidos.
- `SUPERVISOR`: administra a estrutura operacional conforme suas permissões.
- `ANALISTA_TI`: executa as operações técnicas permitidas sobre tipos e ativos.
- `GESTOR_TI`: possui o conjunto completo de permissões.

O frontend apresenta `ANALISTA_TI` e `GESTOR_TI` como perfis da área de **T.I.**, sem esconder a diferença de autorização existente na API.

## Checklist desta fundação

- [x] identidade baseada nas telas revisadas de `production`;
- [x] layout desktop-first com adaptação para tablet;
- [x] sessão e papel atual exibidos no cabeçalho;
- [x] ações ocultadas quando a sessão não possui a permissão necessária;
- [x] Hub conectado à conferência real por setor e turno;
- [x] Gestão Operacional conectada aos usuários, acessos, setores e turnos atuais;
- [x] Gestão de Ativos conectada ao inventário universal e às movimentações existentes;
- [x] Atendimento conectado apenas aos chamados do usuário autenticado;
- [x] Planner identificado como armazenamento local;
- [ ] retirada e devolução de PDA por usuário;
- [ ] vínculo de PDA por turno;
- [ ] histórico operacional e alertas de atraso;
- [ ] triagem, atribuição, prioridade e conversa nos chamados;
- [ ] persistência do Planner na API.

## Limites atuais

- A API ainda não oferece endpoints para o Planner. Seus dados são armazenados somente no navegador.
- A API ainda não possui atualização ou exclusão para vários cadastros. As interfaces apresentam apenas ações realmente suportadas.
- Como os arquivos são abertos por `file://`, a configuração local da API permite a origem CORS `null`. Essa origem não deve ser usada em produção.
