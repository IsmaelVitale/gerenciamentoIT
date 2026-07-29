import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import './App.css'

type Screen = 'identification' | 'home'

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

const apiBaseUrl =
  import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api'
const simulationMode = import.meta.env.VITE_SIMULATION_MODE !== 'false'

function App() {
  const [screen, setScreen] = useState<Screen>('identification')
  const [registration, setRegistration] = useState('')
  const [leaderName, setLeaderName] = useState('Líder')
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
    if (screen === 'identification') {
      registrationInput.current?.focus()
    }
  }, [screen])

  useEffect(() => {
    if (!notice) return

    const timeout = window.setTimeout(() => setNotice(null), 4000)
    return () => window.clearTimeout(timeout)
  }, [notice])

  function handleIdentification(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const normalizedRegistration = registration.trim()

    if (!normalizedRegistration) {
      setNotice('Bipe o crachá ou informe uma matrícula para continuar.')
      registrationInput.current?.focus()
      return
    }

    setLeaderName(
      simulationMode ? 'Líder de Recebimento' : normalizedRegistration,
    )
    setRegistration('')
    setScreen('home')
  }

  function handleLogout() {
    setScreen('identification')
    setLeaderName('Líder')
    setNotice('Sessão encerrada.')
  }

  function showPlannedFeature(feature: string) {
    setNotice(`${feature} será conectado à API em uma próxima entrega.`)
  }

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
          {simulationMode && <span className="simulation-pill">Simulação</span>}
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
            if (screen === 'home') setScreen('home')
          }}
          disabled={screen === 'identification'}
        >
          <Icon name="home" />
          <span>Início</span>
        </button>
        <button
          type="button"
          className="sidebar-button"
          aria-label="Conferências"
          onClick={() => showPlannedFeature('A conferência')}
          disabled={screen === 'identification'}
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
        {screen === 'identification' ? (
          <IdentificationScreen
            registration={registration}
            inputRef={registrationInput}
            onRegistrationChange={setRegistration}
            onSubmit={handleIdentification}
          />
        ) : (
          <HomeScreen
            leaderName={leaderName}
            online={online}
            onLogout={handleLogout}
            onPlannedFeature={showPlannedFeature}
          />
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

interface IdentificationScreenProps {
  registration: string
  inputRef: React.RefObject<HTMLInputElement | null>
  onRegistrationChange: (value: string) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
}

function IdentificationScreen({
  registration,
  inputRef,
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
          <p>ou digite sua matrícula para iniciar a operação</p>
        </div>

        <form className="scan-form" onSubmit={onSubmit}>
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
            />
            <button type="submit">Continuar</button>
          </div>
        </form>

        <div className="ready-status" role="status">
          <Icon name="check" />
          <span>Pronto para leitura</span>
        </div>

        <p className="simulation-hint">
          No modo simulado, qualquer matrícula permite visualizar o Hub.
        </p>
      </div>
    </section>
  )
}

interface HomeScreenProps {
  leaderName: string
  online: boolean
  onLogout: () => void
  onPlannedFeature: (feature: string) => void
}

function HomeScreen({
  leaderName,
  online,
  onLogout,
  onPlannedFeature,
}: HomeScreenProps) {
  return (
    <section className="home-screen" aria-labelledby="home-title">
      <div className="home-heading">
        <div>
          <span className="eyebrow">Operação atual</span>
          <h1 id="home-title">Bom dia, {leaderName}</h1>
          <p>Recebimento • Turno T1</p>
        </div>
        <div className="context-chip">
          <span>Contexto simulado</span>
          <strong>Recebimento / T1</strong>
        </div>
      </div>

      <div className="pending-banner">
        <span className="pending-icon" aria-hidden="true">
          !
        </span>
        <div>
          <span>Abertura pendente</span>
          <strong>12 PDAs esperadas</strong>
        </div>
        <button
          type="button"
          onClick={() => onPlannedFeature('A conferência de abertura')}
        >
          Ver detalhes
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
            <small>Fila offline</small>
            <strong>0 operações</strong>
          </div>
        </article>

        <article className="status-card">
          <span className="status-icon green">
            <Icon name="refresh" />
          </span>
          <div>
            <small>Última sincronização</small>
            <strong>{online ? 'agora' : 'aguardando conexão'}</strong>
          </div>
        </article>

        <article className="status-card">
          <span className="status-icon blue">
            <Icon name="check" />
          </span>
          <div>
            <small>Encerramento anterior</small>
            <strong>concluído sem divergências</strong>
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
  const paths: Record<IconName, React.ReactNode> = {
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
