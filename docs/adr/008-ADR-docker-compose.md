# 008 – Zabojniki Docker in zagon z Docker Compose

## Kontekst in opredelitev problema

Frontend, backend in podatkovna baza potrebujejo ponovljivo razvojno okolje ter prenosljiv način postavitve. Ročno nameščanje odvisnosti na vsak računalnik ali strežnik poveča možnost razlik med okolji. Želimo osnovo za poznejši CI/CD in objavo na strežniku s podprtim okoljem Docker, brez zahtevne infrastrukture za orkestracijo.

## Obravnavane možnosti

* Ročna namestitev in zagon posameznih delov sistema na gostitelju.
* Slike Docker in Docker Compose za povezovanje ter zagon storitev.
* Orkestracijska platforma, na primer Kubernetes.

## Sprejeta odločitev

Izbrana možnost: »Docker in Docker Compose«, ker omogoča pakiranje komponent skupaj z njihovimi izvajalnimi odvisnostmi in skupen opis storitev, omrežja, konfiguracije ter trajne hrambe. Orodji že poznamo in sta primerni za obseg projekta. Za lokalni razvoj bo Compose zagnal frontend, backend in PostgreSQL; podatki baze bodo shranjeni v trajnem volumnu, skrivnosti pa podane prek konfiguracije okolja in ne vgrajene v slike ali shranjene v Git.

Slike aplikacije bodo osnova za poznejši CI/CD: gradnjo, preverjanje in prenos v ciljno okolje. Compose je primeren tudi za začetno postavitev na enem združljivem strežniku Docker, vendar bo za objavo treba posebej urediti produkcijsko konfiguracijo, HTTPS, varnostne kopije in spremljanje delovanja. Sama uporaba Compose še ne predstavlja izdelanega procesa CI/CD.

### Posledice

* Pozitivno, ker je okolje lažje ponoviti in zagnati z manj ročnega nameščanja odvisnosti.
* Pozitivno, ker lahko iste različice slik prenašamo med združljivimi okolji in jih vključimo v avtomatizirano objavo.
* Pozitivno, ker ne potrebujemo kompleksnosti platforme Kubernetes za začetno postavitev.
* Negativno, ker je treba vzdrževati slike, konfiguracijo in posodobitve odvisnosti ter upravljati skrivnosti in trajne podatke.
* Negativno, ker Docker zahteva dodatne vire in združljivo okolje; prenosljivost ne odpravi razlik med procesorskimi arhitekturami ali konfiguracijami gostiteljev.
* Negativno, ker postavitev Compose na enem strežniku sama ne zagotavlja visoke razpoložljivosti, samodejnega skaliranja ali varnostnih kopij.
