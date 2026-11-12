import { useState } from 'react'
import { JournalError, request, type PlaceOption } from '../../api/activities'

export default function PlaceForm({ place, onSaved, onCancel }: { place?: PlaceOption; onSaved: () => void; onCancel: () => void }) {
  const [form, setForm] = useState({ name: place?.name ?? '', latitude: place?.latitude.toString() ?? '', longitude: place?.longitude.toString() ?? '', radiusM: place?.radiusM.toString() ?? '100' })
  const [fields, setFields] = useState<Record<string, string>>({})
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [locating, setLocating] = useState(false)
  const locate = () => {
    if (!navigator.geolocation) { setError('Brskalnik ne podpira lokacije. Koordinate vnesi ročno.'); return }
    setLocating(true); setError('')
    navigator.geolocation.getCurrentPosition(position => {
      setForm(current => ({ ...current, latitude: String(position.coords.latitude), longitude: String(position.coords.longitude) }))
      setFields(current => ({ ...current, latitude: '', longitude: '' })); setLocating(false)
    }, failure => { setLocating(false); setError(failure.code === 1 ? 'Dovoljenje za lokacijo je zavrnjeno. Koordinate vnesi ročno.' : 'Lokacija ni dosegljiva. Koordinate vnesi ročno.') }, { timeout: 10000 })
  }
  const save = async () => {
    const invalid: Record<string, string> = {}
    if (!form.name.trim() || form.name.trim().length > 120) invalid.name = 'Vnesi ime z največ 120 znaki.'
    for (const [key, limit] of [['latitude', 90], ['longitude', 180]] as const) {
      if (!form[key].trim() || !Number.isFinite(Number(form[key])) || Math.abs(Number(form[key])) > limit) invalid[key] = `Vrednost mora biti med −${limit} in ${limit}.`
    }
    if (!Number.isInteger(Number(form.radiusM)) || Number(form.radiusM) <= 0 || Number(form.radiusM) > 2147483647) invalid.radiusM = 'Radij mora biti pozitivno celo število metrov.'
    setFields(invalid); if (Object.keys(invalid).length) return
    setBusy(true); setError('')
    try {
      await request(`places${place ? `/${place.id}` : ''}`, { name: form.name.trim(), latitude: Number(form.latitude), longitude: Number(form.longitude), radiusM: Number(form.radiusM) }, place ? 'PUT' : 'POST')
      onSaved()
    } catch (failure) { setError('Shranjevanje ni uspelo. Poskusi znova.'); if (failure instanceof JournalError) setFields(failure.fields) }
    finally { setBusy(false) }
  }
  return <form className="panel form" noValidate onSubmit={event => { event.preventDefault(); if (!busy) void save() }}>
    <h2>{place ? 'Uredi lokacijo' : 'Nova lokacija'}</h2>
    {([['name', 'Ime lokacije'], ['latitude', 'Zemljepisna širina'], ['longitude', 'Zemljepisna dolžina'], ['radiusM', 'Radij (metri)']] as const).map(([key, label]) => <label className="field" key={key}><span>{label}</span><input type={key === 'name' ? 'text' : 'number'} step="any" value={form[key]} aria-invalid={!!fields[key]} onChange={event => setForm({ ...form, [key]: event.target.value })} />{fields[key] && <small role="alert">{fields[key]}</small>}</label>)}
    <button type="button" className="outline" onClick={locate} disabled={locating}>{locating ? 'Pridobivam lokacijo …' : 'Uporabi trenutno lokacijo'}</button>
    {error && <p role="alert">{error}</p>}
    <div className="row"><button type="button" className="outline" onClick={onCancel}>Prekliči</button><button className="primary" disabled={busy || locating}>Shrani lokacijo</button></div>
  </form>
}
