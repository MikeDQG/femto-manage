# Ročni vnos aktivnosti (US-01)

Opravila [#7](https://github.com/MikeDQG/femto-manage/issues/7), [#8](https://github.com/MikeDQG/femto-manage/issues/8) in [#9](https://github.com/MikeDQG/femto-manage/issues/9) uporabljajo prijavljeno sejo iz [navodil za avtentikacijo](08-avtentikacija.md).

`GET /api/categories` vrne aktivne skupne kategorije, `GET /api/places` lastne shranjene kraje, `GET /api/activities?date=YYYY-MM-DD&zone=Europe/Ljubljana` pa potrjene aktivnosti, ki segajo v izbrani lokalni dan. Izbrani kraj je lahko prazen; geolokacija in AI za ročni vnos nista potrebna.

`POST /api/activities` sprejme `title`, `startAt`, `endAt`, `categoryId`, neobvezna `placeId` in `description` ter `confirmOverlap`. Časa vsebujeta odmik časovne cone. Lastnika strežnik določi iz seje. Neveljavna polja vrnejo 400 z `fields`; prekrivanje vrne 409 z `overlaps` (naziv in interval). Ponovna potrditev s `confirmOverlap: true` ponovno preveri validacijo, lastništvo in prekrivanje, nato ustvari zapis. Zgolj stik intervalov ni prekrivanje.

Migracija V3 vzpostavi osnovne skupne kategorije in tabeli za kraje ter aktivnosti. Samostojno upravljanje krajev in kategorij je predmet drugih uporabniških zgodb; ročni vnos uporablja samo obstoječe zapise. Produkcijska migracija V4 tabelam onemogoči javni dostop prek Supabase Data API.
