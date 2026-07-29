import './styles.css'

type AppStatus = 'Em teste' | 'Fundação pronta' | 'Planejado'

type FrontendApp = {
  id: string
  initials: string
  name: string
  shortName: string
  description: string
  status: AppStatus
  stage: string
  audience: string[]
  href: string
  externalUrl?: string
  available: boolean
  accent: string
  scope: string[]
  ready: string[]
  next: string[]
}

const hubUrl = import.meta.env.VITE_HUB_PDAS_URL || 'http://localhost:5173'
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || '/api'

const apps: FrontendApp[] = [
  {
    id: 'hub-pdas',
    initials: 'PDA',
    name: 'Hub de PDAs',
    shortName: 'Hub de PDAs',
    description:
      'Operação e conferência dos coletores por setor, com visão rápida dos pools.',
    status: 'Em teste',
    stage: 'Conferência online conectada à API',
    audience: ['Líder', 'Supervisor', 'T.I.'],
    href: '/apps/hub-pdas.html',
    externalUrl: hubUrl,
    available: true,
    accent: '#2b75ff',
    scope: [
      'Conferência do pool por setor',
      'Identificação do usuário e da sessão',
      'Registro de PDAs presentes, ausentes e divergentes',
      'Histórico de conferências e movimentações',
    ],
    ready: [
      'SPA desktop-first criada',
      'Integração real com a API local',
      'Sessão e consulta dos tipos de ativo',
      'Fluxo inicial de conferência online',
    ],
    next: [
      'Fechar todas as ações da tela de conferência',
      'Validar cenários de erro e concorrência',
      'Consolidar permissões de Líder, Supervisor e T.I.',
    ],
  },
  {
    id: 'gestao-operacional',
    initials: 'OPS',
    name: 'Gestão Operacional',
    shortName: 'Operacional',
    description:
      'Administração da estrutura operacional, pessoas, setores, turnos e acessos.',
    status: 'Planejado',
    stage: 'Escopo organizado para desenvolvimento',
    audience: ['Supervisor', 'T.I.'],
    href: '/apps/gestao-operacional.html',
    available: false,
    accent: '#8a5cf6',
    scope: [
      'Usuários, papéis e permissões',
      'Setores, turnos e vínculos',
      'Equipes e responsabilidades',
      'Auditoria das alterações administrativas',
    ],
    ready: [
      'Domínio correspondente existente na API',
      'Papéis-base definidos: Usuário, Líder, Supervisor e T.I.',
    ],
    next: [
      'Definir jornadas e matriz de permissões',
      'Criar wireframes desktop-first',
      'Implementar cadastros universais consumindo a API',
    ],
  },
  {
    id: 'gestao-ativos',
    initials: 'ATM',
    name: 'Gestão de Ativos da T.I.',
    shortName: 'Ativos de T.I.',
    description:
      'Inventário, ciclo de vida, localização e movimentação dos ativos de tecnologia.',
    status: 'Fundação pronta',
    stage: 'Recursos universais disponíveis na API',
    audience: ['T.I.'],
    href: '/apps/gestao-ativos.html',
    available: false,
    accent: '#00a882',
    scope: [
      'Cadastro universal de ativos e tipos',
      'Número de série, patrimônio e estado',
      'Movimentações entre pessoas e setores',
      'Histórico e rastreabilidade do ciclo de vida',
    ],
    ready: [
      'Tipos de ativo universais na API',
      'Entidades de ativos e movimentações no backend',
      'PDA modelado como um tipo de ativo',
    ],
    next: [
      'Fechar regras obrigatórias por tipo de ativo',
      'Criar telas de inventário e movimentação',
      'Adicionar filtros, exportação e histórico',
    ],
  },
  {
    id: 'atendimento-itsm',
    initials: 'IT',
    name: 'Atendimento ITSM',
    shortName: 'Atendimento ITSM',
    description:
      'Entrada, acompanhamento e tratamento de solicitações e incidentes de T.I.',
    status: 'Fundação pronta',
    stage: 'Chamados disponíveis no domínio da API',
    audience: ['Usuário', 'Líder', 'Supervisor', 'T.I.'],
    href: '/apps/atendimento-itsm.html',
    available: false,
    accent: '#ef7a2d',
    scope: [
      'Abertura e acompanhamento de chamados',
      'Fila e atribuição para a equipe de T.I.',
      'Prioridade, categoria, status e SLA',
      'Comunicação e histórico do atendimento',
    ],
    ready: [
      'Base de chamados existente no backend',
      'Separação do frontend de atendimento definida',
    ],
    next: [
      'Definir catálogo e fluxo de status',
      'Criar visão do solicitante e fila da T.I.',
      'Implementar comentários, anexos e notificações',
    ],
  },
  {
    id: 'planner-ti',
    initials: 'PLN',
    name: 'Planner da T.I.',
    shortName: 'Planner da T.I.',
    description:
      'Planejamento do trabalho interno, prioridades, responsáveis e entregas da equipe.',
    status: 'Planejado',
    stage: 'Produto reservado para uma etapa futura',
    audience: ['T.I.'],
    href: '/apps/planner-ti.html',
    available: false,
    accent: '#d3486f',
    scope: [
      'Backlog e quadro de trabalho',
      'Prioridades, responsáveis e prazos',
      'Relacionamento com chamados e projetos',
      'Visão de capacidade e entregas',
    ],
    ready: ['Separação como frontend independente definida'],
    next: [
      'Definir o recorte mínimo do produto',
      'Decidir o vínculo com o módulo de atendimento',
      'Modelar os endpoints universais necessários',
    ],
  },
]

