import { useEffect, useRef, useState } from 'react'
import type { FormEvent, ReactNode, RefObject } from 'react'
import {
  ApiRequestError,
  apiBaseUrl,
  createSession,
  getAuthenticatedUser,
  getOperationalContexts,
  revokeCurrentSession,
} from './api'
import type {
  AuthenticatedUser,
  OperationalContext,
} from './api'
import './App.css'

type Screen = 'identification' | 'context' | 'home'

type IconName =
  | 'badge'
  | 'barcode'
  | 'check'
  | 'clipboard'
  | 'home'
  | 'logout'
  | 'menu'
  | 'refresh'
  | 'settings'
  | 'warning'
  | 'wifi'

const sessionTokenKey = 'hub-pdas.session-token'

function App() {
  const [screen, setScreen] = useState<Screen>('identification')
  const [restoringSession, setRestoringSession] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [registration, setRegistration] = useState('')
  const [token, setToken] = useState<string | null>(null)
  const [user, setUser] = useState<AuthenticatedUser | null>(null)
  const [contexts, setContexts] = useState<OperationalContext[]>([])
  const [selectedContext, setSelectedContext] =
    useState<OperationalContext | null>(null)
  const [online, setOnline] = useState(navigator.onLine)
  const [notice, setNotice] = useState<string | null>(null)
  const registrationInput = useRef<HTMLInputElement>(null)

  useEffect(() => {
    const handleOnline = () => setOnline(true)
    const handleOffline = () => setOnline(false)

    window.addEventListener('online', handleOnline)
    window.addEventListener('offline', handleOffline)

    return () => {
      window.removeEventListener('online', handleOnline)
      window.removeEventListener('offline', handleOffline)
    }
  }, [])

  useEffect(() => {
    let active = true
    const savedToken = sessionStorage.getItem(sessionTokenKey)

    if (!savedToken) {
      setRestoringSession(false)
      return
    }

    Promise.all([
      getAuthenticatedUser(savedToken),
      getOperationalContexts(savedToken),
    ])
      .then(([authenticatedUser, availableContexts]) => {
        if (!active) return
        applyAuthenticatedSession(
          savedToken,
          authenticatedUser,
          availableContexts,
        )
      })
      .catch((error: unknown) => {
        if (!active) return
        if (error instanceof ApiRequestError && error.status === 401) {
          sessionStorage.removeItem(sessionTokenKey)
          setNotice('A sessão anterior expirou. Identifique-se novamente.')
        } else {
          setNotice(errorMessage(error))
        }
      })
      .finally(() => {
        if (active) setRestoringSession(false)
      })

    return () => {
      active = false
    }
  }, [])

  useEffect(() => {
    if (!restoringSession && screen === 'identification') {
      registrationInput.current?.focus()
    }
  }, [restoringSession, screen])

  useEffect(() => {
    if (!notice) return

    const timeout = window.setTimeout(() => setNotice(null), 6000)
    return () => window.clearTimeout(timeout)
  }, [notice])

  function applyAuthenticatedSession(
    sessionToken: string,
    authenticatedUser: AuthenticatedUser,
    availableContexts: OperationalContext[],
  ) {
    sessionStorage.setItem(sessionTokenKey, sessionToken)
    setToken(sessionToken)
    setUser(authenticatedUser)
    setContexts(availableContexts)

    if (availableContexts.length === 1) {
      setSelectedContext(availableContexts[0])
      setScreen('home')
      return
    }

    setSelectedContext(null)
    setScreen('context')
  }

  async function handleIdentification(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const normalizedRegistration = registration.trim()

    if (!normalizedRegistration) {
      setNotice('Bipe o crachá ou informe uma matrícula para continuar.')
      registrationInput.current?.focus()
      return
    }

    if (!navigator.onLine) {
      setNotice('O primeiro acesso real exige conexão com a API.')
      return
    }

    setSubmitting(true)
    let createdToken: string | null = null

    try {
      const createdSession = await createSession(normalizedRegistration)
      createdToken = createdSession.token
      const availableContexts = await getOperationalContexts(createdToken)
      applyAuthenticatedSession(
        createdToken,
        createdSession.usuario,
        availableContexts,
      )
      setRegistration('')
    } catch (error) {
      if (createdToken) {
        await revokeCurrentSession(createdToken).catch(() => undefined)
      }
      sessionStorage.removeItem(sessionTokenKey)
      setNotice(errorMessage(error))
      registrationInput.current?.focus()
    } finally {
      setSubmitting(false)
    }
  }

  async function handleLogout() {
    const currentToken = token
    sessionStorage.removeItem(sessionTokenKey)
    setToken(null)
    setUser(null)
    setContexts([])
    setSelectedContext(null)
    setScreen('identification')
    setNotice('Sessão encerrada.')

    if (currentToken) {
      try {
        await revokeCurrentSession(currentToken)
      } catch {
        setNotice(
          'Sessão encerrada neste terminal, mas a API não confirmou a revogação.',
        )
      }
    }
  }

  function handleContextSelection(context: OperationalContext) {
    setSelectedContext(context)
    setScreen('home')
  }

  function showPlannedFeature(feature: string) {
    setNotice(`${feature} será implementada na próxima etapa do MVP.`)
  }

  const authenticated = user !== null && token !== null

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand">
          <button
            className="icon-button menu-button"
            type="button"
            aria-label="Abrir menu"
            disabled
          >
            <Icon name="menu" />
          </button>
          <div>
            <strong>Hub de PDAs</strong>
            <span>GerenciamentoIT</span>
          </div>
        </div>

        <div className="topbar-status">
          <span className="real-api-pill">API real</span>
          <span
            className={`connection-status ${online ? 'is-online' : 'is-offline'}`}
            role="status"
          >
            <span className="connection-dot" aria-hidden="true" />
            {online ? 'Terminal online' : 'Terminal offline'}
          </span>
        </div>
      </header>

      <aside className="sidebar" aria-label="Navegação principal">
        <button
          type="button"
          className={`sidebar-button ${screen === 'home' ? 'is-active' : ''}`}
          aria-label="Início"
          onClick={() => {
            if (selectedContext) setScreen('home')
          }}
          disabled={!authenticated || !selectedContext}
        >
          <Icon name="home" />
          <span>Início</span>
        </button>
        <button
          type="button"
          className="sidebar-button"
          aria-label="Conferências"
          onClick={() => showPlannedFeature('A conferência')}
          disabled={screen !== 'home'}
        >
          <Icon name="clipboard" />
          <span>Conferir</span>
        </button>
        <button
          type="button"
          className="sidebar-button sidebar-settings"
          aria-label="Configurações"
          onClick={() => showPlannedFeature('As configurações')}
        >
          <Icon name="settings" />
          <span>Ajustes</span>
        </button>
      </aside>

      <main className="main-content">
        {restoringSession ? (
          <LoadingScreen />
        ) : screen === 'identification' ? (
          <IdentificationScreen
            registration={registration}
            inputRef={registrationInput}
            submitting={submitting}
            online={online}
            onRegistrationChange={setRegistration}
            onSubmit={handleIdentification}
          />
        ) : screen === 'context' && user ? (
          <ContextSelectionScreen
            user={user}
            contexts={contexts}
            onSelect={handleContextSelection}
            onLogout={handleLogout}
          />
        ) : user && selectedContext ? (
          <HomeScreen
            user={user}
            context={selectedContext}
            contextsCount={contexts.length}
            online={online}
            onChangeContext={() => setScreen('context')}
            onLogout={handleLogout}
            onPlannedFeature={showPlannedFeature}
          />
        ) : (
          <LoadingScreen />
        )}
      </main>

      <footer className="app-footer">
        <span>API local: {apiBaseUrl}</span>
        <span>Desktop-first • suporte a tablet em modo paisagem</span>
      </footer>

      {notice && (
        <div className="toast" role="alert">
          <Icon name="warning" />
          <span>{notice}</span>
          <button
            type="button"
            onClick={() => setNotice(null)}
            aria-label="Fechar aviso"
          >
            ×
          </button>
        </div>
      )}
    </div>
  )
}

