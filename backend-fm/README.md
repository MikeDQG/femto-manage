# Backend

Java 21, Spring Boot, Spring Data JPA in Maven. Skupni zagon je opisan v [korenskem README](../README.md).

## Java paketi

Koren je `si.um.feri.riris.backendfm`. `BackendFmApplication` ostaja v korenu, da Spring najde vse podpakete.

| Paket | Odgovornost |
|---|---|
| `api/auth`, `api/activity`, `api/place`, `api/reflection`, `api/summary`, `api/admin` | HTTP kontrolerji, vhodni zahtevki in odgovori ustreznega sklopa |
| `api/error` | Skupna oblika napak in obravnava izjem API-ja |
| `engine/auth`, `engine/place`, `engine/activity`, `engine/reflection`, `engine/summary`, `engine/admin` | Storitve za poslovne operacije |
| `persistence/user`, `persistence/activity`, `persistence/place`, `persistence/category`, `persistence/reflection`, `persistence/summary` | JPA entitete in pripadajoči repozitoriji |
| `config` | Spring konfiguracija in varnost |
| `bootstrap` | Opravila ob zagonu, na primer začetni skrbnik |

Nove razrede dodaj po odgovornosti in nato po entiteti oziroma funkcionalnem sklopu. Skupne napake sodijo v `api/error`; posamezne DTO-je ohrani ob API-ju, ki jih uporablja. Praznih plasti ne ustvarjaj vnaprej. Preurejanje paketov samo po sebi ne spreminja HTTP pogodb, imen tabel ali migracij.

Testi v `src/test/java` sledijo paketom preverjanih vstopnih točk oziroma komponent. Integracijski testi API-ja so v `api/<sklop>`, testi začetnega skrbnika v `bootstrap`. Testni profil uporablja H2, produkcijsko shemo pa upravlja Flyway.

## Preverjanje

Iz te mape zaženi `./mvnw test` oziroma v PowerShellu `.\mvnw.cmd test`. Če wrapper ni dosegljiv, uporabi nameščeni Maven: `mvn test`. Ob premikanju paketov uporabi čisto izhodno mapo, da stari `.class` ne ostanejo v Springovem pregledu komponent.

`support/BrowserTestApplication` v testnih virih zaganja izolirano H2 aplikacijo za celotne brskalniške teste. Uporablja se samo prek `npm run test:e2e:isolated` v frontendu in ni v produkcijskem JAR. Glej [preverjanje deljenja](../docs/16-deljenje-povzetkov.md).