const pageId = document.body.dataset.page || 'home'
const currentApp = apps.find((item) => item.id === pageId)
const root = document.querySelector<HTMLDivElement>('#app')

if (!root) {
  throw new Error('Elemento principal do portal não encontrado.')
}

const statusClass = (status: AppStatus) =>
  status === 'Em teste'
    ? 'status--testing'
    : status === 'Fundação pronta'
      ? 'status--foundation'
      : 'status--planned'

const icon = (name: 'grid' | 'activity' | 'users' | 'arrow' | 'menu' | 'close') => {
  const paths = {
    grid: '<rect x="3" y="3" width="7" height="7" rx="2"/><rect x="14" y="3" width="7" height="7" rx="2"/><rect x="3" y="14" width="7" height="7" rx="2"/><rect x="14" y="14" width="7" height="7" rx="2"/>',
    activity:
      '<path d="M3 12h4l2.3-6 4.4 12 2.3-6H21"/><path d="M4 4h16a1 1 0 0 1 1 1v14a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1Z"/>',
    users:
      '<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>',
    arrow: '<path d="M5 12h14M13 6l6 6-6 6"/>',
    menu: '<path d="M4 6h16M4 12h16M4 18h16"/>',
    close: '<path d="m6 6 12 12M18 6 6 18"/>',
  }

  return `<svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">${paths[name]}</svg>`
}

const navItems = apps
  .map(
    (item) => `
      <a class="nav-item ${item.id === pageId ? 'nav-item--active' : ''}" href="${item.href}">
        <span class="nav-monogram" style="--app-accent: ${item.accent}">${item.initials}</span>
        <span>
          <strong>${item.shortName}</strong>
          <small>${item.status}</small>
        </span>
      </a>
    `,
  )
  .join('')

