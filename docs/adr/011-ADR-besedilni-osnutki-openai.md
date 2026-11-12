# 011 – Besedilni osnutki aktivnosti z OpenAI

Dopolnjuje: [006 – OpenAI dnevni povzetki](006-ADR-openai-dnevni-povzetki.md). Ta zapis določa ločeno uporabo AI za [#21](https://github.com/MikeDQG/femto-manage/issues/21) in [#22](https://github.com/MikeDQG/femto-manage/issues/22), brez uvedbe dnevnih besedilnih povzetkov.

## Kontekst in opredelitev problema

Besedilo uporabnika mora postati preverljiv, nepopoln osnutek aktivnosti. Potrebujemo jezikovno razumevanje, varovanje ključa in ločitev zunanje storitve od lastništva ter potrjevanja zapisov.

## Obravnavane možnosti

* Razčlenjevanje slovenskega besedila z lokalnimi pravili.
* OpenAI Responses API s strukturiranim izhodom in strežniškim preverjanjem.
* Lasten gostovan jezikovni model.

## Sprejeta odločitev

Izbrana možnost: »OpenAI Responses API«, ker ne zahteva gostovanja modela in podpira strukturirane rezultate. HTTP adapter izvaja vmesnik `ActivityTextExtractor`; poslovna storitev preveri kategorijo, kraj in lokalne čase. Model ter ključ sta izrecna strežniška konfiguracija. Vhod vsebuje samo uporabnikovo besedilo, referenčni datum, časovni pas in aktivne kategorije; ne pošilja zgodovine, krajev s koordinatami ali refleksije. `store` je `false`.

Odgovor uporablja [Structured Outputs](https://developers.openai.com/api/docs/guides/structured-outputs) z obveznimi, lahko praznimi polji. Zavrnitev, nedokončan odgovor, napačna oblika in nedosegljivost vrnejo napako. Poslovne dvoumnosti zahtevajo uporabnikovo dopolnitev; osnutka ne shranjujemo. Potrditev uporabi isti validirani API kot ročni vnos, z virom `AI`.

### Posledice

* Pozitivno, ker ključ in preverjanje lastništva ostaneta na strežniku.
* Pozitivno, ker je adapter mogoče preveriti z nadzorovanim HTTP odzivom brez stroškov AI.
* Negativno, ker je pretvorba odvisna od zunanje storitve in kakovosti njenega izluščanja; uporabnik mora rezultat pregledati.
* Negativno, ker se opis pošlje ponudniku, na kar uporabnika pred pripravo osnutka opozori vmesnik.
