import { useState } from 'react';
import { MapPin, CalendarDays, House, ChartNoAxesColumnIncreasing, Settings, LocateFixed } from 'lucide-react';
import HomeGreeting from './components/home/HomeGreeting';
import DailyBalance from './components/home/DailyBalance';
import ActivityOverview from './components/home/ActivityOverview';
import type { Activity as Event, Category } from './types';
import type { User } from './api/auth';

type Screen = 'home' | 'goals' | 'calendar' | 'locations' | 'settings' | 'event' | 'locationForm';
const cats: Category[] = [['Delo', 240, 115], ['Gibanje', 60, 45], ['Osebna rast', 60, 25], ['Odnosi', 90, 30], ['Počitek', 120, 70], ['Ustvarjanje', 90, 20]];
const seed: Event[] = [{ title: 'Jutranji tek', start: '07:00', end: '08:00', category: 'Gibanje', place: 'Mestni park', description: 'Lahek tek za dober začetek dneva.' }, { title: 'Razvoj projekta', start: '09:00', end: '11:00', category: 'Delo', place: 'Domača pisarna', description: 'Delo na prototipu.' }, { title: 'Kosilo s prijatelji', start: '12:30', end: '13:30', category: 'Odnosi', place: 'Kavarna Rožmarin', description: 'Sproščeno kosilo.' }];
export default function WorkspaceScreen({ user, onLogout }: { user: User; onLogout: () => void }) { const [theme, setTheme] = useState<'light' | 'dark'>(() => localStorage.getItem('femto-theme') === 'dark' ? 'dark' : 'light'), [screen, S] = useState<Screen>('home'), [events, E] = useState(seed), [locations, L] = useState(['Domača pisarna', 'Mestni park', 'Kavarna Rožmarin']), [modal, M] = useState<Event | null>(null), [form, F] = useState<Event>({ title: '', start: '09:00', end: '10:00', category: 'Delo', place: '', description: '' }), [name, N] = useState(''), [empty, Z] = useState(false), [toast, T] = useState(''); const notify = (s: string) => { T(s); setTimeout(() => T(''), 2000); }; const nav: [
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
    const start = (e?: Event) => {
        F(e || { title: '', start: '09:00', end: '10:00', category: 'Delo', place: '', description: '' });
        S('event');
        M(null); };
    const field = (label: string, value: string, change: (s: string) => void, type = 'text') =>
        <label className="field">
            <span>{label}</span>
            <input type={type} value={value} onChange={e => change(e.target.value)}/>
        </label>;
    return <div className="app" data-theme={theme}>
        {<>
                <main className="content">{screen === 'home' && <>
                    <HomeGreeting name={user.email.split('@')[0]} date="SREDA · 11. NOVEMBER 2026" onViewDay={() => S('calendar')} />
                    <div className="grid">
                        <DailyBalance categories={cats} onViewGoals={() => S('goals')} />
                        <ActivityOverview activities={events} date="SREDA, 11. NOV" onViewTimeline={() => S('calendar')} />
                    </div>
                    <p className="quote">✳&#x3000;“Več namena v času, ki ga imaš.”</p></>}
                    {screen === 'goals' && <><Page title="Cilji & kategorije" sub="Majhne namere, ki oblikujejo tvoj dan." action={
                        <button className="primary" onClick={() => M({ title: 'goal', start: '', end: '', category: '', place: '', description: '' })}>＋ Dodaj kategorijo</button>}/>
                        {empty ? <Empty action={() => Z(false)}/> : <div className="list">{cats.map(([n, g, d], i) =>
                            <div className="panel goal" key={n}><span className="cat">✳</span><b>{n}<small>{g} min dnevno</small></b>
                                <div className="progress">{d} / {g} min <i><i style={{ width: `${d / g * 100}%`, background: ['#356859', '#cb805a', '#8175b5'][i % 3] }}/></i></div>
                                <b>{Math.round(d / g * 100)}%</b></div>)}</div>}
                        <button className="link" onClick={() => Z(!empty)}>{empty ? 'Prikaži vzorčne podatke' : 'Predogled praznega stanja'}</button>
                    </>}{screen === 'calendar' && <><Page title="Časovnica" sub="Tvoj dan, v tvojem ritmu." action={<button className="primary" onClick={() => start()}>＋ Dodaj aktivnost</button>}/><div className="panel calendar">{Array.from({ length: 16 }, (_, i) => i + 6).map(h => <div className="hour" key={h}><small>{String(h).padStart(2, '0')}:00</small><div>{events.filter(e => +e.start.slice(0, 2) === h).map(e => <button className="event" onClick={() => M(e)} key={e.title}><b>{e.start} — {e.end}&#x3000;{e.title}</b><small>{e.place} · {e.category}</small></button>)}</div></div>)}</div></>}{screen === 'locations' && <><Page title="Lokacije" sub="Kraji, kjer se tvoje življenje odvija." action={<button className="primary" onClick={() => { N(''); S('locationForm'); }}>＋ Dodaj lokacijo</button>}/><div className="places">{locations.map((p, i) => <button className="panel place" key={p} onClick={() => { M({ title: 'place', start: '', end: '', category: '', place: p, description: '' }); }}><span>{['⌂', '⌖', '☕'][i % 3]}</span><b>{p}</b></button>)}</div></>}{screen === 'settings' && <><Page title="Nastavitve" sub="Profil in predstavitvene možnosti."/><div className="panel form"><b>Videz</b><p>Izberi temo za to napravo.</p><div className="theme-choice"><button className={theme === 'light' ? 'selected' : ''} onClick={() => { setTheme('light'); localStorage.setItem('femto-theme', 'light'); }}>Svetla</button><button className={theme === 'dark' ? 'selected' : ''} onClick={() => { setTheme('dark'); localStorage.setItem('femto-theme', 'dark'); }}>Temna</button></div><b>Videz</b><p>Izberi temo za to napravo.</p><div className="theme-choice"><button className={theme === 'light' ? 'selected' : ''} onClick={() => { setTheme('light'); localStorage.setItem('femto-theme', 'light'); }}>Svetla</button><button className={theme === 'dark' ? 'selected' : ''} onClick={() => { setTheme('dark'); localStorage.setItem('femto-theme', 'dark'); }}>Temna</button></div><b>{user.email}</b><p>{user.role}</p><button className="outline" onClick={onLogout}>Odjava</button><p>Vzorčni podatki · brez povezave</p></div></>}{screen === 'event' && <><Page title="Nova aktivnost" sub="Zapiši čas, ki si ga namenil pomembnemu."/><form className="panel form" onSubmit={e => { e.preventDefault(); if (!form.title || form.end <= form.start) {
    notify('Vnesi naziv in veljaven čas.');
    return;
} E([...events, form]); S('calendar'); notify('Aktivnost shranjena'); }}>{field('NAZIV', form.title, v => F({ ...form, title: v }))}<div className="twocol">{field('ZAČETEK', form.start, v => F({ ...form, start: v }), 'time')}{field('KONEC', form.end, v => F({ ...form, end: v }), 'time')}</div><label className="field"><span>LOKACIJA</span><select value={form.place} onChange={e => F({ ...form, place: e.target.value })}>{locations.map(x => <option>{x}</option>)}</select></label><label className="field"><span>KATEGORIJA</span><select value={form.category} onChange={e => F({ ...form, category: e.target.value })}>{cats.map(x => <option>{x[0]}</option>)}</select></label><label className="field"><span>OPIS</span><textarea value={form.description} onChange={e => F({ ...form, description: e.target.value })}/></label><div className="row"><button type="button" className="outline" onClick={() => notify('Glasovni vnos bo na voljo pozneje.')}>◉ Diktiraj</button><button className="primary">Shrani aktivnost ↗</button></div></form></>}{screen === 'locationForm' && <><Page title="Nova lokacija" sub="Kraji dajo aktivnostim kontekst."/><form className="panel form" onSubmit={e => { e.preventDefault(); if (!name) {
    notify('Naziv lokacije je obvezen.');
    return;
} L([...locations, name]); S('locations'); }}>{field('NAZIV LOKACIJE', name, v => N(v))}<button type="button" className="location-use" onClick={() => notify('Uporabljena vzor?na lokacija')}><LocateFixed size={18}/> Uporabi trenutno lokacijo <small>Prihrani ro?ni vnos naslova</small></button>{field('NASLOV (NEOBVEZNO)', '', () => { })}<div className="twocol">{field('ZEM. ŠIRINA', '46.0569', () => { })}{field('ZEM. DOLŽINA', '14.5058', () => { })}</div>{field('RADIJ (METRI)', '100', () => { }, 'number')}<button type="button" className="outline" onClick={() => notify('Uporabljena vzorčna lokacija')}>⌖ Uporabi trenutno lokacijo</button><button className="primary">Shrani lokacijo</button></form></>}</main><nav>{nav.map(([key, icon, label]) => <button className={screen === key ? 'active' : ''} onClick={() => S(key)} key={key}>{icon}<small>{label}</small></button>)}</nav></>}{modal && <div className="shade" onClick={() => M(null)}><div className="panel modal" onClick={e => e.stopPropagation()}><div className="row"><label className="eyebrow">PODROBNOST</label><button onClick={() => M(null)}>×</button></div>{modal.title === 'place' ? <><h2>{modal.place}</h2><p>⌖&#x3000;Maribor · 46.0569° N · radij 100 m</p><div className="row"><button className="outline" onClick={() => { N(modal.place); M(null); S('locationForm'); }}>Uredi</button><button className="danger" onClick={() => { L(locations.filter(x => x !== modal.place)); M(null); }}>Odstrani</button></div></> : <><h2>{modal.title}</h2><p>{modal.start}—{modal.end} · {modal.place} · {modal.category}</p><p>{modal.description}</p><div className="row"><button className="outline" onClick={() => start(modal)}>Uredi</button><button className="danger" onClick={() => { E(events.filter(x => x !== modal)); M(null); }}>Odstrani</button></div></>}</div></div>}{toast && <div className="toast">{toast}</div>}</div>; }
function Page({ title, sub, action }: {
    title: string;
    sub: string;
    action?: React.ReactNode;
}) { return <div className="head"><div><label className="eyebrow">FEMTO · TVOJ PROSTOR</label><h1>{title}<span>.</span></h1><p>{sub}</p></div>{action}</div>; }
function Empty({ action }: {
    action: () => void;
}) { return <div className="panel empty"><h2>Še nimaš kategorij</h2><p>Dodaj prvo kategorijo in določi, čemu želiš nameniti čas.</p><button className="primary" onClick={action}>＋ Dodaj</button></div>; }
