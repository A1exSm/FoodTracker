package org.alexander.database.tables.foodtype;

import org.alexander.database.Entity;

public class FoodType extends Entity<String> {
    private String name;

    public FoodType(String name) {
        super(name);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        setKey(name);
    }
}
