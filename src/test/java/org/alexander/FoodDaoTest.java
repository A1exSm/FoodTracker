package org.alexander;

import org.alexander.database.DatabaseManager;
import org.alexander.database.food.Food;
import org.alexander.database.food.FoodDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FoodDaoTest {
    private FoodDao foodDao;
    private Food food;
    static {
        DatabaseManager.initialise();
    }

    @BeforeEach
    void setUp() {
        foodDao = new FoodDao();
        food = null; // Initialize with a valid Food object if needed
    }

    @Test
    void testFoodCreation() {
        String name = "TestApple";
        Double servingGrams = 150.0;
        Double servingCalories = 80.0;

        food = new Food(name, servingGrams, servingCalories);

        assertNotNull(food);
        assert food.getName().equals(name);
        assert food.getServingGrams().equals(servingGrams);
        assert food.getServingCalories().equals(servingCalories);
    }

    @Test
    void testAddFood() {
        String name = "TestBanana";
        Double servingGrams = 120.0;
        Double servingCalories = 100.0;

        food = foodDao.addFood(name, servingGrams, servingCalories);
        assertNotNull(food);
        assert food.getName().equals(name);
        assert food.getServingGrams().equals(servingGrams);
        assert food.getServingCalories().equals(servingCalories);

        // Clean up
        boolean deleted = foodDao.deleteFood(food);
        assertTrue(deleted);
    }
}
