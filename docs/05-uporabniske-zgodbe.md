# 5. Uporabniške zgodbe

Zgodbe so usklajene z zasnovo, obsegom, podatkovnim modelom in pravicami v dokumentih 01–04. FR-01–FR-12 sledijo številčenju funkcionalnosti v dokumentu 02. FR-13 in FR-14 označujeta že opredeljeno podporno upravljanje računov, vlog in skupnih kategorij.

US-01–US-07, US-13 in US-16–US-17 pokrivajo devet MVP funkcionalnosti, US-08–US-12 poznejše nadgradnje, US-14–US-15 pa podporne skrbniške funkcionalnosti. Dnevnik vsebuje dejanske aktivnosti; načrtovani dogodki in predlogi postanejo potrjeni zapisi šele po uporabnikovi potrditvi.

## US-01: Ročno beleženje aktivnosti

- **Povezana funkcionalnost:** FR-01 – Časovni bloki [MVP].
- **Uporabniška vloga:** Uporabnik.
- **Uporabniška zgodba:** Kot uporabnik želim vnesti časovni blok izvedene aktivnosti, da lahko spremljam, za kaj sem porabil čas.

**Merila sprejemljivosti (acceptance criteria):**

1. Uporabnik vnese naziv, začetek, konec in aktivno kategorijo; opis in izbira lastnega shranjenega kraja sta neobvezna. Vnos deluje tudi brez lokacije in AI.
2. Manjkajoča obvezna polja ali konec, ki ni poznejši od začetka, preprečijo shranjevanje; sistem označi vsako neveljavno polje in razlog.
3. Pred shranjevanjem sistem ob prekrivanju z lastno potrjeno aktivnostjo prikaže njen naziv in interval ter zahteva popravek ali izrecno nadaljevanje kljub opozorilu. Stik konca ene aktivnosti z začetkom druge ni prekrivanje.
4. Potrditev ustvari eno potrjeno aktivnost v lastnem dnevniku in posodobi dnevni pregled; preklic ne ustvari potrjene aktivnosti. Vnos v tuj dnevnik je zavrnjen tudi prek API-ja.

## US-02: Upravljanje lokacij

- **Povezana funkcionalnost:** FR-02 – Upravljanje lokacij [MVP].
- **Uporabniška vloga:** Uporabnik.
- **Uporabniška zgodba:** Kot uporabnik želim dodajati, pregledovati, urejati in brisati svoje lokacije, da lahko pri beleženju izbiram ustrezne shranjene kraje.

**Merila sprejemljivosti:**

1. Uporabnik lahko vnese ime, zemljepisno širino, zemljepisno dolžino in radij v metrih; ob dovoljenju za lokacijo lahko koordinate prevzame iz trenutne meritve.
2. Uporabnik vidi seznam svojih lokacij in lahko spremeni njihove podatke ali lokacijo izbriše; izbris je odstrani iz izbire za nove aktivnosti in prepoznavanja kraja, ne izbriše pa preteklih aktivnosti. Ročni vnos koordinat deluje tudi ob zavrnjenem dovoljenju za lokacijo.
3. Prazno ime, širina zunaj območja od −90 do 90, dolžina zunaj območja od −180 do 180 ali radij, manjši ali enak nič, preprečijo shranjevanje in povzročijo prikaz razloga.
4. Potrjene spremembe so vidne ob naslednjem vnosu lastne aktivnosti; drug uporabnik lokacije ne more prebrati, uporabiti, urejati ali izbrisati niti prek API-ja.

## US-03: Prepoznavanje kraja

- **Povezana funkcionalnost:** FR-03 – Prepoznavanje kraja [MVP].
- **Uporabniška vloga:** Uporabnik.
- **Uporabniška zgodba:** Kot uporabnik želim preveriti, ali sem na katerem od svojih znanih krajev, da mi pri vnosu ni treba ponovno izbirati kraja.

