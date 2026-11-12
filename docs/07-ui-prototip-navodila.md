# 7. Navodila za osnovni UI prototip

Ta dokument dopolnjuje [wireframe](06-wireframes.md) z odločitvami iz pogovora o postavitvi. Namenjen je izdelavi klikabilnega React PWA prototipa z lažnimi podatki, ki ga je mogoče pregledati na telefonu in širšem zaslonu. Funkcionalne zahteve in uporabniške zgodbe ostanejo vir za dejansko implementacijo.

## Skupna navigacija

- Spodnja navigacijska vrstica je stalno vidna na glavnih zaslonih. Od leve proti desni ima pet ikon: **Lokacije**, **Koledar / časovnica**, **Domov / dashboard**, **Cilji / kategorije**, **Nastavitve**.
- Prijava in registracija sta samostojna zaslona brez spodnje navigacije. Med njima je vidna povezava za prehod.
- Vsebina nastavitev in obnašanje navigacije na podrobnostih ter obrazcih še nista določena; prototip naj ti odločitvi jasno označi za pregled.

## Domov

- Na vrhu je pozdrav uporabnika.
- Glavni prikaz je informativen **radarski oziroma pajkov diagram** dnevnega doseganja ciljev. Vsaka uporabnikova kategorija s časovnim ciljem ima svojo os. Šest osi je primer za prototip, ne vnaprej določen seznam ali omejitev števila kategorij.
- Razdalja po osi predstavlja razmerje med današnjim potrjenim časom v kategoriji in njenim dnevnim ciljem: nič je v središču, dosežen cilj na zunanjem robu. Točke se povežejo v lik. Ob diagramu je prehod na seznam ciljev.
- Pod diagramom so po skici trije položaji za prejšnjo, trenutno in naslednjo aktivnost. Kaj natančno pomeni »naslednja« ter kako se pokaže manjkajoča aktivnost, je še treba določiti; v prototipu naj bodo jasno označeni vzorčni podatki.
- Spodaj desno je gumb za dodajanje aktivnosti. Njegov natančen naslednji korak še ni določen.

## Cilji in kategorije

- Kategorije so prikazane v navpičnem seznamu kartic oziroma vrstic.
- Vsaka vrstica že na prvi pogled kaže ime kategorije, dnevni časovni cilj, današnji napredek kot **dosežene minute / ciljne minute** in vrstico napredka z odstotkom.
- Dotik vrstice odpre urejanje kategorije in njenih podatkov. Na zaslonu je tudi možnost dodajanja kategorije.
- Napredek je dnevni; isti podatki napajajo diagram na domači strani.

## Koledar in aktivnost

- Dnevna časovnica uporablja **mrežo po urah**. Višina bloka odraža trajanje aktivnosti, praznine med bloki pa ostanejo vidne. Na telefonu se časovnica po potrebi pomika navpično; ni je treba stisniti na višino enega zaslona.
- Na širšem zaslonu je časovnica širša in izkoristi razpoložljiv prostor.
- Blok na mreži kaže le osnovne podatke. Dotik odpre pojavno podrobnost z vsemi podatki aktivnosti; daljša vsebina v njej se lahko pomika. V zgornjem delu sta akciji za urejanje in odstranitev.
- Dodajanje in urejanje aktivnosti uporablja obrazec iz obstoječega wireframa. Neposredno vlečenje ali spreminjanje časa na mreži še ni določeno in ni del osnovnega prototipa.

## Lokacije

- Lokacije so prikazane v seznamu kartic. Na kartici je za zdaj **samo naziv**.
- Gumb za dodajanje lokacije je pri dnu zaslona.
- Dotik kartice odpre pojavno podrobnost lokacije z njenimi podatki. Na vrhu sta akciji za urejanje in brisanje.
- Obrazec lokacije vsebuje naziv, koordinate, radij in dodatno polje za naslov. Gumb **Uporabi trenutno lokacijo** je blizu koordinat oziroma naslova in je dovolj opazen, da olajša vnos.
- Naslov je dodatna želja za UI; v [FR-02](02-funkcionalnosti-in-obseg.md) in trenutnem [podatkovnem modelu](03-podatkovni-model.md) še ni opredeljen. Pred povezavo z API-jem je treba določiti, ali se naslov hrani ter kako se pridobi. Ime, veljavne koordinate in pozitivni radij ostanejo obvezni po US-02.

## Prijava in registracija

- Zaslona nimata spodnje navigacije. Prijava ima polji za e-pošto in geslo. Registracija ima ime, e-pošto, geslo in ponovno vneseno geslo.
- Na vsakem zaslonu je prehod na drugega. Polje za ime je nova UI odločitev; pred dejansko implementacijo je treba uskladiti [model uporabnika](03-podatkovni-model.md) in API.

## Videz in pregled prototipa

- Videz, barve, tipografija in podrobna oblika komponent za zdaj niso dokončno določeni. Prvi prototip naj omogoča hitro primerjavo in popravljanje sloga; uporabnik ne želi generičnega »AI« videza.
- Uporabi dosledne komponente za kartice, gumbe, polja, pojavna okna in spodnjo navigacijo. Izdelaj prikaze za telefon in širši zaslon.
- Prototip lahko uporablja lažne podatke. Pred pregledom pripravi posnetke glavnih zaslonov in pojavnih podrobnosti na obeh širinah.

## Odprte odločitve

Popravki [#57](https://github.com/MikeDQG/femto-manage/issues/57) poenotijo razmake domačih kartic na 18 px, skrajšajo vidni besedili lokacijskih akcij (dostopni imeni ohranita kraj) in odstranijo vrhnjo oznako na časovnici, ciljih ter nastavitvah. Nastavitve vsebujejo en izbor teme, shranjen v napravi. Trajanja po kategorijah ostanejo na časovnici do izvedbe [#50](https://github.com/MikeDQG/femto-manage/issues/50). Preverjanje: build, lint in brskalniška testa `ui-fixes.spec.ts` ter `places.spec.ts` na telefonu in namizju; testi shranijo tudi posnetke prizadetih zaslonov.

1. Kaj na domačem zaslonu pomeni »naslednja aktivnost« in kako se prikažejo prazni položaji?
2. Ali gumb za dodajanje odpre neposredno obrazec ali najprej izbiro načina vnosa?
3. Ali spodnja navigacija ostane vidna tudi na obrazcih in pojavnih podrobnostih?
4. Kako se urejajo kategorije: na ločenem zaslonu ali v pojavnem oknu?
5. Ali se naslov lokacije samo ročno vnese ali ga aplikacija predlaga iz koordinat?
6. Kakšna je vsebina zaslona Nastavitve?
7. Kakšen vizualni slog bo izbran po pregledu prvega prototipa?
