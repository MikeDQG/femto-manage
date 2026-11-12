# Navodila za delo v FemtoManage

## Samostojna izvedba GitHub opravil

- Ko uporabnik naroči izvedbo opravil z njihovimi številkami, povezavami ali enoličnimi naslovi, sam pridobi njihove podatke iz GitHuba. Repozitorij določi iz Git remote; za ta projekt je `MikeDQG/femto-manage`.
- Uporabi povezani GitHub vtičnik; če potrebna zmožnost ni na voljo, uporabi prijavljeni GitHub CLI, kadar je na voljo. Preberi celoten opis, komentarje, podopravila, merila sprejemljivosti in povezane odvisnosti. Odpri tudi relevantne povezave na projektno dokumentacijo.
- Pred implementacijo primerjaj zahteve z dejanskim stanjem kode. Pri več opravilih določi vrstni red glede na odvisnosti in jih združi po featurejih; za vsak samostojen feature uporabi svojo vejo. Že implementiranih odvisnosti ne implementiraj ponovno.
- Če zahtevana odvisnost še ni implementirana in ni vključena v naročilo, preveri, ali je nalogo mogoče dokončati v dogovorjenem obsegu. Če ni mogoče, pojasni konkretno manjkajočo odvisnost in pridobi odločitev o razširitvi obsega.
- Preveri izhodiščno vejo in jo uskladi z oddaljenim repozitorijem brez prepisovanja uporabnikovih sprememb. Novo feature vejo ustvari iz ustrezne posodobljene osnove; kadar uporabnik določi osnovo, upoštevaj njegovo izbiro.
- Naročilo za implementacijo vključuje ustvarjanje potrebne lokalne feature veje in sprotnih lokalnih commitov po teh navodilih. V okviru dovoljenj okolja nadaljuj od pregleda zahtev do implementacije, preverjanja, dokumentacije in commitov, brez dodatnega potrjevanja rutinskih korakov.
- Naloga je dokončana, ko so izpolnjena njena merila sprejemljivosti, povezane plasti delujejo skupaj, ustrezna preverjanja uspejo in so spremembe commitane. Če ostane ovira ali nepreverjeno merilo, to jasno označi in ne poročaj o popolnem zaključku.
- Ob predaji navedi opravljene issue, imena vej, oznake commitov, rezultate preverjanj in morebitne odprte točke. Push, odpiranje pull requesta, merge in zapiranje issue izvedi, kadar jih uporabnik naroči.

## Razumevanje naloge in odločitve

- Pred implementacijo preberi korenski `README.md`, relevantne dokumente v `docs/`, kazalo `docs/adr/README.md` in ADR-je, ki se nanašajo na nalogo. Preglej tudi navodila in obstoječo implementacijo v delu projekta, ki ga spreminjaš.
- Zahteve in nejasnosti najprej razreši z obstoječo dokumentacijo, uporabniškimi zgodbami in sprejetimi arhitekturnimi odločitvami.
- Če dokumentacija odgovora ne vsebuje, izberi smiselno rešitev, skladno s projektom, in pomembno predpostavko ali odločitev kratko zabeleži v ustreznem dokumentu Markdown. Vprašaj, kadar nejasnost bistveno spreminja zahtevano vedenje ali je ni mogoče odgovorno razrešiti iz konteksta.
- Arhitekturne odločitve zapiši kot samostojne ADR-je v `docs/adr/` po `adr-template.md`. Uporabi naslednjo prosto zaporedno številko in obliko `NNN-ADR-ime-odlocitve.md` ter posodobi kazalo.
- Sprejetih ADR-jev ne prepisuj. Spremembo ali dopolnitev opiši v novem ADR-ju s povezavo `Nadomešča` ali `Dopolnjuje`, kot določa kazalo ADR.
- Manjše lokalne implementacijske izbire ne potrebujejo svojega ADR-ja. Dokumentacijo o posebnostih, omejitvah, konfiguraciji in uporabi posodobi tam, kjer jo bo naslednji razvijalec pričakoval.

## Obseg in slog implementacije

- Nalogo dokončaj skozi vse potrebne plasti aplikacije. Po potrebi prilagodi obstoječe razrede, atribute, prenos podatkov, API, komponente in postavitev, da funkcionalnost deluje kot celota.
- Povezane prilagoditve opravi samostojno; obseg naj določa zahtevana funkcionalnost. Nepovezane prenove ne vključuj v isto nalogo.
- Kodo razdeli na logične sklope z jasnimi odgovornostmi in razumljivimi imeni. Piši pregledno, z običajnim oblikovanjem in opisnimi imeni spremenljivk.
- Abstrakcije in vmesnike uvedi, kadar smiselno ločijo odgovornosti ali omogočijo dejansko ponovno uporabo. Globino strukture prilagodi kompleksnosti naloge.
- Uporabi obstoječe komponente, knjižnice in vzorce, kjer ustrezajo nalogi. Pomembne spremembe pristopa utemelji in dokumentiraj.

## Frontend: komponente in struktura