**Merila sprejemljivosti:**

1. Ob odprtju aplikacije z dovoljenim dostopom ali ob uporabnikovi zahtevi sistem pridobi trenutno lokacijo in prikaže natančnost meritve v metrih.
2. Med lastnimi kraji predlaga najbližjega, pri katerem je razdalja do središča manjša ali enaka shranjenemu radiju; pri enaki razdalji omogoči izbiro uporabniku.
3. Če noben kraj ne ustreza, sistem prikaže, da znani kraj ni prepoznan. Ne ustvari novega kraja ali aktivnosti brez potrditve.
4. Ob zavrnjenem dovoljenju ali napaki meritve prikaže razlog in omogoči ročno izbiro kraja; ob zaprti aplikaciji ne izvaja neprekinjenega sledenja.

## US-04: Kontekstni predlog aktivnosti

- **Povezana funkcionalnost:** FR-04 – Kontekstni predlog [MVP].
- **Uporabniška vloga:** Uporabnik.
- **Uporabniška zgodba:** Kot uporabnik želim dobiti predlog aktivnosti glede na kraj, uro in potrjeno zgodovino, da lahko ponavljajočo se aktivnost zabeležim z manj vnosa.

**Merila sprejemljivosti:**

1. Za zgodovino, kjer na istem kraju v isti uri začetka prevladuje ena kategorija, sistem predlaga to kategorijo; osnutki in zavrnjeni predlogi ne štejejo v pogostost.
2. Predlog je prikazan kot osnutek z razpoložljivimi podatki o času, kraju in kategoriji; manjkajoči obvezni podatki so označeni za dopolnitev.
3. Uporabnik lahko predlog popravi, potrdi ali zavrne. Potrditev zahteva veljavna obvezna polja in obravnavo opozorila o prekrivanju; zavrnitev ne spremeni dnevnih seštevkov.
4. Če ni potrjene zgodovine, sistem sporoči, da zanjo še ni podatkov, in omogoči ročni vnos brez izmišljenega predloga.

## US-05: Besedilo v osnutek

- **Povezana funkcionalnost:** FR-05 – Besedilo v osnutek [MVP].
- **Uporabniška vloga:** Uporabnik.
- **Uporabniška zgodba:** Kot uporabnik želim aktivnost opisati v naravnem jeziku, da lahko pripravim zapis brez ločenega izpolnjevanja vsakega polja.

**Merila sprejemljivosti:**

1. Za besedilo »Danes od 9. do 11. ure sem študiral« sistem pripravi osnutek za današnji datum od 09:00 do 11:00 s kategorijo študij, če je ta aktivna.
2. Manjkajoči ali dvoumni obvezni podatki zahtevajo dopolnitev. Kraj se poveže samo z lastnim shranjenim krajem; ob več zadetkih sistem zahteva izbiro.
3. Uporabnik lahko izluščena polja pregleda in popravi; šele njegova potrditev po preverjanju intervala in prekrivanj ustvari potrjeno aktivnost.
4. Če AI ni nastavljen ali zahteva ne uspe, sistem prikaže nedostopnost storitve, ohrani vneseno besedilo in omogoči ročni vnos brez ustvarjanja potrjene aktivnosti.

## US-06: Dnevni pregled

- **Povezana funkcionalnost:** FR-06 – Dnevni pregled [MVP].
- **Uporabniška vloga:** Uporabnik.
- **Uporabniška zgodba:** Kot uporabnik želim pregledati časovnico in trajanja po kategorijah za izbrani dan, da lahko razumem svoje dnevne navade.

**Merila sprejemljivosti:**

