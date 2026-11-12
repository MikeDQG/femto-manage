# Kontekstni predlog (US-04)

Opravili [#19](https://github.com/MikeDQG/femto-manage/issues/19) in [#20](https://github.com/MikeDQG/femto-manage/issues/20) uporabljata pravila iz [ADR 005](adr/005-ADR-pravila-za-predloge.md).

`POST /api/activities/context-suggestion` sprejme `placeId`, `startAt` (časovni trenutek) in `zone` (IANA časovni pas). Zahteva prijavo in CSRF. Lastništvo ter neizbrisanost kraja preveri strežnik. V zgodovini lastnih potrjenih aktivnosti na tem kraju primerja lokalno uro začetka (0–23) v zahtevanem časovnem pasu, tudi za zgodovinske zapise čez poletni čas. Med aktivnimi kategorijami predlaga le enoznačno najpogostejšo. Izenačenje in prazna zgodovina vrneta razlog ter `draft: null`.

Osnutek vsebuje izbrani začetek, kraj, kategorijo, vir `SUGGESTION` in opozorili za manjkajoča naziv ter konec. Ne ugiba trajanja in ne piše v bazo. Uporabnik ga v obrazcu popravi, dopolni, potrdi ali zavrne. Potrditev uporablja obstoječi `/api/activities`, vključno z validacijo in izrecno potrditvijo prekrivanja. Zavrnitev osnutek zavrže brez spremembe seštevkov.

Do vnosa se pride prek Koledar → Dodaj aktivnost. Ročni vnos ostane privzet; zavihek Kontekstni predlog omogoča izbiro lastnega ali prepoznanega kraja ter časa. Napake in odsotnost zgodovine ohranijo ročni vnos.

Preverjanje: backend testi `ContextSuggestionTests`, celotni Maven testi, frontend build/lint ter `context-suggestion.spec.ts` na telefonu in namizju.
