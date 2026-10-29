# Arhitekturne odločitve

Vsaka arhitekturna odločitev ima svojo datoteko, pripravljeno po [predlogi ADR](adr-template.md). Identifikatorji si sledijo kot `001`, `002`, …, ime datoteke pa ima obliko `NNN-ADR-ime-odlocitve.md`.

## Pravilo sprememb

Ko se sprejeta odločitev spremeni ali dopolni, obstoječega ADR ne spreminjamo. Ustvarimo nov ADR z naslednjo zaporedno številko. V njem pred razdelkom »Kontekst in opredelitev problema« navedemo povezavo `Nadomešča: [oznaka in naslov](ime-datoteke.md)` ali `Dopolnjuje: [oznaka in naslov](ime-datoteke.md)` in pojasnimo obseg spremembe. Tako ohranimo zgodovino odločitev; številke označujejo zaporedje zapisov, ne različic iste datoteke. Kazalo posodobimo s povezavo na novi zapis in navedbo, kateri ADR nadomešča ali dopolnjuje.

## Kazalo

| Oznaka | Odločitev |
|---|---|
| 001 | [Enoten monolitni backend](001-ADR-en-backend.md) |
| 002 | [Relacijska podatkovna baza PostgreSQL](002-ADR-relacijska-baza.md) |
| 003 | [Enoten uporabniški vmesnik React PWA](003-ADR-react-pwa.md) |
| 004 | [Avtentikacija s strežniško sejo](004-ADR-strezniska-seja.md) |
| 005 | [Predlogi aktivnosti s preprostimi pravili](005-ADR-pravila-za-predloge.md) |
| 006 | [Osnutki dnevnih povzetkov z OpenAI](006-ADR-openai-dnevni-povzetki.md) |
| 007 | [Deljenje posnetkov dnevnih povzetkov](007-ADR-posnetki-deljenih-povzetkov.md) |
| 008 | [Docker in Docker Compose](008-ADR-docker-compose.md) |

Odločitev 006 obravnava osnutke dnevnih besedilnih povzetkov. Obstoječe specifikacije besedilnega vnosa aktivnosti opisujejo drugo uporabo AI in same po sebi ne določajo obsega te odločitve.
