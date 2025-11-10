package org.alexander.database.tables.meal.dao;

import org.alexander.database.DatabaseComparer;
import org.alexander.database.DatabaseManager;
import org.alexander.database.QueryHelper;
import org.alexander.database.tables.TableDao;
import org.alexander.database.tables.day.Day;
import org.alexander.database.tables.meal.Meal;
import org.alexander.database.tables.meal.MealTypes;
import org.alexander.database.tables.week.Week;
import org.alexander.logging.CentralLogger;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class MealDao implements MealDaoInterface, TableDao, DatabaseComparer.ReadOnlyDao<Meal> {
    private final CentralLogger logger = CentralLogger.getInstance();
    private final Connection conn;

    public MealDao() { this.conn = null; }
    public MealDao(Connection conn) { this.conn = conn; }

    private Connection getConnection() throws SQLException {
        if (conn != null && !conn.isClosed()) return conn;
        return DatabaseManager.connect();
    }

    @Override
    public boolean contains(String entity, String attribute) {
        if (!attribute.equals("id")) throw new IllegalArgumentException("Can only check by 'id'.");
        return QueryHelper.entityExists(Integer.parseInt(entity), "id", "MEAL");
    }

    /**
     * Checks if a meal exists in the database by its ID.
     * @param meal The meal to check
     * @return true if the meal exists, false otherwise
     */
    public boolean contains(Meal meal) {
        if (meal == null) return false;
        return contains(String.valueOf(meal.getId()), "id");
    }

    public boolean contains(LocalDate date, LocalTime time, MealTypes type) {
        String query = "SELECT 1 FROM MEAL WHERE date = ? AND time = ? AND type = ? LIMIT 1";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            ps.setTime(2, java.sql.Time.valueOf(time));
            ps.setString(3, type.name());
            return ps.executeQuery().next();
        } catch (SQLException e) {
            logger.logError(e);
            return false;
        }
    }

    @Override
    public void validateAttribute(String attribute) {
        if (!attribute.equals("id") && !attribute.equals("date") && !attribute.equals("time") && !attribute.equals("type")) {
            throw new IllegalArgumentException("Invalid attribute: " + attribute);
        }
    }

    @Override
    public Meal addMeal(LocalDate date, MealTypes type, LocalTime time) {
        if (contains(date, time, type)) return null;
        String query = "INSERT INTO MEAL (date, time, type) VALUES (?, ?, ?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            ps.setTime(2, java.sql.Time.valueOf(time));
            ps.setString(3, type.name());
            if (ps.executeUpdate() > 0) {
                try (var rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return new Meal(rs.getInt(1), date, type, time);
                }
            }
        } catch (SQLException e) {
            logger.logError(e);
        }
        return null;
    }

    @Override
    public Meal addMeal(Day day, MealTypes type, LocalTime time) { return addMeal(day.getDate(), type, time); }
    @Override
    public Meal addMeal(Day day, MealTypes type) { return addMeal(day, type, type.defaultTime()); }
    @Override
    public Meal addMeal(Meal meal) { return addMeal(meal.getDate(), meal.getType(), meal.getTime()); }
    @Override
    public boolean deleteMeal(int id) { return QueryHelper.deleteEntity(id, "id", "MEAL"); }
    @Override
    public boolean deleteMeal(Meal meal) { return deleteMeal(meal.getId()); }

    @Override
    public Meal getMeal(int id) {
        String query = "SELECT id, date, time, type FROM MEAL WHERE id = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
            ps.setInt(1, id);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) return getMealFromResultSet(rs);
            }
        } catch (SQLException e) {
            logger.logError(e);
        }
        return null;
    }

    @Override
    public List<Meal> getMeals() { return getAll(); }

    private Meal getMealFromResultSet(ResultSet rs) throws SQLException {
        return new Meal(rs.getInt("id"), rs.getDate("date").toLocalDate(), MealTypes.valueOf(rs.getString("type")), rs.getTime("time").toLocalTime());
    }

    @Override
    public List<Meal> getDayMeals(LocalDate date) {
        ArrayList<Meal> mealList = new ArrayList<>();
        String query = "SELECT id, date, time, type FROM MEAL WHERE date = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            try (var rs = ps.executeQuery()) {
                while (rs.next()) mealList.add(getMealFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.logError(e);
        }
        return mealList;
    }

    @Override
    public List<Meal> getDayMeals(Day day) { return getDayMeals(day.getDate()); }

    @Override
    public List<Meal> getMealsBetweenDates(LocalDate start, LocalDate end) {
        ArrayList<Meal> mealList = new ArrayList<>();
        String query = "SELECT id, date, time, type FROM MEAL WHERE date BETWEEN ? AND ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
            ps.setDate(1, java.sql.Date.valueOf(start));
            ps.setDate(2, java.sql.Date.valueOf(end));
            try (var rs = ps.executeQuery()) {
                while (rs.next()) mealList.add(getMealFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.logError(e);
        }
        return mealList;
    }

    @Override
    public List<Meal> getWeekMeals(Week week) {
        return getMealsBetweenDates(week.getStartDate(), week.getEndDate());
    }

    @Override
    public List<Meal> getMealsByType(MealTypes mealType) {
        ArrayList<Meal> mealList = new ArrayList<>();
        String query = "SELECT id, date, time, type FROM MEAL WHERE type = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
            ps.setString(1, mealType.name());
            try (var rs = ps.executeQuery()) {
                while (rs.next()) mealList.add(getMealFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.logError(e);
        }
        return mealList;
    }

    @Override
    public Meal updateMeal(int id, LocalDate date, LocalTime time, MealTypes type) {
        if (!contains(String.valueOf(id), "id")) return null;
        String query = "UPDATE MEAL SET date = ?, time = ?, type = ? WHERE id = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            ps.setTime(2, java.sql.Time.valueOf(time));
            ps.setString(3, type.name());
            ps.setInt(4, id);
            if (ps.executeUpdate() > 0) return new Meal(id, date, type, time);
        } catch (SQLException e) {
            logger.logError(e);
        }
        return null;
    }

    @Override
    public Meal updateMeal(Meal meal) { return updateMeal(meal.getId(), meal.getDate(), meal.getTime(), meal.getType()); }

    @Override
    public List<Meal> getAll() {
        ArrayList<Meal> mealList = new ArrayList<>();
        String query = "SELECT id, date, time, type FROM MEAL";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(query); var rs = ps.executeQuery()) {
            while (rs.next()) mealList.add(getMealFromResultSet(rs));
        } catch (SQLException e) {
            logger.logError(e);
        }
        return mealList;
    }
}