package org.alexander;

import org.alexander.database.DatabaseManager;
import org.alexander.database.food.Food;
import org.alexander.database.food.FoodDao;
import org.alexander.database.foodtype.FoodType;
import org.alexander.database.foodtype.dao.FoodTypeDao;
import org.alexander.database.foodtypefood.FoodJunctionType;
import org.alexander.database.foodtypefood.dao.FoodJunctionTypeDao;

/**
 * Main class to test the database connection and operations.
 * @since 1.0.0
 */
public class Main {
    public static void main(String[] args) {
        DatabaseManager.initialise();
        DatabaseManager.save();
    }
}