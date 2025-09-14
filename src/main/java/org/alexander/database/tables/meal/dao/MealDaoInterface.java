package org.alexander.database.tables.meal.dao;

import org.alexander.database.tables.day.Day;
import org.alexander.database.tables.meal.Meal;
import org.alexander.database.tables.meal.MealTypes;
import org.alexander.database.tables.week.Week;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface MealDaoInterface {
    Meal addMeal(LocalDate date,  MealTypes type, LocalTime time);
    Meal addMeal(Day day, MealTypes type, LocalTime time);
    Meal addMeal(Day day, MealTypes type);
    Meal addMeal(Meal meal);
    boolean deleteMeal(int id);
    boolean deleteMeal(Meal meal);
    Meal getMeal(int id);
    List<Meal> getMeals();
    List<Meal> getDayMeals(LocalDate date);
    List<Meal> getDayMeals(Day day);
    List<Meal> getMealsBetweenDates(LocalDate start, LocalDate end);
    List<Meal> getWeekMeals(Week week);
    List<Meal> getMealsByType(MealTypes mealType);
    Meal updateMeal(int id, LocalDate date, LocalTime time, MealTypes type);
    Meal updateMeal(Meal meal);
}
