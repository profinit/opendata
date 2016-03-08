# Jak fungují datové zdroje a datové instance

Datový zdroj (DataSource) představuje typ dokumentu vydávaný jednou institucí. Všechny dokumenty z daného DataSource by měly poskytovat stejný druh záznamů (faktury, smlouvy, objednávky, nebo jiné platby), měly by mít stejný formát a metadata o nich by měla být aktualizována podobným způsobem. Nic z toho ale není povinné. DataSource je v základu jenom objekt v databázi, na který je volán Handler, který si lze nadefinovat libovolně.

Datová instance je konkrétní datový soubor, který je (nebo bylo) možné stáhnout ze serveru publikující instituce. Má URL, formát a má přiřazený mapovací XML soubor, který specifikuje, jak se mají přelít obsažená data do databáze.

Při spuštění aplikace se načtou všechny aktivní datové zdroje, u každého je instanciován Handler a spuštěn. Většina datových zdrojů bude nejspíš zpracovávána nějakým potomkem třídy GenericDataSourceHandler. Ten nejdřív zkontroluje, jestli je potřeba generovat nové datové instance podle periodicity zdroje a data posledního zpracování. Samotná logika generování instancí není touto třídou implementována. Například MFCRHandler volá REST API, JusticeHandler kontroluje, zda na dané adrese existuje XLS dokument a ostatní datové zdroje nedělají nic, protože předpokládají manuální vložení datových instancí do databáze.

Jakmile je generování datových instancí u konce, GenericDataSourceHandler sesbírá ty instance, které by měly být staženy a zpracovány, opět podle jejich periodicity a data posledního zpracování. Všechny DI označené pro zpracování jsou pak po jednom předávány TransformDriveru, který se postará o celou extrakci.

Datové instance mohou být aperiodické, což způsobí, že budou zpracovány právě jednou. Pokud nejsou, mohou být ještě označeny jako inkrementální. Inkrementální instance jsou zpracovávány od posledního řádku, který byl přečten při minulé extrakci. Většina datových instancí inkrementální není, protože nelze zaručit, že nové záznamy přibývají na konec. Datové instance, které nejsou inkrementální, nemají v databázi vyplněný sloupec last_processed_row.

# Jak funguje zpracování dokumentů

Jako první je soubor stažen do InputStreamu. Poté je načteno mapování dané datové instance, které musí být na classpath. Formát mapování udává soubor datasource_mapping.xsd. Mapování je načteno do JAXB třídy Mapping. Pomocné datové typy vygenerované z XSD souboru mají trochu nešikovné názvy, proto je potřeba prostudovat, jak vypadají soubory samotné.

## Mapování a postup extrakce

Na nejvyšší úrovni vypadá XML soubor takto:

<mapping>
  <mappedSheet number="0">...</mappedSheet> <!-- povinne alespon jeden -->
  <mappedSheet number="1">...</mappedSheet> <!-- misto "number" lze specifikovat "name" -->
  ...
  <propertySet name="set1">...</propertySet> <!-- nepovinne -->
  <propertySet name="set2">...</propertySet>
  ...
</mapping>

Každý mappedSheet odpovídá jednomu listu. Element propertySet se používá k definici několika properties, které jsou sdíleny napříč více listy. Obsah propertySet je jeden nebo více elementů <property> (viz. níže).

Definice mappedSheet:

<mappedSheet number="0">
  <headerRow>1</headerRow> <!-- Řádek s popisky sloupců, indexováno od 0. -->
  <filter className="xxx">...</filter>
  <retriever className="xxx">...</retriever>

  <property>...</property>
  <propertySet ref="set1"/> <!-- property a propertySet lze libovolně míchat -->
  ...
</mappedSheet>

HeaderRow specifikuje, kde je možné najít názvy sloupců. Extrakce dat pak začne na prvním neprázdném řádku hned pod headerRow. Všechny další elementy představují TransformComponenty, které jsou zavolány jedna po druhé pro každý neprázdný řádek zdrojového souboru. Každý sheet může definovat maximálně jeden filtr, jeden retriever a neomezené množství properties (nebo odkazů na propertySet, obojí je ekvivalentní).

Filter, retriever i property, která nenastavuje fixní hodnotu, mají uvnitř elementu jeden nebo více elementů sourceFileColumn (viz. níže), které specifikují, jaké sloupce jsou pro jejich činnost relevantní.

Filter umožňuje rozhodnout, zda vůbec daný řádek zpracovávat. Komponenta s daným className implementující SourceRowRetriever vrací false, pokud se má řádek úplně přeskočit.

Retriever má za úkol prohledat již uložené záznamy a zabránit duplicitám. Umožňuje taky zpracování záznamů rozpadlých na více než jeden řádek. Komponenta implementující RecordRetriever vrací buď null, pokud má být vytvořen nový objekt typu Record, nebo vrací již uložený Record, který odpovídá hodnotám na daném řádku.

Element property může mít jeden ze dvou formátů:

<property name="currency" onlyNewRecords="true" value="CZK"/>

Takto je definována fixní hodnota určitého atributu pro všechny záznamy, které vzejdou z daného mapování. V poli value může být jakýkoli literál nebo hodnota enum - aplikace se pokusí provést automatické přetypování podle deklarovaného typu jmenovaného atributu. OnlyNewRecords="true" značí, že pro dříve uložené záznamy má být tato property úplně přeskočena.

<property name="amountCzk" onlyNewRecords="true" converter="xxx">
  <sourceFileColumn originalName="abc" argumentName="def"/> <!-- Misto "originalName" lze uvest "number", indexovane od 0 -->
  ...
</property>

Takto je definován atribut, který má být nastaven podle hodnot přečtených ze zdrojového řádku. Converter je jméno třídy implementující RecordPropertyConverter, jejíž jediná metoda přijímá aktuální Record, mapu zdrojových hodnot a jméno atributu. Mapa zdrojových hodnot je sestavena tak, že klíče jsou jednotlivé argumentName a hodnoty jsou buňky (Cell objekty) zdrojového souboru, které odpovídají sloupcům s hlavičkou "originalName". Pokud v dokumentu sloupec s daným originalName není nalezen, je přeskočen. RecordPropertyConverter zdrojové hodnoty přečte a nastaví atributy objektu Record.

Jakmile jsou přečteny všechny properties, Record je uložen a pokračuje se dalším řádkem až do konce listu.
