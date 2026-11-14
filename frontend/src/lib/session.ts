//JWT session in localStorage; keys match the legacy frontend
export type Session = { token: string; username: string; role: string }

const KEYS = { token: 'jwt', username: 'username', role: 'role' } as const

export function getSession(): Session | null {
  try {
    const token = localStorage.getItem(KEYS.token)
    if (!token) return null
    //expired token: drop it here so protected pages never render with a dead session
    if (isExpired(token)) {
      clearSession()
      return null
    }
    return {
      token,
      username: localStorage.getItem(KEYS.username) ?? '',
      role: localStorage.getItem(KEYS.role) ?? '',
    }
  } catch {
    return null
  }
}

//reads exp from the JWT payload; the signature is checked by the server, this is UX only
function isExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))
    return typeof payload.exp === 'number' && payload.exp * 1000 <= Date.now()
  } catch {
    return true
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
