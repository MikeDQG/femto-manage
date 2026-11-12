import { useEffect, useState } from 'react'
import { JournalError } from '../../../api/activities'
import { confirmSummary, getMentors, getShares, previewSummary, revokeSummary, type Mentor, type SummaryPreview, type SummaryShare } from '../../../api/summaries'
import SharedSummaryList from './SharedSummaryList'
import SummaryPreviewCard from './SummaryPreviewCard'
import styles from './SummarySharingPanel.module.css'

export default function SummarySharingPanel({ date }: { date: string }) {
  const [mentors, setMentors] = useState<Mentor[]>([])
  const [shares, setShares] = useState<SummaryShare[]>([])
  const [mentorId, setMentorId] = useState('')
  const [comment, setComment] = useState('')
  const [preview, setPreview] = useState<SummaryPreview | null>(null)
  const [revoking, setRevoking] = useState<SummaryShare | null>(null)
  const [loading, setLoading] = useState(true)
  const [loaded, setLoaded] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [retry, setRetry] = useState(0)
  useEffect(() => {
    let active = true
    Promise.all([getMentors(), getShares()]).then(([recipients, existing]) => {
      if (active) { setMentors(recipients); setShares(existing); setLoaded(true); setLoading(false); setError('') }
    }).catch(() => { if (active) { setLoaded(false); setLoading(false); setError('Podatkov za deljenje ni bilo mogoče naložiti.') } })
    return () => { active = false }
  }, [retry])
  const showError = (failure: unknown) => setError(failure instanceof Error ? failure.message : 'Zahteva ni uspela.')
  const prepare = async () => {
    if (busy) return
    if (!mentorId) { setError('Izberi mentorja.'); return }
    setBusy(true); setError(''); setMessage('')
    try { setPreview(await previewSummary(date, mentorId, comment)) }
    catch (failure) { showError(failure) }
    finally { setBusy(false) }
  }
  const confirm = async () => {
    if (busy || !preview) return
    setBusy(true); setError(''); setMessage('')
    try {
      const shared = await confirmSummary(preview.previewId)
      setShares(current => [shared, ...current]); setPreview(null); setComment(''); setMessage('Povzetek je deljen.')
    } catch (failure) {
      if (failure instanceof JournalError && failure.status === 409) {
        setPreview(null)
        try {
          setPreview(await previewSummary(date, mentorId, comment))
          setMessage('Podatki so se spremenili. Preglej nov predogled in ponovno potrdi deljenje.')
        } catch (refreshFailure) { showError(refreshFailure) }
      } else showError(failure)
    } finally { setBusy(false) }
  }
  const revoke = async () => {
    if (busy || !revoking) return
    setBusy(true); setError(''); setMessage('')
    try {
      await revokeSummary(revoking.id)
      setRevoking(null)
      setShares(await getShares()); setMessage('Deljenje je preklicano.')
    } catch (failure) { showError(failure) }
    finally { setBusy(false) }
  }
  return <section className={`panel ${styles.panel}`} aria-label="Deljenje dnevnega povzetka">
    <h2>Deljenje dnevnega povzetka</h2>
    {loading ? <p role="status">Nalagam delitve …</p> : loaded && <>
      <p>Dan: {date}. Zasebna refleksija ostane samo tvoja.</p>
      {mentors.length === 0 ? <p>Ni računa z vlogo mentorja. Za dodelitev se obrni na skrbnika.</p> : preview ?
        <SummaryPreviewCard preview={preview} busy={busy} onConfirm={() => void confirm()} onCancel={() => { setPreview(null); setMessage('') }} /> : <>
          <label className="field"><span>Mentor za deljenje</span><select value={mentorId} onChange={event => setMentorId(event.target.value)} disabled={busy}>
            <option value="">Izberi mentorja</option>{mentors.map(mentor => <option key={mentor.id} value={mentor.id}>{mentor.email}</option>)}
          </select></label>
          <label className="field"><span>Komentar za mentorja (neobvezno)</span><textarea value={comment} maxLength={2000} onChange={event => setComment(event.target.value)} disabled={busy} /></label>
          <button className="outline" onClick={() => void prepare()} disabled={busy}>{busy ? 'Pripravljam predogled …' : 'Predogled za deljenje'}</button>
        </>}
      <SharedSummaryList shares={shares} onRevoke={setRevoking} />
    </>}
    {message && <p role="status">{message}</p>}{error && <div role="alert"><p>{error}</p><button className="outline" onClick={() => { setLoading(true); setRetry(current => current + 1) }} disabled={busy}>Ponovno naloži delitve</button></div>}
    {revoking && <div className="shade"><div className="panel modal" role="dialog" aria-modal="true" aria-label="Potrditev preklica deljenja">
      <h2>Prekličem deljenje?</h2><p>{revoking.date} · {revoking.mentor.email}</p><p>Mentor ne bo več mogel odpreti tega posnetka. Tvoj dnevnik ostane shranjen.</p>
      <div className="row"><button className="outline" onClick={() => setRevoking(null)} disabled={busy}>Ohrani deljenje</button><button className="danger" onClick={() => void revoke()} disabled={busy}>Potrdi preklic deljenja</button></div>
    </div></div>}
  </section>
}
