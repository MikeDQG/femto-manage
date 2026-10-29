# 003 – Enoten uporabniški vmesnik React PWA

## Kontekst in opredelitev problema

Aplikacija mora biti uporabna na telefonu in računalniku, razvojni čas pa je omejen. Ločen razvoj nativnih aplikacij za različne platforme bi povečal obseg dela in vzdrževanja. Za osnovne funkcionalnosti zadošča odziven spletni vmesnik z dostopom do lokacije ob uporabnikovem dovoljenju.

## Obravnavane možnosti

* Odzivna spletna aplikacija React PWA.
* Ločene nativne aplikacije za mobilne platforme in spletni vmesnik za računalnik.
* Večplatformska mobilna aplikacija z dodatnim spletnim vmesnikom.

## Sprejeta odločitev

Izbrana možnost: »React PWA«, ker omogoča eno kodno osnovo za telefon in računalnik ter hitrejši razvoj in objavo. Vmesnik bo izdelan z Reactom in orodjem Vite, s podporo PWA za namestitev na podprtih napravah in osnovno predpomnjenje vmesnika. PWA se izvaja znotraj zmožnosti brskalnika; ta odločitev ne vključuje urejanja dnevnika brez povezave.

### Posledice

* Pozitivno, ker razvijamo in vzdržujemo eno kodno osnovo za različne naprave.
* Pozitivno, ker uporabnik dostopa prek spletnega naslova, posodobitve pa ne zahtevajo ločenega izdajanja v trgovinah z aplikacijami.
* Negativno, ker je dostop do funkcij naprave in izvajanja v ozadju omejen z brskalnikom ter operacijskim sistemom.
* Negativno, ker se podpora namestitvi in posameznim spletnim API-jem med napravami razlikuje, zato je treba preveriti delovanje na ciljnih brskalnikih.
