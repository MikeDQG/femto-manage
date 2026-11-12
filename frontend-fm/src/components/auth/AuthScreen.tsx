import { useState } from 'react'
import { AuthError, login, register } from '../../api/auth'
import type { User } from '../../api/auth'
import styles from './AuthScreen.module.css'

type Props = {
  mode: 'register' | 'login'
  onModeChange: (mode: 'register' | 'login') => void
  onAuthenticated: (user: User) => void
}

export default function AuthScreen({ mode, onModeChange, onAuthenticated }: Props) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<AuthError | null>(null)
  const [busy, setBusy] = useState(false)
  const [registered, setRegistered] = useState(false)

  async function submit(event: React.SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    if (busy) return
    setError(null)
    setBusy(true)
    try {
      if (mode === 'register') {
        await register(email, password)
        setPassword('')
        setRegistered(true)
        onModeChange('login')
      } else {
        const user = await login(email, password)
        setPassword('')
        onAuthenticated(user)
      }
    } catch (error) {
      setError(error as AuthError)
    } finally {
      setBusy(false)
    }
  }

  function switchMode() {
    setPassword('')
    setError(null)
    setRegistered(false)
    onModeChange(mode === 'login' ? 'register' : 'login')
  }

  return <main className="auth">
    <div className="authart">MAJHNI KORAKI.<br /><i>POLNO ŽIVLJENJE.</i></div>
    <form className={`panel form ${styles.form}`} onSubmit={submit}>
      <span className="eyebrow">DOBRODOŠEL V FEMTO</span>
      <h1>{mode === 'register' ? 'Začni s svojim ritmom' : 'Lepo te je spet videti'}</h1>
      {registered && <p role="status">Račun je ustvarjen. Zdaj se lahko prijaviš.</p>}
      {error && <p role="alert" className={styles.error}>{error.message}</p>}
      <label className="field"><span id="email-label">E-pošta</span>
        <input name="email" type="email" autoComplete="email" required maxLength={254}
          value={email} onChange={event => setEmail(event.target.value)} aria-labelledby="email-label"
          aria-invalid={!!error?.fields.email} aria-describedby={error?.fields.email ? 'email-error' : undefined} />
        {error?.fields.email && <small id="email-error" className={styles.error}>{error.fields.email}</small>}
      </label>
      <label className="field"><span id="password-label">Geslo</span>
        <input name="password" type="password" required minLength={mode === 'register' ? 12 : undefined}
          maxLength={mode === 'register' ? 72 : undefined} autoComplete={mode === 'register' ? 'new-password' : 'current-password'}
          value={password} onChange={event => setPassword(event.target.value)} aria-labelledby="password-label"
          aria-invalid={!!error?.fields.password} aria-describedby="password-help" />
        <small id="password-help" className={error?.fields.password ? styles.error : undefined}>
          {error?.fields.password ?? (mode === 'register' ? 'Uporabi vsaj 12 znakov, največ 72 bajtov UTF-8.' : '')}
        </small>
      </label>
      <button className="primary" disabled={busy}>
        {busy ? 'Počakaj …' : mode === 'register' ? 'Ustvari račun' : 'Prijava'} ↗
      </button>
      <p>{mode === 'login' ? 'Še nimaš računa?' : 'Že imaš račun?'}{' '}
        <button type="button" className="link" disabled={busy} onClick={switchMode}>
          {mode === 'login' ? 'Registriraj se' : 'Prijavi se'}
        </button>
      </p>
    </form>
  </main>
}
