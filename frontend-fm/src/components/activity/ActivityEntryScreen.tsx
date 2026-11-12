import { useState } from 'react'
import type { ActivityDraft, CategoryOption, PlaceOption } from '../../api/activities'
import type { PlaceRecognitionState } from '../place-recognition/usePlaceRecognition'
import ManualActivityForm from './ManualActivityForm'
import ContextSuggestionPanel from './ContextSuggestionPanel'

type Props = { categories: CategoryOption[]; places: PlaceOption[]; recognition: PlaceRecognitionState; onSaved: () => void; onCancel: () => void }

export default function ActivityEntryScreen(props: Props) {
  const [mode, setMode] = useState<'manual' | 'context'>('manual')
  const [draft, setDraft] = useState<ActivityDraft | undefined>()
  return <section>
    <div className="head"><div><h1>Nova aktivnost<span>.</span></h1><p>Zapiši izvedeno aktivnost.</p></div></div>
    {!draft && <div className="row entry-modes"><button className="outline" aria-pressed={mode === 'manual'} onClick={() => setMode('manual')}>Ročni vnos</button>
      <button className="outline" aria-pressed={mode === 'context'} onClick={() => setMode('context')}>Kontekstni predlog</button></div>}
    {mode === 'context' && !draft ? <ContextSuggestionPanel places={props.places} recognition={props.recognition} onDraft={setDraft} onManual={() => setMode('manual')} />
      : <ManualActivityForm key={draft ? 'draft' : 'manual'} {...props} draft={draft} onCancel={props.onCancel} />}
    {mode === 'context' && !draft && <button className="link" onClick={props.onCancel}>Prekliči</button>}
  </section>
}
