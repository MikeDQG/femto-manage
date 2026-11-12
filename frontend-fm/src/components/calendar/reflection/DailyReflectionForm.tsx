import { useEffect, useState } from 'react'
import { JournalError, request } from '../../../api/activities'
import styles from './DailyReflectionForm.module.css'

type Reflection = { date: string; note: string | null; mood: string | null; energy: number | null }

export default function DailyReflectionForm({ date }: { date: string }) {
  const [form, setForm] = useState({ note: '', mood: '', energy: '' })
  const [loading, setLoading] = useState(true)
  const [loaded, setLoaded] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [fields, setFields] = useState<Record<string, string>>({})
  const [saved, setSaved] = useState(false)
  const [retry, setRetry] = useState(0)
  useEffect(() => {
    let active = true
    request<Reflection>(`journal/reflection?date=${date}`).then(result => {
      if (active) { setForm({ note: result.note ?? '', mood: result.mood ?? '', energy: result.energy?.toString() ?? '' }); setLoaded(true); setLoading(false); setError('') }
    }).catch(() => { if (active) { setLoaded(false); setLoading(false); setError('Refleksije ni bilo mogoče naložiti.') } })
    return () => { active = false }
  }, [date, retry])
  const change = (key: keyof typeof form, value: string) => {
    setForm(current => ({ ...current, [key]: value })); setSaved(false); setFields(current => ({ ...current, [key]: '' }))
  }
  const save = async () => {
    if (busy || loading) return
    setError(''); setSaved(false); setBusy(true); setFields({})
    try {
      await request<Reflection>(`journal/reflection?date=${date}`, { ...form, energy: form.energy === '' ? null : Number(form.energy) }, 'PUT')
      setSaved(true)
    } catch (failure) {
      if (failure instanceof JournalError) { setFields(failure.fields); setError(failure.message) }
      else setError('Shranjevanje refleksije ni uspelo.')
    } finally { setBusy(false) }
  }
  return <form className={`panel form ${styles.form}`} aria-label="Zasebna dnevna refleksija" noValidate onSubmit={event => { event.preventDefault(); void save() }}>
    <h2>Zasebna dnevna refleksija</h2><p>Za dan {date}. Refleksija je vidna samo tebi in se ne deli z mentorjem.</p>
    {loading ? <p role="status">Nalagam refleksijo …</p> : loaded && <>
      <label className="field"><span>Zasebna opomba</span><textarea maxLength={4000} value={form.note} onChange={event => change('note', event.target.value)} disabled={busy} aria-invalid={!!fields.note} />{fields.note && <small role="alert">{fields.note}</small>}</label>
      <label className="field"><span>Počutje (neobvezno)</span><input maxLength={100} value={form.mood} onChange={event => change('mood', event.target.value)} disabled={busy} aria-invalid={!!fields.mood} />{fields.mood && <small role="alert">{fields.mood}</small>}</label>
      <label className="field"><span>Energija (1–5, neobvezno)</span><input type="number" min="1" max="5" step="1" value={form.energy} onChange={event => change('energy', event.target.value)} disabled={busy} aria-invalid={!!fields.energy} />{fields.energy && <small role="alert">{fields.energy}</small>}</label>
      <button className="primary" disabled={busy}>{busy ? 'Shranjujem refleksijo …' : 'Shrani refleksijo'}</button>
    </>}
    {saved && <p role="status">Refleksija shranjena.</p>}
    {error && <div role="alert"><p>{error}</p><button className="outline" type="button" onClick={() => { setLoading(true); setRetry(current => current + 1) }} disabled={busy}>Ponovno naloži refleksijo</button></div>}
  </form>
}