1. Časovnica prikazuje potrjene aktivnosti po začetku z nazivom, kategorijo, začetkom, trajanjem in krajem, kadar je naveden; osnutki niso vključeni.
2. Seštevki so prikazani v urah in minutah: študij 09:00–10:00 in 11:00–11:30 pomeni 1 h 30 min. Aktivna kategorija brez zapisov kaže 0 min; dan brez zapisov ima prazno časovnico.
3. Aktivnost 23:30–00:30 prispeva po 30 minut v vsak pripadajoči dan glede na uporabnikov lokalni čas. Potrjena prekrivanja se seštevajo po aktivnostih; pregled pojasni, da je prekrivajoči čas lahko štet večkrat.
4. Po potrditvi aktivnosti se pregled posodobi. Uporabnik lahko za dan shrani in ponovno odpre zasebno refleksijo z neobvezno opombo, počutjem in energijo od 1 do 5; druga vrednost energije se zavrne, refleksija pa ne spremeni trajanj in se ne deli z mentorjem.

## US-07: Deljenje in preklic povzetka

- **Povezana funkcionalnost:** FR-07 – Deljenje povzetka [MVP].
- **Uporabniška vloga:** Uporabnik.
- **Uporabniška zgodba:** Kot uporabnik želim izbranemu mentorju omogočiti in preklicati vpogled v povzetek dneva, da lahko dobim nasvet ob nadzoru nad deljenimi podatki.

**Merila sprejemljivosti:**

1. Pred deljenjem uporabnik izbere dan in obstoječi račun z vlogo mentorja ter pregleda datum, seštevke po kategorijah in izbirni komentar, namenjen deljenju.
2. Šele izrecna potrditev ustvari posnetek in mentorju dodeli bralni dostop. Povzetek in odgovor API-ja ne vsebujeta lokacij, podrobnih opisov, izvornih vnosov, zasebnih refleksij ali posameznih časovnih blokov.
3. Poznejša sprememba dnevnika ne spremeni deljenega posnetka; nove podatke uporabnik deli šele s pregledom in potrditvijo novega posnetka.
4. Po preklicu mentorjeva naslednja zahteva za povzetek, tudi prek neposredne povezave, ne vrne vsebine; lastni dnevnik ostane shranjen.

## US-08: Branje dogodkov Google Calendar

- **Povezana funkcionalnost:** FR-08 – Branje Google Calendar [nadgradnja].
- **Uporabniška vloga:** Uporabnik.
- **Uporabniška zgodba:** Kot uporabnik želim uporabiti načrtovane koledarske dogodke kot kontekst za vnos, da lahko hitreje zabeležim, kaj sem dejansko opravil.

**Merila sprejemljivosti:**

1. Branje se omogoči šele po uporabnikovi odobritvi dostopa; zavrnitev ne prepreči uporabe lokalnega dnevnika.
2. Za izbrani dan sistem prikaže naslov, začetek in konec dogodkov iz izbranega koledarja z oznako, da so načrtovani; ne vključi jih v dnevne seštevke.
3. Uporabnik lahko iz dogodka pripravi osnutek, popravi dejanski čas in kategorijo ter ga po preverjanju obveznih polj in prekrivanj potrdi; samo branje ne ustvari potrjene aktivnosti.
4. Pri poteklem dovoljenju ali napaki povezave sistem prikaže neuspeh in možnost ponovnega povezovanja; neuspešnega branja ne prikaže kot praznega koledarja.

## US-09: Izvoz v Google Calendar

- **Povezana funkcionalnost:** FR-09 – Izvoz v Google Calendar [nadgradnja].
- **Uporabniška vloga:** Uporabnik.
- **Uporabniška zgodba:** Kot uporabnik želim izbrane potrjene aktivnosti izvoziti v Google Calendar, da jih lahko pregledujem skupaj z drugimi dogodki.

**Merila sprejemljivosti:**

