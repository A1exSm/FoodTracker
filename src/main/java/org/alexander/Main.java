package org.alexander;

import org.alexander.database.DatabaseManager;
import org.alexander.database.dao.FoodTypeDao;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Main class to test the database connection and operations.
 * @since 1.0.0
 */
public class Main {
    public static void main(String[] args) {
        DatabaseManager.initialise();
        new FoodTypeDao().getFoodTypes().forEach(System.out::println);
        DatabaseManager.save();
    }
}