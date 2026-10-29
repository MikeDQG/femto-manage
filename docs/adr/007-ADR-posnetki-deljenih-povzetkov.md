# 007 – Deljenje ločenih posnetkov dnevnih povzetkov

## Kontekst in opredelitev problema

Aktivnosti in zasebne refleksije so privzeto dostopne samo uporabniku, ki jih zabeleži. Mentor oziroma svetovalec potrebuje omejen vpogled za pomoč uporabniku, uporabnik pa mora izbrati, kaj deli, in dostop pozneje preklicati. To je poslovno pravilo; arhitekturna odločitev določa, kako ga podpreti s podatkovnim modelom in preverjanjem dostopa.

## Obravnavane možnosti

* Neposreden mentorski dostop do izbranih izvornih zapisov dnevnika.
* Sproti izračunan pogled, ki se spreminja skupaj z dnevnikom.
* Ločen shranjen posnetek povzetka z izrecno dodeljenim in preklicljivim dostopom.

## Sprejeta odločitev

Izbrana možnost: »Ločen posnetek povzetka«, ker uporabniku omogoča predogled točno določene vsebine, ki bo na voljo mentorju. Posnetek vsebuje datum, seštevke časa po kategorijah in izbirni komentar, namenjen deljenju. Ne vsebuje lokacij, posameznih časovnih blokov, podrobnih opisov, izvornih vnosov ali zasebnih refleksij.

Posnetek se shrani ločeno od dnevnika kot DailySummary, dostop izbranega mentorja pa kot AccessGrant. Backend ob vsakem branju preveri prejemnika in veljavnost dovoljenja. Poznejše spremembe dnevnika ne spreminjajo že deljenega posnetka; za posodobljeno vsebino uporabnik ustvari in izrecno deli nov posnetek. Preklic prepreči nadaljnji dostop prek aplikacije. Morebitno besedilo, ustvarjeno z AI, mora uporabnik pred vključitvijo v komentar pregledati in izrecno nameniti deljenju.

### Posledice

* Pozitivno, ker uporabnik pred deljenjem vidi natančno vsebino, mentor pa nima neposrednega dostopa do dnevnika.
* Pozitivno, ker spremembe zasebnih zapisov ne razširijo vsebine že deljenega povzetka.
* Pozitivno, ker je dostop mogoče dodeliti in preklicati za posamezen posnetek in mentorja.
* Negativno, ker hranimo dodatne podatke in upravljamo dovoljenja ter več različic povzetkov istega dne.
* Negativno, ker lahko posnetek po popravkih dnevnika zastara; za nov pregled je potrebno ponovno deljenje.
* Negativno, ker preklic ne more odstraniti kopij ali posnetkov zaslona, ki jih je mentor že ustvaril zunaj aplikacije.
