# Dnevna časovnica (US-06)

Opravili [#24](https://github.com/MikeDQG/femto-manage/issues/24) in [#25](https://github.com/MikeDQG/femto-manage/issues/25) dodajata dnevni pregled potrjenih aktivnosti.

`GET /api/journal/day?date=YYYY-MM-DD&zone=Europe/Ljubljana` zahteva prijavo. Odgovor vsebuje `date`, `zone`, `activities` in `totals`. Vsaka aktivnost ima izvorni interval `startAt`/`endAt`, za izbrani lokalni dan odrezani interval `segmentStartAt`/`segmentEndAt`, trajanje segmenta v minutah ter naziv kategorije in morebitnega kraja. Ime izbrisanega kraja ostane vidno ob zgodovinski aktivnosti. `totals` vsebuje vse aktivne kategorije, tudi tiste z 0 minutami. Osnutki in tuji zapisi niso vključeni.

Seštevki uporabljajo dejansko trajanje v sekundah znotraj meja lokalnega dne. Čas prehoda na poletni ali zimski čas zato ni obravnavan kot fiksnih 24 ur. Sekunde se najprej seštejejo po kategoriji, šele nato za prikaz pretvorijo v cele minute. Potrjena prekrivanja se seštejejo po posameznih aktivnostih, zato lahko čas prispeva večkrat.

Zaslon Koledar omogoča izbiro dneva in prikaže bloke na časovni mreži ter seštevke po kategorijah. Datum ostane izbran tudi po vnosu nove aktivnosti. Prazni dnevi in napake nalaganja imajo ločen prikaz.
