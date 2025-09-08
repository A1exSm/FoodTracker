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
     * @return Connection object to the database.
     * @throws SQLException if a database access error occurs or the url is null.
     */
    public static Connection connect() throws SQLException {
        if (currentConnection != null && !currentConnection.isClosed()) {
            throw new SQLException("Please close the existing connection before opening a new one.");
        }
        currentConnection = DriverManager.getConnection(URL);
        return currentConnection;
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
