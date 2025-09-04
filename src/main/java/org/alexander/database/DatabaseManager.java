package org.alexander.database;

import java.sql.*;

/**
 * DatabaseManager is responsible for managing the database connection.
 * @since 1.0.0
 */
public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:src/main/resources/data/data.sqlite";
    private static final FileManager fileManager = new FileManager();
    private static final TableConstructor tableConstructor = new TableConstructor();
    private static final DataConstructor dataConstructor = new DataConstructor();

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }
    /**
     * Initialises the FileManager to handle file operations.
     * This must be called before any database operations are performed.
     */
    public static void initialise() {
        fileManager.initialise();
    }

    public static void save() {
        fileManager.save();
    }

    public static boolean tableExists(String tableName) {
        String query = "SELECT name AS table_name FROM sqlite_master WHERE type='table' AND name=?";
        try (
                Connection conn = connect();
                java.sql.PreparedStatement stmt = conn.prepareStatement(query)
                ) {
            stmt.setString(1, tableName);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public static boolean entityExists(String entityName, String colName, String tableName) {
        if (!tableExists(tableName)) {
            throw new IllegalArgumentException("Table " + tableName + " does not exist");
        }
        String query = String.format("SELECT 1 FROM %s WHERE %s = ? LIMIT 1", tableName, colName);
        try (
                Connection conn = connect();
                PreparedStatement stmt = conn.prepareStatement(query)
                ) {
            stmt.setString(1, entityName);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }


    /**
     * Creates A fresh database with the required tables.
     */
    protected static void createFreshData() {
        tableConstructor.construct();
        dataConstructor.construct();

    }
}
