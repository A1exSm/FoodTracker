package org.alexander.database.tables.foodtypefood;

import org.alexander.database.CompoundKey;
import org.alexander.database.Entity;
import org.alexander.database.tables.food.Food;
import org.alexander.database.tables.foodtype.FoodType;

public class FoodJunctionType extends Entity<CompoundKey<String, String>> {
    private String name;
    private String type;

    public FoodJunctionType(Food food, FoodType foodType) {
        super(new CompoundKey<>(food.getName(), foodType.getName()));
        name = food.getName();
        type = foodType.getName();
    }

    public FoodJunctionType(String name, String type) {
        super(new CompoundKey<>(name, type));
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }
    public String getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
        updateKey();
    }

    public void setType(String type) {
        this.type = type;
        updateKey();
    }

    private void updateKey() {
        setKey(new CompoundKey<>(name, type));
    }
}
