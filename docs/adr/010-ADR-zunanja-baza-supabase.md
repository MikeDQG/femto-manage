# 010 – Supabase kot zunanja podatkovna baza

Nadomešča obseg Docker Compose odločitve glede lokalnega zagona baze: [008 – Docker in Docker Compose](008-ADR-docker-compose.md).

## Kontekst in opredelitev problema

Projekt za podatke uporablja PostgreSQL, vendar lokalna baza zahteva dodatno storitev in trajno hrambo. Za razvoj želimo uporabljati že gostovano instanco Supabase.

## Sprejeta odločitev

Supabase PostgreSQL je zunanja podatkovna baza. Docker Compose zažene samo frontend in backend; backend se poveže na Supabase prek nastavitev v korenskem `.env`.

### Posledice

* Pozitivno, ker lokalno ni treba nameščati ali vzdrževati baze.
* Negativno, ker zagon aplikacije zahteva omrežno povezavo, Supabase projekt in veljavne poverilnice.
* Negativno, ker se Supabase poverilnice hranijo lokalno in jih ne smemo commitati.