const shell = (content: string) => `
  <div class="portal-shell">
    <aside class="sidebar" id="sidebar">
      <div class="brand">
        <span class="brand-mark">G</span>
        <span>
          <strong>GerenciamentoIT</strong>
          <small>Portal de desenvolvimento</small>
        </span>
      </div>

      <nav aria-label="Frontends do projeto">
        <a class="nav-item nav-item--home ${pageId === 'home' ? 'nav-item--active' : ''}" href="/">
          <span class="nav-icon">${icon('grid')}</span>
          <span><strong>Visão geral</strong><small>Mapa do projeto</small></span>
        </a>
        <p class="nav-label">Aplicações</p>
        ${navItems}
      </nav>

      <div class="sidebar-footer">
        <span class="environment-dot"></span>
        <span><strong>Ambiente local</strong><small>Pré-MVP</small></span>
      </div>
    </aside>

    <div class="sidebar-scrim" id="sidebar-scrim"></div>

    <main class="main">
      <header class="topbar">
        <button class="menu-button" id="menu-button" type="button" aria-label="Abrir menu">
          ${icon('menu')}
        </button>
        <div>
          <span class="eyebrow">GERENCIAMENTOIT</span>
          <strong>${currentApp?.name || 'Visão geral dos frontends'}</strong>
        </div>
        <div class="api-health" id="api-health" role="status">
          <span></span>
          <div><strong>Verificando API</strong><small>${apiBaseUrl}</small></div>
        </div>
      </header>
      <div class="content">${content}</div>
    </main>
  </div>
`

const homeContent = `
  <section class="hero">
    <div>
      <span class="section-kicker">CENTRAL DE TESTES</span>
      <h1>Todos os produtos,<br><em>sem perder o rumo.</em></h1>
      <p>
        Este portal temporário reúne os frontends do projeto, mostra o estágio real
        de cada um e mantém o acesso ao que já pode ser testado.
      </p>
    </div>
    <div class="hero-summary">
      <span class="summary-number">05</span>
      <span>frontends<br>independentes</span>
    </div>
  </section>

  <section class="metrics" aria-label="Resumo do projeto">
    <article><span class="metric-icon metric-icon--blue">${icon('activity')}</span><div><strong>1</strong><small>frontend em teste</small></div></article>
    <article><span class="metric-icon metric-icon--green">${icon('grid')}</span><div><strong>2</strong><small>com fundação na API</small></div></article>
    <article><span class="metric-icon metric-icon--purple">${icon('users')}</span><div><strong>4</strong><small>papéis do produto</small></div></article>
  </section>

  <section class="section-heading">
    <div><span class="section-kicker">APLICAÇÕES</span><h2>Mapa dos frontends</h2></div>
    <p>Cada produto evolui separadamente, compartilhando a mesma API universal.</p>
  </section>

  <section class="app-grid">
    ${apps
      .map(
        (item, index) => `
          <a class="app-card" href="${item.href}" style="--app-accent: ${item.accent}">
            <div class="card-top">
              <span class="card-index">${String(index + 1).padStart(2, '0')}</span>
              <span class="status ${statusClass(item.status)}">${item.status}</span>
            </div>
            <span class="app-symbol">${item.initials}</span>
            <h3>${item.name}</h3>
            <p>${item.description}</p>
            <div class="card-footer">
              <span>${item.audience.join(' · ')}</span>
              <span class="round-arrow">${icon('arrow')}</span>
            </div>
          </a>
        `,
      )
      .join('')}
  </section>

  <section class="role-section">
    <div class="section-heading">
      <div><span class="section-kicker">ACESSO</span><h2>Papéis do produto</h2></div>
      <p>O frontend interpreta as permissões universais devolvidas pela API.</p>
    </div>
    <div class="role-grid">
      <article><span>01</span><h3>Usuário</h3><p>Solicita e acompanha serviços. Sem função operacional no Hub por enquanto.</p></article>
      <article><span>02</span><h3>Líder</h3><p>Executa a rotina de conferência e acompanha o pool de seu setor.</p></article>
      <article><span>03</span><h3>Supervisor</h3><p>Acompanha setores, exceções e resultados da operação.</p></article>
      <article><span>04</span><h3>T.I.</h3><p>Administra ativos, acessos, suporte e evolução técnica dos produtos.</p></article>
    </div>
  </section>
`

const checklist = (items: string[], done: boolean) =>
  items
    .map(
      (item) => `
        <li>
          <span class="check ${done ? 'check--done' : ''}">${done ? '✓' : '→'}</span>
          ${item}
        </li>
      `,
    )
    .join('')

