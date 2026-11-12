# Registracija in prijava

Implementacija opravila [#13](https://github.com/MikeDQG/femto-manage/issues/13) sledi [ADR-004](adr/004-ADR-strezniska-seja.md).

## Registracija

- `GET /api/auth/csrf` vrne žeton in ime glave; odjemalec ju uporabi pri vsaki zahtevi POST skupaj s sejnom piškotkom.
- `POST /api/auth/register` sprejme JSON `email`, `password` in neobvezen `role` (samo `USER`). Vrne 201 in javne podatke računa; registracija uporabnika ne prijavi.
- E-pošta se obreže in pretvori v male črke; enoličnost zagotavlja tudi baza. Podvojitev vrne 409.
- Geslo ima 12–72 znakov, največ 72 bajtov UTF-8, ni prazno in ne vsebuje ničelnih znakov. BCrypt uporablja faktor 12. Geslo se ne obrezuje.
- Napake imajo obliko `{ "message": "…", "fields": { "email": "…" } }`; neveljavni podatki vrnejo 400, manjkajoč CSRF 403, dostop brez prijave 401.

Flyway upravlja shemo `app_user`; Hibernate jo samo preverja. PostgreSQL migracija omogoči RLS brez javnih politik in prekliče dostop javnim vlogam Supabase. Backend uporablja neposredno JDBC povezavo z lastnikom tabele; računi niso dostopni prek Supabase Data API. Ne uporabljamo Supabase Auth.

Testni profil `test` uporablja izolirano H2 bazo v načinu PostgreSQL. Produkcijska PostgreSQL migracija za RLS se izvaja samo na PostgreSQL.

## Preverjanje

Backend: `cd backend-fm` in `./mvnw test` (Windows: `.\mvnw.cmd test`) z Javo 21.
Frontend: `npm run build` in `npm run lint` v `frontend-fm/`.
Po `docker compose up --build` zaženi `npm run test:e2e` v `frontend-fm/`.
Playwright uporablja nameščen Microsoft Edge in preverja namizno ter mobilno širino.
Brskalniški testi ustvarijo račune z naključnimi naslovi `@example.com`; uporabljaj razvojno bazo.
