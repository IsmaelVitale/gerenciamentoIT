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

export { apiBaseUrl }
