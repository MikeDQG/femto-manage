# Upravljanje lokacij

[#11](https://github.com/MikeDQG/femto-manage/issues/11) razširja obstoječi model Place. API `/api/places` podpira GET in POST, `/api/places/{id}` pa GET, PUT in DELETE. Pisanje zahteva sejo in CSRF. Lastnika določa strežnik; tuj ali izbrisani kraj vrne 404. Napake validacije vrnejo 400 z razlogi v `fields`.

Migracija V5 dodaja oznako `deleted`. Izbris ohrani kraj in povezave v preteklih aktivnostih, izključi pa ga iz seznama in novih povezav. Upravljanje lastnih krajev ne spreminja lastništva. Koordinate so v stopinjah, radij je pozitivno celo število metrov.

Zaslon iz [#12](https://github.com/MikeDQG/femto-manage/issues/12) je v `components/places/`: seznam in potrditev brisanja upravlja `PlacesScreen`, vnos in prevzem koordinat pa `PlaceForm`. Geolokacija se sproži samo na uporabnikovo zahtevo; zavrnitev ali napaka ohrani ročni vnos. Spremembe osvežijo tudi izbor krajev za aktivnosti.

Preverjanje: backend `mvn test` (oziroma Maven wrapper), frontend `npm run build`, `npm run lint` in `npm run test:e2e -- places.spec.ts`. Brskalniški testi ustvarjajo razvojne račune z naključnimi naslovi.
