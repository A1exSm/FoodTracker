package org.alexander.database.tables.week.dao;

import org.alexander.database.tables.week.Week;

import java.time.LocalDate;
import java.util.List;

public interface WeekDaoInterface {
    Week addWeek(LocalDate startDate, LocalDate endDate);
    Week addWeek(LocalDate startDate); // adds 6 days to get end date
    Week addWeek(Week week);
    boolean deleteWeek(Week week);
    boolean deleteWeek(int id);
    List<Week> getWeekList();
    Week getWeek(LocalDate startDate);
}
