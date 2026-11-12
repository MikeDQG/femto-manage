# Kontekstni predlog (US-04)

Opravili [#19](https://github.com/MikeDQG/femto-manage/issues/19) in [#20](https://github.com/MikeDQG/femto-manage/issues/20) uporabljata pravila iz [ADR 005](adr/005-ADR-pravila-za-predloge.md).

`POST /api/activities/context-suggestion` sprejme `placeId`, `startAt` (časovni trenutek) in `zone` (IANA časovni pas). Zahteva prijavo in CSRF. Lastništvo ter neizbrisanost kraja preveri strežnik. V zgodovini lastnih potrjenih aktivnosti na tem kraju primerja lokalno uro začetka (0–23) v zahtevanem časovnem pasu, tudi za zgodovinske zapise čez poletni čas. Med aktivnimi kategorijami predlaga le enoznačno najpogostejšo. Izenačenje in prazna zgodovina vrneta razlog ter `draft: null`.

Osnutek vsebuje izbrani začetek, kraj, kategorijo, vir `SUGGESTION` in opozorili za manjkajoča naziv ter konec. Ne ugiba trajanja in ne piše v bazo. Uporabnik ga v obrazcu popravi, dopolni, potrdi ali zavrne. Potrditev uporablja obstoječi `/api/activities`, vključno z validacijo in izrecno potrditvijo prekrivanja. Zavrnitev osnutek zavrže brez spremembe seštevkov.

Do vnosa se pride prek Koledar → Dodaj aktivnost. Ročni vnos ostane privzet; zavihek Kontekstni predlog omogoča izbiro lastnega ali prepoznanega kraja ter časa. Napake in odsotnost zgodovine ohranijo ročni vnos.

Preverjanje: backend testi `ContextSuggestionTests`, celotni Maven testi, frontend build/lint ter `context-suggestion.spec.ts` na telefonu in namizju.

## Kje je implementirano

- Backend: `ContextSuggestionController` (`api/activity`) sprejme zahtevek; `ContextSuggestionService` (`engine/activity`) preveri lastništvo in prešteje kategorije potrjenih aktivnosti na istem kraju v isti lokalni uri začetka.
- Frontend: `components/activity/ActivityEntryScreen.tsx` sestavi načine vnosa, `ContextSuggestionPanel.tsx` pridobi predlog, `ManualActivityForm.tsx` pa omogoči popravek, dopolnitev, potrditev ali zavrnitev. HTTP klic je v `src/api/activities.ts`.
- To je predlog kategorije po pravilih, ne samodejni predlog na domačem zaslonu. AI, GPS in zunanji koledar za ročno izbran kraj niso potrebni. Datum ni pogoj ujemanja; ura začetka in ID kraja sta. Aktivnost od 9.00 do 11.00 šteje pri uri 9, ne pri uri 10.

## Prikaz z računom `a@a.a`

Priprava podatkov in teh navodil spada pod [#65 – FIX kontekstualni predlog](https://github.com/MikeDQG/femto-manage/issues/65). Pregled obstoječe implementacije ni pokazal potrebe po spremembi pravil: predlog se pripravi za izbrani kraj in uro začetka, zato je za ponovljiv prikaz pomembna ustrezna potrjena zgodovina in pravilna izbira ure.

V razvojno bazo so bili 12. 11. 2026 dodani štirje potrjeni zapisi za obstoječi testni kraj FERI: projektno delo 5., 6. in 7. novembra ter učenje 8. novembra, vsi od 9.00 do 10.00 v coni `Europe/Ljubljana`. Skupaj s tremi obstoječimi zapisi projektnega dela 10.–12. novembra ima ta kraj ob uri 9 šest zapisov kategorije **Delo** in enega kategorije **Osebna rast**.

1. Na <http://localhost:5173> se prijavi kot `a@a.a` in po potrebi osveži stran.
2. Odpri **Koledar → Dodaj aktivnost → Kontekstni predlog**.
3. Izberi **FERI** (ID `687d58f4-7e19-33a0-b547-81976e0a6c84`) ter začetek **13. 11. 2026 ob 9.00**. Brskalnik naj uporablja časovni pas `Europe/Ljubljana`.
4. Klikni **Pripravi kontekstni predlog**. Pričakovan je osnutek s kategorijo **Delo**, izbranim krajem in začetkom. Naziv ter konec ostaneta prazna z opozoriloma za dopolnitev.
5. Vpiši naziv, npr. »Predstavitev kontekstnega predloga«, ter konec **13. 11. 2026 ob 10.00**. Predlog lahko popraviš in potrdiš; potrjen zapis se prikaže v koledarju na ta datum. **Zavrni predlog** zapre osnutek brez novega zapisa.

Za prikaz odsotnosti zgodovine izberi isti kraj ob **7.00**. Premajhna zgodovina, druga ura, drug kraj z enakim imenom ali izenačeni kategoriji lahko pojasnijo odsotnost predloga. Za uspešen primer zadostuje enoznačna vodilna kategorija; posebnega minimalnega števila zapisov ni.

Ponovljivi dodatek je v [`us04-demo-data.sql`](us04-demo-data.sql). Namenjen je izključno razvojni bazi in obstoječemu računu `a@a.a`; predpogoj so [razvojni podatki #33](11-razvojni-testni-podatki.md) za referenčni datum `2026-11-12`. Izvede se izrecno prek skrbniške SQL povezave oziroma SQL urejevalnika, nikoli kot migracija ali ob zagonu aplikacije. Stabilni UUID-ji preprečijo podvajanje in ponovitev ne prepiše obstoječih zapisov. Odstranitev podatkov #33 ne odstrani teh štirih dodatnih zapisov; ker uporabljajo isti kraj FERI, ta ostane ohranjen.

Preverjanje razvojne baze 12. 11. 2026: potrjeno razmerje 6 : 1 ob uri 9, brez potrjene zgodovine ob uri 7, brez prekrivanja 13. 11. med 9.00 in 10.00; po ponovitvi SQL dodatka so ostali natanko štirje dodatni zapisi. Ročni prikaz v obstoječi seji `a@a.a` ni bil preverjen zaradi omejenega dostopa do Chrome; brskalniški testi uporabljajo ločeno H2 bazo in svoje testne račune.

`npm run test:e2e:isolated -- context-suggestion.spec.ts`: vseh osem testov uspešnih (namizje in mobilna širina), vključno z dopolnitvijo, popravkom, potrditvijo, zavrnitvijo, prazno zgodovino in potrditvijo prekrivanja.

`mvn.cmd test`: 41 testov uspešnih, brez napak ali preskočenih testov. Uporabljen je bil nameščeni Maven, ker prvi poskus z wrapperjem v omejenem okolju ni uspel.
