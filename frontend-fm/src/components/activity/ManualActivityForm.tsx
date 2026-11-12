import { useState } from 'react'
import { createActivity, updateActivity, JournalError, type ActivityDetails, type ActivityDraft, type ActivityInput, type CategoryOption, type Overlap, type PlaceOption } from '../../api/activities'
import PlaceRecognitionPanel from '../place-recognition/PlaceRecognitionPanel'
import type { PlaceRecognitionState } from '../place-recognition/usePlaceRecognition'

type Props = { categories: CategoryOption[]; places: PlaceOption[]; recognition: PlaceRecognitionState; draft?: ActivityDraft; activity?: ActivityDetails; onSaved: () => void; onCancel: () => void }

function localInput(instant: string | null | undefined) {
  if (!instant) return ''
  const date = new Date(instant)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}T${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

export default function ManualActivityForm({ categories, places, recognition, draft, activity, onSaved, onCancel }: Props) {
  const initial = activity ?? draft
  const [form, setForm] = useState({ title: initial?.title ?? '', startAt: localInput(initial?.startAt), endAt: localInput(initial?.endAt), categoryId: initial?.categoryId ?? '', placeId: initial?.placeId ?? '', description: activity?.description ?? '' })
  const [fields, setFields] = useState<Record<string, string>>(draft?.fields ?? {})
  const [overlaps, setOverlaps] = useState<Overlap[]>([])
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const change = (field: keyof typeof form, value: string) => {
    setForm(current => ({ ...current, [field]: value }))
    setFields(current => ({ ...current, [field]: '' }))
    setOverlaps([])
  }
  const save = async (confirmOverlap: boolean) => {
    if (busy) return
    if ((draft?.placeCandidates?.length ?? 0) > 1 && !form.placeId) {
      setFields(current => ({ ...current, placeId: 'Izberi enega od svojih krajev.' }))
      return
    }
    setBusy(true)
    setError('')
    setOverlaps([])
    const input: ActivityInput = { ...form, source: draft?.source ?? 'MANUAL', categoryId: form.categoryId || null, placeId: form.placeId || null,
      startAt: form.startAt ? new Date(form.startAt).toISOString() : null,
      endAt: form.endAt ? new Date(form.endAt).toISOString() : null, confirmOverlap }
    try {
      if (activity) {
        const update = { ...input }
        delete update.source
        if (form.startAt === localInput(activity.startAt)) update.startAt = activity.startAt
        if (form.endAt === localInput(activity.endAt)) update.endAt = activity.endAt
        await updateActivity(activity.id, update)
      } else await createActivity(input)
      onSaved()
    } catch (failure) {
      if (failure instanceof JournalError) {
        setFields(failure.fields)
        setOverlaps(failure.overlaps)
        setError(failure.status === 409 ? '' : failure.status === 400 ? 'Preveri označena polja.' : failure.message)
      } else setError('Shranjevanje ni uspelo.')
    } finally { setBusy(false) }
  }
  const field = (key: keyof typeof form, label: string, type = 'text') => <label className="field">
    <span>{label}</span><input type={type} value={form[key]} disabled={busy} onChange={event => change(key, event.target.value)} aria-invalid={!!fields[key]} />
    {fields[key] && <small role="alert">{fields[key]}</small>}
  </label>
  return <form className="panel form" noValidate onSubmit={event => { event.preventDefault(); void save(false) }}>
    {draft && <p role="status">Osnutek aktivnosti — pred potrditvijo preglej in dopolni podatke.</p>}
    {(draft?.placeCandidates?.length ?? 0) > 1 && <p>Možni kraji: {draft?.placeCandidates?.map(place => `${place.name} (${place.id.slice(0, 8)})`).join(', ')}. Izberi ustrezen kraj spodaj.</p>}
    {field('title', 'Naziv')}
    <div className="twocol">{field('startAt', 'Začetek', 'datetime-local')}{field('endAt', 'Konec', 'datetime-local')}</div>
    <label className="field"><span>Kategorija</span><select value={form.categoryId} disabled={busy} onChange={event => change('categoryId', event.target.value)} aria-invalid={!!fields.categoryId}>
      {activity && !categories.some(category => category.id === activity.categoryId) && <option value={activity.categoryId}>{activity.categoryName} (neaktivna – obstoječa izbira)</option>}
      <option value="">Izberi kategorijo</option>{categories.map(category => <option key={category.id} value={category.id}>{category.name}</option>)}
    </select>{fields.categoryId && <small role="alert">{fields.categoryId}</small>}</label>
    <label className="field"><span>Kraj (neobvezno)</span><select value={form.placeId} disabled={busy} onChange={event => change('placeId', event.target.value)} aria-invalid={!!fields.placeId}>
      {activity?.placeId && !places.some(place => place.id === activity.placeId) && <option value={activity.placeId}>{activity.placeName ?? 'Pretekli kraj'} (izbrisan – obstoječa izbira)</option>}
      <option value="">Brez kraja</option>{places.map(place => <option key={place.id} value={place.id}>{place.name}{(draft?.placeCandidates?.length ?? 0) > 1 ? ` (${place.id.slice(0, 8)})` : ''}</option>)}
    </select>{fields.placeId && <small role="alert">{fields.placeId}</small>}</label>
    <PlaceRecognitionPanel recognition={recognition} onSelect={id => change('placeId', id)} />
    <label className="field"><span>Opis (neobvezno)</span><textarea value={form.description} disabled={busy} aria-invalid={!!fields.description} onChange={event => change('description', event.target.value)} />
      {fields.description && <small role="alert">{fields.description}</small>}</label>
    {error && <p role="alert">{error}</p>}
    {overlaps.length > 0 && <div role="alert" className="overlap-warning"><strong>Prekrivanje s potrjeno aktivnostjo</strong>
      {overlaps.map((overlap, index) => <p key={index}>{overlap.title}: {new Date(overlap.startAt).toLocaleString('sl-SI')} – {new Date(overlap.endAt).toLocaleString('sl-SI')}</p>)}
      <p>Popravi čas ali izrecno potrdi prekrivanje.</p><button type="button" className="outline" onClick={() => void save(true)} disabled={busy}>Nadaljuj kljub prekrivanju</button>
    </div>}
    <div className="row"><button type="button" className="outline" onClick={onCancel} disabled={busy}>{draft ? 'Zavrni predlog' : 'Prekliči'}</button><button className="primary" disabled={busy}>{activity ? 'Shrani spremembe' : 'Potrdi aktivnost'}</button></div>
  </form>
}
