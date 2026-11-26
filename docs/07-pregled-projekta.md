# Pregled prevzetega projekta FemtoManage

## Namen pregleda

Pregled je pripravljen za nalogo pri predmetu RIRS z naslovom »Menjava projektov in CI/CD«. Namen pregleda je ugotoviti dejansko stanje prevzetega projekta, uporabljene tehnologije, obseg implementiranih funkcionalnosti, kakovost obstoječega preverjanja ter omejitve, ki jih je treba upoštevati pri pripravi cevovoda CI/CD.

Pregled je temeljil na izvorni kodi, konfiguraciji, testih in dokumentaciji v repozitoriju. Funkcionalnost je označena kot implementirana samo, kadar jo podpirajo izvorna koda in povezani testi oziroma je njeno delovanje drugače preverljivo iz projekta.

## Osnovni podatki in struktura

Projekt je organiziran v dve glavni aplikacijski mapi:

- [backend-fm](../backend-fm/) vsebuje backend aplikacijo, teste, migracije in Dockerfile.
- [frontend-fm](../frontend-fm/) vsebuje React uporabniški vmesnik, Playwright teste in Dockerfile.
- [docs](./) vsebuje funkcionalno specifikacijo, podatkovni model, arhitekturne opise, navodila in ADR-je.
- [compose.yaml](../compose.yaml) definira lokalni zagon frontenda in backenda.

Backend sledi razdelitvi na API kontrolerje in DTO-je, poslovni sloj `engine`, obstojni sloj `persistence`, konfiguracijo ter zagonska opravila. Glavni razred aplikacije je v korenskem paketu, zato Spring pregleda podpakete. Frontend je razdeljen na zaslone in komponente za avtentikacijo, aktivnosti, koledar, lokacije, prepoznavanje kraja, refleksije, deljenje in nastavitve.

Arhitekturne odločitve so zbrane v [kazalu ADR](adr/README.md). Med pomembnejšimi odločitvami so enoten backend, PostgreSQL, React PWA, strežniška seja, Docker Compose in uporaba zunanje baze Supabase.

## Uporabljene tehnologije in različice

Različice so povzete iz konfiguracijskih datotek repozitorija:

| Področje | Tehnologija oziroma različica |
|---|---|
| Backend jezik | Java 21 |
| Backend ogrodje | Spring Boot 4.1.2 |
| Backend gradnja | Maven prek `mvnw` |
| Backend podatkovni dostop | Spring Data JPA |
| Varnost | Spring Security, strežniška seja in CSRF |
| Migracije | Flyway |
| Produkcijska baza | PostgreSQL, v lokalni konfiguraciji zunanja Supabase baza |
| Testna baza | H2 v pomnilniku |
| Frontend | React 19.3.1 |
| Frontend jezik | TypeScript 6.0.3 |
| Frontend gradnja | Vite 8.3.6 |
| Frontend preverjanje sloga | ESLint 10.14.0 |
| Brskalniški testi | Playwright 1.65.0 |
| Kontejnerizacija | Docker in Docker Compose |
| AI integracija | OpenAI adapter na backendu za pripravo osnutka aktivnosti |

Frontend uporablja tudi `react-dom`, `lucide-react`, `typescript-eslint` ter React ESLint vtičnika za hooke in osveževanje.

## Konfiguracija in lokalni zagon

Docker Compose zažene dve storitvi:

1. backend na vratih `8080`,
2. frontend na vratih `5173`.

Frontend prek Vite proxyja posreduje zahteve `/api` backendu. V Compose okolju je cilj proxyja `http://backend:8080`. Backend se ne poveže na lokalno PostgreSQL storitev v Compose, temveč na PostgreSQL povezavo, podano prek spremenljivk `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` in `SPRING_DATASOURCE_SSLMODE`. Dodatne nastavitve so namenjene začetnemu skrbniku, seji in OpenAI integraciji.

Za lokalni zagon korenski [README](../README.md) predvideva:

```sh
cp .env.example .env
docker compose up --build
```

V `.env` se lahko uporabijo samo lokalne oziroma razvojne skrivnosti. Datoteka `.env.example` vsebuje imena potrebnih nastavitev, ne pa dejanskih poverilnic.

