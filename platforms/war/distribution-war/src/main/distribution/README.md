
Axel
====

Detta är en färdigpaketerad distribution av Axel baserad på Apache Tomcat[1].


Konfigurera
------------------
Default-konfigurationen är troligen inte användbar utan måste ändras.

Om vi antar att denna distribution installerades i `/opt/axel-1.0` vilket vi kallar `${axel.home}`,
konfigurationsfilerna ligger då i `${axel.home}/etc`.


### Konfigurationsfiler

* `shs-cmdline.properties`

    Inställningar för SHS kommandoradsklienten. (<axel>/bin/shs)


Köra
----------------

För att köra shs kommandorads-klient:

    $ ${axel.home}/bin/shs request -p <productid> -t <till> -f <från> -in <fil>

För att få hjälp:

    $ ${axel.home}/bin/shs --help

