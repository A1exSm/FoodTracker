package org.alexander;
import static org.junit.jupiter.api.Assertions.*;

import org.alexander.database.tables.day.Day;
import org.alexander.database.tables.day.dao.DayDao;
import org.alexander.database.tables.meal.Meal;
import org.alexander.database.tables.meal.MealTypes;
import org.alexander.database.tables.meal.dao.MealDao;
import org.alexander.database.tables.snack.Snack;
import org.alexander.database.tables.snack.dao.SnackDao;
import org.alexander.database.tables.week.Week;
import org.alexander.database.tables.week.dao.WeekDao;
import org.alexander.logging.CentralLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class MealSnackTest {
    WeekDao weekDao;
    DayDao dayDao;

    @BeforeEach
    void setUp() {
        weekDao = new WeekDao();
        dayDao = new DayDao();
    }

    @AfterEach
    void tearDown() {
        WeekDayDaoTest.clearWeeks(weekDao);
        WeekDayDaoTest.clearDays(dayDao);
        clearMeals(new MealDao());
        clearSnacks(new SnackDao());
        weekDao = null;
        dayDao = null;
    }

    @Test
    void newMealTest() {
        // Week setup
        var week = initWeek();
        // Day setup
        var today = initDay();
        // Meal setup
        MealDao mealDao = new MealDao();
        Meal breakfast = mealDao.addMeal(today, MealTypes.BREAKFAST);
        assertNotNull(breakfast);
        assertTrue(mealDao.contains(breakfast));
        assertFalse(mealDao.getMeals().isEmpty());
        List<Meal> todayMeals = mealDao.getDayMeals(today);
        assertFalse(todayMeals.isEmpty());
        assertEquals(breakfast.getId(), todayMeals.getFirst().getId());
        // Cleanup
        assertTrue(mealDao.deleteMeal(breakfast));
        assertTrue(dayDao.deleteDay(today));
        assertTrue(weekDao.deleteWeek(week));
        assertFalse(mealDao.contains(String.valueOf(breakfast.getId()), "id"));
    }

    @Test
    void newSnackTest() {
        // Day-Week setup
        var week = initWeek();
        var today = initDay();
        // Snack setup
        SnackDao snackDao = new SnackDao();
        Snack snack = snackDao.addSnack(today, LocalTime.now());
        assertNotNull(snack);
        assertTrue(snackDao.contains(snack.getId()));
        assertFalse(snackDao.getSnacks().isEmpty());
        List<Snack> todaySnacks = snackDao.getDaySnacks(today);
        assertFalse(todaySnacks.isEmpty());
        assertEquals(snack.getId(), todaySnacks.getFirst().getId());
        // Cleanup
        assertTrue(snackDao.deleteSnack(snack));
        assertTrue(dayDao.deleteDay(today));
        assertTrue(weekDao.deleteWeek(week));
        assertFalse(snackDao.contains(snack.getId()));
    }

    public static void clearMeals(MealDao mealDao) {
        if (!mealDao.getMeals().isEmpty()) {
            for (Meal m : mealDao.getMeals()) {
                CentralLogger.getInstance().logInfo(String.format("\nDeleting meal id: %d, date: %s, type: %s%n", m.getId(), m.getDate(), m.getType()));
                assertTrue(mealDao.deleteMeal(m));
            }
        }
    }

    public static void clearSnacks(SnackDao snackDao) {
        if (!snackDao.getSnacks().isEmpty()) {
            for (var s : snackDao.getSnacks()) {
                CentralLogger.getInstance().logInfo(String.format("\nDeleting snack id: %d, date: %s, time: %s%n", s.getId(), s.getDate(), s.getTime()));
                assertTrue(snackDao.deleteSnack(s));
            }
        }
    }

    private Week initWeek() {
        Week week = weekDao.addWeek(weekDao.getClosestMonday(LocalDate.now()));
        assertNotNull(week);
        return week;
    }

    private Day initDay() {
        Day today = dayDao.addDay(LocalDate.now());
        assertNotNull(today);
        return today;
    }
}
