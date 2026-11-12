import PlacesScreen from './components/places/PlacesScreen';
import { useCallback, useEffect, useState } from 'react';
import ManualActivityForm from './components/activity/ManualActivityForm';
import { getActivities, getCategories, getPlaces, type CategoryOption, type PlaceOption } from './api/activities';
import { MapPin, CalendarDays, House, ChartNoAxesColumnIncreasing, Settings, LocateFixed } from 'lucide-react';
import HomeGreeting from './components/home/HomeGreeting';
import DailyBalance from './components/home/DailyBalance';
import ActivityOverview from './components/home/ActivityOverview';
import type { Activity as Event, Category } from './types';
import type { User } from './api/auth';

type Screen = 'home' | 'goals' | 'calendar' | 'locations' | 'settings' | 'event' | 'locationForm';
const cats: Category[] = [['Delo', 240, 115], ['Gibanje', 60, 45], ['Osebna rast', 60, 25], ['Odnosi', 90, 30], ['Počitek', 120, 70], ['Ustvarjanje', 90, 20]];
export default function WorkspaceScreen({ user, onLogout }: { user: User; onLogout: () => void }) { const [theme, setTheme] = useState<'light' | 'dark'>(() => localStorage.getItem('femto-theme') === 'dark' ? 'dark' : 'light'), [screen, S] = useState<Screen>('home'), [events, E] = useState<Event[]>([]), [categoryOptions, setCategoryOptions] = useState<CategoryOption[]>([]), [placeOptions, setPlaceOptions] = useState<PlaceOption[]>([]), [locations, L] = useState(['Domača pisarna', 'Mestni park', 'Kavarna Rožmarin']), [modal, M] = useState<Event | null>(null), [name, N] = useState(''), [empty, Z] = useState(false), [toast, T] = useState(''); const notify = (s: string) => { T(s); setTimeout(() => T(''), 2000); }; const nav: [
    Screen,
    React.ReactNode,
    string
][] = [
    ['locations', <MapPin />, 'Lokacije'],
    ['calendar', <CalendarDays />, 'Koledar'],
    ['home', <House />, 'Domov'],
    ['goals', <ChartNoAxesColumnIncreasing />, 'Cilji'],
    ['settings', <Settings />, 'Nastavitve']
];
    const loadJournal = useCallback(() => {
        const date = new Date().toLocaleDateString('en-CA');
        void Promise.all([getCategories(), getPlaces(), getActivities(date)]).then(([categories, places, activities]) => {
            setCategoryOptions(categories);
            setPlaceOptions(places);
            E(activities.map(activity => ({ title: activity.title,
                start: new Date(activity.startAt).toLocaleTimeString('sl-SI', { hour: '2-digit', minute: '2-digit' }),
                end: new Date(activity.endAt).toLocaleTimeString('sl-SI', { hour: '2-digit', minute: '2-digit' }),
                category: categories.find(category => category.id === activity.categoryId)?.name ?? '',
                place: places.find(place => place.id === activity.placeId)?.name ?? '',
                description: activity.description ?? '' })));
        }).catch(() => T('Dnevnika ni bilo mogoče naložiti.'));
    }, []);
    useEffect(() => { loadJournal(); }, [loadJournal]);
    const start = () => { S('event'); M(null); };
    const field = (label: string, value: string, change: (s: string) => void, type = 'text') =>
        <label className="field">
            <span>{label}</span>
            <input type={type} value={value} onChange={e => change(e.target.value)}/>
        </label>;
    return <div className="app" data-theme={theme}>
        {<>
                <main className="content">{screen === 'home' && <>
                    <HomeGreeting name={user.email.split('@')[0]} date={new Date().toLocaleDateString("sl-SI", { weekday: "long", day: "numeric", month: "long", year: "numeric" })} onViewDay={() => S('calendar')} />
                    <div className="grid">
                        <DailyBalance categories={cats} onViewGoals={() => S('goals')} />
                        <ActivityOverview activities={events} date={new Date().toLocaleDateString("sl-SI")} onViewTimeline={() => S('calendar')} />
                    </div>
                    <p className="quote">✳&#x3000;“Več namena v času, ki ga imaš.”</p></>}
                    {screen === 'goals' && <><Page title="Cilji & kategorije" sub="Majhne namere, ki oblikujejo tvoj dan." action={
                        <button className="primary" onClick={() => M({ title: 'goal', start: '', end: '', category: '', place: '', description: '' })}>＋ Dodaj kategorijo</button>}/>
                        {empty ? <Empty action={() => Z(false)}/> : <div className="list">{cats.map(([n, g, d], i) =>
                            <div className="panel goal" key={n}><span className="cat">✳</span><b>{n}<small>{g} min dnevno</small></b>
                                <div className="progress">{d} / {g} min <i><i style={{ width: `${d / g * 100}%`, background: ['#356859', '#cb805a', '#8175b5'][i % 3] }}/></i></div>
                                <b>{Math.round(d / g * 100)}%</b></div>)}</div>}
                        <button className="link" onClick={() => Z(!empty)}>{empty ? 'Prikaži vzorčne podatke' : 'Predogled praznega stanja'}</button>
                    </>}{screen === 'calendar' && <><Page title="Časovnica" sub="Današnje potrjene aktivnosti." action={<button className="primary" onClick={() => start()}>＋ Dodaj aktivnost</button>}/><div className="panel list">{events.length === 0 && <p>Za danes še ni aktivnosti.</p>}{events.map((activity, index) => <div className="mini" key={index}><b>{activity.start}–{activity.end}</b><div><strong>{activity.title}</strong><small>{activity.category}{activity.place ? ` · ${activity.place}` : ''}</small></div></div>)}</div></>}{screen === 'locations' && <PlacesScreen onChanged={loadJournal} />}{screen === 'settings' && <><Page title="Nastavitve" sub="Profil in predstavitvene možnosti."/><div className="panel form"><b>Videz</b><p>Izberi temo za to napravo.</p><div className="theme-choice"><button className={theme === 'light' ? 'selected' : ''} onClick={() => { setTheme('light'); localStorage.setItem('femto-theme', 'light'); }}>Svetla</button><button className={theme === 'dark' ? 'selected' : ''} onClick={() => { setTheme('dark'); localStorage.setItem('femto-theme', 'dark'); }}>Temna</button></div><b>Videz</b><p>Izberi temo za to napravo.</p><div className="theme-choice"><button className={theme === 'light' ? 'selected' : ''} onClick={() => { setTheme('light'); localStorage.setItem('femto-theme', 'light'); }}>Svetla</button><button className={theme === 'dark' ? 'selected' : ''} onClick={() => { setTheme('dark'); localStorage.setItem('femto-theme', 'dark'); }}>Temna</button></div><b>{user.email}</b><p>{user.role}</p><button className="outline" onClick={onLogout}>Odjava</button><p>Vzorčni podatki · brez povezave</p></div></>}{screen === 'event' && <><Page title="Nova aktivnost" sub="Zapiši izvedeno aktivnost."/><ManualActivityForm categories={categoryOptions} places={placeOptions} onSaved={() => { loadJournal(); S('calendar'); notify('Aktivnost shranjena'); }} onCancel={() => S('calendar')} /></>}{screen === 'locationForm' && <><Page title="Nova lokacija" sub="Kraji dajo aktivnostim kontekst."/><form className="panel form" onSubmit={e => { e.preventDefault(); if (!name) {
    notify('Naziv lokacije je obvezen.');
    return;
} L([...locations, name]); S('locations'); }}>{field('NAZIV LOKACIJE', name, v => N(v))}<button type="button" className="location-use" onClick={() => notify('Uporabljena vzor?na lokacija')}><LocateFixed size={18}/> Uporabi trenutno lokacijo <small>Prihrani ro?ni vnos naslova</small></button>{field('NASLOV (NEOBVEZNO)', '', () => { })}<div className="twocol">{field('ZEM. ŠIRINA', '46.0569', () => { })}{field('ZEM. DOLŽINA', '14.5058', () => { })}</div>{field('RADIJ (METRI)', '100', () => { }, 'number')}<button type="button" className="outline" onClick={() => notify('Uporabljena vzorčna lokacija')}>⌖ Uporabi trenutno lokacijo</button><button className="primary">Shrani lokacijo</button></form></>}</main><nav>{nav.map(([key, icon, label]) => <button className={screen === key ? 'active' : ''} onClick={() => S(key)} key={key}>{icon}<small>{label}</small></button>)}</nav></>}{modal && <div className="shade" onClick={() => M(null)}><div className="panel modal" onClick={e => e.stopPropagation()}><div className="row"><label className="eyebrow">PODROBNOST</label><button onClick={() => M(null)}>×</button></div>{modal.title === 'place' ? <><h2>{modal.place}</h2><p>⌖&#x3000;Maribor · 46.0569° N · radij 100 m</p><div className="row"><button className="outline" onClick={() => { N(modal.place); M(null); S('locationForm'); }}>Uredi</button><button className="danger" onClick={() => { L(locations.filter(x => x !== modal.place)); M(null); }}>Odstrani</button></div></> : <><h2>{modal.title}</h2><p>{modal.start}—{modal.end} · {modal.place} · {modal.category}</p><p>{modal.description}</p><div className="row"><button className="outline" onClick={() => { M(null); notify('Urejanje aktivnosti še ni na voljo.'); }}>Uredi</button><button className="danger" onClick={() => { M(null); notify('Brisanje aktivnosti še ni na voljo.'); }}>Odstrani</button></div></>}</div></div>}{toast && <div className="toast">{toast}</div>}</div>; }
function Page({ title, sub, action }: {
    title: string;
    sub: string;
    action?: React.ReactNode;
}) { return <div className="head"><div><label className="eyebrow">FEMTO · TVOJ PROSTOR</label><h1>{title}<span>.</span></h1><p>{sub}</p></div>{action}</div>; }
function Empty({ action }: {
    action: () => void;
}) { return <div className="panel empty"><h2>Še nimaš kategorij</h2><p>Dodaj prvo kategorijo in določi, čemu želiš nameniti čas.</p><button className="primary" onClick={action}>＋ Dodaj</button></div>; }
