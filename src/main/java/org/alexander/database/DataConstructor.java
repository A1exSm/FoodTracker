package org.alexander.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class DataConstructor {
    public void construct() {
        try (Connection conn = DatabaseManager.connect()) {
            // Check connection
            if (conn == null) {
                throw new SQLException("Error creating database connection, connection is null");
            }
            // Check Existence of tables
            for (Tables table : Tables.values()) {
                if (!checkTableExistence(conn, table.name())) {
                    throw new SQLException("Table " + table.name() + " does not exist");
                }
            }
            // Start data population
            populateFoodTypes(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    private boolean checkTableExistence(Connection conn, String table) throws SQLException {
        try (java.sql.Statement stmt = conn.createStatement()) {
            return stmt.executeQuery("SELECT name AS table_name FROM sqlite_master WHERE type='table' AND name='" + table + "'").next();
        }
    }
    // population functions
    private void populateFoodTypes(Connection conn) {
        try (java.sql.PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO FOOD_TYPE VALUES (?)")) {
            insertFoodTypes(preparedStatement, "Carbohydrate");
            insertFoodTypes(preparedStatement, "Protein");
            insertFoodTypes(preparedStatement, "Fat");
            insertFoodTypes(preparedStatement, "Fiber");
            preparedStatement.executeBatch();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    private void insertFoodTypes(PreparedStatement preparedStatement, String type) throws SQLException {
        preparedStatement.setString(1, type);
        preparedStatement.addBatch();
    }
}
