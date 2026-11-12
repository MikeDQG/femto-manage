export type CategoryOption = { id: string; code: string; name: string; active: boolean }
export type PlaceOption = { id: string; name: string; latitude: number; longitude: number; radiusM: number }
export type JournalActivity = { id: string; title: string; description?: string; startAt: string; endAt: string; categoryId: string; placeId?: string }
export type Overlap = { title: string; startAt: string; endAt: string }
export type ActivityInput = { title: string; startAt: string | null; endAt: string | null; categoryId: string | null; placeId: string | null; description: string; confirmOverlap: boolean }

export class JournalError extends Error {
  status: number
  fields: Record<string, string>
  overlaps: Overlap[]
  constructor(status: number, fields: Record<string, string>, overlaps: Overlap[]) {
    super(status === 0 ? 'Povezava s strežnikom ni uspela.' : 'Zahteva ni uspela.')
    this.status = status
    this.fields = fields
    this.overlaps = overlaps
  }
}

export async function request<T>(path: string, body?: unknown, method = body === undefined ? 'GET' : 'POST'): Promise<T> {
  try {
    const headers: Record<string, string> = {}
    if (method !== 'GET') {
      const csrfResponse = await fetch('/api/auth/csrf', { credentials: 'same-origin' })
      if (!csrfResponse.ok) throw new Error('csrf')
      const csrf: { headerName: string; token: string } = await csrfResponse.json()
      headers[csrf.headerName] = csrf.token
      headers['Content-Type'] = 'application/json'
    }
    const response = await fetch(`/api/${path}`, { method, credentials: 'same-origin', headers,
      body: body === undefined ? undefined : JSON.stringify(body) })
    if (!response.ok) {
      const error = await response.json().catch(() => ({}))
      throw new JournalError(response.status, error.fields ?? {}, error.overlaps ?? [])
    }
    return response.status === 204 ? undefined as T : await response.json()
  } catch (error) {
    if (error instanceof JournalError) throw error
    throw new JournalError(0, {}, [])
  }
}

export const getCategories = () => request<CategoryOption[]>('categories')
export const getPlaces = () => request<PlaceOption[]>('places')
export const getActivities = (date: string) => request<JournalActivity[]>(`activities?date=${date}&zone=${encodeURIComponent(Intl.DateTimeFormat().resolvedOptions().timeZone)}`)
export const createActivity = (activity: ActivityInput) => request<JournalActivity>('activities', activity)
