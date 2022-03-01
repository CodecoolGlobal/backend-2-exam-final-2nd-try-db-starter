# Hamburger térkép
Egy új étteremlánc nyílt meg Magyarországon három helyszínnel és a tulajdonos téged keresett meg, hogy segíts statisztikát készíteni a hambrugereik sikerességéről.
Készítsünk egy programot amivel nyilván lehet tartani, hogy melyik étteremükben hány rendelés érkezett hamburgerekből.

# Adatbázis

Az adatbázis két táblából áll amelynek nevei `orders` és `locations`. 

A `orders` tábla következő oszlopokal rendelkezik:

- item_id SERIAL
- item_name VARCHAR(255)
- item_type VARCHAR(255)
- location_id INT

Például:

| item_id | item_name                | item_type      | location_id |
|:--------|:-------------------------|:---------------|:------------|
| 1       | Sajtburger               | HAMBURGER      | 1           | 
| 2       | Extra csípős marhaburger | HAMBURGER      | 1           | 
| 3       | Coca cola                | ITAL           | 1           | 
| 4       | Ice tea                  | ITAL           | 2           | 
| 5       | Sültkrumpli              | KÖRET          | 2           | 
| 6       | Sajtburger               | HAMBURGER      | 2           | 

A `locations` tábla következő oszlopokal rendelkezik:

- location_id SERIAL
- restaurant_name VARCHAR(255)
- city VARCHAR(255)

Például:

| location_id  | restaurant_name      | city     |
|:-------------|:---------------------|:---------|
| 1            |  MoonShine           | Pécs     |
| 2            |  Sunshine            | Budapest |
| 3            |  StarShine           | Siófok   |


# Java alkalmazás

Az `RestaurantManager` osztály konstruktora a következő paraméterekkel rendelkezik: 
- `String dbUrl` az url amin az adatbázis elérhető.
- `String dbUser` felhasználónév amivel csatlakozhatunk az adatbázishoz.
- `String dbPassword`  A `dbUser`-hez tartozó jelszó.

Készítsd el a `RestaurantManager` osztály `getNumberOfHamburgersByLocation` metódusát! Abban az esetben ha az adatbázis üres a metódus térjen vissza egy üres `CityStat` listával. Egyéb esetben a metódus térjen vissza egy `CityStat` listában az összes város nevével ABC sorrendben és a hozzájuk tartozó burger rendelések számával. 
A `CityStat` osztálynak két mezője van, egy `String city` és egy `int numberOfBurgers`. Készítsd el ezt az osztályt, definiálj neki konstruktort és ne felejtsd el felülírni a hozzá tartozó `equals` függvényt.
Egy városnév pontosan egyszer szerepeljen függetlenül attól, hogy hányszor rendeltek onnan hamburgert.


A megoldáshoz használj `PreparedStatement`-et!

# Test-ek
```java
class RestaurantManagerTest {
    private static final List<CityStat> EXPECTED_CITIES = List.of(new CityStat("Budapest", 1), new CityStat("Pécs", 2), new CityStat("Siófok", 0));

    private static final String DB_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    private RestaurantManager restaurantManager;

    @BeforeEach
    void init() throws SQLException {
        restaurantManager = new RestaurantManager(DB_URL, DB_USER, DB_PASSWORD);
        createTable();
    }

    @AfterEach
    void destruct() throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String dropTableOrders = "DROP TABLE IF EXISTS orders";
            Statement statementOrders = connection.createStatement();
            statementOrders.execute(dropTableOrders);
        }
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String dropTableLocations = "DROP TABLE IF EXISTS locations";
            Statement statementLocations = connection.createStatement();
            statementLocations.execute(dropTableLocations);
        }
    }

    private void createTable() throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String createTableOrders = "CREATE TABLE IF NOT EXISTS orders (" +
                    " item_id SERIAL," +
                    " item_name VARCHAR(255)," +
                    " item_type VARCHAR(255)," +
                    " location_id INT" +
                    ");";
            Statement statementOrders = connection.createStatement();
            statementOrders.execute(createTableOrders);
        }
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String createTableLocations = "CREATE TABLE IF NOT EXISTS locations (" +
                    "location_id SERIAL, " +
                    "restaurant_name VARCHAR(255), " +
                    "city VARCHAR(255)" +
                    ");";
            Statement statementLocations = connection.createStatement();
            statementLocations.execute(createTableLocations);
        }
    }

    @Test
    void test_getNumberOfHamburgersByLocation_anyOrder() throws SQLException {
        insertMultipleOrders();
        List<CityStat> actualCities = restaurantManager.getNumberOfHamburgersByLocation();
        for (CityStat actualCity : actualCities) {
            assertTrue(EXPECTED_CITIES.contains(actualCity));
        }
    }

    @Test
    void test_getNumberOfHamburgersByLocation_AllLocationsCheck_anyOrder() throws SQLException {
        insertMultipleOrders();
        List<CityStat> actualCities = restaurantManager.getNumberOfHamburgersByLocation();

        assertEquals(EXPECTED_CITIES.size(), actualCities.size());
        for (CityStat stat : EXPECTED_CITIES) {
            assertTrue(actualCities.contains(stat));
        }
    }

    @Test
    void test_getNumberOfHamburgersByLocation_alphabeticOrder() throws SQLException {
        insertMultipleOrders();
        List<CityStat> actualCities = restaurantManager.getNumberOfHamburgersByLocation();
        assertEquals(EXPECTED_CITIES, actualCities);
    }
    
    @Test
    void test_getNumberOfHamburgersByLocation_emptyDatabase() {
        assertEquals(List.of(), restaurantManager.getNumberOfHamburgersByLocation());
    }

    private void insertMultipleOrders() throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String insertOrders = "INSERT INTO orders (item_name, item_type, location_id) VALUES " +
                    "('Sajtburger', 'HAMBURGER',1), " +
                    "('Extra csípős marhaburger','HAMBURGER' ,1), " +
                    "('Coca cola',  'ITAL', 1), " +
                    "('Ice tea',  'ITAL', 2), " +
                    "('Sültkrumpli','KÖRET', 2), " +
                    "('Sajtburger', 'HAMBURGER', 2);";
            Statement statement = connection.createStatement();
            statement.execute(insertOrders);
        }

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String insertLocations = "INSERT INTO locations (location_id, restaurant_name, city) VALUES " +
                    "(1, 'MoonShine', 'Pécs'), " +
                    "(3, 'StarShine', 'Siófok'), " +
                    "(2, 'Sunshine', 'Budapest');";
            Statement statement = connection.createStatement();
            statement.execute(insertLocations);
        }
    }
}
```

# Pontozás

A feladat 0 pontot ér, bármely alábbi esetben:
- le sem fordul az adott projekt.
- teszteset sem fut le sikeresen
- ha a forráskód olvashatatlan, nem felel meg a konvencióknak, nem követi a clean code alapelveket
- ha kielégíti a teszteseteket, de a szöveges követelményeknek nem felel meg

Clean code-ra adható pontok: max 10

tesztekre adható pontok:

| Teszt | Pont |
--- | ----
test_getNumberOfHamburgersByLocation_emptyDatabase() | 2.5 pont
test_getNumberOfHamburgersByLocation_anyOrder() | 2.5 pont
test_getNumberOfHamburgersByLocation_AllLocationsCheck_anyOrder() | 2.5 pont
test_getNumberOfHamburgersByLocation_alphabeticOrder() | 2.5 pont
