# Prepoznavanje kraja (US-03)

Opravili [#16](https://github.com/MikeDQG/femto-manage/issues/16) in [#17](https://github.com/MikeDQG/femto-manage/issues/17) uporabljata shranjene lastne kraje iz [upravljanja lokacij](10-lokacije.md).

Prijavljen uporabnik lahko pokliče `GET /api/places/recognition?latitude=46.55&longitude=15.64`. Odgovor vsebuje `recognized` in `candidates`; vsak kandidat ima `id`, `name`, `distanceM` in `radiusM`. API uporablja haversinovo razdaljo v metrih ter vključi kraj na meji radija. Vrne samo najbližjega ustreznega kandidata oziroma vse kandidate z razdaljo znotraj 1 mm od najbližjega, da zaokrožitvena napaka ne skrije izenačenja. Če ni ujemanja, vrne `recognized: false` in prazen seznam. Upošteva samo neizbrisane kraje iz seje; meritev se ne shrani.

Po prijavi domači zaslon izvede eno meritev samo, če je dovoljenje za lokacijo že odobreno. Sicer je na voljo gumb »Preveri trenutni kraj«. Natančnost meritve in rezultat sta prikazana na domačem zaslonu in v obrazcu nove aktivnosti. V obrazcu uporabnik predlagani kraj izrecno izbere; ročni izbor ali vnos brez kraja ostaneta mogoča tudi ob napaki. Brskalnik ne izvaja neprekinjenega sledenja.
