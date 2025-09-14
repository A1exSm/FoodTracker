package org.alexander.database.tables.food;

import org.alexander.database.Entity;

public class Food extends Entity<String> {
    private String name;
    private Double servingGrams; // can be null
    private Double servingCalories; // can be null

    public Food(String name, Double servingGrams, Double servingCalories) {
        super(name);
        this.name = name;
        this.servingGrams = servingGrams;
        this.servingCalories = servingCalories;
    }

    public String getName() {
        return name;
    }

    public Double getServingGrams() {
        return servingGrams;
    }

    public Double getServingCalories() {
        return servingCalories;
    }

    public void setName(String name) {
        this.name = name;
        setKey(name);
    }

    public void setServingGrams(Double servingGrams) {
        this.servingGrams = servingGrams;
    }

    public void setServingCalories(Double servingCalories) {
        this.servingCalories = servingCalories;
    }

}