1. Uporabnik lahko izbere samo lastne potrjene aktivnosti in ciljni koledar, za katerega je odobril pisanje.
2. Pred izvozom pregleda nazive in intervale; zapis v koledar se izvede šele po izrecni potrditvi.
3. Vsak uspešno izvožen dogodek ohrani naziv, začetek in konec aktivnosti z upoštevano časovno cono; zasebne refleksije se ne izvozijo.
4. Sistem za vsako aktivnost prikaže uspeh ali napako in ne spremeni izvornega dnevnika. Ponovitev iste potrditvene zahteve ne ustvari dodatnih kopij že uspešno izvoženih dogodkov.

## US-10: Glasovni vnos

- **Povezana funkcionalnost:** FR-10 – Glasovni vnos [nadgradnja].
- **Uporabniška vloga:** Uporabnik.
- **Uporabniška zgodba:** Kot uporabnik želim aktivnost opisati z govorom, da lahko pripravim zapis, kadar mi tipkanje ne ustreza.

**Merila sprejemljivosti:**

1. Snemanje se začne šele na uporabnikovo zahtevo z dovoljenjem za mikrofon; uporabnik vidi stanje snemanja ter ga lahko ustavi ali prekliče.
2. Po ustavitvi sistem prikaže prepis, ki ga uporabnik lahko popravi pred pretvorbo v osnutek.
3. Osnutek zahteva pregled, dopolnitev obveznih podatkov, preverjanje časa in prekrivanj ter izrecno potrditev pred nastankom potrjene aktivnosti.
4. Zavrnjen mikrofon, prazen prepis ali napaka prepisa povzročijo prikaz ustreznega sporočila in možnost besedilnega oziroma ročnega vnosa; preklic snemanja ne ustvari aktivnosti.

## US-11: Predloge rutin

- **Povezana funkcionalnost:** FR-11 – Predloge rutin [nadgradnja].
- **Uporabniška vloga:** Uporabnik.
- **Uporabniška zgodba:** Kot uporabnik želim shraniti in uporabiti predlogo zaporedja aktivnosti, da lahko ponavljajočo se rutino zabeležim z manj ponovnega vnosa.

**Merila sprejemljivosti:**

1. Uporabnik lahko pod imenom shrani zaporedje najmanj ene aktivnosti z nazivom, kategorijo, pozitivnim trajanjem in odmikom od začetka rutine; manjkajoči podatki preprečijo shranjevanje predloge.
2. Ob uporabi izbere datum in začetno uro; sistem pripravi osnutke z izračunanimi intervali. Uporabnik jih lahko spremeni ali odstrani brez spremembe predloge.
3. Pred potrditvijo sistem preveri obvezna polja, aktivnost kategorij in intervale ter označi prekrivanja z dnevnikom in med osnutki; neveljavne podatke je treba popraviti, prekrivanja pa odpraviti ali izrecno potrditi.
4. Potrditev shrani pregledane aktivnosti v lastni dnevnik, preklic ne shrani nobene; drug uporabnik do predloge nima dostopa niti prek API-ja.

## US-12: Uvoz dnevnika iz CSV

- **Povezana funkcionalnost:** FR-12 – Uvoz dnevnika [nadgradnja].
- **Uporabniška vloga:** Uporabnik.
- **Uporabniška zgodba:** Kot uporabnik želim pred potrditvijo preveriti uvoz starega dnevnika iz CSV, da lahko prenesem obstoječe zapise brez ročnega prepisovanja.

**Merila sprejemljivosti:**

1. Sistem ponudi opis in vzorec formata UTF-8 s stolpci `title`, `start_at`, `end_at`, `category_code`, `description` in `place_id`; zadnja dva sta neobvezna, časovne vrednosti vsebujejo odmik časovne cone.
2. Predogled pokaže razbrane vrstice ali številko vrstice z razlogom napake: manjkajoč naziv, neveljaven čas, konec pred začetkom, neaktivna kategorija ali tuj oziroma neobstoječ kraj.
3. Prekrivanja z dnevnikom in med uvoženimi vrsticami so označena; uporabnik mora takšne vrstice izločiti ali izrecno potrditi nadaljevanje.
4. Potrditev shrani samo izbrane veljavne vrstice v lastni dnevnik in prikaže število shranjenih ter izločenih vrstic. Preklic ne shrani ničesar; ponovitev iste potrditvene zahteve ne podvoji uspešno uvoženih zapisov.

