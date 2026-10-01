package com.smartcanteen.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class to manage MySQL JDBC database connections and resource cleanup.
 * Shared across Aswani, Shreya, and Neeraja modules.
 */
public class DatabaseUtil {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/SmartCanteen?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";

    static {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Ensure mysql-connector-java is in classpath.");
        }
    }

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private DatabaseUtil() {
    }

    /**
     * Opens and returns a new JDBC connection to the database.
     *
     * @return active java.sql.Connection
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        String url = System.getProperty("db.url", DEFAULT_URL);
        String user = System.getProperty("db.user", DEFAULT_USER);
        String password = System.getProperty("db.password", "");
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Closes an active Connection safely without throwing checked exceptions.
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    /**
     * Closes an active Statement safely.
     */
    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("Error closing statement: " + e.getMessage());
            }
        }
    }

    /**
     * Closes an active ResultSet safely.
     */
    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("Error closing result set: " + e.getMessage());
            }
        }
    }

    /**
     * Convenience method to test if the MySQL database is reachable.
     *
     * @return true if connection succeeds, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}
