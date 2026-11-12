# Besedilo v osnutek (US-05)

[#21](https://github.com/MikeDQG/femto-manage/issues/21) in [#22](https://github.com/MikeDQG/femto-manage/issues/22) uvajata Koledar → Dodaj aktivnost → Besedilni vnos. Odločitev o ponudniku določa [ADR 011](adr/011-ADR-besedilni-osnutki-openai.md).

V korenski `.env` nastavi `OPENAI_API_KEY` in `OPENAI_ACTIVITY_MODEL` z modelom, ki podpira Structured Outputs, nato ponovno ustvari backend z `docker compose up --build`. Ključ ni del frontenda ali Git zgodovine. Brez obeh nastavitev pretvorba vrne 503, ročni vnos pa deluje. `app.ai.url` je privzeto `https://api.openai.com/v1/responses`; drugačen URL je namenjen nadzorovanim testom adapterja.

`POST /api/activities/text-draft` zahteva prijavo in CSRF ter sprejme `text` (1–4000 znakov), `referenceDate` in IANA `zone`. »Danes« se razreši glede na odjemalčev lokalni datum. Odgovor vsebuje uredljiva polja osnutka, `fields` z razlogi dopolnitve ter `placeCandidates`. Kategorija je lahko samo aktivna, kraj pa samo lastni neizbrisani kraj z ustreznim imenom. Več zadetkov zahteva izbiro. Neveljavni intervali ter dvoumni ali neobstoječi lokalni časi ob premiku ure ostanejo za dopolnitev.

Zahtevek ne shranjuje osnutka ali aktivnosti. Potrditev prek `/api/activities` ponovno preveri vsa polja in prekrivanje ter shrani vir `AI`. Izvorno besedilo ostane v stanju obrazca, tudi po napaki ali prehodu na ročni vnos; ni samodejno kopirano v opis ali deljeni povzetek.

Preverjanje: celotni Maven testi, `TextActivityDraftTests` za poslovne meje, `OpenAiActivityTextExtractorTests` z lokalno HTTP storitvijo za zunanjo pogodbo, build/lint ter `text-activity.spec.ts` na obeh širinah. Brskalniški uspešni primer uporablja nadzorovan odgovor API-ja za osnutek; potrditev in preverjanje nedostopnega AI tečeta proti dejanskemu backendu. Živ klic OpenAI zahteva uporabnikov ključ in model ter ni del samodejnih testov.
