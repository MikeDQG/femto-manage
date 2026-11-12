import type { Activity } from '../../types';
import TextButton from '../ui/buttons/TextButton';

type ActivityOverviewProps = {
    activities: Activity[];
    date: string;
    onViewTimeline: () => void;
};

export default function ActivityOverview({ activities, date, onViewTimeline }: ActivityOverviewProps) {
    return (
        <section className="panel">
            <label className="eyebrow">{date}</label>
            <div className="row">
                <h2>Dan v treh dejanjih</h2>
                <TextButton onClick={onViewTimeline}>Odpri časovnico →</TextButton>
            </div>
            {activities.slice(0, 3).map((activity, i) => (
                <div className="mini" key={activity.title}>
                    <b>{['PREJ', 'ZDAJ', 'SLEDI'][i]}</b>
                    <div>
                        <strong>{activity.title}</strong>
                        <small>{activity.start} · {activity.place}</small>
                    </div>
                </div>
            ))}
        </section>
    );
}
