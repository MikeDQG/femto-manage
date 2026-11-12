import type { SummaryShare } from '../../../api/summaries'

export default function SharedSummaryList({ shares, onRevoke }: { shares: SummaryShare[]; onRevoke: (share: SummaryShare) => void }) {
  return <section aria-label="Moje delitve"><h2>Moje delitve</h2>
    {shares.length === 0 ? <p>Še nisi delil nobenega povzetka.</p> : <div className="list">{shares.map(share =>
      <article className="panel" key={share.id}><strong>{share.date} · {share.mentor.email}</strong>
        <p>{share.revokedAt ? 'Preklicano' : 'Dostop aktiven'}</p>
        <p>Deljeno: {new Date(share.createdAt).toLocaleString('sl-SI')}</p>
        {!share.revokedAt && <button className="danger" onClick={() => onRevoke(share)}>Prekliči deljenje</button>}
      </article>)}</div>}
  </section>
}
