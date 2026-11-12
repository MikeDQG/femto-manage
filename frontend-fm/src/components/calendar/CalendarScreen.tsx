import { useEffect, useMemo, useState } from 'react'
import { getDailyJournal, type DailyJournal, type DayActivity } from '../../api/activities'
import styles from './CalendarScreen.module.css'
import DailyReflectionForm from './reflection/DailyReflectionForm'

type Props = { date: string; onDateChange: (date: string) => void; onAddActivity: () => void; refreshKey: number }

function formatMinutes(minutes: number) {
  const hours = Math.floor(minutes / 60)
  const rest = minutes % 60
  return hours ? `${hours} h ${rest} min` : `${rest} min`
}

function clock(instant: string) {
  return new Date(instant).toLocaleTimeString('sl-SI', { hour: '2-digit', minute: '2-digit' })
}

type Positioned = { activity: DayActivity; top: number; height: number; column: number; columns: number }

function position(activities: DayActivity[], dayStart: number, dayEnd: number): Positioned[] {
  const sorted = [...activities].sort((a, b) => Date.parse(a.segmentStartAt) - Date.parse(b.segmentStartAt))
  const groups: DayActivity[][] = []
  for (const activity of sorted) {
    const last = groups.at(-1)
    if (last && last.some(item => Date.parse(item.segmentEndAt) > Date.parse(activity.segmentStartAt))) last.push(activity)
    else groups.push([activity])
  }
  return groups.flatMap(group => {
    const columnEnds: number[] = []
    const assigned = group.map(activity => {
      const start = Date.parse(activity.segmentStartAt)
      let column = columnEnds.findIndex(end => end <= start)
      if (column < 0) column = columnEnds.length
      columnEnds[column] = Date.parse(activity.segmentEndAt)
      return { activity, column }
    })
    return assigned.map(({ activity, column }) => ({ activity, column, columns: columnEnds.length,
      top: (Date.parse(activity.segmentStartAt) - dayStart) / (dayEnd - dayStart) * 100,
      height: (Date.parse(activity.segmentEndAt) - Date.parse(activity.segmentStartAt)) / (dayEnd - dayStart) * 100 }))
  })
}

export default function CalendarScreen({ date, onDateChange, onAddActivity, refreshKey }: Props) {
  const [journal, setJournal] = useState<DailyJournal | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [retry, setRetry] = useState(0)

  useEffect(() => {
    let active = true
    getDailyJournal(date).then(result => {
      if (active) { setJournal(result); setError(''); setLoading(false) }
    }).catch(() => {
      if (active) { setJournal(null); setError('Dnevnega pregleda ni bilo mogoče naložiti.'); setLoading(false) }
    })
    return () => { active = false }
  }, [date, refreshKey, retry])

  const start = new Date(`${date}T00:00:00`)
  const end = new Date(start)
  end.setDate(end.getDate() + 1)
  const dayStart = start.getTime(), dayEnd = end.getTime()
  const hours = useMemo(() => {
    const markers = []
    for (let instant = dayStart; instant < dayEnd; instant += 3_600_000) {
      markers.push({ instant, label: new Date(instant).toLocaleTimeString('sl-SI', { hour: '2-digit', minute: '2-digit' }) })
    }
    return markers
  }, [dayStart, dayEnd])
  const blocks = journal ? position(journal.activities, dayStart, dayEnd) : []

  return <section className={styles.screen}>
    <div className="head"><div><h1>Časovnica<span>.</span></h1>
      <p>Potrjene aktivnosti izbranega dne.</p></div>
      <button className="primary" onClick={onAddActivity}>＋ Dodaj aktivnost</button></div>
    <label className={styles.datePicker}>Izberi dan <input type="date" value={date} onChange={event => { if (event.target.value) { setLoading(true); onDateChange(event.target.value) } }} /></label>
    <DailyReflectionForm key={date} date={date} />
    {loading && <p role="status">Nalagam dnevni pregled …</p>}
    {error && <div className="panel" role="alert">{error}<button className="outline" onClick={() => { setLoading(true); setJournal(null); setRetry(current => current + 1) }}>Poskusi znova</button></div>}
    {!loading && journal && <div className={styles.layout}>
      <div className={`panel ${styles.timeline}`} aria-label="Dnevna časovnica">
        {journal.activities.length === 0 && <p className={styles.empty}>Za izbrani dan še ni potrjenih aktivnosti.</p>}
        <div className={styles.ruler}>{hours.map(hour => <div className={styles.hour} key={hour.instant}><small>{hour.label}</small><span /></div>)}</div>
        <div className={styles.blocks}>{blocks.map(({ activity, top, height, column, columns }) =>
          <div className={styles.block} key={activity.id} style={{ top: `${top}%`, height: `${height}%`,
            left: `${column / columns * 100}%`, width: `${100 / columns}%` }}>
            <strong>{activity.title}</strong><span>{clock(activity.segmentStartAt)}–{clock(activity.segmentEndAt)} · {formatMinutes(activity.durationMinutes)}</span>
            <span>{activity.categoryName}{activity.placeName ? ` · ${activity.placeName}` : ''}</span>
          </div>)}</div>
      </div>
      <aside className={`panel ${styles.totals}`}><h2>Trajanje po kategorijah</h2>
        {journal.totals.map(total => <div className={styles.total} key={total.categoryId}><span>{total.name}</span><strong>{formatMinutes(total.minutes)}</strong></div>)}
        <p>Pri potrjenih prekrivanjih se čas lahko šteje večkrat.</p>
      </aside>
    </div>}
  </section>
}