Lokalni zagon je bil preverjen z Docker Desktopom. Obe sliki sta se uspešno zgradili, frontend in backend kontejner sta ostala v stanju `Up`, povezava s Supabase PostgreSQL je delovala, Flyway pa je uspešno izvedel 9 migracij do različice v9. Frontend je vrnil HTTP 200 na `http://localhost:5173`, backendov endpoint `GET /api/auth/csrf` pa HTTP 200 na `http://localhost:8080`. Za preverjanje je bil uporabljen Session pooler in ločena lokalna `.env` konfiguracija; skrivnosti niso del repozitorija.

## Pregled implementiranih funkcionalnosti MVP

Dokument [funkcionalnosti in obseg](02-funkcionalnosti-in-obseg.md) kot MVP navaja ročno beleženje aktivnosti, upravljanje lokacij, prepoznavanje kraja, kontekstne predloge, besedilne osnutke, dnevni pregled, deljenje povzetkov ter upravljanje lastnih kategorij in ciljev. Podporni funkcionalnosti sta dodelitev mentorja in upravljanje skupnih kategorij.

V izvorni kodi so jasno prisotne naslednje funkcionalnosti:

- registracija, prijava, odjava, seja in preverjanje uporabniške vloge;
- ustvarjanje in urejanje lastnih potrjenih aktivnosti;
- preverjanje obveznih polj, časovnih intervalov, lastništva in prekrivanj;
- upravljanje lastnih lokacij ter ohranitev zgodovinske povezave pri izbrisu;
- prepoznavanje kraja glede na koordinate, radij in najbližji zadetek;
- kontekstni predlog na podlagi potrjene zgodovine;
- priprava osnutka aktivnosti iz naravnega jezika prek konfiguriranega AI adapterja;
- dnevna časovnica, seštevki po kategorijah in razdelitev aktivnosti čez polnoč;
- zasebna dnevna refleksija z validacijo energije;
- predogled, potrditev, posnetek in preklic deljenja dnevnega povzetka;
- bralni dostop mentorja samo do izrecno deljenih povzetkov;
- skrbniška dodelitev obstoječega uporabnika v vlogo mentorja.

Za kategorije in cilje je v frontend kodi prisoten zaslon z lokalnim oziroma vzorčnim stanjem, vendar v pregledani backend kodi ni ločenih kontrolerjev, storitev ali API operacij za celotno upravljanje uporabniških kategorij in ciljev. Zato US-16 in US-17 v tem pregledu nista označeni kot v celoti implementirani.

## Uporabniške zgodbe

Preverjenih je vseh 17 zgodb iz [dokumenta uporabniških zgodb](05-uporabniske-zgodbe.md). Za vsako je podana previdna ocena dejanskega stanja:

| Zgodba | Ocena glede na pregled kode in testov |
|---|---|
| US-01 – ročno beleženje in urejanje aktivnosti | Implementirano; backend in frontend testi pokrivajo validacijo, prekrivanja, urejanje istega zapisa in premik med dnevi. |
| US-02 – upravljanje lokacij | Implementirano; prisotni so CRUD API, frontend obrazci, geolokacijski fallback in testi lastništva. |
| US-03 – prepoznavanje kraja | Implementirano; prisotni so API, izračun kandidatov, robni primeri in ročna izbira ob napaki. |
| US-04 – kontekstni predlog | Implementirano; predlog uporablja potrjeno zgodovino, zavrnitev ne zapisuje aktivnosti, prisotni so testi. |
| US-05 – besedilo v osnutek | Implementirano ob konfiguriranem AI ponudniku; prisotni so osnutek, pregled, validacija in ročni fallback. |
| US-06 – dnevni pregled | Implementirano; časovnica, seštevki, polnoč in refleksija so pokriti s kodo in testi. |
| US-07 – deljenje in preklic povzetka | Implementirano; preverjeni so predogled, nespremenljiv posnetek, preklic in omejitev podatkov. |
| US-08 – branje Google Calendar | Ni implementirano; v pregledani kodi ni Google OAuth ali Calendar API integracije. |
| US-09 – izvoz v Google Calendar | Ni implementirano. |
| US-10 – glasovni vnos | Ni implementirano. |
| US-11 – predloge rutin | Ni implementirano. |
| US-12 – uvoz dnevnika iz CSV | Ni implementirano. |
| US-13 – mentorski pregled povzetkov | Backend dostop je implementiran in testiran; celoten uporabniški zaslon za mentorski pregled v pregledani frontend strukturi ni potrjen. |
| US-14 – dodelitev vloge mentorja | Backend je implementiran in testiran; uporabniški tok je predvsem API oziroma testno podprt. |
| US-15 – upravljanje skupnih kategorij | Ni potrjeno kot implementirano; v pregledani kodi ni ločenega upravljalskega API-ja za skupne kategorije. |
| US-16 – lastne kategorije in cilji | Ni potrjeno kot v celoti implementirano; frontend vsebuje vzorčno lokalno stanje, trajna backend podpora ni prisotna. |
| US-17 – pregled doseganja ciljev | Ni potrjeno kot v celoti implementirano; prikaz ciljev obstaja kot frontend vzorec, povezava z dejanskimi podatki ni potrjena. |

