# 001 – Enoten monolitni backend

## Kontekst in opredelitev problema

FemtoManage razvija en razvijalec, zato sta pomembna hiter razvoj in majhen obseg infrastrukture. Backend upravlja podatke posameznega uporabnika: aktivnosti, kategorije, kraje, cilje in povzetke. Ti podatki so tesno povezani, sistem pa nima zahtev po obdelavi v realnem času ali izjemno nizkih odzivnih časih. Potrebujemo predvsem zanesljivo obdelavo in doslednost podatkov.

## Obravnavane možnosti

* Enoten monolitni backend z notranjo razdelitvijo odgovornosti.
* Mikrostoritvena arhitektura z ločenimi storitvami za posamezna področja.

## Sprejeta odločitev

Izbrana možnost: »Enoten monolitni backend«, ker omogoča razvoj in objavo ene strežniške aplikacije z manj infrastrukture. Poslovna logika bo razdeljena po odgovornostih znotraj aplikacije Spring Boot. Tesno povezane podatke lahko obdelujemo v skupnih transakcijah, brez omrežne komunikacije med ločenimi domenskimi storitvami.

### Posledice

* Pozitivno, ker so razvoj, lokalni zagon, odpravljanje napak in objava preprostejši za enega razvijalca.
* Pozitivno, ker je lažje zagotavljati doslednost tesno povezanih podatkov.
* Negativno, ker sprememba posameznega dela praviloma zahteva ponovno objavo celotnega backenda.
* Negativno, ker posameznih funkcionalnosti ne moremo neodvisno skalirati; napaka v skupnem procesu lahko prizadene več funkcionalnosti.
* Negativno, ker lahko brez jasne notranje razdelitve odgovornosti nastane preveč povezana in težko vzdrževana koda.
