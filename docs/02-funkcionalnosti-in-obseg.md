# 2. Funkcionalnosti in obseg

## Uporabniške vloge

| Vloga | Pravice | Omejitve |
|---|---|---|
| Uporabnik | Vodi svoj dnevnik, shrani kraje, potrjuje predloge ter deli in prekliče povzetke. | Ne dostopa do tujih dnevnikov in ne upravlja drugih računov. |
| Mentor / svetovalec | Bere izrecno deljene povzetke za pomoč pri organizaciji časa. | Ne ureja dnevnika; ne vidi lokacij, podrobnih opisov in nedeljenih podatkov. |
| Skrbnik | Upravlja račune, vloge in skupne kategorije. | Nima aplikacijskega dostopa do osebnih dnevnikov; ne potrjuje aktivnosti drugih. |

## Predvidene funkcionalnosti

1. **FR-01 [MVP] Časovni bloki:** beleženje naziva, začetka, konca in aktivne kategorije dejansko izvedene aktivnosti z neobveznim opisom in lastnim shranjenim krajem; preverjanje obveznih podatkov in časovnega intervala ter opozorilo na prekrivanje z možnostjo popravka ali izrecnega nadaljevanja. Ročni vnos deluje tudi brez lokacije in AI. **Zgodba:** US-01.
2. **FR-02 [MVP] Upravljanje lokacij:** dodajanje, pregled, urejanje in brisanje lastnih lokacij z imenom, koordinatami in pozitivnim radijem; ročni vnos ali prevzem trenutnih koordinat ob dovoljenju uporabnika ter ponovna uporaba v lastnem dnevniku. **Zgodba:** US-02.
3. **FR-03 [MVP] Prepoznavanje kraja:** ob odprtju ali na zahtevo primerjava trenutne lokacije z lastnimi znanimi kraji, predlog najbližjega ustreznega kraja in prikaz natančnosti meritve; ob nedostopni lokaciji ostane ročna izbira. Preverjanje ne ustvari kraja ali aktivnosti samodejno. **Zgodba:** US-03.
4. **FR-04 [MVP] Kontekstni predlog:** priprava osnutka po preprostih pravilih glede na kraj, uro in pogostost potrjenih zapisov; uporabnik predlog dopolni, popravi, potrdi ali zavrne. Osnutki in zavrnjeni predlogi ne vplivajo na zgodovino potrjenih aktivnosti. **Zgodba:** US-04.
5. **FR-05 [MVP] Besedilo v osnutek:** pretvorba naravnega jezika v osnutek aktivnosti, dopolnitev manjkajočih oziroma dvoumnih podatkov ter uporabnikov pregled in potrditev po preverjanju časa in prekrivanj; ob nedostopnosti AI ostane ročni vnos. **Zgodba:** US-05.
6. **FR-06 [MVP] Dnevni pregled in zasebna refleksija:** časovnica potrjenih aktivnosti za izbrani dan in trajanja po kategorijah v urah in minutah, z razdelitvijo aktivnosti čez polnoč in pojasnilom seštevanja prekrivanj. Uporabnik lahko shrani in ponovno odpre zasebno dnevno opombo, počutje in samooceno energije od 1 do 5; refleksija ni časovni blok in se ne deli z mentorjem. **Zgodba:** US-06.
7. **FR-07 [MVP] Deljenje in mentorski pregled povzetka:** predogled in izrecno deljenje posnetka dneva izbranemu mentorju, ki vidi seznam njemu dostopnih povzetkov in njihovo vsebino samo bere. Posnetek vsebuje datum, seštevke po kategorijah in izbirni komentar za deljenje, brez lokacij, podrobnih opisov, izvornih vnosov, zasebnih refleksij in posameznih časovnih blokov. Spremembe dnevnika ne spreminjajo že deljenega posnetka; preklic prepreči nadaljnji dostop. **Zgodbi:** US-07, US-13.
8. **FR-08 [Nadgradnja] Branje Google Calendar:** ob odobrenem dostopu prikaz načrtovanih dogodkov izbranega koledarja in dneva kot konteksta; iz dogodka lahko uporabnik pripravi in popravi osnutek dejanske aktivnosti. Samo branje ne ustvari potrjenih zapisov in ne spremeni seštevkov. **Zgodba:** US-08.
9. **FR-09 [Nadgradnja] Izvoz v Google Calendar:** predogled in izrecno potrjen prenos izbranih lastnih potrjenih aktivnosti v koledar z odobrenim pisanjem, z ohranitvijo časovnih intervalov in prikazom uspeha ali napake za vsako aktivnost; ponovitev iste potrditvene zahteve ne podvoji uspešno izvoženih dogodkov. **Zgodba:** US-09.
10. **FR-10 [Nadgradnja] Glasovni vnos:** snemanje na zahtevo z dovoljenjem za mikrofon, ustavitev ali preklic, pregled in popravek prepisa ter priprava osnutka za preverjanje in potrditev; ob napaki je mogoč besedilni ali ročni vnos. **Zgodba:** US-10.
11. **FR-11 [Nadgradnja] Predloge rutin:** shranjevanje poimenovanih zaporedij aktivnosti s kategorijami, trajanji in časovnimi odmiki; uporaba za izbrani datum in začetno uro ustvari osnutke, ki jih uporabnik pred potrditvijo pregleda, spremeni ali odstrani brez spreminjanja predloge. **Zgodba:** US-11.
12. **FR-12 [Nadgradnja] Uvoz dnevnika:** uvoz dogovorjenega CSV-formata z opisom in vzorcem, predogledom vrstic, razlogi napak ter preverjanjem kategorij, lastništva krajev, časov in prekrivanj. Uporabnik potrdi izbrane veljavne vrstice, sistem poroča o rezultatu; ponovitev iste potrditvene zahteve ne podvoji uspešno uvoženih zapisov. **Zgodba:** US-12.
13. **FR-13 [Podpora] Upravljanje računov in vlog – dodelitev mentorja:** skrbnik poišče obstoječi račun po e-poštnem naslovu in potrdi spremembo vloge navadnega uporabnika v mentorja. Dodelitev ne omogoči dostopa do nedeljenih povzetkov; običajna registracija ne omogoča izbire privilegirane vloge. **Zgodba:** US-14.
14. **FR-14 [Podpora] Upravljanje skupnih kategorij:** skrbnik dodaja kategorije z enolično nespremenljivo kodo, spreminja njihova prikazna imena in jih aktivira ali deaktivira. Deaktivirane kategorije niso na voljo za nove potrjene aktivnosti; obstoječe aktivnosti in deljeni posnetki ostanejo shranjeni. **Zgodba:** US-15.

