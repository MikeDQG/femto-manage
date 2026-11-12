import type { PlaceRecognitionState } from './usePlaceRecognition'

type Props = { recognition: PlaceRecognitionState; onSelect?: (id: string) => void }

export default function PlaceRecognitionPanel({ recognition, onSelect }: Props) {
  return <section className="panel form" aria-label="Prepoznavanje kraja">
    <div className="row"><strong>Trenutni kraj</strong>
      <button type="button" className="outline" onClick={recognition.check} disabled={recognition.loading}>
        {recognition.loading ? 'Preverjam …' : 'Preveri trenutni kraj'}
      </button>
    </div>
    {recognition.accuracy !== undefined && <p>Natančnost meritve: {Math.round(recognition.accuracy)} m</p>}
    {recognition.error && <p role="alert">{recognition.error}</p>}
    {recognition.result && (recognition.result.recognized
      ? <div><p>{recognition.result.candidates.length > 1
        ? onSelect ? 'Enako blizu je več krajev. Izberi kraj:' : 'Enako blizu je več krajev. Kraj izberi pri vnosu aktivnosti.'
        : 'Predlagani kraj:'}</p>
        {recognition.result.candidates.map(candidate => <div className="row" key={candidate.id}>
          <span>{candidate.name} ({Math.round(candidate.distanceM)} m)</span>
          {onSelect && <button type="button" className="outline" onClick={() => onSelect(candidate.id)}>Izberi {candidate.name}</button>}
        </div>)}</div>
      : <p>Znani kraj ni prepoznan.</p>)}
  </section>
}
