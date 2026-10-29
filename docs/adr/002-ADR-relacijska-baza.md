# 002 – Relacijska podatkovna baza PostgreSQL

## Kontekst in opredelitev problema

Večina podatkov aplikacije je strukturirana in povezana z uporabnikom ter drugimi entitetami. Potrebujemo jasno lastništvo, povezave in omejitve veljavnosti podatkov ter poizvedbe za dnevne seštevke, nadzorno ploščo in predloge naslednjih aktivnosti oziroma rutin. Nekatere dodatne podrobnosti aktivnosti so polstrukturirane in se lahko razlikujejo med kategorijami.

## Obravnavane možnosti

* Relacijska baza PostgreSQL s polji JSONB za polstrukturirane podrobnosti.
* Dokumentna baza NoSQL.

## Sprejeta odločitev

Izbrana možnost: »PostgreSQL s polji JSONB«, ker relacijski model dobro predstavlja večino podatkov, omogoča povezovanje tabel in agregacije ter uveljavljanje referenčne integritete in omejitev vrednosti. Osnovni podatki in povezave bodo shranjeni v strukturiranih stolpcih, spremenljive dodatne podrobnosti pa po potrebi v JSONB. PostgreSQL izberemo tudi zaradi predhodnega poznavanja, kar skrajša razvoj.

### Posledice

* Pozitivno, ker so lastništvo, povezave in omejitve podatkov jasno določeni ter podprti na ravni baze.
* Pozitivno, ker SQL olajša izračune seštevkov in analize za uporabnikovo nadzorno ploščo.
* Pozitivno, ker JSONB omogoča shranjevanje prilagodljivih podrobnosti brez dodatne dokumentne baze.
* Negativno, ker spremembe strukturiranega modela zahtevajo načrtovanje in migracije sheme.
* Negativno, ker pretirana uporaba JSONB oteži preverjanje podatkov in poizvedovanje; ključne povezave zato ostanejo v relacijskem modelu.
