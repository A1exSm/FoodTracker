package org.alexander.database.tables.foodmeal.dao;

import org.alexander.database.tables.food.Food;
import org.alexander.database.tables.foodmeal.FoodMeal;
import org.alexander.database.tables.meal.Meal;

import java.util.List;

public interface FoodMealDaoInterface {
    // Add
    FoodMeal addFoodMeal(String foodName, int mealId, Double num_servings);
    FoodMeal addFoodMeal(Food food, Meal meal, Double num_servings);
    FoodMeal addFoodMeal(String foodName, int mealId);
    FoodMeal addFoodMeal(Food food, Meal meal);
    // Delete
    boolean deleteFoodMeal(String foodName, int mealId);
    boolean deleteFoodMeal(Food food, Meal meal);
    boolean deleteFoodMeal(FoodMeal foodMeal);
    // Get
    FoodMeal getFoodMeal(String foodName, int mealId);
    FoodMeal getFoodMeal(Food food, Meal meal);
    FoodMeal getFoodMeal(FoodMeal foodMeal);
    // List
    List<FoodMeal> getFoodMealList();
}