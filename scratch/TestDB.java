package test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDB {
    public static void main(String[] args) {
        String URL = "jdbc:mysql://localhost:3306/creaco";
        String USER = "root";
        String PASS = "";
        try {
            Connection connection = DriverManager.getConnection(URL, USER, PASS);
            if (connection != null) {
                System.out.println("SUCCESS: Connected to database!");
                connection.close();
            } else {
                System.out.println("FAILURE: Connection is null (should not happen without exception)");
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Connection failed: " + e.getMessage());
        }
    }
}
