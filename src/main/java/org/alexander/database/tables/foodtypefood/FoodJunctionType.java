package org.alexander.database.tables.foodtypefood;

import org.alexander.database.CompoundEntity;
import org.alexander.database.CompoundKey;
import org.alexander.database.Entity;
import org.alexander.database.tables.food.Food;
import org.alexander.database.tables.foodtype.FoodType;

public class FoodJunctionType extends CompoundEntity<String,String> {
    private String name;
    private String type;

    public FoodJunctionType(String name, String type) {
        super(name, type);
        this.name = name;
        this.type = type;
    }

    public FoodJunctionType(Food food, FoodType foodType) {
        this(food.getName(), foodType.getName());
    }

    public String getName() {
        return name;
    }
    public String getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
        updateKey(this.name, type);
    }

    public void setType(String type) {
        this.type = type;
        updateKey(name, this.type);
    }

}
