# FemtoManage

FemtoManage je načrtovana spletna aplikacija za osebni dnevnik dejansko izvedenih aktivnosti in spremljanje porabe časa. Namenjena je predvsem študentom in posameznikom, ki želijo bolje razumeti svoje dnevne navade ter spremljati doseganje časovnih ciljev.

## Glavne funkcionalnosti

- Beleženje aktivnosti s časom začetka in konca, kategorijo ter neobveznim opisom in krajem.
- Dnevna časovnica, seštevki časa po kategorijah in zasebna refleksija dneva.
- Upravljanje lastnih kategorij in dnevnih časovnih ciljev ter prikaz njihovega doseganja.
- Shranjevanje krajev in predlogi aktivnosti glede na lokacijo, uro in pretekle zapise.
- Pretvorba besedilnega vnosa v osnutek aktivnosti s pomočjo umetne inteligence.
- Izrecno deljenje dnevnih povzetkov z mentorjem in možnost preklica dostopa.

Uporabnik vsak predlog pred shranjevanjem potrdi. Lokacija se uporablja z njegovim dovoljenjem; mentor vidi le deljene povzetke brez lokacij, podrobnih opisov in zasebnih refleksij.

## Predvidene tehnologije

- **Uporabniški vmesnik:** React, Vite in PWA.
- **Strežniški del:** Java, Spring Boot.
- **Podatkovna baza:** PostgreSQL in Flyway.
- **Razvojno okolje:** Docker Compose, Maven in npm.
- **Zunanji storitvi:** brskalniški Geolocation API in OpenAI API za pripravo osnutkov.

## Stanje projekta

Repozitorij trenutno vsebuje zasnovo, specifikacije in žične modele uporabniškega vmesnika. Aplikacija in konfiguracija za zagon še nista implementirani. Predviden je lokalni zagon z Docker Compose.

Poznejše nadgradnje vključujejo povezavo z Google Calendar, glasovni vnos, predloge rutin in uvoz dnevnika iz CSV.

## Dokumentacija

- [Zasnova rešitve](docs/01-zasnova-resitve.md)
- [Funkcionalnosti in obseg](docs/02-funkcionalnosti-in-obseg.md)
- [Podatkovni model](docs/03-podatkovni-model.md)
- [Arhitektura in tehnološki sklad](docs/04-arhitektura-in-tehnoloski-sklad.md)
- [Uporabniške zgodbe](docs/05-uporabniske-zgodbe.md)
- [Žični modeli uporabniškega vmesnika](docs/06-wireframes.md)
