# FemtoManage

Za lokalni zagon z Docker Compose:

1. Kopiraj `.env.example` v `.env` in vnesi Supabase povezovalne podatke.
2. V korenu repozitorija zaženi:

   ```sh
   docker compose up --build
   ```

3. Odpri frontend na <http://localhost:5173>. Backend API je na <http://localhost:8080>.

Compose zažene frontend in backend v skupnem omrežju. Frontend posreduje klice `/api` backendu, ki se poveže na Supabase PostgreSQL. Zaustavitev: `docker compose down`. Za ponovni zagon brez ponovne gradnje uporabi `docker compose up`.

V korenskem `.env` obvezno nastavi `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` in `SPRING_DATASOURCE_SSLMODE=require`. Supabase podatkovna baza mora biti dosegljiva iz Docker okolja.

Več o projektu je v mapi [`docs/`](docs/).

Izrecni razvojni ukaz za testne kraje in aktivnosti je opisan v [navodilih za razvojne podatke](docs/11-razvojni-testni-podatki.md).

Prepoznavanje znanega kraja je opisano v [navodilih za US-03](docs/12-prepoznavanje-kraja.md).

Dnevna časovnica in API sta opisana v [navodilih za US-06](docs/13-dnevna-casovnica.md).

Kontekstni predlog aktivnosti je opisan v [navodilih za US-04](docs/14-kontekstni-predlog.md).

Besedilni vnos in strežniška konfiguracija AI sta opisana v [navodilih za US-05](docs/15-besedilni-vnos.md).

Registracija in API sta opisana v [navodilih za avtentikacijo](docs/08-avtentikacija.md).
