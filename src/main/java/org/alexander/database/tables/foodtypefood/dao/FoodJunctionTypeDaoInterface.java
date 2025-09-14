package org.alexander.database.tables.foodtypefood.dao;

import org.alexander.database.tables.food.Food;
import org.alexander.database.tables.foodtype.FoodType;
import org.alexander.database.tables.foodtypefood.FoodJunctionType;
import java.util.List;

public interface FoodJunctionTypeDaoInterface {
    FoodJunctionType addFoodTypeFood(Food food, FoodType type);
    boolean deleteFoodTypeFood(String name, String type);
    boolean deleteFoodTypeFood(Food food, FoodType type);
    List<FoodType> getTypes(Food food);
}