## US-13: Mentorjev pregled povzetkov

- **Povezana funkcionalnost:** FR-07 – Deljenje povzetka, bralni dostop [MVP].
- **Uporabniška vloga:** Mentor / svetovalec.
- **Uporabniška zgodba:** Kot mentor želim pregledati povzetke, ki so jih uporabniki izrecno delili z menoj, da jim lahko svetujem pri organizaciji časa.

**Merila sprejemljivosti:**

1. Mentor vidi seznam trenutno dostopnih povzetkov z lastnikom in datumom dneva; seznam brez povzetkov ima prikazano prazno stanje.
2. Odprt povzetek pokaže datum, trajanja po kategorijah in komentar, namenjen deljenju, brez lokacij, podrobnih opisov, izvornih vnosov in zasebnih refleksij.
3. Nedeljeni povzetki in povzetki, deljeni drugemu mentorju, niso dostopni niti prek API-ja; mentorjeve zahteve za spreminjanje povzetkov ali lastnikovega dnevnika so zavrnjene.
4. Po preklicu dostopa se povzetek ob naslednji osvežitvi odstrani s seznama in naslednja zahteva za vsebino je zavrnjena.

## US-14: Dodelitev vloge mentorja

- **Povezana funkcionalnost:** FR-13 – Upravljanje računov in vlog, dodelitev mentorja [podpora].
- **Uporabniška vloga:** Skrbnik.
- **Uporabniška zgodba:** Kot skrbnik želim obstoječemu računu dodeliti vlogo mentorja, da ga lahko uporabniki izberejo za prejemnika povzetkov.

**Merila sprejemljivosti:**

1. Skrbnik lahko poišče račun po celotnem e-poštnem naslovu in vidi naslov ter trenutno vlogo; pri neobstoječem računu dobi sporočilo, da ni zadetka.
2. Po potrditvi dodelitve se vloga navadnega uporabnika spremeni v MENTOR; ob naslednji prijavi ima mentorski pregled in je na voljo med prejemniki deljenja. Ponovna dodelitev ne ustvari dodatnega računa.
3. Zahteva za dodelitev brez skrbniške prijave je zavrnjena tudi prek API-ja; običajna registracija ne omogoča izbire vloge mentorja ali skrbnika.
4. Nova vloga ne odpre nobenega nedeljenega povzetka; skrbniški seznam in API za upravljanje računov ne vračata vsebine osebnih dnevnikov.

## US-15: Upravljanje skupnih kategorij

- **Povezana funkcionalnost:** FR-14 – Upravljanje skupnih kategorij [podpora].
- **Uporabniška vloga:** Skrbnik.
- **Uporabniška zgodba:** Kot skrbnik želim dodajati in deaktivirati skupne kategorije, da imajo uporabniki ustrezne izbire brez izgube preteklih zapisov.

**Merila sprejemljivosti:**

1. Skrbnik lahko ustvari aktivno kategorijo z nepraznim imenom in enolično kodo; prazno ime ali podvojena koda preprečita shranjevanje s prikazom razloga.
2. Nova aktivna kategorija je na voljo pri naslednjem odpiranju obrazca. Skrbnik lahko spremeni ime in aktivnost kategorije, koda pa po ustvarjanju ostane nespremenjena.
3. Deaktivirana kategorija ni na voljo za nove potrjene aktivnosti in API tak nov zapis zavrne; obstoječe aktivnosti in že deljeni posnetki se ne izbrišejo.
4. Uporabnik in mentor ne moreta upravljati skupnih kategorij niti prek API-ja; skrbniško upravljanje kategorij ne omogoča branja osebnih dnevnikov.

