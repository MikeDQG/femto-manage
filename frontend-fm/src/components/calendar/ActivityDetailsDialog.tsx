import { useEffect, useRef, useState } from 'react'
import { getActivity, type ActivityDetails } from '../../api/activities'
import styles from './ActivityDetailsDialog.module.css'

type Props = { id: string; onClose: () => void; onEdit: (activity: ActivityDetails) => void }

export default function ActivityDetailsDialog({ id, onClose, onEdit }: Props) {
  const dialog = useRef<HTMLDialogElement>(null)
  const [activity, setActivity] = useState<ActivityDetails | null>(null)
  const [error, setError] = useState('')
  const [retry, setRetry] = useState(0)
  useEffect(() => {
    const element = dialog.current
    element?.showModal()
    return () => element?.close()
  }, [])
  useEffect(() => {
    let active = true
    getActivity(id).then(result => { if (active) { setActivity(result); setError('') } })
      .catch(() => { if (active) setError('Podrobnosti aktivnosti ni bilo mogoče naložiti. Poskusi znova.') })
    return () => { active = false }
  }, [id, retry])
  return <dialog ref={dialog} className={`panel ${styles.dialog}`} aria-labelledby="activity-details-title" onCancel={onClose}>
    <div className="row"><h2 id="activity-details-title">Podrobnosti aktivnosti</h2><button type="button" className="outline" onClick={onClose} aria-label="Zapri podrobnosti">×</button></div>
    {error ? <div role="alert"><p>{error}</p><button className="outline" onClick={() => { setError(''); setRetry(current => current + 1) }}>Poskusi znova</button></div>
      : activity ? <>
        <h3>{activity.title}</h3>
        <dl><dt>Začetek</dt><dd>{new Date(activity.startAt).toLocaleString('sl-SI')}</dd>
          <dt>Konec</dt><dd>{new Date(activity.endAt).toLocaleString('sl-SI')}</dd>
          <dt>Kategorija</dt><dd>{activity.categoryName}</dd>
          <dt>Kraj</dt><dd>{activity.placeName ?? 'Brez kraja'}</dd>
          <dt>Opis</dt><dd className={styles.description}>{activity.description || 'Brez opisa'}</dd></dl>
        <button type="button" className="primary" onClick={() => onEdit(activity)}>Uredi</button>
      </> : <p role="status">Nalagam podrobnosti …</p>}
  </dialog>
}
