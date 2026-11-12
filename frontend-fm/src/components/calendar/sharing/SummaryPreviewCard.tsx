import type { SummaryPreview } from '../../../api/summaries'

type Props = { preview: SummaryPreview; busy: boolean; onConfirm: () => void; onCancel: () => void }

export default function SummaryPreviewCard({ preview, busy, onConfirm, onCancel }: Props) {
  return <section className="panel form" aria-label="Predogled deljenega povzetka">
    <h2>Predogled povzetka</h2><p>Datum: {preview.content.date} · Mentor: {preview.mentor.email}</p>
    {preview.content.totals.map(total => <div className="row" key={total.categoryId}><span>{total.name}</span><strong>{total.minutes} min</strong></div>)}
    <p>Komentar za mentorja: {preview.content.comment || 'Brez komentarja.'}</p>
    <p>Mentor vidi samo datum, seštevke po kategorijah in ta komentar. Lokacije, posamezne aktivnosti, podrobni opisi, izvorni vnosi in zasebna refleksija se ne delijo.</p>
    <div className="row"><button className="outline" onClick={onCancel} disabled={busy}>Prekliči predogled</button>
      <button className="primary" onClick={onConfirm} disabled={busy}>{busy ? 'Delim …' : 'Potrdi deljenje z mentorjem'}</button></div>
  </section>
}
