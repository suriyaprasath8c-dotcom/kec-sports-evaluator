package com.kec.sports;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton database connection manager for KEC Sports Evaluator.
 * Reads configuration from application.properties on the classpath,
 * falling back to environment variables or safe defaults.
 */
public class DBConnection {

    private static DBConnection instance;
    private String dbUrl;
    private String dbUsername;
    private String dbPassword;

    private DBConnection() {
        loadConfiguration();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("[DBConnection] MySQL JDBC Driver registered successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    private void loadConfiguration() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
                System.out.println("[DBConnection] Loaded configuration from application.properties");
            }
        } catch (Exception e) {
            System.err.println("[DBConnection] Could not read application.properties: " + e.getMessage());
        }

        this.dbUrl = System.getenv("KEC_DB_URL");
        if (this.dbUrl == null || this.dbUrl.trim().isEmpty()) {
            this.dbUrl = props.getProperty("db.url",
                    "jdbc:mysql://localhost:3306/kec_sports_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        }

        this.dbUsername = System.getenv("KEC_DB_USERNAME");
        if (this.dbUsername == null || this.dbUsername.trim().isEmpty()) {
            this.dbUsername = props.getProperty("db.username", "root");
        }

        this.dbPassword = System.getenv("KEC_DB_PASSWORD");
        if (this.dbPassword == null) {
            this.dbPassword = props.getProperty("db.password", "");
        }
    }

    /**
     * Creates and returns a new JDBC Connection.
     * Each caller gets a fresh connection to prevent multithreaded concurrency conflicts.
     * Callers must close it (e.g. using try-with-resources).
     */
    public Connection createConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }

    /**
     * Convenience static method to obtain a new connection.
     */
    public static Connection getConnection() throws SQLException {
        return getInstance().createConnection();
    }

    public boolean testConnection() {
        try (Connection conn = createConnection()) {
            boolean valid = conn.isValid(5);
            System.out.println("[DBConnection] Connection test: " + (valid ? "SUCCESS" : "FAILED"));
            return valid;
        } catch (SQLException e) {
            System.err.println("[DBConnection] Connection test FAILED: " + e.getMessage());
            return false;
        }
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUsername() {
        return dbUsername;
    }
}