15. **FR-15 [MVP] Upravljanje lastnih kategorij in ciljev:** dodajanje, pregled, urejanje in brisanje lastnih kategorij ter nastavljanje dnevnega časovnega cilja za vsako kategorijo. **Zgodba:** US-16.
16. **FR-16 [MVP] Pregled doseganja ciljev:** diagram na domačem zaslonu prikazuje doseženi dnevni čas vsake kategorije relativno glede na njen cilj (doseženi čas / ciljni čas). **Zgodba:** US-17.

- Seznam vsebuje **16 funkcionalnosti, pokritih s 17 uporabniškimi zgodbami**; FR-07 ima ločeni zgodbi za lastnika in mentorja.
- Prva različica vključuje **9 domenskih MVP funkcionalnosti (FR-01–FR-07, FR-15–FR-16)** in podporno skrbniško upravljanje (FR-13–FR-14); domensko jedro so predvsem FR-01, FR-03, FR-04, FR-05 in FR-06.
- Prijava in preverjanje vloge, lastništva ter dovoljenj sta tehnična podpora vsem funkcionalnostim; omejitve veljajo tudi pri neposrednih zahtevah API-ju. Skrbniško upravljanje ne omogoča branja osebnih dnevnikov.
- Zasebna refleksija je del FR-06, komentar za deljenje pa del FR-07; zasebna opomba se ne prenese samodejno v deljeni povzetek.

## Netrivialen delovni proces

- Kontekst ali besedilni vnos → osnutek → preverjanje časa, kraja in prekrivanj → uporabnikova potrditev oziroma popravek → shranjevanje → posodobitev dnevnega pregleda.
- Zavrnjen predlog ne postane aktivnost; nepopoln osnutek zahteva dopolnitev.
- Deljenje: izbor dneva in mentorja → predogled dovoljenih podatkov → izrecna potrditev → posnetek povzetka in bralni dostop → mentorski pregled oziroma uporabnikov preklic dostopa.

## Kasnejše nadgradnje

### 1. Nadgradnja

- Branje dogodkov iz Google Calendar.
- Izvoz potrjenih aktivnosti v Google Calendar.
- Glasovni vnos aktivnosti.
- Predloge ponavljajočih se rutin.
- Uvoz starega dnevnika iz CSV.

### Dodatna Nadgradnja - Samo ce bi bil cas, po vrsti:

- Poln koledarski sistem za načrtovanje obveznosti.
- Napredni osebni model strojnega učenja (model bi potreboval veliko podatkov, omogocil pa bi vecjo avtonomijo sistema z enakimi vhodnimi podatki)
- Zdravstvena diagnostika, spremljanje srčnega utripa in uporabe drugih aplikacij (Health Connect)


## Izven obsega projekta

- Neprekinjeno GPS-sledenje ob zaprti aplikaciji. (mozno le za native)
- Samodejno beleženje ali spreminjanje koledarja brez potrditve.
- Urejanje brez povezave in reševanje konfliktov med napravami.
