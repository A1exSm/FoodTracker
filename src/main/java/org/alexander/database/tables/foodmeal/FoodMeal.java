package org.alexander.database.tables.foodmeal;

import org.alexander.database.CompoundEntity;
import org.alexander.database.tables.food.Food;
import org.alexander.database.tables.meal.Meal;
import org.jetbrains.annotations.NotNull;

public class FoodMeal extends CompoundEntity<String, Integer> {
    private String foodName;
    private Integer mealId;
    private Double num_servings;

    public FoodMeal(String foodName, Integer mealId, Double num_servings) {
        super(foodName, mealId);
        this.foodName = foodName;
        this.mealId = mealId;
        this.num_servings = num_servings;
    }

    public FoodMeal(String foodName, Integer mealId) {
        this(foodName, mealId, null);
    }

    public FoodMeal(@NotNull Food food, @NotNull Meal meal, Double num_servings) {
        this(food.getName(), meal.getId(), num_servings);
    }

    public FoodMeal(@NotNull Food food, @NotNull Meal meal) {
        this(food.getName(), meal.getId());
    }

    public String getFoodName() {
        return foodName;
    }

    public Integer getMealId() {
        return mealId;
    }

    public Double getNumServings() {
        return num_servings;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
        updateKey(this.foodName, mealId);
    }

    public void setMealId(Integer mealId) {
        this.mealId = mealId;
        updateKey(foodName, this.mealId);
    }

    public void setNumServings(Double num_servings) {
        this.num_servings = num_servings;
    }
}