function errorMessage(error: unknown): string {
  if (error instanceof ApiRequestError) {
    if (error.code === 'MATRICULA_NAO_ENCONTRADA') {
      return 'Matrícula não encontrada na API.'
    }
    if (error.status === 401) {
      return 'Sessão ausente, expirada ou inválida.'
    }
    return error.message
  }
  return 'Não foi possível concluir a operação.'
}

function LoadingScreen() {
  return (
    <section className="loading-screen" aria-live="polite">
      <span className="loading-spinner" aria-hidden="true" />
      <strong>Validando sessão com a API...</strong>
    </section>
  )
}

interface IdentificationScreenProps {
  registration: string
  inputRef: RefObject<HTMLInputElement | null>
  submitting: boolean
  online: boolean
  onRegistrationChange: (value: string) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
}

function IdentificationScreen({
  registration,
  inputRef,
  submitting,
  online,
  onRegistrationChange,
  onSubmit,
}: IdentificationScreenProps) {
  return (
    <section className="identification-screen" aria-labelledby="identify-title">
      <div className="identification-card">
        <div className="badge-illustration" aria-hidden="true">
          <Icon name="badge" />
          <span className="badge-signal badge-signal-left">)))</span>
          <span className="badge-signal badge-signal-right">(((</span>
        </div>

        <div className="identification-copy">
          <span className="eyebrow">Identificação do responsável</span>
          <h1 id="identify-title">Bipe seu crachá</h1>
          <p>ou digite sua matrícula para iniciar a operação real</p>
        </div>

        <form
          className="scan-form"
          onSubmit={onSubmit}
          aria-busy={submitting}
        >
          <label htmlFor="registration">Matrícula ou código do crachá</label>
          <div className="scan-input-wrapper">
            <Icon name="barcode" />
            <input
              ref={inputRef}
              id="registration"
              name="registration"
              value={registration}
              onChange={(event) => onRegistrationChange(event.target.value)}
              autoComplete="off"
              placeholder="Aguardando leitura..."
              disabled={submitting}
            />
            <button type="submit" disabled={submitting || !online}>
              {submitting ? 'Validando...' : 'Continuar'}
            </button>
          </div>
        </form>

        <div className={`ready-status ${online ? '' : 'is-unavailable'}`}>
          <Icon name={online ? 'check' : 'warning'} />
          <span>
            {online
              ? 'API real preparada para autenticação'
              : 'Sem conexão para o primeiro acesso'}
          </span>
        </div>

        <p className="simulation-hint">
          A matrícula deve existir e estar ativa na API local.
        </p>
      </div>
    </section>
  )
}

interface ContextSelectionScreenProps {
  user: AuthenticatedUser
  contexts: OperationalContext[]
  onSelect: (context: OperationalContext) => void
  onLogout: () => void
}

function ContextSelectionScreen({
  user,
  contexts,
  onSelect,
  onLogout,
}: ContextSelectionScreenProps) {
  return (
    <section className="context-screen" aria-labelledby="context-title">
      <div className="context-heading">
        <span className="eyebrow">Contexto operacional</span>
        <h1 id="context-title">Olá, {user.nome}</h1>
        <p>Selecione o setor e o turno em que você irá operar.</p>
      </div>

      {contexts.length > 0 ? (
        <div className="context-grid">
          {contexts.map((context) => (
            <button
              key={`${context.setor.id}:${context.turno.id}`}
              type="button"
              className="context-card"
              onClick={() => onSelect(context)}
            >
              <span>{context.setor.codigo}</span>
              <strong>{context.setor.nome}</strong>
              <small>
                {context.turno.nome} • cota de {context.setor.cotaPdas} PDAs
              </small>
            </button>
          ))}
        </div>
      ) : (
        <div className="empty-context">
          <Icon name="warning" />
          <div>
            <strong>Nenhum contexto operacional disponível</strong>
            <p>
              A API não encontrou uma combinação ativa de setor e turno
              autorizada para este usuário.
            </p>
          </div>
        </div>
      )}

      <button type="button" className="logout-button" onClick={onLogout}>
        <Icon name="logout" />
        <span>Sair e bloquear o terminal</span>
      </button>
    </section>
  )
}

interface HomeScreenProps {
  user: AuthenticatedUser
  context: OperationalContext
  contextsCount: number
  online: boolean
  onChangeContext: () => void
  onLogout: () => void
  onPlannedFeature: (feature: string) => void
}

function HomeScreen({
  user,
  context,
  contextsCount,
  online,
  onChangeContext,
  onLogout,
  onPlannedFeature,
}: HomeScreenProps) {
  return (
    <section className="home-screen" aria-labelledby="home-title">
      <div className="home-heading">
        <div>
          <span className="eyebrow">Operação atual</span>
          <h1 id="home-title">Bom dia, {user.nome}</h1>
          <p>
            {context.setor.nome} • {context.turno.nome}
          </p>
        </div>
        <button
          type="button"
          className="context-chip"
          onClick={onChangeContext}
          disabled={contextsCount < 2}
          title={
            contextsCount < 2
              ? 'Este usuário possui apenas um contexto'
              : 'Trocar contexto operacional'
          }
        >
          <span>Contexto autorizado</span>
          <strong>
            {context.setor.codigo} / {context.turno.codigo}
          </strong>
        </button>
      </div>

      <div className="pending-banner">
        <span className="pending-icon" aria-hidden="true">
          <Icon name="check" />
        </span>
        <div>
          <span>Integração real ativa</span>
          <strong>Cota cadastrada: {context.setor.cotaPdas} PDAs</strong>
        </div>
        <button
          type="button"
          onClick={() => onPlannedFeature('A conferência de abertura')}
        >
          Próxima etapa
        </button>
      </div>

      <div className="action-grid">
        <button
          type="button"
          className="action-card primary-action"
          onClick={() => onPlannedFeature('A conferência de abertura')}
        >
          <span className="action-icon">
            <Icon name="clipboard" />
          </span>
          <span>
            <strong>Iniciar conferência</strong>
            <small>Validar o pool no começo do turno</small>
          </span>
        </button>

        <button
          type="button"
          className="action-card"
          onClick={() => onPlannedFeature('O menu rápido da PDA')}
        >
          <span className="action-icon">
            <Icon name="barcode" />
          </span>
          <span>
            <strong>Bipar PDA</strong>
            <small>Consultar ou iniciar uma ação rápida</small>
          </span>
        </button>
      </div>

      <div className="status-grid">
        <article className="status-card">
          <span className="status-icon green">
            <Icon name="wifi" />
          </span>
          <div>
            <small>Terminal</small>
            <strong>{online ? 'rede disponível' : 'sem conexão'}</strong>
          </div>
        </article>

        <article className="status-card">
          <span className="status-icon green">
            <Icon name="refresh" />
          </span>
          <div>
            <small>Sessão</small>
            <strong>{user.matricula} • HUB_PDA</strong>
          </div>
        </article>

        <article className="status-card">
          <span className="status-icon blue">
            <Icon name="check" />
          </span>
          <div>
            <small>Contexto</small>
            <strong>autorizado pela API</strong>
          </div>
        </article>
      </div>

      <button type="button" className="logout-button" onClick={onLogout}>
        <Icon name="logout" />
        <span>Sair e bloquear o terminal</span>
      </button>
    </section>
  )
}

function Icon({ name }: { name: IconName }) {
  const paths: Record<IconName, ReactNode> = {
    badge: (
      <>
        <rect x="7" y="3" width="10" height="4" rx="2" />
        <rect x="4" y="6" width="16" height="15" rx="3" />
        <circle cx="9" cy="12" r="2" />
        <path d="M6.5 17c.8-2 4.2-2 5 0M14 11h3M14 15h3" />
      </>
    ),
    barcode: (
      <>
        <path d="M4 5v14M7 5v14M10 5v14M14 5v14M17 5v14M20 5v14" />
        <path d="M3 3h4M3 3v4M21 3h-4M21 3v4M3 21h4M3 21v-4M21 21h-4M21 21v-4" />
      </>
    ),
    check: <path d="m5 12 4 4L19 6" />,
    clipboard: (
      <>
        <rect x="5" y="4" width="14" height="17" rx="2" />
        <path d="M9 4V2h6v2M9 10h6M9 14h6M9 18h4" />
      </>
    ),
    home: (
      <>
        <path d="m3 11 9-8 9 8" />
        <path d="M5 10v11h14V10M9 21v-7h6v7" />
      </>
    ),
    logout: (
      <>
        <path d="M10 4H5v16h5M14 8l4 4-4 4M8 12h10" />
      </>
    ),
    menu: <path d="M4 7h16M4 12h16M4 17h16" />,
    refresh: (
      <>
        <path d="M20 7v5h-5M4 17v-5h5" />
        <path d="M6.1 8a7 7 0 0 1 11.7-1L20 12M4 12l2.2 5a7 7 0 0 0 11.7-1" />
      </>
    ),
    settings: (
      <>
        <circle cx="12" cy="12" r="3" />
        <path d="M19 12a7 7 0 0 0-.1-1l2-1.5-2-3.4-2.4 1a8 8 0 0 0-1.8-1L14.4 3h-4.8l-.3 3.1a8 8 0 0 0-1.8 1l-2.4-1-2 3.4 2 1.5a7 7 0 0 0 0 2l-2 1.5 2 3.4 2.4-1a8 8 0 0 0 1.8 1l.3 3.1h4.8l.3-3.1a8 8 0 0 0 1.8-1l2.4 1 2-3.4-2-1.5a7 7 0 0 0 .1-1Z" />
      </>
    ),
    warning: (
      <>
        <path d="M12 3 2.5 20h19L12 3Z" />
        <path d="M12 9v5M12 17h.01" />
      </>
    ),
    wifi: (
      <>
        <path d="M3 9a14 14 0 0 1 18 0M6 13a9 9 0 0 1 12 0M9.5 16.5a4 4 0 0 1 5 0" />
        <circle cx="12" cy="20" r=".5" />
      </>
    ),
  }

  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {paths[name]}
    </svg>
  )
}

export default App