- Frontend je v `frontend-fm/` in uporablja React ter TypeScript.
- Upoštevaj `frontend-fm/src/components/README.md`. Skupne osnovne komponente, kot so gumbi, paneli in vnosna polja, sodijo v `src/components/ui/`, razdeljene po namenu, na primer `ui/buttons/`.
- Komponente posameznega zaslona ali funkcionalnega sklopa združi v njegovi mapi, skladno z obstoječim `src/components/home/`.
- Zaslon naj ima svojo vrhnjo komponento, ki sestavlja njegove sekcije in povezuje podatke ter dejanja. Večje vsebinske dele izdvoji v samostojne komponente; tudi te imajo lahko svoje podkomponente in podmape.
- Hierarhija map naj sledi vsebinski sestavi zaslona. Na primer: `home/HomeScreen.tsx`, `home/activity-overview/ActivityOverview.tsx` in `home/activity-overview/ActivityItem.tsx`. To je vzorec organizacije, ne zahteva za takojšnje preimenovanje obstoječih datotek.
- Posamezen zaslon naj bo sestavljen iz komponent; njegove celotne vsebine in logike ne kopiči v eni veliki datoteki ali v `App.tsx`.
- Komponente, specifične za sklop, hrani ob njem. Ko komponenta postane uporabna v več sklopih, jo prestavi v ustrezen skupni sklop. Osnovne UI komponente naj dobijo podatke in dejanja prek tipiziranih props ter naj ne vsebujejo domenske logike.
- Stanje naj upravlja komponenta ali hook, ki ga potrebuje; skupno stanje dvigni do ustreznega skupnega prednika. Logiko in dostop do API-ja po potrebi izloči iz prikaznih komponent.
- Sloge osnovnih komponent hrani ob komponenti v `*.module.css`, skladno z obstoječimi navodili.
- Pri spremembi zaslona njegovo strukturo smiselno uredi v okviru naloge. Drugih zaslonov ni treba preurejati hkrati.

## Backend: odgovornosti in preverjanje

- Upoštevaj strukturo v `backend-fm/README.md`: `api/<sklop>` za kontrolerje in DTO-je, `engine/<sklop>` za storitve, `persistence/<entiteta>` za entitete in repozitorije, `config` za konfiguracijo in `bootstrap` za zagonska opravila. Skupne API napake sodijo v `api/error`.
- Pri premikih posodobi deklaracije paketov, importe in teste; testne pakete prilagodi preverjani komponenti. Glavni aplikacijski razred naj ostane v korenskem paketu zaradi Springovega pregleda komponent.

- Backend je v `backend-fm/` in uporablja Java 21, Spring Boot ter Maven.
- Kodo razdeli po smiselnih funkcionalnih sklopih in odgovornostih. HTTP obravnavo, poslovno logiko in dostop do podatkov loči tam, kjer te odgovornosti obstajajo.
- Ob spremembi podatkov dosledno posodobi povezane modele, validacijo, preslikave, API in po potrebi shemo baze po obstoječem načinu migracij.
- Preveri nove in spremenjene metode ter prizadete API poti. Testi naj pokrijejo dejansko vedenje: uspešne primere, pomembne robne primere, neveljavne vnose in pravice, kjer so relevantne.
- Za spremembe backenda zaženi teste z `./mvnw test` iz `backend-fm/`; v PowerShellu uporabi `.\mvnw.cmd test`. Teste po potrebi dodaj ali dopolni.

## Preverjanje delovanja

- Po spremembah frontenda iz `frontend-fm/` zaženi `npm run build`, ki vključuje TypeScript preverjanje in Vite gradnjo, ter `npm run lint`. V PowerShellu lahko uporabiš `npm.cmd`.
- Pri implementaciji funkcionalnosti tudi sam zaženi aplikacijo in preveri prizadeti uporabniški tok oziroma API. Za skupni zagon uporabi `docker compose up --build` iz korena, kot opisuje `README.md`.
- Uspešna gradnja dopolnjuje preverjanje delovanja. Napak ne prikrivaj z izklapljanjem preverjanj ali odstranjevanjem testov.
- Če preverjanje preprečujejo manjkajoče nastavitve, nedosegljiva storitev ali druga omejitev okolja, jasno zapiši, kaj si preveril, kaj ostaja nepreverjeno in zakaj.
- Pri spremembah samo dokumentacije preveri pravilnost vsebine, poti in povezav; zaganjanje aplikacije in celotnih testov ni potrebno.

## Git in sledljivost

- Vsak nov feature razvijaj na svoji veji z imenom `feat/kratko-ime`. Ime naj jasno pove, kateri funkcionalnosti pripada; če je znana številka issue, jo lahko vključiš, na primer `feat/12-prijava`.
- Pred začetkom preveri trenutno vejo in delovno drevo. Če že delaš na ustrezni veji za nalogo, jo uporabi. Ohrani uporabnikove obstoječe spremembe.
- Med delom ustvarjaj manjše, smiselno zaključene commite: na primer backend, nato frontend in nato potrebne popravke. Vsak commit naj vsebuje povezano celoto in naj bo ustrezno preverjen.
- Sporočilo commita naj bo v obliki `[številka GitHub issue] [opravljeno]`, na primer `#12 implemented login API` ali `#12 added login screen`.
- Veja, commiti in morebitni pull request naj bodo jasno povezani z istim featurejem in pripadajočo nalogo oziroma GitHub issue. V relevantni dokumentaciji in ADR-jih dodaj povezavo na nalogo, kadar je znana.
- Če številka issue ni znana, je ne izmišljaj. Implementacijo lahko pripraviš, pred commitom pa pridobi manjkajočo številko.
- V commit vključi samo spremembe za trenutno nalogo. Skrivnosti in lokalne konfiguracije, kot je `.env`, ohrani zunaj Git zgodovine.

## Komentarji in predaja

- Komentarjev naj bo malo. Uporabi jih za pojasnilo razlogov, nejasne logike ali pomembnih omejitev.
- V TypeScriptu ne dodajaj rutinskih opisov funkcij, props in tipov, kadar je njihov pomen razviden iz kode.
- Posebnosti, ki jih mora poznati naslednji razvijalec, zapiši v ustrezno datoteko Markdown in jo poveži z obstoječo dokumentacijo.
- Z uporabnikom komuniciraj v slovenščini. Ob zaključku kratko opiši, kaj je implementirano, katera preverjanja so uspela in katere omejitve ali odprte točke še ostajajo.
