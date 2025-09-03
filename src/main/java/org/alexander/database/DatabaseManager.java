package org.alexander.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DatabaseManager is responsible for managing the database connection.
 * @since 1.0.0
 */
public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:src/main/resources/data/data.sqlite";

    public static Connection connect() {
        Connection conn = null;
        if (!FileManager.isInitialised()) {
            FileManager.initialise();
        }
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    /**
     * Creates A fresh database with the required tables.
     */
    protected static void createFreshData() {
        try (Connection conn = DriverManager.getConnection(URL)) {
            if (conn != null) {
                java.sql.Statement stmt = conn.createStatement();
                stmt.addBatch("""
                       CREATE TABLE IF NOT EXISTS DATA (
                           id INTEGER PRIMARY KEY,
                           name TEXT NOT NULL
                       );
               """);
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    private static boolean isValidTable(String tableName) {
        // TODO: need to add validation for table name to prevent SQL injection
        return true;
    }

    /**
     * Prints all entries in the specified table.
     * Should only be used internally NEVER directly for user input
     * @param tableName the name of the table to print
     */
    public static void printTable(String tableName) {
        try (Connection conn = connect()) {
            if (!isValidTable(tableName)) {
                throw new IllegalArgumentException("Invalid table name");
            }
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM " + tableName + ";");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                System.out.println(id + "\t" + name);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
