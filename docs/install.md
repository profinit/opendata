# Nastavení databáze

Spojení s databází je definováno v main/resources/META-INF/persistence.xml, které se pro zjištění uživatelského jména a hesla doplňuje o informace obsažené v pom.xml. Jsou to properties jménem database.connection.username a database.connection.password a dají se definovat pro každý profil zvlášť.

## Vytvoření databáze
Databázi lze nově vytvořit použitím následující sekvence skriptů:

+ create.sql, vytvoří schéma
+ constants.sql, naplní enumerační tabulky konstantami
+ data.sql, přidá ministerstva, datové zdroje a ty instance, které není možné vytvořit automaticky

# Classpath

Sestavený jar hledá knihovny v adresáři lib/ na classpath, ideálně ve stejném adresáři jako je sám.

# Unit testy

Unit testy vyžadují připojení na testovací databázi, která by měla být na začátku prázdná. Používají spojení definované v test/resources/META-INF/persistence.xml a relevantní properties v pom.xml jsou test.database.connection.username a test.database.connection.password.

Unit testy taky vyžadují připojení k internetu, protože se pokouší stahovat metadata o datových instancích. Testovací soubory samotné jsou ale uložené lokálně v test/resources

# Použití aplikace

Jsou-li v databázi vloženy všechny relevantní datové zdroje a instance, stačí jenom spustit aplikační jar a aplikace se o všechno ostatní postará.
