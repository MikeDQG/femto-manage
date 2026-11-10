# 009 – Struktura repozitorija za aplikacijske komponente

## Kontekst in opredelitev problema

Projekt vsebuje frontend in backend, Docker Compose pa bo povezoval tudi podatkovno bazo. Struktura naj omogoča preprost skupen zagon in jasno ločitev komponent.

## Obravnavane možnosti

* Mapi `backend/` in `frontend/` v korenu repozitorija ter Compose v korenu.
* Skupna mapa, na primer `razvoj/`, z Compose in podmapama za projekte.

## Sprejeta odločitev

Izbrana je prva možnost. Compose konfiguracija bo v korenu repozitorija, aplikacijski projekti pa v ločenih mapah `backend/` in `frontend/`.

### Posledice

* Pozitivno, ker je skupni zagon preprost, poti do projektov pa jasne.
* Pozitivno, ker so razvojna in produkcijska okolja lahko določena z ločenimi Compose datotekami v korenu.
* Negativno, ker je koren namenjen tudi konfiguraciji in orodjem, ne le dokumentaciji.