## Obstoječe preverjanje

Backend vsebuje 17 testnih datotek, med njimi teste za aktivnosti, lokacije, prepoznavanje kraja, refleksije, deljenje povzetkov, prijavo, registracijo, začetnega skrbnika in OpenAI adapter. Testni profil uporablja H2 v pomnilniku, zato backend testi niso odvisni od produkcijske baze.

Frontend vsebuje 13 Playwright specifikacij. Te pokrivajo prijavo in registracijo, ročni vnos, urejanje, koledar, lokacije, prepoznavanje kraja, kontekstni predlog, besedilni vnos, refleksijo, deljenje ter dodelitev mentorja. Za izolirane brskalniške teste obstaja skripta, ki pripravi H2 backend in testno okolje.

V repozitoriju trenutno ni konfiguracije za:

- skupni CI workflow;
- backend linter;
- merjenje in objavo pokritosti kode;
- shranjevanje build artefaktov v CI;
- produkcijski deploy;
- zaščito veje `main`.

## Ugotovitve in tveganja za CI/CD

1. Lokalni Compose zahteva veljavno zunanjo PostgreSQL/Supabase konfiguracijo, zato mora CI uporabljati ločeno testno bazo ali H2 testni profil.
2. Backend Dockerfile gradi aplikacijo z Mavenom, frontend Dockerfile pa namesti npm odvisnosti in zažene razvojni Vite strežnik. Za produkcijski deploy bo treba jasno ločiti razvojni strežnik od statične produkcijske namestitve.
3. Deploy zahteva previdno obravnavo že zgrajenih artefaktov, saj trenutna Dockerfile procesa gradita znotraj slike.
4. Frontend URL backenda je trenutno nastavljen prek `BACKEND_URL` za Vite proxy. Pri javni namestitvi bo treba določiti, ali bosta frontend in backend pod istim izvorom ali bo potreben CORS in ločen javni API URL.
5. OpenAI funkcionalnost je odvisna od skrivnosti `OPENAI_API_KEY`; CI testi morajo uporabljati nadzorovane oziroma ponarejene odzive in ne smejo zahtevati produkcijskega ključa.
6. Kategorije in cilji so pomembna odprta funkcionalna vrzel glede na dokumentirani MVP in jih ni smiselno v CI označiti kot uspešno implementirane brez dodatne potrditve.

## Sklep

FemtoManage ima dobro razdeljen backend, osnovni React frontend, dokumentirane arhitekturne odločitve in precejšnje testno izhodišče. Jedro MVP za beleženje aktivnosti, lokacije, predloge, dnevni pregled, refleksije in deljenje povzetkov je v pomembnem delu implementirano in testirano. Projekt pa še nima CI/CD cevovoda, javne namestitve, coverage artefaktov in zaščite veje `main`. Poleg tega kategorije in cilji niso potrjeni kot celovita trajna funkcionalnost, razširitve Google Calendar, glasovni vnos, rutine in CSV uvoz pa so dokumentirane kot neimplementirane nadgradnje.
