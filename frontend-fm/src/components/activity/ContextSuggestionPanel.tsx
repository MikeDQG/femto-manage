import { useState } from 'react'
import { getContextSuggestion, type ActivityDraft, type PlaceOption } from '../../api/activities'
import type { PlaceRecognitionState } from '../place-recognition/usePlaceRecognition'
import PlaceRecognitionPanel from '../place-recognition/PlaceRecognitionPanel'

type Props = { places: PlaceOption[]; recognition: PlaceRecognitionState; onDraft: (draft: ActivityDraft) => void; onManual: () => void }

export default function ContextSuggestionPanel({ places, recognition, onDraft, onManual }: Props) {
  const [placeId, setPlaceId] = useState('')
  const [startAt, setStartAt] = useState('')
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('')
  const suggest = async () => {
    if (busy) return
    if (!placeId || !startAt) { setMessage('Izberi svoj kraj in začetek aktivnosti.'); return }
    setBusy(true); setMessage('')
    try {
      const result = await getContextSuggestion(placeId, new Date(startAt).toISOString())
      setMessage(result.reason)
      if (result.draft) onDraft(result.draft)
    } catch { setMessage('Predloga ni bilo mogoče pridobiti. Na voljo je ročni vnos.') }
    finally { setBusy(false) }
  }
  return <section className="panel form" aria-label="Kontekstni predlog">
    <h2>Kontekstni predlog</h2><p>Predlagaj kategorijo iz potrjene zgodovine na izbranem kraju in v isti uri.</p>
    <label className="field"><span>Kraj za predlog</span><select value={placeId} onChange={event => setPlaceId(event.target.value)} disabled={busy}>
      <option value="">Izberi kraj</option>{places.map(place => <option key={place.id} value={place.id}>{place.name}</option>)}
    </select></label>
    <label className="field"><span>Začetek za predlog</span><input type="datetime-local" value={startAt} onChange={event => setStartAt(event.target.value)} disabled={busy} /></label>
    <PlaceRecognitionPanel recognition={recognition} onSelect={setPlaceId} />
    {message && <p role="status">{message}</p>}
    <div className="row"><button className="outline" onClick={onManual} disabled={busy}>Ročni vnos</button>
      <button className="primary" onClick={() => void suggest()} disabled={busy}>{busy ? 'Pripravljam predlog …' : 'Pripravi kontekstni predlog'}</button></div>
  </section>
}
