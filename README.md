# Opendata projekt

Dokumentace:
- [Instalace](/docs/install.md)
- [Katalog dat](/docs/catalogue.md)
- [DataSource](/docs/datasource.md)

# Pro vývojáře
- Prosím nainstalujte do svého IDE [Sonar lint](https://www.sonarlint.org/)
- Proísm používejte [sémantické FEST asserce](https://github.com/alexruiz/fest-assert-2.x/wiki/Using-fest-assertions)

# Cíle

Cílem je vytvořit snadno konfigurovatelný modul, který dokáže stahovat zveřejňované informace o hospodaření, tato data převede do strukturované podoby a uloží do relační databáze.

Očekávané zdroje dat jsou ve formátu csv, txt, nebo xls/xlsx, cílová databáze je PostgreSQL.
V případě úspěšného projektu počítáme se zveřejněním získaných dat v konsolidované podobě na veřejném webu.

## Očekávané zdroje jsou:

Ministerstva - faktury, smlouvy, objednavky
Otevřená data Ministerstva financí ČR - http://data.mfcr.cz/
Otevřená data Ministerstva vnitra ČR - zde (NKOD)
Otevřená data Ministerstva spravedlnosti ČR - http://data.justice.cz/ (smlouvy a faktury)
Otevřená data Ministerstva životního prostředí ČR - http://www.mzp.cz/cz/otevrena_data (smlouvy a faktury)
Otevřená data Ministerstva kultury ČR - zde (smlouvy a faktury)

Data z Ministerstva pro místní rozvoj ČR - zde
Data z Informačního systému o veřejných zakázkách - zde (MMR)

# Požadované výstupy

Výstupem bude knihovna v jazyce JAVA, dokumentace k modulu.

# Doporučené zdroje

http://www.otevrenadata.cz

# Další požadavky

Co se týká rozhraní knohovny, tak by měla být rozumně strukturovaná - zřejmě po ministerstvech / úřadech. První část by se měla starat o stáhnutí dat, druhá o naparsování, třetí o uložení. První část asi bude potřebovat nějakou tabulku s metadaty -> kdy se stáhla data a jak velký soubor byl, aby bylo možné zpracovávat i inkrementy. Ukládací část by měla být schopná snadno změnit cílovou DB - tzn. třeba použít Spring.
Cílem je, aby se data dala použít jednak na nějakou formu reportingu - tj. něco ve stylu vsechnyzakazky.cz, ale hlavně aby se daly natahovat vztahy. Děláme vizualizační nástroj SVAT (www.nfgsvat.com), ve kterém bychom to pak chtěli zobrazovat. Samozřejmě pak asi půjde najít víc zajímavých informací, ale primárně zatím takto.
