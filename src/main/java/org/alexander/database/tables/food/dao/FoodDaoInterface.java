package org.alexander.database.tables.food.dao;

import org.alexander.database.tables.food.Food;

import java.util.List;

public interface FoodDaoInterface {
    Food addFood(String name, Double serving_grams, Double serving_calories); // Double can be null
    Food addFood(Food food);
    boolean deleteFood(String name);
    boolean deleteFood(Food food);
    List<Food> getFoodList();
    Food getFood(String name);
    Food updateFood(String name, Double serving_grams, Double serving_calories); // Integer can be null
}
