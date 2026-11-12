# Ročni vnos aktivnosti (US-01)

Opravila [#7](https://github.com/MikeDQG/femto-manage/issues/7), [#8](https://github.com/MikeDQG/femto-manage/issues/8) in [#9](https://github.com/MikeDQG/femto-manage/issues/9) uporabljajo prijavljeno sejo iz [navodil za avtentikacijo](08-avtentikacija.md).

`GET /api/categories` vrne aktivne skupne kategorije, `GET /api/places` lastne shranjene kraje, `GET /api/activities?date=YYYY-MM-DD&zone=Europe/Ljubljana` pa potrjene aktivnosti, ki segajo v izbrani lokalni dan. Izbrani kraj je lahko prazen; geolokacija in AI za ročni vnos nista potrebna.

`POST /api/activities` sprejme `title`, `startAt`, `endAt`, `categoryId`, neobvezna `placeId` in `description` ter `confirmOverlap`. Časa vsebujeta odmik časovne cone. Lastnika strežnik določi iz seje. Neveljavna polja vrnejo 400 z `fields`; prekrivanje vrne 409 z `overlaps` (naziv in interval). Ponovna potrditev s `confirmOverlap: true` ponovno preveri validacijo, lastništvo in prekrivanje, nato ustvari zapis. Zgolj stik intervalov ni prekrivanje.

Migracija V3 vzpostavi osnovne skupne kategorije in tabeli za kraje ter aktivnosti. Samostojno upravljanje krajev in kategorij je predmet drugih uporabniških zgodb; ročni vnos uporablja samo obstoječe zapise. Produkcijska migracija V4 tabelam onemogoči javni dostop prek Supabase Data API.

## Urejanje potrjene aktivnosti (#66)

[#66](https://github.com/MikeDQG/femto-manage/issues/66) dodaja `GET /api/activities/{id}` in `PUT /api/activities/{id}`. Branje vrne izvorni interval, opis, ID-ja in prikazni imeni kategorije ter kraja, vir, stanje in časa nastanka ter posodobitve. Vrne tudi zgodovinsko neaktivno kategorijo ali izbrisani lastni kraj. Tuj, neobstoječ ali nepotrjen zapis vrne enako napako 404; brez prijave je odgovor 401.

`PUT` sprejme `title`, `startAt`, `endAt`, `categoryId`, neobvezna `placeId` in `description` ter `confirmOverlap`. Gre za zamenjavo urejljivih polj, zato zahtevana polja niso izbirna; manjkajoča neobvezna polja se izpraznijo. Neznana polja so zavrnjena s 400, tudi `id`, `userId`, `createdAt`, `updatedAt`, `source` in `status`. Lastnik je določen iz seje. ID, lastnik, createdAt, vir in stanje se ne spreminjajo; updatedAt se ob uspehu osveži. Pisanje zahteva CSRF. Uspeh vrne 200 z istim zapisom in ne poveča števila aktivnosti.

Ustvarjanje in urejanje uporabljata skupno validacijo v `engine/activity/ActivityService`: obvezen naziv do 200 znakov, veljaven interval, kategorija in opis do 2000 znakov. Urejani zapis se izloči iz preverjanja prekrivanj. Druge lastne potrjene aktivnosti vrnejo 409 z `overlaps` (naziv, izvorni začetek in konec); `confirmOverlap: true` ponovi validacijo in dovoli shranjevanje. Stik intervalov je dovoljen. Validacija in nepotrjeno prekrivanje se zaključita pred spremembo entitete, zato ni delno shranjenih podatkov.

Lokalna odločitev za zgodovinske povezave: ob nespremenjenem ID-ju kategorije se ohrani tudi neaktivna kategorija; ob nespremenjenem ID-ju kraja se ohrani izbrisan kraj, vendar strežnik še vedno preveri lastništvo. Nova kategorija mora biti aktivna, nov kraj pa lasten in neizbrisan. Obrazec doda označeno zgodovinsko možnost, če je ni v seznamu dovoljenih novih izbir; uporabnik lahko kraj tudi odstrani. Nova shema ali ADR za to lokalno pravilo nista potrebna.

Na časovnici izberi aktivnost → **Uredi** → spremeni polja → **Shrani spremembe**. Obrazec ohrani izvorna časovna trenutka, če uporabnik ne spremeni prikazanih časov, tudi sekund in odmika v ponovljeni uri ob menjavi časa. Ob napaki ostane vnos; med oddajo so polja in gumb za shranjevanje onemogočeni. Preklic vrne na isti izbrani dan. Po uspehu se ponovno naložijo časovnica, dnevni seštevki in današnji seznam na domačem zaslonu. Cilji in radarski diagram so v tej osnovi še prototip s stalnimi podatki; #66 ne uvaja upravljanja ciljev. Že deljeni posnetki se ne spreminjajo.

Preverjanje: `ActivityUpdateTests` pokrijejo metapodatke, validacijo, pravice, CSRF, zgodovinske povezave, prekrivanje, stik intervalov in premik med dnevi; `SummarySharingTests` preverijo nespremenjenost deljenega posnetka po urejanju prek API-ja. Brskalniški `activity-edit.spec.ts` pokrijejo potek na telefonu in namizju, napake, preklic, osveževanje in izvorni interval čez polnoč.

Preverjeno za #66 dne 12. 11. 2026:

- `backend-fm`: `.\mvnw.cmd test` — 47 uspešnih testov.
- `frontend-fm`: `npm.cmd run build` in `npm.cmd run lint` — uspešno.
- `npm.cmd run test:e2e:isolated -- activity-edit.spec.ts calendar.spec.ts context-suggestion.spec.ts --workers=4` — 20 uspešnih brskalniških testov v H2 na telefonu in namizju.
- `docker compose up --build -d` — oba servisa uspešno zgrajena in zagnana; `npm.cmd run test:e2e -- activity-edit.spec.ts --workers=2 --timeout=60000 --output=../backend-fm/target/issue-66-compose-browser` — 8 uspešnih testov proti dejanski razvojni aplikaciji na `localhost:5173`. Testi ustvarijo lastne račune z naključnimi naslovi `edit-…@example.com`; obstoječih računov in aktivnosti ne popravljajo. Shranijo tudi posnetka podrobnosti ter obrazca za obe širini; vizualni pregled je potrdil ustrezno postavitev.
- Poti in lokalne povezave spremenjenih dokumentov ter `git diff --check` — brez napak.
