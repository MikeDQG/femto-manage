# Organizacija komponent

- `ui/`: osnovne komponente za uporabo na različnih zaslonih, brez domenske logike.
- `ui/buttons/`: gumbi in njihovi slogi; `TextButton` je besedilni gumb.
- `home/`: sestavljene komponente domačega zaslona, ki uporabljajo osnovne UI komponente.
- `auth/`: obrazca registracije in prijave ter hook za trenutno sejo; HTTP dostop je v `src/api/auth.ts`.
- `activity/`: vrhnji zaslon vnosa, izbira načina ter obrazec za ročni vnos in pregled kontekstnega oziroma besedilnega osnutka.
- `calendar/`: dnevna časovnica; `reflection/` upravlja zasebno refleksijo, `sharing/` pa predogled, seznam in preklic delitev. HTTP dostop je v `src/api/activities.ts` in `src/api/summaries.ts`.
- `settings/`: zaslon nastavitev in enoten izbor teme.

Sloge osnovnih komponent hranimo ob komponenti v datoteki `*.module.css`.
Podatke in dejanja jim posredujemo prek props; navigacijo in stanje zaslona upravlja nadrejena komponenta.
