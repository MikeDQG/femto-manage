import type { Category } from '../../types';
import TextButton from '../ui/buttons/TextButton';

type DailyBalanceProps = {
    categories: Category[];
    onViewGoals: () => void;
};

export default function DailyBalance({ categories, onViewGoals }: DailyBalanceProps) {
    return (
        <section className="panel">
            <div className="row">
                <div>
                    <label className="eyebrow">TVOJ DANAŠNJI RITEM</label>
                    <h2>Ravnovesje dneva</h2>
                </div>
                <TextButton onClick={onViewGoals}>Vsi cilji ↗</TextButton>
            </div>
            <Radar categories={categories} />
            <div className="caption">● Današnji napredek <span>63% povprečno</span></div>
        </section>
    );
}

function Radar({ categories }: { categories: Category[] }) {
    const points = (scale: number) => categories.map((_, i) => {
        const angle = -Math.PI / 2 + i * Math.PI / 3;
        return `${150 + Math.cos(angle) * 98 * scale},${125 + Math.sin(angle) * 98 * scale}`;
    }).join(' ');
    const progressPoints = categories.map((category, i) => {
        const angle = -Math.PI / 2 + i * Math.PI / 3;
        const radius = 98 * category[2] / category[1];
        return `${150 + Math.cos(angle) * radius},${125 + Math.sin(angle) * radius}`;
    }).join(' ');

    return (
        <svg viewBox="0 0 300 260">
            <g fill="none" stroke="#e7e5dd">
                {[.33, .66, 1].map(scale => <polygon key={scale} points={points(scale)} />)}
                {categories.map(([name], i) => {
                    const angle = -Math.PI / 2 + i * Math.PI / 3;
                    return <line key={name} x1="150" y1="125" x2={150 + Math.cos(angle) * 98} y2={125 + Math.sin(angle) * 98} />;
                })}
            </g>
            <polygon points={progressPoints} fill="#c7775c44" stroke="#bb694c" strokeWidth="2" />
            {categories.map(([name], i) => {
                const angle = -Math.PI / 2 + i * Math.PI / 3;
                return <text key={name} x={150 + Math.cos(angle) * 120} y={125 + Math.sin(angle) * 120} textAnchor="middle">{name}</text>;
            })}
        </svg>
    );
}
