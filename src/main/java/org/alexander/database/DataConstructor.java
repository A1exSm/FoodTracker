package org.alexander.database;

import org.alexander.database.tables.Tables;
import org.alexander.database.tables.foodtype.dao.FoodTypeDao;

import java.sql.Connection;
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
                if (QueryHelper.tableExists(table.name())) {
                    throw new SQLException("Table " + table.name() + " does not exist");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        // Function Execution
        populateFoodTypes(); // must be called outside the try-with-resources block to avoid over-lapping connections
    }
    // population functions
    private void populateFoodTypes() {
        FoodTypeDao dao = new FoodTypeDao();
        dao.createFoodType("Carbohydrate");
        dao.createFoodType("Protein");
        dao.createFoodType("Fat");
        dao.createFoodType("Fiber");
    }
}
