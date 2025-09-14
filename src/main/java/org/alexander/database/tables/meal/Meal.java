package org.alexander.database.tables.meal;

import org.alexander.database.Entity;

import java.time.LocalDate;
import java.time.LocalTime;

public class Meal extends Entity<Integer> {
 private final int id;
 private LocalDate date;
 private LocalTime time;
 private MealTypes type;

    public Meal(int id, LocalDate date, MealTypes type, LocalTime time) {
        super(id);
        this.id = id;
        this.date = date;
        this.type = type;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public MealTypes getType() {
        return type;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setType(MealTypes type) {
        this.type = type;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }
}
