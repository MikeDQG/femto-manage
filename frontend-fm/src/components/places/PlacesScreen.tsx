import { useCallback, useEffect, useState } from 'react'
import { getPlaces, request, type PlaceOption } from '../../api/activities'
import PlaceForm from './PlaceForm'

export default function PlacesScreen({ onChanged }: { onChanged: () => void }) {
  const [places, setPlaces] = useState<PlaceOption[]>([])
  const [editing, setEditing] = useState<PlaceOption | 'new' | null>(null)
  const [deleting, setDeleting] = useState<PlaceOption | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const reload = useCallback(async () => {
    setLoading(true); setError('')
    try { setPlaces(await getPlaces()) } catch { setError('Lokacij ni bilo mogoče naložiti.') } finally { setLoading(false) }
  }, [])
  useEffect(() => {
    let active = true
    void getPlaces().then(data => { if (active) setPlaces(data) }).catch(() => { if (active) setError('Lokacij ni bilo mogoče naložiti.') }).finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [])
  const changed = () => { setEditing(null); setDeleting(null); void reload(); onChanged() }
  const remove = async () => {
    if (!deleting || busy) return
    setBusy(true); setError('')
    try { await request(`places/${deleting.id}`, undefined, 'DELETE'); changed() } catch { setError('Brisanje ni uspelo. Poskusi znova.') } finally { setBusy(false) }
  }
  if (editing) return <PlaceForm place={editing === 'new' ? undefined : editing} onSaved={changed} onCancel={() => setEditing(null)} />
  return <section><div className="head"><h1>Lokacije</h1><button className="primary" onClick={() => setEditing('new')}>Dodaj lokacijo</button></div>
    {loading && <p role="status">Nalagam lokacije …</p>}{error && <div role="alert">{error}<button className="outline" onClick={() => void reload()}>Poskusi znova</button></div>}
    {!loading && !error && places.length === 0 && <p>Še nimaš shranjenih lokacij.</p>}
    <div className="list">{places.map(place => <article className="panel row" key={place.id}><strong>{place.name}</strong><div className="row"><button className="outline" onClick={() => setEditing(place)}>Uredi {place.name}</button><button className="danger" onClick={() => setDeleting(place)}>Izbriši {place.name}</button></div></article>)}</div>
    {deleting && <div className="shade"><div className="panel modal" role="dialog" aria-modal="true" aria-label="Potrditev brisanja"><h2>Izbrišem lokacijo {deleting.name}?</h2><p>Pretekle aktivnosti bodo ostale shranjene.</p><div className="row"><button className="outline" onClick={() => setDeleting(null)} disabled={busy}>Prekliči</button><button className="danger" onClick={() => void remove()} disabled={busy}>Potrdi brisanje</button></div></div></div>}
  </section>
}
