const apiBaseUrl = (
  import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api'
).replace(/\/+$/, '')

export interface AuthenticatedUser {
  id: string
  matricula: string
  nome: string
  papeis: string[]
  permissoes: string[]
  setores: string[]
  turnos: string[]
  origemAplicacao: string
}

export interface CreatedSession {
  token: string
  expiraEm: string
  usuario: AuthenticatedUser
}

export interface OperationalContext {
  setor: {
    id: string
    codigo: string
    nome: string
    cotaPdas: number
  }
  turno: {
    id: string
    codigo: string
    nome: string
    horaInicio: string | null
    horaFim: string | null
  }
}

export type ConferenceStatus = 'EM_ANDAMENTO' | 'CONCLUIDA'
export type ConferenceType = 'ABERTURA' | 'ENCERRAMENTO'
export type ConferenceItemStatus = 'PENDENTE' | 'CONFIRMADA' | 'AUSENTE'
export type ConferenceReadingResult =
  | 'CONFIRMADA'
  | 'DUPLICADA'
  | 'EXTRA'
  | 'NAO_CADASTRADA'
  | 'OUTRO_SETOR'
  | 'NAO_CONTROLADA'
  | 'BAIXADA'
  | 'IDENTIFICADOR_AMBIGUO'

export interface ConferenceSummary {
  esperadas: number
  leituras: number
  confirmadas: number
  pendentes: number
  ausentes: number
  duplicadas: number
  extras: number
  naoCadastradas: number
  outroSetor: number
  divergencias: number
}

export interface Conference {
  id: string
  tipo: ConferenceType
  status: ConferenceStatus
  setorId: string
  setorCodigo: string
  setorNome: string
  turnoId: string
  turnoCodigo: string
  turnoNome: string
  responsavelId: string
  responsavelMatricula: string
  responsavelNome: string
  iniciadaEm: string
  concluidaEm: string | null
  resumo: ConferenceSummary
}

export interface ConferenceItem {
  id: string
  ativoId: string
  numeroSerie: string
  patrimonio: string | null
  tipoCodigo: string
  modelo: string | null
  disponibilidade: string
  status: ConferenceItemStatus
  confirmadaEm: string | null
}

export interface ConferenceReading {
  id: string
  codigo: string
  resultado: ConferenceReadingResult
  ativoId: string | null
  numeroSerie: string | null
  patrimonio: string | null
  setorCodigo: string | null
  lidaEm: string
}

export interface ConferenceReadingResponse {
  leitura: ConferenceReading
  resumo: ConferenceSummary
}

interface ApiErrorBody {
  codigo?: string
  mensagem?: string
}

export class ApiRequestError extends Error {
  readonly status: number
  readonly code: string

  constructor(status: number, code: string, message: string) {
    super(message)
    this.name = 'ApiRequestError'
    this.status = status
    this.code = code
  }
}

async function request<T>(
  path: string,
  init: RequestInit = {},
  token?: string,
): Promise<T> {
  const controller = new AbortController()
  const timeout = window.setTimeout(() => controller.abort(), 10_000)

  try {
    const headers = new Headers(init.headers)
    headers.set('Accept', 'application/json')
    if (init.body) headers.set('Content-Type', 'application/json')
    if (token) headers.set('Authorization', `Bearer ${token}`)

    const response = await fetch(`${apiBaseUrl}${path}`, {
      ...init,
      headers,
      signal: controller.signal,
    })

    if (!response.ok) {
      const error = await readError(response)
      throw new ApiRequestError(
        response.status,
        error.codigo ?? 'ERRO_API',
        error.mensagem ?? 'A API recusou a operação.',
      )
    }

    if (response.status === 204) return undefined as T
    return (await response.json()) as T
  } catch (error) {
    if (error instanceof ApiRequestError) throw error

    const message =
      error instanceof DOMException && error.name === 'AbortError'
        ? 'A API demorou mais de 10 segundos para responder.'
        : 'Não foi possível conectar à API local.'
    throw new ApiRequestError(0, 'API_INDISPONIVEL', message)
  } finally {
    window.clearTimeout(timeout)
  }
}

async function readError(response: Response): Promise<ApiErrorBody> {
  try {
    return (await response.json()) as ApiErrorBody
  } catch {
    return {}
  }
}

export function createSession(registration: string): Promise<CreatedSession> {
  return request<CreatedSession>('/v1/sessoes', {
    method: 'POST',
    body: JSON.stringify({
      matricula: registration,
      origemAplicacao: 'HUB_PDA',
    }),
  })
}

export function getAuthenticatedUser(
  token: string,
): Promise<AuthenticatedUser> {
  return request<AuthenticatedUser>('/v1/me', {}, token)
}

export function getOperationalContexts(
  token: string,
): Promise<OperationalContext[]> {
  return request<OperationalContext[]>(
    '/v1/me/contextos-operacionais',
    {},
    token,
  )
}

export function revokeCurrentSession(token: string): Promise<void> {
  return request<void>(
    '/v1/sessoes/atual',
    {
      method: 'DELETE',
    },
    token,
  )
}

export function openConference(
  token: string,
  context: OperationalContext,
): Promise<Conference> {
  return request<Conference>(
    '/v1/conferencias',
    {
      method: 'POST',
      body: JSON.stringify({
        setorId: context.setor.id,
        turnoId: context.turno.id,
        tipo: 'ABERTURA',
      }),
    },
    token,
  )
}

export function getConferenceItems(
  token: string,
  conferenceId: string,
): Promise<ConferenceItem[]> {
  return request<ConferenceItem[]>(
    `/v1/conferencias/${conferenceId}/itens`,
    {},
    token,
  )
}

export function getConferenceReadings(
  token: string,
  conferenceId: string,
): Promise<ConferenceReading[]> {
  return request<ConferenceReading[]>(
    `/v1/conferencias/${conferenceId}/leituras`,
    {},
    token,
  )
}

export function addConferenceReading(
  token: string,
  conferenceId: string,
  code: string,
): Promise<ConferenceReadingResponse> {
  return request<ConferenceReadingResponse>(
    `/v1/conferencias/${conferenceId}/leituras`,
    {
      method: 'POST',
      body: JSON.stringify({ codigo: code }),
    },
    token,
  )
}

export function completeConference(
  token: string,
  conferenceId: string,
): Promise<Conference> {
  return request<Conference>(
    `/v1/conferencias/${conferenceId}/conclusoes`,
    {
      method: 'POST',
    },
    token,
  )
}

export { apiBaseUrl }
