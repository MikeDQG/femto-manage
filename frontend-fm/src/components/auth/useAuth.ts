import { useEffect, useState } from 'react'
import { AuthError, currentUser, logout } from '../../api/auth'
import type { User } from '../../api/auth'

export function useAuth() {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  async function refresh() {
    setLoading(true)
    setError('')
    try { setUser(await currentUser()) }
    catch (error) {
      if (error instanceof AuthError && error.status === 401) setUser(null)
      else setError((error as Error).message)
    } finally { setLoading(false) }
  }

  useEffect(() => {
    let active = true
    currentUser().then(user => { if (active) setUser(user) }).catch(error => {
      if (active && !(error instanceof AuthError && error.status === 401)) setError(error.message)
    }).finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [])

  async function signOut() {
    setLoading(true)
    setError('')
    try {
      await logout()
      setUser(null)
    } catch (error) {
      if (error instanceof AuthError && error.status === 401) setUser(null)
      else setError((error as Error).message)
    } finally { setLoading(false) }
  }

  return { user, loading, error, setUser, refresh, signOut }
}
