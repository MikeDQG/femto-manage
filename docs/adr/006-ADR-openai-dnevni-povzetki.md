# 006 – Priprava osnutkov dnevnih povzetkov z OpenAI prek backenda

## Kontekst in opredelitev problema

Ko uporabnik zabeleži aktivnosti izbranega dne, želimo iz njih pripraviti berljiv osnutek dnevnega besedilnega povzetka. Statična predloga z vstavljenimi aktivnostmi in trajanji bi bila predvidljiva, vendar ponavljajoča in omejena pri povezovanju različnih dnevnih potekov. Potrebujemo prilagodljivo besedilo, ki ga uporabnik pred uporabo pregleda in lahko zavrne.

## Obravnavane možnosti

* Statična predloga poročila, izpolnjena s podatki aktivnosti in trajanji.
* Zunanji jezikovni model OpenAI, klican prek backenda.
* Lasten gostovan jezikovni model.

## Sprejeta odločitev

Izbrana možnost: »OpenAI API prek backenda«, ker je jezikovni model primeren za oblikovanje raznolikega in povezanega besedila iz strukturiranih dnevnih podatkov. Backend pripravi strukturirano poročilo izbranega dne iz potrjenih aktivnosti ter z vnaprej določenimi navodili zahteva osnutek povzetka, ki temelji na priloženih podatkih. Posreduje le podatke, potrebne za povzetek; zasebnih refleksij in natančnih koordinat ne vključuje samodejno.

API-ključ je shranjen na strežniku, klici pa potekajo izključno prek backenda. AI nima neposrednega dostopa do baze. Uporabnik lahko osnutek pregleda, popravi, sprejme ali zavrne. Besedilo ne spremeni potrjenih aktivnosti ali njihovih seštevkov in se ne deli z mentorjem brez ločene izrecne potrditve. Ob napaki zunanje storitve ostaneta dnevnik in pregled dejanskih podatkov dostopna, povzetek pa lahko uporabnik napiše sam.

Ta odločitev obravnava generiranje besedilnega povzetka že zabeleženega dne. Pretvorba uporabnikovega besedila v osnutek aktivnosti je ločena funkcionalnost in ni predmet te odločitve.

### Posledice

* Pozitivno, ker lahko dobimo bolj prilagodljivo in manj ponavljajoče se besedilo kot s statično predlogo.
* Pozitivno, ker ne razvijamo in gostujemo lastnega jezikovnega modela.
* Pozitivno, ker backend nadzoruje poslane podatke in varuje API-ključ.
* Negativno, ker lahko model poda netočno razlago ali doda nepodprto trditev; potreben je uporabnikov pregled, dejanski seštevki pa ostanejo izračunani iz baze.
* Negativno, ker nastanejo stroški klicev, dodatni odzivni čas in odvisnost od dosegljivosti zunanjega ponudnika.
* Negativno, ker se del osebnih podatkov posreduje zunanji storitvi, kar zahteva jasno obveščanje uporabnika in omejevanje poslanih podatkov.
