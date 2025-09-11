package org.alexander;

import org.alexander.database.week.Week;
import org.alexander.database.week.dao.WeekDao;
import org.alexander.logging.CentralLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WeekDaoTest {
    private WeekDao weekDao;
    private Week week;

    @BeforeEach
    void setUp() {
        weekDao = new WeekDao();
        week = null; // Initialize with a valid Week object if needed
    }

    @Test
    void testWeek() {
        if (!weekDao.getWeekList().isEmpty()) {
            for (Week w : weekDao.getWeekList()) {
                System.out.printf("\nDeleting week id: %d, start_date: %s, end_date: %s%n", w.getId(), w.getStartDate(), w.getEndDate());
                assertTrue(weekDao.deleteWeek(w));
            }
        }
        LocalDate start_date = LocalDate.now();
        LocalDate end_date = start_date.plusDays(6);
        CentralLogger.getInstance().logInfo(String.format("\nstart_date: %s\nend_date: %s", start_date, end_date));
        week  = weekDao.addWeek(LocalDate.now());
        assertNotNull(week);
        assertEquals(start_date, week.getStartDate());
        assertEquals(end_date, week.getEndDate());
        assertTrue(weekDao.contains(String.valueOf(weekDao.getWeek(LocalDate.now()).getId()), "week_id"));
        CentralLogger.getInstance().logInfo(String.valueOf(weekDao.getWeek(LocalDate.now()).getId()));
        System.out.println("is there a week in the database with a week_id = week.getId() -1: " + weekExists((weekDao.getWeek(LocalDate.now()).getId() -1), weekDao.getWeekList()));
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
}
