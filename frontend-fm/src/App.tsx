import { useState } from 'react'
import AuthScreen from './components/auth/AuthScreen'
import WorkspaceScreen from './WorkspaceScreen'
import { useAuth } from './components/auth/useAuth'
import './App.css'

export default function App() {
  const { user, loading, error, setUser, refresh, signOut } = useAuth()
  const [mode, setMode] = useState<'login' | 'register'>('login')

  if (loading) return <main className="content" role="status">Overjam sejo …</main>
  if (error) return <main className="content"><p role="alert">{error}</p>
    <button className="primary" onClick={() => void refresh()}>Poskusi znova</button>
  </main>
  if (!user) return <div className="app"><AuthScreen mode={mode} onModeChange={setMode} onAuthenticated={setUser} /></div>
  return <WorkspaceScreen key={user.id} user={user} onLogout={() => void signOut()} />
}
