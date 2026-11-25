# Pregled projekta FemtoManage

## Ocena prvih petih uporabniških zgodb

| Zgodba | Ocena | Ugotovitve |
|---|---|---|
| **US-01 – Ročno beleženje in urejanje aktivnosti** | Večinoma pokrita | Obrazec omogoča vnos naziva, začetka, konca in kategorije ter neobveznega kraja in opisa. Frontend ob validacijski napaki prikaže označena polja, ob prekrivanju pa naziv in čas prekrivajoče aktivnosti ter zahteva izrecno nadaljevanje. Backend preverja lastništvo, interval, kategorijo in kraj; urejanje posodobi obstoječi zapis. Časovnica ponuja podrobnosti in urejanje. V kodi so testi za vnos in urejanje. |
| **US-02 – Upravljanje lokacij** | Večinoma pokrita | Zaslon omogoča pregled, dodajanje, urejanje in brisanje lastnih krajev. Obrazec preverja ime, geografske koordinate in pozitiven radij; lokacijo je mogoče vnesti ročno ali jo pridobiti z dovoljenjem brskalnika. Backend izpostavlja CRUD API in preverja lastništvo. Brisanje ohrani pretekle aktivnosti. Obstajata frontend in backend testna datoteka za lokacije. |
| **US-03 – Prepoznavanje kraja** | Večinoma pokrita | Frontend pridobi lokacijo samo prek brskalniškega dovoljenja, pokaže napako ob zavrnitvi ali neuspešni meritvi ter pošlje koordinate backendu. Backend primerja koordinate z uporabnikovimi kraji. Vmesnik prepoznavo omogoča ob odprtju/zahtevi, ročna izbira ostaja na voljo. Obstajajo namenski frontend in backend testi. |
| **US-04 – Kontekstni predlog aktivnosti** | Večinoma pokrita | Uporabnik izbere kraj in čas začetka; backend vrne predlog iz potrjene zgodovine, ki ga je mogoče dopolniti v obrazcu pred potrditvijo. Brez predloga je na voljo ročni vnos. Prisotni so testi za kontekstni predlog. Koda podpira osnovni tok, vendar pregled brez izvajanja testov ne potrjuje vsakega merila, na primer izključevanja vseh osnutkov in zavrnjenih predlogov iz statistike. |
| **US-05 – Besedilo v osnutek** | Večinoma pokrita | Uporabnik vnese besedilo, backend prek integracije OpenAI pripravi osnutek, obrazec ga prikaže za pregled in dopolnitev, pred shranjevanjem pa veljajo običajna preverjanja obveznih podatkov in prekrivanj. Vmesnik prikaže napako ter omogoča prehod na ročni vnos. Obstajajo testi storitve za OpenAI in brskalniški test besedilnega vnosa. Zunanja storitev zahteva pravilno konfiguracijo; natančno vedenje ob odsotnosti ključa ali napaki je smiselno potrditi lokalno. |

