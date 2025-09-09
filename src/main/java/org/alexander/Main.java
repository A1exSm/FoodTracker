package org.alexander;

import org.alexander.database.DatabaseManager;
import org.alexander.database.foodtype.dao.FoodTypeDao;

/**
 * Main class to test the database connection and operations.
 * @since 1.0.0
 */
public class Main {
    public static void main(String[] args) {
        DatabaseManager.initialise();
        new FoodTypeDao().getFoodTypeList().forEach(foodType -> System.out.println(foodType.getName()));
        DatabaseManager.save();
    }
}