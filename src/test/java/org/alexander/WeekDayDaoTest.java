package org.alexander;

import org.alexander.database.tables.day.Day;
import org.alexander.database.tables.day.dao.DayDao;
import org.alexander.database.tables.week.Week;
import org.alexander.database.tables.week.dao.WeekDao;
import org.alexander.logging.CentralLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WeekDayDaoTest {
    private WeekDao weekDao;
    private Week week;

    @BeforeEach
    void setUp() {
        weekDao = new WeekDao();
        week = null; // Initialize with a valid Week object if needed
    }

    @Test
    void testWeek() {
       clearWeeks();
        LocalDate start_date = LocalDate.now().minusDays(6);
        LocalDate end_date = start_date.plusDays(6);
        week  = weekDao.addWeek(LocalDate.now().minusDays(6));
        assertNotNull(week);
        assertEquals(start_date, week.getStartDate());
        assertEquals(end_date, week.getEndDate());
        assertTrue(weekDao.contains(String.valueOf(weekDao.getWeek(LocalDate.now().minusDays(6)).getId()), "week_id"));
        assertTrue(weekDao.deleteWeek(week));
    }

    private boolean weekExists(int id, List<Week> weekList) {
        for (Week w : weekList) {
            if (w.getId() == id) {
                return true;
            }
        }
        return false;
    }

    private void clearWeeks() {
        if (!weekDao.getWeekList().isEmpty()) {
            for (Week w : weekDao.getWeekList()) {
                CentralLogger.getInstance().logInfo(String.format("\nDeleting week id: %d, start_date: %s, end_date: %s%n", w.getId(), w.getStartDate(), w.getEndDate()));
                assertTrue(weekDao.deleteWeek(w));
            }
        }
    }

    private void clearDays() {
        DayDao dayDao = new DayDao();
        if (!dayDao.getDayList().isEmpty()) {
            for (Day d : dayDao.getDayList()) {
                CentralLogger.getInstance().logInfo(String.format("\nDeleting day date: %s, week_id: %d, body_weight: %s%n", d.getDate(), d.getWeek_id(), d.getBodyWeight()));
                assertTrue(dayDao.deleteDay(d));
            }
        }
    }

    @Test
    void testDay() {
        clearWeeks();
        clearDays();
        // Week
        week = weekDao.addWeek(LocalDate.now().minusDays(6));
        assertNotNull(week);
        assertTrue(weekDao.contains(String.valueOf(week.getId()), "week_id"));
        // Day
        DayDao dayDao = new DayDao();
        Day day = new Day(LocalDate.now(), week.getId(), null);
        Day addedDay = dayDao.addDay(day);
        // Day Assertions
        assertNotNull(addedDay);
        assertEquals(day.getDate(), addedDay.getDate());
        assertEquals(day.getWeek_id(), addedDay.getWeek_id());
        assertNull(addedDay.getBodyWeight());
        Day today = dayDao.getDay(LocalDate.now());
        assertNotNull(today);
        assertTrue(containsDay(today, dayDao.getDaysInWeek(week)));

        // Delete
        assertTrue(dayDao.deleteDay(addedDay));
        assertTrue(weekDao.deleteWeek(week));
    }

    private boolean containsDay(Day day, List<Day> daysInWeek) {
        for (Day d : daysInWeek) {
            if (d.getDate().equals(day.getDate()) && d.getWeek_id() == day.getWeek_id()) {
                return true;
            }
        }
        return false;
    }
}
