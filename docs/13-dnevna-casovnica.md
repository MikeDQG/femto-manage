# Dnevna časovnica (US-06)

Opravili [#24](https://github.com/MikeDQG/femto-manage/issues/24) in [#25](https://github.com/MikeDQG/femto-manage/issues/25) dodajata dnevni pregled potrjenih aktivnosti.

`GET /api/journal/day?date=YYYY-MM-DD&zone=Europe/Ljubljana` zahteva prijavo. Odgovor vsebuje `date`, `zone`, `activities` in `totals`. Vsaka aktivnost ima izvorni interval `startAt`/`endAt`, za izbrani lokalni dan odrezani interval `segmentStartAt`/`segmentEndAt`, trajanje segmenta v minutah ter naziv kategorije in morebitnega kraja. Ime izbrisanega kraja ostane vidno ob zgodovinski aktivnosti. `totals` vsebuje vse aktivne kategorije, tudi tiste z 0 minutami. Osnutki in tuji zapisi niso vključeni.

Seštevki uporabljajo dejansko trajanje v sekundah znotraj meja lokalnega dne. Čas prehoda na poletni ali zimski čas zato ni obravnavan kot fiksnih 24 ur. Sekunde se najprej seštejejo po kategoriji, šele nato za prikaz pretvorijo v cele minute. Potrjena prekrivanja se seštejejo po posameznih aktivnostih, zato lahko čas prispeva večkrat.

Zaslon Koledar omogoča izbiro dneva in prikaže bloke na časovni mreži ter seštevke po kategorijah. Datum ostane izbran tudi po vnosu nove aktivnosti. Prazni dnevi in napake nalaganja imajo ločen prikaz.

## Zasebna refleksija (#26)

[#26](https://github.com/MikeDQG/femto-manage/issues/26) dodaja ločeno tabelo `daily_reflection` (migraciji V6 in V7). Enoličnost uporabnik–datum in zaklep lastnika pri shranjevanju preprečita podvojitev tudi ob sočasni prvi zahtevi. PostgreSQL tabela ima RLS in odvzete pravice javnim vlogam Data API; dostop poteka prek Springove seje.

`GET /api/journal/reflection?date=YYYY-MM-DD` vrne lastno refleksijo ali prazna polja; `PUT` na isti naslov shrani oziroma posodobi `note` (največ 4000 znakov), `mood` (največ 100) in neobvezno celo `energy` (1–5). Pisanje zahteva CSRF; neznana polja, tudi lastnik, so zavrnjena. Vloge mentorja in skrbnika nimajo dostopa do tuje refleksije. API ne ponuja naslavljanja po tujem uporabniku ali ID-ju.

Obrazec na Koledarju je vezan na izbrani datum. Pri menjavi dneva se naloži drug obrazec, zato pozni odgovori ne zamenjajo njegove vsebine. Ob napaki shranjevanja vnos ostane; ob neuspešnem prvem branju je treba najprej ponovno naložiti podatke. Refleksija ne spreminja časovnih blokov ali seštevkov in ni vhod v deljenje.

Preverjanje: Maven testi `DailyReflectionTests`, frontend build/lint in `reflection.spec.ts` na telefonu ter namizju.
