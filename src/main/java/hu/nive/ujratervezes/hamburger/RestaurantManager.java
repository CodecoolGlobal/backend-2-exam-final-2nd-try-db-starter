package hu.nive.ujratervezes.hamburger;

public class RestaurantManager {

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public RestaurantManager(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }
}
