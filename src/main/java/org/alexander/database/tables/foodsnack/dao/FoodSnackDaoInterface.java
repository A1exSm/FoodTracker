package org.alexander.database.tables.foodsnack.dao;

import org.alexander.database.tables.food.Food;
import org.alexander.database.tables.foodsnack.FoodSnack;
import org.alexander.database.tables.snack.Snack;

import java.util.List;

public interface FoodSnackDaoInterface {
    // Add
    FoodSnack addFoodSnack(String foodName, Integer snackId, Double num_servings);
    FoodSnack addFoodSnack(String foodName, Integer snackId);
    FoodSnack addFoodSnack(Food food, Snack snack, Double num_servings);
    FoodSnack addFoodSnack(FoodSnack foodSnack);
    // Delete
    boolean deleteFoodSnack(String foodName, Integer snackId);
    boolean deleteFoodSnack(FoodSnack foodSnack);
    boolean deleteFoodSnack(Food food, Snack snack);
    // Get
    FoodSnack getFoodSnack(String foodName, Integer snackId);
    FoodSnack getFoodSnack(Food food, Snack snack);
    FoodSnack getFoodSnack(FoodSnack foodSnack);
    // List
    List<FoodSnack> getFoodSnackList();
}
