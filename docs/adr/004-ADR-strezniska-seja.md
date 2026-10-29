# 004 – Avtentikacija s strežniško sejo

## Kontekst in opredelitev problema

Uporabniki potrebujejo prijavo in dostop samo do podatkov, za katere imajo dovoljenje. Frontend in REST API sta ob objavi predvidena pod istim izvorom, sistem pa ima enoten backend. Potrebujemo preprost način vzdrževanja prijave, odjave in preverjanja pravic brez dodatne infrastrukture za upravljanje žetonov.

## Obravnavane možnosti

* Strežniška seja z identifikatorjem v piškotku.
* Dostopni in osvežitveni žetoni, na primer JWT.
* Zunanji ponudnik identitete prek OpenID Connect.

## Sprejeta odločitev

Izbrana možnost: »Strežniška seja«, ker ustreza spletni aplikaciji pod istim izvorom in jo podpira Spring Security. Backend po uspešni prijavi vzdržuje stanje seje, brskalnik pa pošilja njen identifikator v piškotku HttpOnly, ob objavi tudi Secure in z ustrezno nastavitvijo SameSite. Zahteve, ki spreminjajo podatke, bodo zaščitene pred CSRF. Backend pri zahtevah preverja vloge, lastništvo in dovoljenja; odjava razveljavi sejo. Gesla bodo shranjena kot zgoščene vrednosti z BCrypt.

### Posledice

* Pozitivno, ker sta prijava in odjava preprosti ter podprti z obstoječimi mehanizmi Spring Security.
* Pozitivno, ker identifikator seje v piškotku HttpOnly ni neposredno dostopen kodi JavaScript v brskalniku.
* Negativno, ker backend hrani stanje sej; pri več instancah bo treba urediti skupno hrambo sej ali usmerjanje zahtev na isto instanco.
* Negativno, ker piškotki zahtevajo ustrezno zaščito CSRF in pravilno konfiguracijo ob objavi.
* Negativno, ker lahko ponovni zagon ob hrambi sej samo v pomnilniku zahteva ponovno prijavo uporabnikov.