## US-16: Upravljanje lastnih kategorij in ciljev

- **Povezana funkcionalnost:** FR-15 – Upravljanje lastnih kategorij in ciljev [MVP].
- **Uporabniška vloga:** Uporabnik.
- **Uporabniška zgodba:** Kot uporabnik želim upravljati svoje kategorije in njihove dnevne časovne cilje, da lahko spremljam porabo časa glede na svoje prioritete.

**Merila sprejemljivosti:**

1. Uporabnik lahko doda kategorijo z imenom in dnevnim časovnim ciljem ter pregleda seznam svojih kategorij; prazno ime ali cilj, manjši ali enak nič, preprečita shranjevanje.
2. Uporabnik lahko spremeni ime in dnevni cilj kategorije; po shranjevanju se seznam in prikaz doseganja ciljev posodobita.
3. Uporabnik lahko izbriše lastno kategorijo; ta ni več na voljo za nove aktivnosti, pretekle aktivnosti pa ostanejo shranjene.
4. Lastna kategorija je na voljo pri vnosu lastne aktivnosti; drug uporabnik je ne more urejati ali izbrisati niti prek API-ja. Upravljanje skupnih kategorij ostaja skrbniška funkcionalnost.

## US-17: Pregled doseganja ciljev

- **Povezana funkcionalnost:** FR-16 – Pregled doseganja ciljev [MVP].
- **Uporabniška vloga:** Uporabnik.
- **Uporabniška zgodba:** Kot uporabnik želim na domačem zaslonu videti dnevni napredek vsake kategorije glede na njen cilj, da lahko presodim, katerim področjem moram nameniti več časa.

**Merila sprejemljivosti:**

1. Diagram na domačem zaslonu za vsako kategorijo z nastavljenim ciljem prikazuje razmerje med doseženim dnevnim časom in ciljnim časom; upošteva potrjene aktivnosti po pravilih dnevnega pregleda.
2. Ena ura športa pri cilju ene ure pomeni 100 %, ena ura učenja pri cilju dveh ur pa 50 %; prikaz temelji na doseganju ciljev posameznih kategorij.
3. Kategorija brez današnjih aktivnosti prikazuje 0 %; če kategorij s cilji še ni, zaslon prikaže prazno stanje z možnostjo nastavitve kategorij in ciljev.
4. Po spremembi potrjenih aktivnosti ali dnevnega cilja se napredek posodobi; gumb ob diagramu odpre seznam kategorij z njihovimi cilji in napredkom.

## Upoštevanje INVEST

- **Independent – neodvisne:** vsaka zgodba ima svoj uporabniški rezultat; potrebne kraje, zgodovino ali delitve je mogoče pripraviti kot testne podatke. Deljenje in mentorjev pregled sta ločena cilja z ločenimi merili.
- **Negotiable – dogovorljive:** določeni so cilj, pravice in opazni rezultati; razporeditev zaslonov, ponudnik AI in notranja izvedba pravil ostajajo predmet dogovora.
- **Valuable – vredne:** vsaka zgodba navede korist za uporabnika, mentorja ali skrbnika in jo poveže z namenom osebnega dnevnika.
- **Estimable – ocenljive:** opredeljeni so vhodni podatki, rezultat in ključni neuspešni primeri; zunanje integracije so ločene od MVP.
- **Small – majhne:** posamezna zgodba pokriva omejen cilj; upravljanje računov je omejeno na dodelitev mentorja, koledarske integracije pa so ločene po smeri prenosa. Velikost je treba ob oceni izvedbe potrditi in zgodbo po potrebi razdeliti.
- **Testable – preverljive:** vsaka zgodba vsebuje štiri konkretna merila; časovni primeri, zavrnitve zahtev in posledice potrditve ali preklica omogočajo preverjanje brez subjektivnih ocen prijaznosti vmesnika.
