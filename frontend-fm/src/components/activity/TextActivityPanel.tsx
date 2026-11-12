import { useState } from 'react'
import { getTextDraft, type ActivityDraft } from '../../api/activities'

type Props = { text: string; onTextChange: (text: string) => void; onDraft: (draft: ActivityDraft) => void; onManual: () => void }

export default function TextActivityPanel({ text, onTextChange, onDraft, onManual }: Props) {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const prepare = async () => {
    if (busy) return
    if (!text.trim()) { setError('Vnesi opis aktivnosti.'); return }
    setBusy(true); setError('')
    try { onDraft(await getTextDraft(text)) }
    catch (failure) { setError(failure instanceof Error ? failure.message : 'Osnutka ni bilo mogoče pripraviti.') }
    finally { setBusy(false) }
  }
  return <section className="panel form">
    <h2>Besedilo v osnutek</h2><p>Opis pošljemo zunanji storitvi OpenAI. Pred shranjevanjem boš pregledal in dopolnil osnutek.</p>
    <label className="field"><span>Opis aktivnosti z besedilom</span><textarea value={text} maxLength={4000} onChange={event => onTextChange(event.target.value)} disabled={busy} /></label>
    {busy && <p role="status">Pripravljam osnutek …</p>}{error && <p role="alert">{error}</p>}
    <div className="row"><button className="outline" onClick={onManual} disabled={busy}>Nadaljuj z ročnim vnosom</button>
      <button className="primary" onClick={() => void prepare()} disabled={busy}>Pripravi osnutek iz besedila</button></div>
  </section>
}
