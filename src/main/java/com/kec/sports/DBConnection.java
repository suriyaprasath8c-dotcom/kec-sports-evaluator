package com.kec.sports;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/*
 * Database Connection Manager - KEC Sports Evaluator
 * 
 * Handles JDBC connection pooling / creation for MySQL.
 * Configured to connect locally during dev, or remotely to OCI VM 2 (MySQL Host) in production.
 *
 * Student 1: Vaisnav K (22CSR180) - App Host
 * Student 2: Database Admin - OCI DB Host
 */
public class DBConnection {

    private static DBConnection instance;
    private String dbUrl;
    private String dbUsername;
    private String dbPassword;

    private DBConnection() {
        loadConfig();
        try {
            // Register MySQL 8 JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("WARN: MySQL driver not found on classpath: " + e.getMessage());
        }
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    // Load connection details from application.properties or environment vars (OCI VM deployment)
    private void loadConfig() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not load application.properties, using fallback defaults.");
        }

        // Check environment variables first (useful for OCI VM deployment without modifying source code)
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

    // Return a fresh connection for concurrent worker threads
    public Connection createConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }

    // Quick static helper
    public static Connection getConnection() throws SQLException {
        return getInstance().createConnection();
    }

    // Test ping to check if database is reachable
    public boolean testConnection() {
        try (Connection conn = createConnection()) {
            return conn.isValid(3);
        } catch (SQLException e) {
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
