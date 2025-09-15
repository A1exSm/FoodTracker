package org.alexander.database.tables.day.dao;

import org.alexander.database.tables.day.Day;
import org.alexander.database.tables.week.Week;

import java.time.LocalDate;
import java.util.List;

public interface DayDaoInterface {
    Day addDay(LocalDate date, int week_id, Double bodyWeight);
    Day addDay(LocalDate date, int week_id); // bodyWeight is null
    Day addDay(LocalDate date); // week_id and bodyWeight are null
    Day addDay(Day day);
    boolean deleteDay(LocalDate date);
    boolean deleteDay(Day day);
    List<Day> getDayList();
    List<Day> getDaysInWeek(int week_id);
    List<Day> getDaysInWeek(Week week);
    Day getDay(LocalDate date);
    Day updateDay(LocalDate date, int week_id, Double bodyWeight);
    Day updateDay(Day day);
}
