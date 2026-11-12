import type { ActivityDetails, CategoryOption, PlaceOption } from '../../api/activities'
import type { PlaceRecognitionState } from '../place-recognition/usePlaceRecognition'
import ManualActivityForm from './ManualActivityForm'

type Props = { activity: ActivityDetails; categories: CategoryOption[]; places: PlaceOption[]; recognition: PlaceRecognitionState; onSaved: () => void; onCancel: () => void }

export default function ActivityEditScreen(props: Props) {
  return <section>
    <div className="head"><div><h1>Uredi aktivnost<span>.</span></h1><p>Popravi shranjeno aktivnost. Spremembe se shranijo šele ob potrditvi.</p></div></div>
    <ManualActivityForm key={props.activity.id} {...props} />
  </section>
}
