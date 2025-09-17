package org.alexander.database.tables.foodsnack;

import org.alexander.database.CompoundEntity;
import org.alexander.database.tables.food.Food;
import org.alexander.database.tables.snack.Snack;
import org.jetbrains.annotations.NotNull;

public class FoodSnack extends CompoundEntity<String, Integer> {
    private String foodName;
    private Integer snackId;
    private Double num_servings;

    public FoodSnack(String foodName, Integer snackId, Double num_servings) {
        super(foodName, snackId);
        this.foodName = foodName;
        this.snackId = snackId;
        this.num_servings = num_servings;
    }

    public FoodSnack(String foodName, Integer snackId) {
        this(foodName, snackId, null);
    }

    public FoodSnack(@NotNull Food food, @NotNull Snack snack, Double num_servings) {
        this(food.getName(), snack.getId(), num_servings);
    }

    public FoodSnack(@NotNull Food food, @NotNull Snack snack) {
        this(food.getName(), snack.getId());
    }

    public String getFoodName() {
        return foodName;
    }

    public Integer getSnackId() {
        return snackId;
    }

    public Double getNumServings() {
        return num_servings;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
        updateKey(this.foodName, snackId);
    }

    public void setSnackId(Integer snackId) {
        this.snackId = snackId;
        updateKey(foodName, this.snackId);
    }

    public void setNumServings(Double num_servings) {
        this.num_servings = num_servings;
    }
}
