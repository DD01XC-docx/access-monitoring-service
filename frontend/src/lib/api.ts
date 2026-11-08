import { clearSession, getSession, type Session } from './session'

export class ApiError extends Error {
  readonly status: number
  //field -> message, from 400 validation responses
  readonly fieldErrors: Record<string, string>
  //seconds, from 429 responses
  readonly retryAfter?: number

  constructor(status: number, message: string, fieldErrors: Record<string, string> = {}, retryAfter?: number) {
    super(message)
    this.status = status
    this.fieldErrors = fieldErrors
    this.retryAfter = retryAfter
  }
}

type RequestOptions = { method?: string; body?: unknown; auth?: boolean }

async function request<T>(path: string, { method = 'GET', body, auth = true }: RequestOptions = {}): Promise<T> {
  const headers: Record<string, string> = {}
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (auth) {
    const session = getSession()
    if (session) headers.Authorization = `Bearer ${session.token}`
  }

  const response = await fetch(path, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  })

  //expired or invalid token on a protected call: drop session, back to login
  if (response.status === 401 && auth) {
    clearSession()
    window.location.assign('/login')
  }

  const isJson = response.headers.get('Content-Type')?.includes('application/json')
  const payload = isJson ? await response.json() : await response.text()

  if (!response.ok) {
    const retryAfter = Number(response.headers.get('Retry-After')) || undefined
    if (isJson && typeof payload === 'object' && payload !== null) {
      throw new ApiError(response.status, 'Validation failed', payload as Record<string, string>, retryAfter)
    }
    throw new ApiError(response.status, String(payload || response.statusText), {}, retryAfter)
  }
  return payload as T
}

//payloads
export type Credentials = { email: string; password: string }
export type Registration = Credentials & { username: string }
export type RegisterResponse = { username: string; role: string }
export type Profile = {
  username: string
  email: string
  role: string
  status: string
  createdAt: string
  lastLogin: string
  lastIp: string
}

export const api = {
  login: (body: Credentials) => request<Session>('/api/auth/login', { method: 'POST', body, auth: false }),
  register: (body: Registration) =>
    request<RegisterResponse>('/api/auth/register', { method: 'POST', body, auth: false }),
  profile: () => request<Profile>('/api/profile/me'),
}
