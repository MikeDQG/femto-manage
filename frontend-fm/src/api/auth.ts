export type User = { id: string; email: string; role: 'USER' | 'MENTOR' | 'ADMIN'; createdAt: string }
export type ApiError = { message: string; fields: Record<string, string> }

export class AuthError extends Error {
  status: number
  fields: Record<string, string>
  constructor(status: number, error: ApiError) {
    super(error.message)
    this.status = status
    this.fields = error.fields ?? {}
  }
}

export async function authRequest<T>(path: string, body?: unknown): Promise<T> {
  try {
    const headers: Record<string, string> = {}
    if (body !== undefined) {
      const csrfResponse = await fetch('/api/auth/csrf', { credentials: 'same-origin', cache: 'no-store' })
      if (!csrfResponse.ok) throw new Error('csrf')
      const csrf: { headerName: string; token: string } = await csrfResponse.json()
      headers[csrf.headerName] = csrf.token
      headers['Content-Type'] = 'application/json'
    }
    const response = await fetch(`/api/auth/${path}`, {
      method: body === undefined ? 'GET' : 'POST', credentials: 'same-origin', cache: 'no-store',
      headers, body: body === undefined ? undefined : JSON.stringify(body),
    })
    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Zahteva ni uspela. Poskusi znova.' }))
      throw new AuthError(response.status, error)
    }
    return response.status === 204 ? undefined as T : await response.json()
  } catch (error) {
    if (error instanceof AuthError) throw error
    throw new AuthError(0, { message: 'Povezava s strežnikom ni uspela. Poskusi znova.', fields: {} })
  }
}

export const register = (email: string, password: string) => authRequest<User>('register', { email, password })
export const login = (email: string, password: string) => authRequest<User>('login', { email, password })
export const currentUser = () => authRequest<User>('me')
export const logout = () => authRequest<void>('logout', {})
