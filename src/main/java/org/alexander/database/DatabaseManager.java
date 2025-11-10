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
    private static Connection currentConnection = null;

    /**
     * Connects to the database specified by the URL.
     * If a connection is already open, it returns the existing one.
     * If the connection is closed, it establishes a new one.
     * @return Connection object to the database.
     * @throws SQLException if a database access error occurs or the url is null.
     */
    public static Connection connect() throws SQLException {
        if (currentConnection == null || currentConnection.isClosed()) {
            currentConnection = DriverManager.getConnection(URL);
        }
        return currentConnection;
    }

    /**
     * Closes the current database connection if it is open. This allows
     * other processes, like the DatabaseComparer, to access the file.
     */
    public static void closeConnection() {
        if (currentConnection != null) {
            try {
                if (!currentConnection.isClosed()) {
                    currentConnection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            } finally {
                // Ensure the connection is set to null so a new one can be created.
                currentConnection = null;
            }
        }
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

    /**
     * Creates A fresh database with the required tables.
     */
    protected static void createFreshData() {
        tableConstructor.construct();
        dataConstructor.construct();
    }
}