const appContent = (app: FrontendApp) => `
  <a class="back-link" href="/">${icon('arrow')} Voltar para a visão geral</a>

  <section class="app-hero" style="--app-accent: ${app.accent}">
    <div class="app-title-row">
      <span class="app-symbol app-symbol--large">${app.initials}</span>
      <div>
        <span class="status ${statusClass(app.status)}">${app.status}</span>
        <h1>${app.name}</h1>
      </div>
    </div>
    <p>${app.description}</p>
    <div class="app-actions">
      ${
        app.available && app.externalUrl
          ? `<a class="primary-action" href="${app.externalUrl}" target="_blank" rel="noreferrer">Abrir aplicação ${icon('arrow')}</a>`
          : `<span class="disabled-action" aria-disabled="true">Frontend ainda não iniciado</span>`
      }
      <span class="stage">${app.stage}</span>
    </div>
  </section>

  <section class="detail-grid">
    <article class="detail-panel detail-panel--scope">
      <span class="section-kicker">RESPONSABILIDADE</span>
      <h2>Foco deste frontend</h2>
      <ul class="numbered-list">
        ${app.scope.map((item, index) => `<li><span>${String(index + 1).padStart(2, '0')}</span>${item}</li>`).join('')}
      </ul>
    </article>
    <article class="detail-panel">
      <span class="section-kicker">PÚBLICO</span>
      <h2>Quem utiliza</h2>
      <div class="audience-list">${app.audience.map((role) => `<span>${role}</span>`).join('')}</div>
      <p class="panel-note">As ações finais serão liberadas por permissão, não apenas pelo nome do papel.</p>
    </article>
  </section>

  <section class="progress-grid">
    <article class="progress-panel">
      <span class="section-kicker">JÁ PREPARADO</span>
      <h2>Base disponível</h2>
      <ul class="checklist">${checklist(app.ready, true)}</ul>
    </article>
    <article class="progress-panel">
      <span class="section-kicker">PRÓXIMOS PASSOS</span>
      <h2>Para ficar funcional</h2>
      <ul class="checklist">${checklist(app.next, false)}</ul>
    </article>
  </section>
`

root.innerHTML = shell(currentApp ? appContent(currentApp) : homeContent)

const sidebar = document.querySelector<HTMLElement>('#sidebar')
const scrim = document.querySelector<HTMLElement>('#sidebar-scrim')
const menuButton = document.querySelector<HTMLButtonElement>('#menu-button')

const closeMenu = () => {
  sidebar?.classList.remove('sidebar--open')
  scrim?.classList.remove('sidebar-scrim--visible')
  menuButton?.setAttribute('aria-label', 'Abrir menu')
  if (menuButton) menuButton.innerHTML = icon('menu')
}

menuButton?.addEventListener('click', () => {
  const isOpen = sidebar?.classList.toggle('sidebar--open') ?? false
  scrim?.classList.toggle('sidebar-scrim--visible', isOpen)
  menuButton.setAttribute('aria-label', isOpen ? 'Fechar menu' : 'Abrir menu')
  menuButton.innerHTML = icon(isOpen ? 'close' : 'menu')
})

scrim?.addEventListener('click', closeMenu)

const healthElement = document.querySelector<HTMLElement>('#api-health')

const checkApi = async () => {
  if (!healthElement) return

  try {
    const response = await fetch(`${apiBaseUrl}/actuator/health`, {
      headers: { Accept: 'application/json' },
      signal: AbortSignal.timeout(4000),
    })

    if (!response.ok) throw new Error(`HTTP ${response.status}`)

    const payload = (await response.json()) as { status?: string }
    const online = payload.status === 'UP'
    healthElement.classList.add(online ? 'api-health--online' : 'api-health--warning')
    healthElement.querySelector('strong')!.textContent = online ? 'API online' : 'API respondeu'
  } catch {
    healthElement.classList.add('api-health--offline')
    healthElement.querySelector('strong')!.textContent = 'API indisponível'
  }
}

void checkApi()
