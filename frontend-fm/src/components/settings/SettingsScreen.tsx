import type { User } from '../../api/auth'

type Props = {
  user: User
  theme: 'light' | 'dark'
  onThemeChange: (theme: 'light' | 'dark') => void
  onLogout: () => void
}

export default function SettingsScreen({ user, theme, onThemeChange, onLogout }: Props) {
  return <section>
    <div className="head"><div><h1>Nastavitve<span>.</span></h1><p>Profil in predstavitvene možnosti.</p></div></div>
    <div className="panel form">
      <b>Videz</b><p>Izberi temo za to napravo.</p>
      <div className="theme-choice">
        <button className={theme === 'light' ? 'selected' : ''} aria-pressed={theme === 'light'} onClick={() => onThemeChange('light')}>Svetla</button>
        <button className={theme === 'dark' ? 'selected' : ''} aria-pressed={theme === 'dark'} onClick={() => onThemeChange('dark')}>Temna</button>
      </div>
      <b>{user.email}</b><p>{user.role}</p><button className="outline" onClick={onLogout}>Odjava</button>
    </div>
  </section>
}
