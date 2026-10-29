# 005 – Predlogi aktivnosti s preprostimi pravili na backendu

## Kontekst in opredelitev problema

Na podlagi uporabnikovih preteklih potrjenih zapisov želimo predlagati naslednjo aktivnost in njene podrobnosti ter prepoznavati rutine. Uporabni znaki so kraj, del dneva, pogostost aktivnosti in njihovo zaporedje. Za MVP sta pomembna kratek razvojni čas in preprosto vzdrževanje; količina podatkov pri novih uporabnikih je omejena.

## Obravnavane možnosti

* Preprosta pravila in statistike potrjenih zapisov na backendu.
* Lasten model strojnega učenja za napoved aktivnosti in podrobnosti.

## Sprejeta odločitev

Izbrana možnost: »Preprosta pravila in statistike na backendu«, ker jih lahko hitro razvijemo in pojasnimo. Predlogi bodo temeljili na pogostih aktivnostih glede na kraj in čas ter pogostih prehodih med aktivnostmi: če aktivnosti A pogosto sledi B, lahko po A predlagamo B. Za to ne potrebujemo lastnega naučenega modela ali celovitega Markovskega modela. Podrobnosti predlagamo iz primerljivih potrjenih zapisov, kadar je zanje dovolj podatkov. Uporabnik predlog pregleda, popravi, potrdi ali zavrne; osnutki in zavrnjeni predlogi ne štejejo kot potrjena zgodovina.

Lasten algoritem strojnega učenja je predvidena možnost po MVP, ko bo na voljo več podatkov in časa za razvoj. Njegovo uvedbo bomo dokumentirali z novim ADR, ki se bo skliceval na tega.

### Posledice

* Pozitivno, ker je razvoj hitrejši in ne zahteva učenja, gostovanja ter spremljanja lastnega modela.
* Pozitivno, ker je mogoče razložiti, na katerih preteklih zapisih temelji predlog.
* Negativno, ker preprosta pravila težje prepoznajo skrite in kompleksne vzorce, zato je lahko točnost nižja kot pri ustrezno naučenem modelu.
* Negativno, ker je pri kratki ali spremenljivi zgodovini uporabnika manj podlage za uporabne predloge.
* Negativno, ker dodatna ročna pravila sčasoma povečajo zahtevnost vzdrževanja.
