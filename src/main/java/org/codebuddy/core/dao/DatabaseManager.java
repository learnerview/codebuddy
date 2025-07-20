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
    private static Connection connection;

    private DatabaseManager() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            Properties props = new Properties();
            try (InputStream input = DatabaseManager.class.getResourceAsStream("/config.properties")) {
                props.load(input);
                String dbUrl = props.getProperty("db.url");
                String dbUser = props.getProperty("db.user");
                String dbPassword = props.getProperty("db.password");
                // Extract DB name from URL
                String dbName = dbUrl.substring(dbUrl.lastIndexOf("/") + 1);
                String serverUrl = dbUrl.substring(0, dbUrl.lastIndexOf("/"));
                // Connect to server (no DB) and create DB if not exists
                try (Connection serverConn = DriverManager.getConnection(serverUrl, dbUser, dbPassword); Statement stmt = serverConn.createStatement()) {
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
                }
                // Now connect to the actual DB
                connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            } catch (IOException e) {
                throw new SQLException("Failed to load database config", e);
            }
        }
        return connection;
    }

    public static void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Create users table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) NOT NULL UNIQUE, " +
                    "password_hash VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL UNIQUE, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // Create problems table with user_id foreign key if not exists
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS problems (" +
                    "problem_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "platform VARCHAR(255) NOT NULL, " +
                    "difficulty VARCHAR(255) NOT NULL, " +
                    "time_taken_min INT NOT NULL, " +
                    "solved_date DATETIME NOT NULL, " +
                    "notes TEXT, " +
                    "link TEXT, " +
                    "user_id INT, " +
                    "UNIQUE KEY unique_problem (name, platform, difficulty, user_id), " +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                    ")");

            // Add default admin user if not present
            stmt.executeUpdate("INSERT IGNORE INTO users (username, password_hash, email) VALUES (" +
                    "'admin', '$2a$10$7QJ8QwQwQwQwQwQwQwQwQOQwQwQwQwQwQwQwQwQwQwQwQwQwQwQw', 'admin@example.com')");
            // Add default problem for admin if not present
            stmt.executeUpdate("INSERT IGNORE INTO problems (name, platform, difficulty, time_taken_min, solved_date, notes, link, user_id) VALUES (" +
                    "'Sample Problem', 'LeetCode', 'Easy', 10, NOW(), 'This is a sample problem.', '', 1)");

            // Add 'user_id' column if it does not exist (for existing tables)
            try {
                stmt.executeUpdate("ALTER TABLE problems ADD COLUMN user_id INT");
            } catch (SQLException e) {
                // Ignore error if column already exists
            }
            // Add foreign key if not exists
            try {
                stmt.executeUpdate("ALTER TABLE problems ADD CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE");
            } catch (SQLException e) {
                // Ignore error if constraint already exists
            }
            // Add 'link' column if it does not exist (for existing tables)
            try {
                stmt.executeUpdate("ALTER TABLE problems ADD COLUMN link TEXT");
            } catch (SQLException e) {
                // Ignore error if column already exists
            }
            // Upsert sample data (insert if not present, update if present)
            // Only insert sample data for user_id = NULL (for demo/migration)
            stmt.executeUpdate("INSERT INTO problems (name, platform, difficulty, time_taken_min, solved_date, notes, link, user_id) VALUES " +
                    "('Two Sum', 'LeetCode', 'Easy', 15, '2024-06-01', 'Classic hashmap problem', '', NULL) " +
                    "ON DUPLICATE KEY UPDATE time_taken_min=VALUES(time_taken_min), solved_date=VALUES(solved_date), notes=VALUES(notes), link=VALUES(link)");
            stmt.executeUpdate("INSERT INTO problems (name, platform, difficulty, time_taken_min, solved_date, notes, link, user_id) VALUES " +
                    "('Median of Two Sorted Arrays', 'LeetCode', 'Hard', 60, '2024-06-02', 'Binary search required', '', NULL) " +
                    "ON DUPLICATE KEY UPDATE time_taken_min=VALUES(time_taken_min), solved_date=VALUES(solved_date), notes=VALUES(notes), link=VALUES(link)");
            stmt.executeUpdate("INSERT INTO problems (name, platform, difficulty, time_taken_min, solved_date, notes, link, user_id) VALUES " +
                    "('Chef and Strings', 'CodeChef', 'Medium', 25, '2024-06-03', 'String manipulation', '', NULL) " +
                    "ON DUPLICATE KEY UPDATE time_taken_min=VALUES(time_taken_min), solved_date=VALUES(solved_date), notes=VALUES(notes), link=VALUES(link)");
            stmt.executeUpdate("INSERT INTO problems (name, platform, difficulty, time_taken_min, solved_date, notes, link, user_id) VALUES " +
                    "('Counting Valleys', 'HackerRank', 'Easy', 10, '2024-06-04', 'Simple counting', '', NULL) " +
                    "ON DUPLICATE KEY UPDATE time_taken_min=VALUES(time_taken_min), solved_date=VALUES(solved_date), notes=VALUES(notes), link=VALUES(link)");
            stmt.executeUpdate("INSERT INTO problems (name, platform, difficulty, time_taken_min, solved_date, notes, link, user_id) VALUES " +
                    "('Graph Paths', 'Other', 'Medium', 40, '2024-06-05', 'Graph traversal', '', NULL) " +
                    "ON DUPLICATE KEY UPDATE time_taken_min=VALUES(time_taken_min), solved_date=VALUES(solved_date), notes=VALUES(notes), link=VALUES(link)");
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Failed to close connection: " + e.getMessage());
            }
        }
    }
}