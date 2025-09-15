package org.alexander.database.tables.snack.dao;

import org.alexander.database.tables.day.Day;
import org.alexander.database.tables.snack.Snack;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface SnackDaoInterface {
    Snack addSnack(Snack snack);
    Snack addSnack(LocalDate date, LocalTime time);
    Snack addSnack(Day day, LocalTime time);
    boolean deleteSnack(int id);
    boolean deleteSnack(Snack snack);
    Snack getSnack(int id);
    Snack getSnack(LocalDate date, LocalTime time);
    Snack updateSnack(int id, LocalDate date, LocalTime time);
    Snack updateSnack(Snack snack);
    List<Snack> getSnacks();
    List<Snack> getDaySnacks(Day day);
}
