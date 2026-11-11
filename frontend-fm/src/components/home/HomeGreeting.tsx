import TextButton from '../ui/buttons/TextButton';

type HomeGreetingProps = {
    name: string;
    date: string;
    onViewDay: () => void;
};

export default function HomeGreeting({ name, date, onViewDay }: HomeGreetingProps) {
    return (
        <div className="home-greeting">
            <h1>Dobro jutro, {name}<span>.</span></h1>
            <div className="home-meta">
                <span className="eyebrow">{date}</span>
                <TextButton onClick={onViewDay}>Poglej dan ↗</TextButton>
            </div>
        </div>
    );
}
