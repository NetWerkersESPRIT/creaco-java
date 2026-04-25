package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//Singleton Design Pattern
public class MyConnection {

    private final String URL = "jdbc:mysql://localhost:3306/creaco";
    private final String USER = "root";
    private final String PASS = "";
    private Connection connection;
    private static MyConnection instance;

    private MyConnection(){
        try {
            connection = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("✅ Connection established successfully to: " + URL);
        } catch (SQLException e) {
            System.err.println("❌ CRITICAL: Database connection failed!");
            System.err.println("URL: " + URL);
            System.err.println("Error: " + e.getMessage());
            // Throwing an exception here prevents the app from continuing with a null connection
            throw new RuntimeException("Could not connect to database. Please ensure MySQL is running and the database 'creaco' exists.", e);
        }
    }

    public static MyConnection getInstance(){
        if(instance == null)
            instance = new MyConnection();
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
