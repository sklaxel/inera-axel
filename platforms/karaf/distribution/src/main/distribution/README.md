
Axel
====

Detta är en färdigpaketerad distribution av Axel baserad på Apache Karaf[1].


Konfigurera
------------------
Default-konfigurationen är troligen inte användbar utan måste ändras.

Om vi antar att denna distribution installerades i `/opt/axel-1.0` vilket vi kallar `${axel.home}`,
konfigurationsfilerna ligger då i `${axel.home}/etc`.


### Konfigurationsfiler

* `se.inera.axel.mongodb.cfg`

    Innehåller inställningar för MongoDB

* `se.inera.axel.shs.broker.cfg`

    Innehåller inställningar för shs-brokern

    - NodeId
    - SHS URL där meddelanden tas emot
    - SHS certifikat-inställningar.

* `se.inera.axel.shs.directory.cfg`

    Innehåller inställningar för SHS Ldap Directory

    Här måste man lägga in ldap-url och kontonamn till den SHS LDAP katalog man vill använda.
    Se nedan för en beskrivning av hur man kan förbereda sin egen LDAP server för SHS.

* `se.inera.axel.shs.routing.cfg`

    Innehåller organisationsnummer för denna SHS-aktör.

* `se.inera.axel.riv.shs.cfg`

    Här ligger inställningar för RIV/SHS-bryggan.

    - HTTP endpoint för mottagning av RIV-meddelanden.
    - RIV certifikat, som behövs för kommunikation med NTP.

* `shs-cmdline.properties`

    Inställningar för SHS kommandoradsklienten. (<axel>/bin/shs)


Se i övrigt Karaf-manualen för resterande konfigurationsfiler.


### Exampel-komponenter

I katalogen `${axel.home}/deploy` ligger

* `shs-ping.xml`

    Ett exempel på en synkron SHS-plugin som får meddelanden med det produktid som är konfigurerat i filen.

* `agreement-product-synchronizer.xml`

    En komponent som importerar och exporterar shs agreements och produkt-filer till data-katalogen.
    En agreement-fil kan alltså läggas i /opt/axel-1.0/data/agreements så läggs den automatiskt in i databasen.


Köra
----------------
Om man har installerat och konfigurerat Axel enligt ovan så är det ganska enkelt att starta servern.

För att starta servern i konsollen:

    $ ${axel.home}/bin/axel

För att starta servern i bakrgrunden:

    $ ${axel.home}/bin/start


För att köra shs kommandorads-klient:

    $ ${axel.home}/bin/shs request -p <productid> -t <till> -f <från> -in <fil>

För att få hjälp:

    $ ${axel.home}/bin/shs --help



Konfigurera OpenLDAP för SHS
-------------------------------

Ldap-servern måste prepareras med

* En rot för SHS, L=SHS
* SHS-schemat måste läggas in.
* Samt importera en liten struktur från en LDIF-fil.

Schema och exempel ldif-fil ligger i `${axel.home}/etc/ldap`.





[1] http://karaf.apache.org
