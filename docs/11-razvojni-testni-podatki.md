# Razvojni testni podatki (#33)

Ukaz je namenjen samo razvojni bazi, ki jo uporablja aplikacija. Pred zagonom preveri, da korenski `.env` kaže na pravo razvojno instanco. Račun mora že obstajati; ukaz ne ustvarja uporabnikov in ne obravnava gesel. Kategorije `WORK`, `MOVEMENT`, `GROWTH`, `RELATIONSHIPS`, `REST`, `CREATIVITY` morajo biti aktivne.

V PowerShellu iz korena repozitorija:

```powershell
$env:SEED_USER_EMAIL = 'a@a.a'
$env:SEED_REFERENCE_DATE = (Get-Date).ToString('yyyy-MM-dd')
$env:SEED_CONFIRM_DEVELOPMENT_DATABASE = 'true'
docker compose run --rm -e SEED_USER_EMAIL -e SEED_REFERENCE_DATE -e SEED_CONFIRM_DEVELOPMENT_DATABASE backend --spring.profiles.active=dev --server.port=0 --app.development-data.enabled=true
```

Referenčni datum je lokalni datum za `Europe/Ljubljana`. Za ponovitev ali odstranitev uporabi isti `SEED_REFERENCE_DATE`. Če ga ne nastaviš, ukaz vzame današnji datum v tej coni. Izpiše število na novo ustvarjenih oziroma odstranjenih krajev in aktivnosti. Ob ponovitvi za isti račun in datum izpiše `0, 0` in ne prepiše ročnih sprememb. Izvedba zahteva profil `dev`, izrecno omogočanje ukaza in potrditev razvojne baze. Ob običajnem zagonu ali migraciji se ne izvede.

Za pregled brez sprememb nastavi `SEED_ACTION=status` in ponovi zgornji ukaz z dodatnim `-e SEED_ACTION` pred `backend`. Izpis »Vidno v uporabnikovih API poizvedbah« šteje neizbrisane kraje in potrjene aktivnosti, ki pripadajo izbranemu računu.

Ustvari tri kraje: Dom, FERI in Mestni park. Ustvari devet potrjenih ročnih aktivnosti med referenčnim dnevom in prejšnjima dvema dnevoma. Zajete so vse navedene kategorije, več aktivnosti istega dne, prekrivanje med delom in učenjem, počitek čez polnoč, aktivnost brez kraja in projektno delo na FERI ob 9.00 vse tri dni. Zapisi imajo stabilne UUID-je, določene iz uporabnika, datuma in oznake zapisa.

Odstranitev samo zapisov tega ukaza za isti račun in datum:

```powershell
$env:SEED_ACTION = 'remove'
docker compose run --rm -e SEED_USER_EMAIL -e SEED_REFERENCE_DATE -e SEED_CONFIRM_DEVELOPMENT_DATABASE -e SEED_ACTION backend --spring.profiles.active=dev --server.port=0 --app.development-data.enabled=true
Remove-Item Env:SEED_ACTION
```

Odstranitev najprej izbriše devet aktivnosti po njihovih UUID-jih in lastniku, nato tri kraje po njihovih UUID-jih in lastniku, če jih ne uporabljajo druge aktivnosti. Ročno dodanih zapisov ne izbriše. Ročno spremenjeni testni zapisi še vedno nosijo svoje stabilne UUID-je in jih odstranitev izbriše.
