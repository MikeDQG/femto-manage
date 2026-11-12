# Deljenje in preklic povzetkov (US-07)

Opravili [#27](https://github.com/MikeDQG/femto-manage/issues/27) in [#28](https://github.com/MikeDQG/femto-manage/issues/28) sledita [ADR 007](adr/007-ADR-posnetki-deljenih-povzetkov.md). Dodelitev mentorja omogoča [#44](https://github.com/MikeDQG/femto-manage/issues/44), opisan v [avtentikaciji](08-avtentikacija.md). Skrbniški zaslon #48 in mentorski seznam US-13 ostajata ločeni nalogi; ta sklop zagotavlja lastnikov zaslon in zaščiteno neposredno branje posnetka.

## Potek in zasebnost

Koledar uporablja izbrani dan tudi za deljenje. Uporabnik izbere obstoječega mentorja in po želji vnese ločen komentar. Predogled pokaže točno datum, seštevke in komentar. Zasebna refleksija, lokacije, posamezni časovni bloki, opisi in izvorna besedila niso v predogledu ali posnetku. Komentar se nikoli ne izpolni iz refleksije.

Predogled živi v lastnikovi strežniški seji največ 15 minut in ne ustvari vrstice v bazi. V eni seji velja zadnji predogled; nov predogled (tudi v drugem zavihku) nadomesti prejšnjega. Potrditev zahteva njegov naključni ID ter `confirmed: true`. Strežnik ponovno preveri mentorja in različico dnevnika. Različica vključuje potrjene aktivnosti in seštevke, zato zazna tudi spremembo brez drugačnega skupnega trajanja. Ob spremembi UI pokaže nov predogled in zahteva novo potrditev. Ponovitev uspešno uporabljene potrditve ne ustvari drugega posnetka.

Potrditev atomarno ustvari `daily_summary` in `access_grant` (V8); PostgreSQL migracija V9 omogoči RLS in odvzame pravice vlogama Data API. Posnetek je nespremenljiv: nove aktivnosti ali refleksije ga ne spreminjajo, tudi za isti dan je naslednje deljenje nov posnetek. Preklic spremeni samo `revoked_at`. Lastnikove aktivnosti, refleksija in posnetek ostanejo shranjeni.

## API

Vse poti zahtevajo sejo; pisanje tudi CSRF. Odgovori deljenja imajo `Cache-Control: no-store`.

| Pot | Namen |
|---|---|
| `GET /api/summaries/mentors` | Obstoječi mentorji (ID in e-pošta) |
| `POST /api/summaries/preview` | `date`, IANA `zone`, `mentorId`, izbirni `comment` do 2000 znakov; vrne `previewId`, mentorja in dovoljeno vsebino |
| `POST /api/summaries/shares` | `previewId`, `confirmed: true`; ustvari posnetek in dovoljenje, 409 za spremenjen ali potekel predogled |
| `GET /api/summaries/shares` | Samo lastnikove delitve z datumom, mentorjem in stanjem |
| `DELETE /api/summaries/shares/{grantId}` | Lastnikov preklic, varen ob ponovitvi |
| `GET /api/summaries/{summaryId}` | Lastnikovo ali trenutno dovoljeno mentorsko branje; samo `date`, `totals`, `comment` |

Mentorjevo trenutno vlogo in dovoljenje preveri vsako branje, tudi z neposrednim URL-jem. Po preklicu naslednja zahteva vrne 404. Drugi uporabnik, drug mentor in skrbnik nimajo posebnega dostopa do tujega posnetka. API za dodelitev vloge ne ustvari nobenega dovoljenja.

## Preverjanje

Backend: `./mvnw test` oziroma `.\mvnw.cmd test`. `SummarySharingTests` preverijo predogled brez zapisovanja, izrecno potrditev, zasebnost, zastarelost, neponovljivost potrditve, nespremenljivost, več različic in preklic.

Frontend: `npm run build`, `npm run lint` in `npm run test:e2e:isolated -- sharing.spec.ts --workers=4`. Izolirana H2 aplikacija začne samo s testnim skrbnikom; testi mentorje registrirajo in jim vlogo dodelijo prek dejanskega #44 API-ja. Nato celoten potek lastnik–mentor preverijo prek pravega UI/API, brez spreminjanja razvojne baze. Samo scenarij praznega seznama mentorjev nadzoruje odgovor seznamov, da ostane neodvisen od vzporednih testov.

Za celotno regresijo uporabi `npm run test:e2e:isolated -- --workers=4`. Zunanji OpenAI ostane izključen; njegovo pogodbo preverjajo strežniški testi adapterja.
