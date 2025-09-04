package org.alexander.database.dao;

import java.sql.SQLException;
import java.util.List;

public interface FoodTypeDaoInterface {
    /**
     * Adds a new food type to the database.
     * @param name the name of the food type to check
     * @return true if the food type exists, false otherwise
     */
    boolean createFoodType(String name);
    /**
     * Deletes a food type by name.
     * @param name the name of the food type to delete
     * @return true if the food type was deleted, false if it did not exist
     */
    boolean deleteFoodType(String name);
    /**
     * Retrieves all food types from the database.
     * @return a list of all food type names
     * @throws SQLException if a database access error occurs
     */
    List<String> getFoodTypes() throws SQLException;
}
