package org.alexander.database.foodtype.dao;

import org.alexander.database.foodtype.FoodType;

import java.sql.SQLException;
import java.util.List;

public interface FoodTypeDaoInterface {
    /**
     * Adds a new food type to the database.
     * @param name the name of the food type to check
     * @return a {@link FoodType} if the food type is successfully created, null otherwise
     */
    FoodType createFoodType(String name);
    /**
     * Deletes a food type by name.
     * @param name the name of the food type to delete
     * @return true if the food type was deleted, false if it did not exist
     */
    boolean deleteFoodType(String name);
    /**
     * Deletes a food type.
     * @param foodType the food type to delete
     * @return true if the food type was deleted, false if it did not exist
     */
    boolean deleteFoodType(FoodType foodType);
    /**
     * Retrieves all food types from the database.
     * @return a list of all food type names
     * @throws SQLException if a database access error occurs
     */
    List<FoodType> getFoodTypeList();

    FoodType getFoodType(String name);
}
