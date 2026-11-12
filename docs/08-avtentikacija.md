# Registracija in prijava

Implementacija opravila [#13](https://github.com/MikeDQG/femto-manage/issues/13) sledi [ADR-004](adr/004-ADR-strezniska-seja.md).

## Registracija

- `GET /api/auth/csrf` vrne žeton in ime glave; odjemalec ju uporabi pri vsaki zahtevi POST skupaj s sejnim piškotkom.
- `POST /api/auth/register` sprejme JSON `email`, `password` in neobvezen `role` (samo `USER`). Vrne 201 in javne podatke računa; registracija uporabnika ne prijavi.
- E-pošta se obreže in pretvori v male črke; enoličnost zagotavlja tudi baza. Podvojitev vrne 409.
- Geslo ima 12–72 znakov, največ 72 bajtov UTF-8, ni prazno in ne vsebuje ničelnih znakov. BCrypt uporablja faktor 12. Geslo se ne obrezuje.
- Napake imajo obliko `{ "message": "…", "fields": { "email": "…" } }`; neveljavni podatki vrnejo 400, manjkajoč CSRF 403, dostop brez prijave 401.

## Prijava in seja

- `POST /api/auth/login` sprejme `email` in `password`; uspeh vrne javne podatke uporabnika in vzpostavi Spring Security sejo. Napačna e-pošta in napačno geslo vrneta enako napako 401.
- Prijava zamenja identifikator seje in razveljavi stari CSRF žeton. Pred naslednjim POST odjemalec pridobi nov žeton prek `/csrf`.
- `GET /api/auth/me` vrne samo račun iz trenutne seje; odjemalec ne izbira uporabnika z ID-jem. Vse druge poti zahtevajo prijavo. Domenski API-ji za aktivnosti, kraje, refleksije in deljenje dodatno preverjajo lastništvo oziroma dovoljenje.
- `POST /api/auth/logout` s CSRF žetonom razveljavi sejo, odstrani piškotek `JSESSIONID` in vrne 204. Menjava računa ponastavi lokalno stanje prototipa.
- Ob odprtju frontend preveri `/me`. Seja poteče po 30 minutah neaktivnosti; piškotek je HttpOnly, SameSite=Lax, ob HTTPS nastavi `SESSION_COOKIE_SECURE=true`. Gesel in žetonov ne shranjujemo v localStorage.
- Seje so v pomnilniku backenda; trajna prijava čez ponovni zagon in trajni piškotek sta ločeno opravilo [#14](https://github.com/MikeDQG/femto-manage/issues/14).

## Začetni skrbnik

V korenskem `.env` neobvezno nastavi oba `BOOTSTRAP_ADMIN_EMAIL` in `BOOTSTRAP_ADMIN_PASSWORD` po isti politiki gesel kot pri registraciji. Compose ju posreduje backendu. Ob prvem zagonu nastane ADMIN; poznejši zagoni ne spreminjajo gesla ali vloge obstoječega skrbnika. Če naslov pripada navadnemu uporabniku ali so nastavitve nepopolne, se zagon ustavi z opisom napake. Brez obeh nastavitev začetni skrbnik ne nastane.

Flyway upravlja shemo `app_user`; Hibernate jo samo preverja. PostgreSQL migracija omogoči RLS brez javnih politik in prekliče dostop javnim vlogam Supabase. Backend uporablja neposredno JDBC povezavo z lastnikom tabele; računi niso dostopni prek Supabase Data API. Ne uporabljamo Supabase Auth.

Enako je zaščitena tehnična tabela `flyway_schema_history`; Flyway callback `afterMigrate` jo zaščiti po sprostitvi migracijskih zaklepov ob vsakem zagonu. Informativno opozorilo Supabase [RLS Enabled No Policy](https://supabase.com/docs/guides/database/database-linter?lint=0008_rls_enabled_no_policy) je za ti tabeli pričakovano: dostop prek javnega Data API je namerno zavrnjen, JDBC lastnik pa lahko izvaja migracije in delo z računi.

Testni profil `test` uporablja izolirano H2 bazo v načinu PostgreSQL. Produkcijska PostgreSQL migracija za RLS se izvaja samo na PostgreSQL.

## Skrbniška dodelitev mentorja (#44)

[#44](https://github.com/MikeDQG/femto-manage/issues/44) dodaja `GET /api/admin/accounts?email=celoten-naslov` (rezultat `found` ter račun z naslovom in vlogo) in `POST /api/admin/accounts/mentor` (`email`, `confirmed: true`). Dostop je dovoljen samo prijavljenemu skrbniku; pisanje zahteva CSRF. Naslov se normalizira kot pri registraciji, iskanje pa ni delno. Neobstoječ račun ob iskanju vrne `found: false`, pri dodelitvi 404.

Potrditev spremeni samo USER v MENTOR. Ponovitev vrne `changed: false`, ne ustvari novega računa in ne spremeni skrbniške vloge. Zaklep računa serijsko obravnava sočasne dodelitve. Ob naslednji prijavi seja prevzame vlogo MENTOR. Odgovori vsebujejo le javne podatke računa; dodelitev sama ne ustvari dovoljenja za noben dnevnik ali povzetek. Skrbniški zaslon je ločeno opravilo #48; ta API ga ne zahteva za delovanje.

Testi `MentorAssignmentTests` pokrivajo iskanje, izrecno potrditev, ponovitev, novo prijavo in pravice.

Za ponovljiv HTTP preizkus uporabi `npm run test:e2e:isolated -- mentor-assignment.spec.ts` v `frontend-fm/`. Zaganjalnik potrebuje Javo, Maven wrapper in nameščen Edge; pripravi H2 ter izključno testnega skrbnika v testnem razredu `BrowserTestApplication`. Frontend in API tečeta na prostih vratih 5174/8081, procesi pa se po testu ustavijo. Ne bere korenske `.env`, ne spreminja razvojnih računov v Supabase in ne vključuje testnih računov v produkcijski JAR.

## Preverjanje

Backend: `cd backend-fm` in `./mvnw test` (Windows: `.\mvnw.cmd test`) z Javo 21.
Frontend: `npm run build` in `npm run lint` v `frontend-fm/`.
Po `docker compose up --build` lahko proti razvojni bazi zaženeš izbrane teste z `npm run test:e2e -- ime.spec.ts` v `frontend-fm/`. Celotna regresija z dodeljevanjem mentorjev uporablja `npm run test:e2e:isolated -- --workers=4`, ki pripravi lastno H2 bazo in testnega skrbnika.
Playwright uporablja nameščen Microsoft Edge in preverja namizno ter mobilno širino.
Brskalniški testi ustvarijo račune z naključnimi naslovi `@example.com`; uporabljaj razvojno bazo.
