(function () {
  'use strict';

  var Api = window.GerenciamentoITApi;
  var page = document.body.getAttribute('data-page') || 'home';
  var insideApps = window.location.pathname.replace(/\\/g, '/').indexOf('/apps/') >= 0;
  var rootPath = insideApps ? '../' : './';
  var state = {
    setores: [],
    turnos: [],
    tiposAtivo: [],
    contextos: [],
    ultimoUsuario: null,
    ultimaConferencia: readLocal('gerenciamentoit.ultimaConferencia') || ''
  };

  var modules = [
    {
      id: 'hub-pdas',
      code: 'PDA',
      name: 'Hub de PDAs',
      status: 'Funcional',
      description: 'Conferência de coletores por setor e turno.',
      file: 'apps/hub-pdas.html',
      accent: '#2b75ff',
      origin: 'HUB_PDA'
    },
    {
      id: 'gestao-operacional',
      code: 'OPS',
      name: 'Gestão Operacional',
      status: 'Funcional',
      description: 'Usuários, acessos, setores e turnos.',
      file: 'apps/gestao-operacional.html',
      accent: '#8a5cf6',
      origin: 'GESTAO_OPERACIONAL'
    },
    {
      id: 'gestao-ativos',
      code: 'ATM',
      name: 'Gestão de Ativos',
      status: 'Funcional',
      description: 'Tipos, inventário e movimentações de ativos.',
      file: 'apps/gestao-ativos.html',
      accent: '#00a882',
      origin: 'GESTAO_ATIVOS'
    },
    {
      id: 'atendimento-itsm',
      code: 'IT',
      name: 'Atendimento ITSM',
      status: 'Funcional',
      description: 'Abertura e acompanhamento de chamados.',
      file: 'apps/atendimento-itsm.html',
      accent: '#ef7a2d',
      origin: 'ITSM'
    },
    {
      id: 'planner-ti',
      code: 'PLN',
      name: 'Planner da T.I.',
      status: 'Local',
      description: 'Quadro temporário salvo no navegador.',
      file: 'apps/planner-ti.html',
      accent: '#d3486f',
      origin: 'PLANNER'
    },
    {
      id: 'console-api',
      code: 'API',
      name: 'Console da API',
      status: 'Utilitário',
      description: 'Teste livre dos endpoints sem Postman.',
      file: 'apps/console-api.html',
      accent: '#267ba8',
      origin: 'API'
    }
  ];

  var currentModule = modules.filter(function (item) {
    return item.id === page;
  })[0];

  function escapeHtml(value) {
    return String(value === undefined || value === null ? '' : value)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  function readLocal(key) {
    try {
      return window.localStorage.getItem(key);
    } catch (_) {
      return null;
    }
  }

  function writeLocal(key, value) {
    try {
      window.localStorage.setItem(key, value);
    } catch (_) {
      // O portal continua funcionando mesmo sem persistência local.
    }
  }

  function icon(name) {
    var paths = {
      grid: '<rect x="3" y="3" width="7" height="7" rx="2"></rect><rect x="14" y="3" width="7" height="7" rx="2"></rect><rect x="3" y="14" width="7" height="7" rx="2"></rect><rect x="14" y="14" width="7" height="7" rx="2"></rect>',
      activity: '<path d="M3 12h4l2.3-6 4.4 12 2.3-6H21"></path>',
      arrow: '<path d="M5 12h14M13 6l6 6-6 6"></path>',
      menu: '<path d="M4 6h16M4 12h16M4 18h16"></path>',
      close: '<path d="m6 6 12 12M18 6 6 18"></path>',
      user: '<circle cx="12" cy="8" r="4"></circle><path d="M4 21a8 8 0 0 1 16 0"></path>',
      plug: '<path d="m8 12 4 4 8-8"></path><path d="M21 12a9 9 0 1 1-5.3-8.2"></path>'
    };
    return (
      '<svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">' +
      paths[name] +
      '</svg>'
    );
  }

  function navHtml() {
    return modules
      .map(function (item) {
        return (
          '<a class="nav-item ' +
          (item.id === page ? 'nav-item--active' : '') +
          '" href="' +
          rootPath +
          item.file +
          Api.navigationHash() +
          '">' +
          '<span class="nav-monogram" style="--app-accent:' +
          item.accent +
          '">' +
          item.code +
          '</span><span><strong>' +
          item.name +
          '</strong><small>' +
          item.status +
          '</small></span></a>'
        );
      })
      .join('');
  }

  function shell(content) {
    var session = Api.getSession();
    var user = session && session.usuario;
    return (
      '<div class="portal-shell">' +
      '<aside class="sidebar" id="sidebar">' +
      '<div class="brand"><span class="brand-mark">G</span><span><strong>GerenciamentoIT</strong><small>Portal HTML local</small></span></div>' +
      '<nav aria-label="Aplicações">' +
      '<a class="nav-item nav-item--home ' +
      (page === 'home' ? 'nav-item--active' : '') +
      '" href="' +
      rootPath +
      'index.html' +
      Api.navigationHash() +
      '"><span class="nav-icon">' +
      icon('grid') +
      '</span><span><strong>Visão geral</strong><small>Central de testes</small></span></a>' +
      '<p class="nav-label">APLICAÇÕES</p>' +
      navHtml() +
      '</nav>' +
      '<div class="sidebar-footer"><span class="environment-dot"></span><span><strong>Arquivo local</strong><small>Sem Vite · Sem instalação</small><a href="../Aplicacoes/index.html">Abrir aplicações finais</a></span></div>' +
      '</aside>' +
      '<div class="sidebar-scrim" id="sidebar-scrim"></div>' +
      '<main class="main">' +
      '<header class="topbar">' +
      '<button class="menu-button" id="menu-button" type="button" aria-label="Abrir menu">' +
      icon('menu') +
      '</button>' +
      '<div class="topbar-title"><span class="eyebrow">GERENCIAMENTOIT</span><strong>' +
      (currentModule ? currentModule.name : 'Central de testes') +
      '</strong></div>' +
      '<button class="session-pill ' +
      (user ? 'session-pill--online' : '') +
      '" type="button" data-action="toggle-session">' +
      '<span class="session-dot"></span><span><strong>' +
      (user ? escapeHtml(user.nome) : 'Entrar na API') +
      '</strong><small>' +
      (user ? escapeHtml(user.matricula) : 'Sessão necessária') +
      '</small></span></button>' +
      '</header>' +
      '<section class="session-panel" id="session-panel" hidden>' +
      sessionPanelHtml() +
      '</section>' +
      '<div class="content">' +
      content +
      '</div>' +
      '</main></div>' +
      '<div class="toast-region" id="toast-region" aria-live="polite"></div>'
    );
  }

  function sessionPanelHtml() {
    var session = Api.getSession();
    var user = session && session.usuario;
    if (user) {
      return (
        '<div class="session-panel-inner">' +
        '<div><span class="section-kicker">SESSÃO ATUAL</span><h3>' +
        escapeHtml(user.nome) +
        '</h3><p>' +
        escapeHtml((user.papeis || []).join(' · ')) +
        '</p></div>' +
        '<div class="session-actions"><label>Endereço da API<input id="api-url" value="' +
        escapeHtml(Api.getBaseUrl()) +
        '"></label><button class="button button--secondary" data-action="save-api-url">Salvar endereço</button><button class="button button--danger" data-action="logout">Encerrar sessão</button></div>' +
        '</div>'
      );
    }

    return (
      '<form class="session-panel-inner" data-form="login">' +
      '<div><span class="section-kicker">AUTENTICAÇÃO LOCAL</span><h3>Conectar à API</h3><p>Use <strong>ADMIN-LOCAL</strong> no ambiente padrão.</p></div>' +
      '<div class="session-actions"><label>Endereço da API<input id="api-url" value="' +
      escapeHtml(Api.getBaseUrl()) +
      '"></label><label>Matrícula<input name="matricula" value="ADMIN-LOCAL" required></label><button class="button button--primary" type="submit">Criar sessão</button></div>' +
      '</form>'
    );
  }

  function pageHeader(kicker, title, description) {
    return (
      '<section class="module-heading"><div><span class="section-kicker">' +
      kicker +
      '</span><h1>' +
      title +
      '</h1><p>' +
      description +
      '</p></div><div class="api-health" id="api-health" role="status"><span></span><div><strong>Verificando API</strong><small>' +
      escapeHtml(Api.getBaseUrl()) +
      '</small></div></div></section>'
    );
  }

  function formField(label, name, type, options) {
    var config = options || {};
    var attributes =
      (config.required ? ' required' : '') +
      (config.placeholder ? ' placeholder="' + escapeHtml(config.placeholder) + '"' : '') +
      (config.value !== undefined ? ' value="' + escapeHtml(config.value) + '"' : '') +
      (config.min !== undefined ? ' min="' + escapeHtml(config.min) + '"' : '');
    return (
      '<label class="field"><span>' +
      label +
      '</span><input type="' +
      (type || 'text') +
      '" name="' +
      name +
      '"' +
      attributes +
      '></label>'
    );
  }

  function selectField(label, name, items, options) {
    var config = options || {};
    var choices = (config.empty === false ? '' : '<option value="">Selecione</option>') +
      items
        .map(function (item) {
          var value = typeof item === 'string' ? item : item.value;
          var text = typeof item === 'string' ? item : item.label;
          return '<option value="' + escapeHtml(value) + '">' + escapeHtml(text) + '</option>';
        })
        .join('');
    return (
      '<label class="field"><span>' +
      label +
      '</span><select name="' +
      name +
      '" ' +
      (config.required ? 'required' : '') +
      '>' +
      choices +
      '</select></label>'
    );
  }

  function panel(title, subtitle, body, className) {
    return (
      '<article class="work-panel ' +
      (className || '') +
      '"><div class="panel-heading"><div><h2>' +
      title +
      '</h2><p>' +
      subtitle +
      '</p></div></div>' +
      body +
      '</article>'
    );
  }

  function output(id, placeholder) {
    return (
      '<div class="result-box result-box--empty" id="' +
      id +
      '"><span>' +
      (placeholder || 'O resultado aparecerá aqui.') +
      '</span></div>'
    );
  }

  function homePage() {
    return (
      '<section class="hero static-hero"><div><span class="section-kicker">CENTRAL DE TESTES ESTÁTICA</span><h1>Abra, clique<br><em>e teste a API.</em></h1><p>Todos os módulos abaixo são arquivos HTML locais. Não há Vite, npm ou servidor de frontend.</p></div><div class="hero-summary"><span class="summary-number">06</span><span>páginas<br>de operação</span></div></section>' +
      '<section class="notice notice--success"><strong>Como usar</strong><span>Inicie somente a API pelo IntelliJ, abra este arquivo e entre com a matrícula ADMIN-LOCAL.</span></section>' +
      '<section class="app-grid static-grid">' +
      modules
        .map(function (item, index) {
          return (
            '<a class="app-card" href="' +
            rootPath +
            item.file +
            Api.navigationHash() +
            '" style="--app-accent:' +
            item.accent +
            '"><div class="card-top"><span class="card-index">' +
            String(index + 1).padStart(2, '0') +
            '</span><span class="status ' +
            (item.status === 'Funcional' ? 'status--testing' : 'status--foundation') +
            '">' +
            item.status +
            '</span></div><span class="app-symbol">' +
            item.code +
            '</span><h3>' +
            item.name +
            '</h3><p>' +
            item.description +
            '</p><div class="card-footer"><span>Abrir arquivo HTML</span><span class="round-arrow">' +
            icon('arrow') +
            '</span></div></a>'
          );
        })
        .join('') +
      '</section>' +
      '<section class="quick-console">' +
      panel(
        'Teste rápido da API',
        'Substitui uma chamada simples do Postman.',
        '<form class="inline-form" data-form="quick-request">' +
          selectField('Método', 'method', ['GET', 'POST', 'PUT', 'PATCH', 'DELETE'], { empty: false }) +
          formField('Endpoint', 'path', 'text', { value: '/v1/me', required: true }) +
          '<button class="button button--primary" type="submit">Enviar</button></form>' +
          output('quick-output', 'Entre na API e envie uma requisição.'),
        'work-panel--wide'
      ) +
      '</section>'
    );
  }

  function hubPage() {
    return (
      pageHeader('OPERAÇÃO', 'Hub de PDAs', 'Execute a conferência completa sem iniciar outro frontend.') +
      '<section class="notice"><strong>Fluxo recomendado</strong><span>1. Carregue seus contextos · 2. Abra a conferência · 3. Registre os códigos · 4. Consulte e conclua.</span></section>' +
      '<section class="workspace-grid">' +
      panel(
        'Contexto operacional',
        'Setores e turnos liberados para a sessão.',
        '<button class="button button--secondary" data-action="load-contexts">Carregar contextos</button>' +
          output('contexts-output', 'Nenhum contexto carregado.')
      ) +
      panel(
        'Abrir conferência',
        'Cria uma conferência de abertura ou encerramento.',
        '<form class="form-grid" data-form="open-conference">' +
          '<label class="field"><span>Setor</span><select name="setorId" data-select="context-setor" required><option value="">Carregue os contextos</option></select></label>' +
          '<label class="field"><span>Turno</span><select name="turnoId" data-select="context-turno" required><option value="">Carregue os contextos</option></select></label>' +
          selectField('Tipo', 'tipo', ['ABERTURA', 'ENCERRAMENTO'], { empty: false }) +
          '<button class="button button--primary" type="submit">Abrir conferência</button></form>' +
          output('open-conference-output', 'A conferência criada aparecerá aqui.')
      ) +
      panel(
        'Registrar leitura',
        'Leia patrimônio ou número de série do PDA.',
        '<form class="form-grid" data-form="conference-read">' +
          formField('ID da conferência', 'conferenceId', 'text', { value: state.ultimaConferencia, required: true }) +
          formField('Código do PDA', 'codigo', 'text', { placeholder: 'PDA-001 ou número de série', required: true }) +
          '<button class="button button--primary" type="submit">Registrar PDA</button></form>' +
          output('conference-read-output', 'A leitura e seu resultado aparecerão aqui.')
      ) +
      panel(
        'Consultar e concluir',
        'Detalhes, itens esperados, leituras e conclusão.',
        '<form class="form-grid" data-form="conference-query">' +
          formField('ID da conferência', 'conferenceId', 'text', { value: state.ultimaConferencia, required: true }) +
          '<div class="button-row"><button class="button button--secondary" name="operation" value="detail" type="submit">Detalhes</button><button class="button button--secondary" name="operation" value="items" type="submit">Itens</button><button class="button button--secondary" name="operation" value="reads" type="submit">Leituras</button><button class="button button--danger" name="operation" value="finish" type="submit">Concluir</button></div></form>' +
          output('conference-query-output', 'Escolha uma operação.')
      ) +
      '</section>'
    );
  }

  function operationalPage() {
    var roles = ['USUARIO', 'LIDER', 'SUPERVISOR', 'ANALISTA_TI', 'GESTOR_TI'];
    return (
      pageHeader('ADMINISTRAÇÃO', 'Gestão Operacional', 'Cadastre e consulte usuários, setores, turnos e atribuições.') +
      '<section class="workspace-grid">' +
      panel(
        'Setores',
        'Cadastre e liste os setores da operação.',
        '<form class="form-grid" data-form="create-sector">' +
          formField('Código', 'codigo', 'text', { placeholder: 'RECEBIMENTO', required: true }) +
          formField('Nome', 'nome', 'text', { placeholder: 'Recebimento', required: true }) +
          formField('Cota de PDAs', 'cotaPdas', 'number', { value: 0, min: 0, required: true }) +
          '<div class="button-row"><button class="button button--primary" type="submit">Cadastrar</button><button class="button button--secondary" type="button" data-action="load-sectors">Listar setores</button></div></form>' +
          output('sectors-output', 'Cadastre ou carregue os setores.')
      ) +
      panel(
        'Turnos',
        'Cadastre e liste os turnos disponíveis.',
        '<form class="form-grid" data-form="create-shift">' +
          formField('Código', 'codigo', 'text', { placeholder: 'MANHA', required: true }) +
          formField('Nome', 'nome', 'text', { placeholder: 'Manhã', required: true }) +
          formField('Início', 'horaInicio', 'time', {}) +
          formField('Fim', 'horaFim', 'time', {}) +
          '<div class="button-row"><button class="button button--primary" type="submit">Cadastrar</button><button class="button button--secondary" type="button" data-action="load-shifts">Listar turnos</button></div></form>' +
          output('shifts-output', 'Cadastre ou carregue os turnos.')
      ) +
      panel(
        'Usuários',
        'Cadastre um usuário ou busque pela matrícula.',
        '<form class="form-grid" data-form="create-user">' +
          formField('Matrícula', 'matricula', 'text', { required: true }) +
          formField('Nome', 'nome', 'text', { required: true }) +
          '<button class="button button--primary" type="submit">Cadastrar usuário</button></form>' +
          '<form class="inline-form separated" data-form="search-user">' +
          formField('Matrícula para busca', 'matricula', 'text', { required: true }) +
          '<button class="button button--secondary" type="submit">Buscar</button></form>' +
          output('users-output', 'O usuário aparecerá aqui.')
      ) +
      panel(
        'Atribuir acesso',
        'Associe papel, setor e turno a um usuário.',
        '<form class="form-grid" data-form="assign-user">' +
          formField('ID do usuário', 'userId', 'text', { required: true }) +
          selectField('Papel', 'papel', roles, { required: true }) +
          '<label class="field"><span>Setor (quando aplicável)</span><select name="setorId" data-select="sector"><option value="">Sem setor</option></select></label>' +
          '<label class="field"><span>Turno (quando aplicável)</span><select name="turnoId" data-select="shift"><option value="">Sem turno</option></select></label>' +
          formField('Início da vigência', 'inicioVigencia', 'datetime-local', {}) +
          formField('Fim da vigência', 'fimVigencia', 'datetime-local', {}) +
          '<button class="button button--primary" type="submit">Criar atribuição</button></form>' +
          output('assign-output', 'A atribuição criada aparecerá aqui.')
      ) +
      panel(
        'Meus contextos',
        'Valide os setores e turnos associados à sessão atual.',
        '<button class="button button--secondary" data-action="load-contexts">Consultar contextos</button>' +
          output('contexts-output', 'Nenhum contexto consultado.')
      ) +
      '</section>'
    );
  }

  function assetsPage() {
    return (
      pageHeader('INVENTÁRIO', 'Gestão de Ativos', 'Opere os endpoints universais de tipos, ativos e movimentações.') +
      '<section class="workspace-grid">' +
      panel(
        'Tipos de ativo',
        'Consulte os padrões ou crie um novo tipo.',
        '<form class="form-grid" data-form="create-asset-type">' +
          formField('Código', 'codigo', 'text', { placeholder: 'MONITOR', required: true }) +
          formField('Nome', 'nome', 'text', { placeholder: 'Monitor', required: true }) +
          '<label class="check-field"><input type="checkbox" name="controlaPool"><span>Controla pool</span></label>' +
          '<div class="button-row"><button class="button button--primary" type="submit">Cadastrar</button><button class="button button--secondary" type="button" data-action="load-asset-types">Listar tipos</button></div></form>' +
          output('asset-types-output', 'Carregue os tipos de ativo.')
      ) +
      panel(
        'Cadastrar ativo',
        'Número de série é obrigatório; patrimônio é opcional.',
        '<form class="form-grid" data-form="create-asset">' +
          '<label class="field"><span>Tipo</span><select name="tipoAtivoId" data-select="asset-type" required><option value="">Carregue os tipos</option></select></label>' +
          formField('Número de série', 'numeroSerie', 'text', { required: true }) +
          formField('Patrimônio', 'patrimonio', 'text', {}) +
          formField('Fabricante', 'fabricante', 'text', {}) +
          formField('Modelo', 'modelo', 'text', {}) +
          '<label class="field field--full"><span>Observação</span><textarea name="observacao"></textarea></label>' +
          '<button class="button button--primary" type="submit">Cadastrar ativo</button></form>' +
          output('create-asset-output', 'O ativo cadastrado aparecerá aqui.')
      ) +
      panel(
        'Pesquisar inventário',
        'Use qualquer combinação de filtros.',
        '<form class="form-grid" data-form="search-assets">' +
          formField('Número de série', 'numeroSerie', 'text', {}) +
          formField('Patrimônio', 'patrimonio', 'text', {}) +
          formField('Tipo (código)', 'tipo', 'text', { placeholder: 'PDA' }) +
          selectField('Situação', 'situacaoPatrimonial', ['', 'EM_PREPARACAO', 'ATIVO', 'AGUARDANDO_BAIXA', 'BAIXADO'], { empty: false }) +
          selectField('Disponibilidade', 'disponibilidade', ['', 'DISPONIVEL', 'INDISPONIVEL'], { empty: false }) +
          formField('Tamanho da página', 'tamanho', 'number', { value: 20, min: 1 }) +
          '<button class="button button--secondary" type="submit">Pesquisar</button></form>' +
          output('assets-output', 'A lista de ativos aparecerá aqui.')
      ) +
      panel(
        'Operações do ativo',
        'Consulte, libere, aloque ou corrija a identificação.',
        '<form class="form-grid" data-form="asset-operation">' +
          formField('ID do ativo', 'assetId', 'text', { required: true }) +
          selectField('Operação', 'operation', [
            { value: 'detail', label: 'Consultar detalhes' },
            { value: 'release', label: 'Liberar ativo' },
            { value: 'allocate', label: 'Alocar ao setor' },
            { value: 'correct', label: 'Corrigir identificação' }
          ], { empty: false }) +
          '<label class="field"><span>Setor (para alocação)</span><select name="setorId" data-select="sector"><option value="">Carregue os setores</option></select></label>' +
          formField('Novo número de série', 'numeroSerie', 'text', {}) +
          formField('Novo patrimônio', 'patrimonio', 'text', {}) +
          '<label class="check-field"><input type="checkbox" name="removerPatrimonio"><span>Remover patrimônio</span></label>' +
          formField('Motivo', 'motivo', 'text', { placeholder: 'Obrigatório para alocar ou corrigir' }) +
          '<button class="button button--primary" type="submit">Executar operação</button></form>' +
          output('asset-operation-output', 'O resultado da operação aparecerá aqui.')
      ) +
      '</section>'
    );
  }

  function itsmPage() {
    return (
      pageHeader('SUPORTE', 'Atendimento ITSM', 'Abra e acompanhe chamados usando a sessão atual.') +
      '<section class="workspace-grid">' +
      panel(
        'Abrir chamado',
        'A descrição é obrigatória.',
        '<form class="form-grid" data-form="create-ticket">' +
          '<label class="field field--full"><span>Descrição</span><textarea name="descricao" required placeholder="Descreva o problema e os testes realizados"></textarea></label>' +
          formField('Telefone de contato', 'telefoneContato', 'text', {}) +
          formField('Identificador externo', 'identificadorExterno', 'text', { placeholder: 'Evita chamados duplicados' }) +
          '<button class="button button--primary" type="submit">Abrir chamado</button></form>' +
          output('create-ticket-output', 'O protocolo criado aparecerá aqui.')
      ) +
      panel(
        'Meus chamados',
        'Lista paginada dos chamados da sessão.',
        '<form class="inline-form" data-form="list-tickets">' +
          formField('Página', 'pagina', 'number', { value: 0, min: 0 }) +
          formField('Quantidade', 'tamanho', 'number', { value: 20, min: 1 }) +
          '<button class="button button--secondary" type="submit">Carregar</button></form>' +
          output('tickets-output', 'Nenhum chamado carregado.')
      ) +
      panel(
        'Localizar chamado',
        'Busque pelo ID interno ou pelo protocolo.',
        '<form class="form-grid" data-form="find-ticket">' +
          selectField('Tipo de busca', 'type', [
            { value: 'id', label: 'ID interno' },
            { value: 'protocol', label: 'Protocolo' }
          ], { empty: false }) +
          formField('Valor', 'value', 'text', { required: true }) +
          '<button class="button button--secondary" type="submit">Buscar chamado</button></form>' +
          output('ticket-detail-output', 'O detalhe do chamado aparecerá aqui.')
      ) +
      '</section>'
    );
  }

  function plannerPage() {
    return (
      pageHeader('PLANEJAMENTO LOCAL', 'Planner da T.I.', 'Protótipo funcional salvo neste navegador enquanto a API do Planner não existe.') +
      '<section class="notice notice--warning"><strong>Sem endpoint no backend</strong><span>As tarefas desta página ficam somente no navegador e não entram no banco MySQL.</span></section>' +
      '<section class="planner-layout">' +
      panel(
        'Nova tarefa',
        'Crie itens para validar o fluxo visual do futuro Planner.',
        '<form class="form-grid" data-form="create-task">' +
          formField('Título', 'titulo', 'text', { required: true }) +
          formField('Responsável', 'responsavel', 'text', {}) +
          selectField('Prioridade', 'prioridade', ['BAIXA', 'MEDIA', 'ALTA', 'URGENTE'], { empty: false }) +
          formField('Prazo', 'prazo', 'date', {}) +
          '<button class="button button--primary" type="submit">Adicionar tarefa</button></form>'
      ) +
      '<div class="planner-board" id="planner-board"></div>' +
      '</section>'
    );
  }

  function consolePage() {
    return (
      pageHeader('FERRAMENTA TÉCNICA', 'Console da API', 'Envie requisições livres e veja status, tempo e JSON sem abrir o Postman.') +
      '<section class="console-layout">' +
      panel(
        'Montar requisição',
        'O token da sessão é incluído automaticamente.',
        '<form class="form-grid" data-form="api-console">' +
          selectField('Método', 'method', ['GET', 'POST', 'PUT', 'PATCH', 'DELETE'], { empty: false }) +
          formField('Endpoint', 'path', 'text', { value: '/v1/me', required: true }) +
          formField('Idempotency-Key', 'idempotencyKey', 'text', {}) +
          '<label class="field field--full"><span>Corpo JSON</span><textarea name="body" class="code-input" placeholder=\'{"campo":"valor"}\'></textarea></label>' +
          '<button class="button button--primary" type="submit">Enviar requisição</button></form>' +
          '<div class="endpoint-shortcuts"><button data-endpoint="/v1/me">Minha sessão</button><button data-endpoint="/v1/setores">Setores</button><button data-endpoint="/v1/turnos">Turnos</button><button data-endpoint="/v1/tipos-ativo">Tipos de ativo</button><button data-endpoint="/v1/ativos">Ativos</button><button data-endpoint="/v1/chamados/meus">Meus chamados</button></div>',
        'work-panel--wide'
      ) +
      output('console-output', 'A resposta completa aparecerá aqui.')
      + '</section>'
    );
  }

  function renderPage() {
    var content;
    switch (page) {
      case 'hub-pdas':
        content = hubPage();
        break;
      case 'gestao-operacional':
        content = operationalPage();
        break;
      case 'gestao-ativos':
        content = assetsPage();
        break;
      case 'atendimento-itsm':
        content = itsmPage();
        break;
      case 'planner-ti':
        content = plannerPage();
        break;
      case 'console-api':
        content = consolePage();
        break;
      default:
        content = homePage();
    }
    document.getElementById('app').innerHTML = shell(content);
    if (page === 'planner-ti') renderPlanner();
    updateDynamicSelects();
    checkHealth();
  }

  function formData(form) {
    var data = {};
    new FormData(form).forEach(function (value, key) {
      data[key] = typeof value === 'string' ? value.trim() : value;
    });
    Array.prototype.forEach.call(form.querySelectorAll('input[type="checkbox"]'), function (input) {
      data[input.name] = input.checked;
    });
    return data;
  }

  function cleanObject(object) {
    Object.keys(object).forEach(function (key) {
      if (object[key] === '' || object[key] === undefined) delete object[key];
    });
    return object;
  }

  function isoOrNull(value) {
    return value ? new Date(value).toISOString() : null;
  }

  function setBusy(element, busy) {
    if (!element) return;
    element.disabled = busy;
    if (busy) {
      element.setAttribute('data-original-text', element.textContent);
      element.textContent = 'Aguarde...';
    } else if (element.getAttribute('data-original-text')) {
      element.textContent = element.getAttribute('data-original-text');
      element.removeAttribute('data-original-text');
    }
  }

  function showResult(targetId, result, error) {
    var target = document.getElementById(targetId);
    if (!target) return;
    var payload = error && error.result ? error.result : result;
    var status = payload && payload.status;
    var data = payload ? payload.data : { mensagem: error ? error.message : 'Sem resposta.' };
    target.className = 'result-box ' + (error ? 'result-box--error' : 'result-box--success');
    target.innerHTML =
      '<div class="result-meta"><span class="http-status">' +
      (status || (error && error.network ? 'REDE' : 'OK')) +
      '</span><span>' +
      escapeHtml(payload && payload.method ? payload.method : '') +
      '</span><span>' +
      escapeHtml(payload && payload.elapsed !== undefined ? payload.elapsed + ' ms' : '') +
      '</span></div><pre>' +
      escapeHtml(JSON.stringify(data, null, 2)) +
      '</pre>';
  }

  function toast(message, kind) {
    var region = document.getElementById('toast-region');
    if (!region) return;
    var item = document.createElement('div');
    item.className = 'toast toast--' + (kind || 'success');
    item.textContent = message;
    region.appendChild(item);
    window.setTimeout(function () {
      item.remove();
    }, 3500);
  }

  async function run(targetId, callback, trigger) {
    setBusy(trigger, true);
    try {
      var result = await callback();
      showResult(targetId, result, null);
      return result;
    } catch (error) {
      showResult(targetId, null, error);
      toast(error.message, 'error');
      throw error;
    } finally {
      setBusy(trigger, false);
    }
  }

  async function checkHealth() {
    var element = document.getElementById('api-health');
    if (!element) return;
    try {
      await Api.health();
      element.classList.add('api-health--online');
      element.querySelector('strong').textContent = 'API online';
    } catch (_) {
      element.classList.add('api-health--offline');
      element.querySelector('strong').textContent = 'API indisponível';
    }
  }

  async function loadSectors(targetId, trigger) {
    var result = await run(targetId || 'sectors-output', function () {
      return Api.request('/v1/setores');
    }, trigger);
    state.setores = result.data || [];
    updateDynamicSelects();
    return result;
  }

  async function loadShifts(targetId, trigger) {
    var result = await run(targetId || 'shifts-output', function () {
      return Api.request('/v1/turnos');
    }, trigger);
    state.turnos = result.data || [];
    updateDynamicSelects();
    return result;
  }

  async function loadAssetTypes(targetId, trigger) {
    var result = await run(targetId || 'asset-types-output', function () {
      return Api.request('/v1/tipos-ativo');
    }, trigger);
    state.tiposAtivo = result.data || [];
    updateDynamicSelects();
    return result;
  }

  async function loadContexts(targetId, trigger) {
    var result = await run(targetId || 'contexts-output', function () {
      return Api.request('/v1/me/contextos-operacionais');
    }, trigger);
    state.contextos = result.data || [];
    updateDynamicSelects();
    return result;
  }

  function optionsHtml(items, emptyText) {
    return (
      '<option value="">' +
      escapeHtml(emptyText || 'Selecione') +
      '</option>' +
      items
        .map(function (item) {
          return '<option value="' + escapeHtml(item.id) + '">' + escapeHtml(item.codigo + ' — ' + item.nome) + '</option>';
        })
        .join('')
    );
  }

  function updateDynamicSelects() {
    Array.prototype.forEach.call(document.querySelectorAll('[data-select="sector"]'), function (select) {
      select.innerHTML = optionsHtml(state.setores, 'Sem setor');
    });
    Array.prototype.forEach.call(document.querySelectorAll('[data-select="shift"]'), function (select) {
      select.innerHTML = optionsHtml(state.turnos, 'Sem turno');
    });
    Array.prototype.forEach.call(document.querySelectorAll('[data-select="asset-type"]'), function (select) {
      select.innerHTML = optionsHtml(state.tiposAtivo, 'Selecione o tipo');
    });

    var setores = [];
    var turnos = [];
    state.contextos.forEach(function (contexto) {
      if (contexto.setor && !setores.some(function (item) { return item.id === contexto.setor.id; })) setores.push(contexto.setor);
      if (contexto.turno && !turnos.some(function (item) { return item.id === contexto.turno.id; })) turnos.push(contexto.turno);
    });
    Array.prototype.forEach.call(document.querySelectorAll('[data-select="context-setor"]'), function (select) {
      select.innerHTML = optionsHtml(setores, 'Selecione o setor');
    });
    Array.prototype.forEach.call(document.querySelectorAll('[data-select="context-turno"]'), function (select) {
      select.innerHTML = optionsHtml(turnos, 'Selecione o turno');
    });
  }

  function plannerTasks() {
    var raw = readLocal('gerenciamentoit.plannerTasks');
    try {
      return raw ? JSON.parse(raw) : [];
    } catch (_) {
      return [];
    }
  }

  function savePlanner(tasks) {
    writeLocal('gerenciamentoit.plannerTasks', JSON.stringify(tasks));
    renderPlanner();
  }

  function renderPlanner() {
    var board = document.getElementById('planner-board');
    if (!board) return;
    var tasks = plannerTasks();
    var columns = [
      { id: 'A_FAZER', label: 'A fazer' },
      { id: 'EM_ANDAMENTO', label: 'Em andamento' },
      { id: 'CONCLUIDO', label: 'Concluído' }
    ];
    board.innerHTML = columns
      .map(function (column) {
        var cards = tasks
          .filter(function (task) { return task.status === column.id; })
          .map(function (task) {
            return (
              '<article class="task-card"><span class="task-priority task-priority--' +
              task.prioridade.toLowerCase() +
              '">' +
              escapeHtml(task.prioridade) +
              '</span><h3>' +
              escapeHtml(task.titulo) +
              '</h3><p>' +
              escapeHtml(task.responsavel || 'Sem responsável') +
              '</p><small>' +
              escapeHtml(task.prazo || 'Sem prazo') +
              '</small><div class="task-actions">' +
              (column.id !== 'A_FAZER' ? '<button data-task-action="back" data-task-id="' + task.id + '">←</button>' : '') +
              '<button data-task-action="delete" data-task-id="' + task.id + '">Excluir</button>' +
              (column.id !== 'CONCLUIDO' ? '<button data-task-action="next" data-task-id="' + task.id + '">→</button>' : '') +
              '</div></article>'
            );
          })
          .join('');
        return '<section class="planner-column"><header><h2>' + column.label + '</h2><span>' + (cards ? tasks.filter(function (task) { return task.status === column.id; }).length : 0) + '</span></header>' + (cards || '<p class="empty-column">Nenhuma tarefa</p>') + '</section>';
      })
      .join('');
  }

  async function handleSubmit(form, submitter) {
    var kind = form.getAttribute('data-form');
    var data = formData(form);
    var result;

    if (kind === 'login') {
      Api.setBaseUrl(document.getElementById('api-url').value);
      await Api.login(data.matricula, currentModule ? currentModule.origin : 'API');
      toast('Sessão criada com sucesso.');
      renderPage();
      return;
    }

    if (kind === 'quick-request') {
      return run('quick-output', function () {
        return Api.request(data.path, { method: data.method });
      }, submitter);
    }

    if (kind === 'open-conference') {
      result = await run('open-conference-output', function () {
        return Api.request('/v1/conferencias', {
          method: 'POST',
          body: { setorId: data.setorId, turnoId: data.turnoId, tipo: data.tipo }
        });
      }, submitter);
      state.ultimaConferencia = result.data.id;
      writeLocal('gerenciamentoit.ultimaConferencia', result.data.id);
      Array.prototype.forEach.call(document.querySelectorAll('[name="conferenceId"]'), function (input) {
        input.value = result.data.id;
      });
      return;
    }

    if (kind === 'conference-read') {
      return run('conference-read-output', function () {
        return Api.request('/v1/conferencias/' + encodeURIComponent(data.conferenceId) + '/leituras', {
          method: 'POST',
          body: { codigo: data.codigo }
        });
      }, submitter);
    }

    if (kind === 'conference-query') {
      var operation = submitter && submitter.value ? submitter.value : data.operation;
      var suffix = operation === 'items' ? '/itens' : operation === 'reads' ? '/leituras' : operation === 'finish' ? '/conclusoes' : '';
      return run('conference-query-output', function () {
        return Api.request('/v1/conferencias/' + encodeURIComponent(data.conferenceId) + suffix, {
          method: operation === 'finish' ? 'POST' : 'GET'
        });
      }, submitter);
    }

    if (kind === 'create-sector') {
      result = await run('sectors-output', function () {
        return Api.request('/v1/setores', {
          method: 'POST',
          body: { codigo: data.codigo, nome: data.nome, cotaPdas: Number(data.cotaPdas) }
        });
      }, submitter);
      await loadSectors('sectors-output');
      return result;
    }

    if (kind === 'create-shift') {
      result = await run('shifts-output', function () {
        return Api.request('/v1/turnos', {
          method: 'POST',
          body: cleanObject({ codigo: data.codigo, nome: data.nome, horaInicio: data.horaInicio, horaFim: data.horaFim })
        });
      }, submitter);
      await loadShifts('shifts-output');
      return result;
    }

    if (kind === 'create-user') {
      result = await run('users-output', function () {
        return Api.request('/v1/usuarios', { method: 'POST', body: { matricula: data.matricula, nome: data.nome } });
      }, submitter);
      state.ultimoUsuario = result.data;
      var userId = document.querySelector('[data-form="assign-user"] [name="userId"]');
      if (userId) userId.value = result.data.id;
      return;
    }

    if (kind === 'search-user') {
      result = await run('users-output', function () {
        return Api.request('/v1/usuarios' + Api.query({ matricula: data.matricula }));
      }, submitter);
      state.ultimoUsuario = result.data;
      var foundUserId = document.querySelector('[data-form="assign-user"] [name="userId"]');
      if (foundUserId) foundUserId.value = result.data.id;
      return;
    }

    if (kind === 'assign-user') {
      return run('assign-output', function () {
        return Api.request('/v1/usuarios/' + encodeURIComponent(data.userId) + '/atribuicoes', {
          method: 'POST',
          body: {
            papel: data.papel,
            setorId: data.setorId || null,
            turnoId: data.turnoId || null,
            inicioVigencia: isoOrNull(data.inicioVigencia),
            fimVigencia: isoOrNull(data.fimVigencia)
          }
        });
      }, submitter);
    }

    if (kind === 'create-asset-type') {
      result = await run('asset-types-output', function () {
        return Api.request('/v1/tipos-ativo', {
          method: 'POST',
          body: { codigo: data.codigo, nome: data.nome, controlaPool: data.controlaPool }
        });
      }, submitter);
      await loadAssetTypes('asset-types-output');
      return result;
    }

    if (kind === 'create-asset') {
      return run('create-asset-output', function () {
        return Api.request('/v1/ativos', {
          method: 'POST',
          body: cleanObject({
            tipoAtivoId: data.tipoAtivoId,
            numeroSerie: data.numeroSerie,
            patrimonio: data.patrimonio,
            fabricante: data.fabricante,
            modelo: data.modelo,
            observacao: data.observacao
          })
        });
      }, submitter);
    }

    if (kind === 'search-assets') {
      return run('assets-output', function () {
        return Api.request('/v1/ativos' + Api.query(cleanObject(data)));
      }, submitter);
    }

    if (kind === 'asset-operation') {
      var path = '/v1/ativos/' + encodeURIComponent(data.assetId);
      var options = { method: 'GET' };
      if (data.operation === 'release') {
        path += '/liberacoes';
        options.method = 'POST';
      } else if (data.operation === 'allocate') {
        path += '/alocacoes-setor';
        options = { method: 'POST', body: { setorId: data.setorId, motivo: data.motivo } };
      } else if (data.operation === 'correct') {
        path += '/correcoes-identificacao';
        options = {
          method: 'POST',
          body: {
            numeroSerie: data.numeroSerie,
            patrimonio: data.patrimonio || null,
            removerPatrimonio: data.removerPatrimonio,
            motivo: data.motivo
          }
        };
      }
      return run('asset-operation-output', function () {
        return Api.request(path, options);
      }, submitter);
    }

    if (kind === 'create-ticket') {
      var headers = {};
      if (data.identificadorExterno) headers['Idempotency-Key'] = data.identificadorExterno;
      return run('create-ticket-output', function () {
        return Api.request('/v1/chamados', {
          method: 'POST',
          headers: headers,
          body: cleanObject({
            descricao: data.descricao,
            telefoneContato: data.telefoneContato,
            identificadorExterno: data.identificadorExterno
          })
        });
      }, submitter);
    }

    if (kind === 'list-tickets') {
      return run('tickets-output', function () {
        return Api.request('/v1/chamados/meus' + Api.query(data));
      }, submitter);
    }

    if (kind === 'find-ticket') {
      var ticketPath = data.type === 'protocol'
        ? '/v1/chamados/meus/protocolo/' + encodeURIComponent(data.value)
        : '/v1/chamados/meus/' + encodeURIComponent(data.value);
      return run('ticket-detail-output', function () {
        return Api.request(ticketPath);
      }, submitter);
    }

    if (kind === 'create-task') {
      var tasks = plannerTasks();
      tasks.push({
        id: String(Date.now()),
        titulo: data.titulo,
        responsavel: data.responsavel,
        prioridade: data.prioridade,
        prazo: data.prazo,
        status: 'A_FAZER'
      });
      savePlanner(tasks);
      form.reset();
      toast('Tarefa adicionada ao Planner local.');
      return;
    }

    if (kind === 'api-console') {
      var parsedBody;
      if (data.body) {
        try {
          parsedBody = JSON.parse(data.body);
        } catch (_) {
          showResult('console-output', null, new Error('O corpo informado não é um JSON válido.'));
          return;
        }
      }
      var consoleHeaders = {};
      if (data.idempotencyKey) consoleHeaders['Idempotency-Key'] = data.idempotencyKey;
      return run('console-output', function () {
        return Api.request(data.path, {
          method: data.method,
          headers: consoleHeaders,
          body: parsedBody
        });
      }, submitter);
    }
  }

  async function handleAction(action, element) {
    if (action === 'toggle-session') {
      var panelElement = document.getElementById('session-panel');
      panelElement.hidden = !panelElement.hidden;
      return;
    }
    if (action === 'save-api-url') {
      Api.setBaseUrl(document.getElementById('api-url').value);
      toast('Endereço da API salvo.');
      renderPage();
      return;
    }
    if (action === 'logout') {
      try {
        await Api.logout();
      } catch (_) {
        // A sessão local é removida mesmo que a API esteja indisponível.
      }
      toast('Sessão encerrada.');
      renderPage();
      return;
    }
    if (action === 'load-sectors') return loadSectors('sectors-output', element);
    if (action === 'load-shifts') return loadShifts('shifts-output', element);
    if (action === 'load-asset-types') return loadAssetTypes('asset-types-output', element);
    if (action === 'load-contexts') return loadContexts('contexts-output', element);
  }

  document.addEventListener('submit', function (event) {
    var form = event.target.closest('[data-form]');
    if (!form) return;
    event.preventDefault();
    handleSubmit(form, event.submitter).catch(function () {
      // O erro já foi apresentado no painel correspondente.
    });
  });

  document.addEventListener('click', function (event) {
    var actionElement = event.target.closest('[data-action]');
    if (actionElement) {
      event.preventDefault();
      handleAction(actionElement.getAttribute('data-action'), actionElement).catch(function (error) {
        toast(error.message, 'error');
      });
      return;
    }

    var endpoint = event.target.closest('[data-endpoint]');
    if (endpoint) {
      var pathInput = document.querySelector('[data-form="api-console"] [name="path"]');
      var methodInput = document.querySelector('[data-form="api-console"] [name="method"]');
      if (pathInput) pathInput.value = endpoint.getAttribute('data-endpoint');
      if (methodInput) methodInput.value = 'GET';
      return;
    }

    var taskAction = event.target.closest('[data-task-action]');
    if (taskAction) {
      var tasks = plannerTasks();
      var id = taskAction.getAttribute('data-task-id');
      var operation = taskAction.getAttribute('data-task-action');
      var order = ['A_FAZER', 'EM_ANDAMENTO', 'CONCLUIDO'];
      if (operation === 'delete') {
        tasks = tasks.filter(function (task) { return task.id !== id; });
      } else {
        tasks.forEach(function (task) {
          if (task.id !== id) return;
          var index = order.indexOf(task.status);
          task.status = order[operation === 'next' ? Math.min(index + 1, 2) : Math.max(index - 1, 0)];
        });
      }
      savePlanner(tasks);
    }
  });

  document.addEventListener('click', function (event) {
    if (event.target.closest('#menu-button')) {
      var sidebar = document.getElementById('sidebar');
      var scrim = document.getElementById('sidebar-scrim');
      sidebar.classList.toggle('sidebar--open');
      scrim.classList.toggle('sidebar-scrim--visible');
    } else if (event.target.id === 'sidebar-scrim') {
      document.getElementById('sidebar').classList.remove('sidebar--open');
      event.target.classList.remove('sidebar-scrim--visible');
    }
  });

  window.addEventListener('git:session-changed', function () {
    renderPage();
  });

  renderPage();

  if (page === 'gestao-operacional' && Api.getToken()) {
    Promise.allSettled([loadSectors('sectors-output'), loadShifts('shifts-output')]);
  }
  if (page === 'gestao-ativos' && Api.getToken()) {
    Promise.allSettled([loadAssetTypes('asset-types-output'), loadSectors(null)]);
  }
  if (page === 'hub-pdas' && Api.getToken()) {
    loadContexts('contexts-output').catch(function () {});
  }
})();
