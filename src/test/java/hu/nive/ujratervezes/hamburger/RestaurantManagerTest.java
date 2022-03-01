package hu.nive.ujratervezes.hamburger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
