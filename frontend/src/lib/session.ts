//JWT session in localStorage; keys match the legacy frontend
export type Session = { token: string; username: string; role: string }

const KEYS = { token: 'jwt', username: 'username', role: 'role' } as const

export function getSession(): Session | null {
  try {
    const token = localStorage.getItem(KEYS.token)
    if (!token) return null
    return {
      token,
      username: localStorage.getItem(KEYS.username) ?? '',
      role: localStorage.getItem(KEYS.role) ?? '',
    }
  } catch {
    return null
  }
}

export function saveSession(session: Session) {
  localStorage.setItem(KEYS.token, session.token)
  localStorage.setItem(KEYS.username, session.username)
  localStorage.setItem(KEYS.role, session.role)
}

export function clearSession() {
  Object.values(KEYS).forEach((key) => localStorage.removeItem(key))
}
