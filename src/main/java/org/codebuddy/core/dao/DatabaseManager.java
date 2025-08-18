package org.codebuddy.core.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Properties;

public class DatabaseManager {
    private static final String CONFIG_FILE = "/config.properties";
    private static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/codebuddy_db";
    private static final String DEFAULT_DB_USER = "root";
    private static final String DEFAULT_DB_PASSWORD = "123321";
    
    private static Connection connection;
    private static boolean initialized = false;

    private DatabaseManager() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Properties props = loadDatabaseProperties();
                String dbUrl = props.getProperty("db.url", DEFAULT_DB_URL);
                String dbUser = props.getProperty("db.user", DEFAULT_DB_USER);
                String dbPassword = props.getProperty("db.password", DEFAULT_DB_PASSWORD);
                
                // Ensure database exists
                ensureDatabaseExists(dbUrl, dbUser, dbPassword);
                
                // Connect to the database
                connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                connection.setAutoCommit(true);
                
            } catch (IOException e) {
                throw new SQLException("Failed to load database config: " + e.getMessage(), e);
            }
        }
        return connection;
    }

    private static Properties loadDatabaseProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream input = DatabaseManager.class.getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                props.load(input);
            }
        }
        return props;
    }

    private static void ensureDatabaseExists(String dbUrl, String dbUser, String dbPassword) throws SQLException {
        // Extract database name from URL
        String dbName = dbUrl.substring(dbUrl.lastIndexOf("/") + 1);
        String serverUrl = dbUrl.substring(0, dbUrl.lastIndexOf("/"));
        
        // Connect to MySQL server (without specifying database)
        try (Connection serverConn = DriverManager.getConnection(serverUrl, dbUser, dbPassword);
             Statement stmt = serverConn.createStatement()) {
            
            // Create database if it doesn't exist
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + dbName + "`");
            System.out.println("Database '" + dbName + "' is ready");
        }
    }

    public static void initializeDatabase() throws SQLException {
        if (initialized) {
            return;
        }
        
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            
            // Create users table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) NOT NULL UNIQUE, " +
                    "password_hash VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL UNIQUE, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // Create problems table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS problems (" +
                    "problem_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "platform VARCHAR(50) NOT NULL, " +
                    "difficulty VARCHAR(20) NOT NULL, " +
                    "time_taken_min INT NOT NULL, " +
                    "solved_date DATETIME NOT NULL, " +
                    "notes TEXT, " +
                    "link TEXT, " +
                    "user_id INT NOT NULL, " +
                    "UNIQUE KEY unique_problem (name, platform, difficulty, user_id), " +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                    ")");

            // Add default admin user if not present
            try {
                stmt.executeUpdate("INSERT INTO users (username, password_hash, email) VALUES (" +
                        "'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'admin@codebuddy.com')");
                System.out.println("Default admin user created: admin / admin123");
            } catch (SQLException e) {
                // User already exists, ignore
                System.out.println("Admin user already exists");
            }

            // Add sample problems for admin user if not present
            try {
                stmt.executeUpdate("INSERT INTO problems (name, platform, difficulty, time_taken_min, solved_date, notes, link, user_id) VALUES " +
                        "('Two Sum', 'LEETCODE', 'EASY', 15, NOW(), 'Classic hashmap problem', 'https://leetcode.com/problems/two-sum/', 1)");
                stmt.executeUpdate("INSERT INTO problems (name, platform, difficulty, time_taken_min, solved_date, notes, link, user_id) VALUES " +
                        "('Median of Two Sorted Arrays', 'LEETCODE', 'HARD', 60, NOW(), 'Binary search required', 'https://leetcode.com/problems/median-of-two-sorted-arrays/', 1)");
                stmt.executeUpdate("INSERT INTO problems (name, platform, difficulty, time_taken_min, solved_date, notes, link, user_id) VALUES " +
                        "('Chef and Strings', 'CODECHEF', 'MEDIUM', 25, NOW(), 'String manipulation', 'https://www.codechef.com/problems/STRINGS', 1)");
                System.out.println("Sample problems added for admin user");
            } catch (SQLException e) {
                // Problems already exist, ignore
                System.out.println("Sample problems already exist");
            }

            initialized = true;
            System.out.println("Database initialized successfully");
            
        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            throw e;
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                initialized = false;
            } catch (SQLException e) {
                System.err.println("Failed to close connection: " + e.getMessage());
            }
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }
}