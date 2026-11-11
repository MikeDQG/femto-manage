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
