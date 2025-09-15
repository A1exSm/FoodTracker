package org.alexander.database.tables.meal.dao;

import org.alexander.database.DatabaseManager;
import org.alexander.database.QueryHelper;
import org.alexander.database.tables.TableDao;
import org.alexander.database.tables.day.Day;
import org.alexander.database.tables.meal.Meal;
import org.alexander.database.tables.meal.MealTypes;
import org.alexander.database.tables.week.Week;
import org.alexander.logging.CentralLogger;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class MealDao implements MealDaoInterface, TableDao {
    private final CentralLogger logger = CentralLogger.getInstance();

    /**
     * Can only be called with "id" as attribute.
     */
    @Override
    public boolean contains(String entity, String attribute) {
        if (!attribute.equals("id")) {
            throw new IllegalArgumentException("Can only check existence by the primary key 'id'.");
        }
        return QueryHelper.entityExists(Integer.parseInt(entity), "id", "MEAL");
    }

    public boolean contains(LocalDate date, LocalTime time, MealTypes type) {
        String query = "SELECT 1 FROM MEAL WHERE date = ? AND time = ? AND type = ? LIMIT 1";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            preparedStatement.setDate(1, java.sql.Date.valueOf(date));
            preparedStatement.setTime(2, java.sql.Time.valueOf(time));
            preparedStatement.setString(3, type.name());
            return preparedStatement.executeQuery().next();
        } catch (SQLException e) {
            logger.logError("SQL Exception in contains(date, time, type): " + e.getMessage());
            return false;
        }
    }

    public boolean contains (Meal meal) {
        return QueryHelper.entityExists(meal.getId(), "id", "MEAL");
    }



    @Override
    public void validateAttribute(String attribute) {
        if (!attribute.equals("id") && !attribute.equals("date") && !attribute.equals("time") && !attribute.equals("type")) {
            throw new IllegalArgumentException("Invalid attribute: " + attribute);
        }
    }

    @Override
    public Meal addMeal(LocalDate date, MealTypes type, LocalTime time) {
        if (contains(date, time, type)) {
            logger.logWarning("Meal already exists! Returning null.");
            return null;
        }
        String query = "INSERT INTO MEAL (date, time, type) VALUES (?, ?, ?)";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            preparedStatement.setDate(1, java.sql.Date.valueOf(date));
            preparedStatement.setTime(2, java.sql.Time.valueOf(time));
            preparedStatement.setString(3, type.name());
            if (preparedStatement.executeUpdate() > 0) {
                try (var rs = preparedStatement.getGeneratedKeys()) {
                    if (rs.next()) {
                        int meal_id = rs.getInt(1);
                        return new Meal(meal_id, date, type, time);
                    }
                    throw new SQLException("Creating Meal failed, no ID obtained.");
                }
            } else {
                logger.logError("Failed to add meal: No rows affected.");
            }
        } catch (SQLException e) {
            logger.logError("SQL Exception in addMeal(date, time, type): " + e.getMessage());
        }
        return null;
    }

    @Override
    public Meal addMeal(Day day, MealTypes type, LocalTime time) {
        return addMeal(day.getDate(), type, time);
    }

    @Override
    public Meal addMeal(Day day, MealTypes type) {
        return addMeal(day, type, type.defaultTime());
    }

    @Override
    public Meal addMeal(Meal meal) {
        return addMeal(meal.getDate(), meal.getType(), meal.getTime());
    }

    @Override
    public boolean deleteMeal(int id) {
        return QueryHelper.deleteEntity(id, "id", "MEAL");
    }

    @Override
    public boolean deleteMeal(Meal meal) {
        return deleteMeal(meal.getId());
    }

    @Override
    public Meal getMeal(int id) {
        if (!contains(String.valueOf(id), "id")) {
            logger.logWarning(String.format("Meal with id '%d' does not exist. Returning null.", id));
            return null;
        }
        String query = "SELECT date, time, type FROM MEAL WHERE id = ?";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            preparedStatement.setInt(1, id);
            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return getMealFromResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            logger.logError("SQL Exception in getMeal(id): " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Meal> getMeals() {
        ArrayList<Meal> mealList = new ArrayList<>();
        String query = "SELECT id, date, time, type FROM MEAL";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query);
                var resultSet = preparedStatement.executeQuery()
        ) {
            while (resultSet.next()) {
                mealList.add(getMealFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            logger.logError("Get Meals failed: Exception occurred while retrieving Meal list. Exception: " + e.getMessage());
        }
        return mealList;
    }

    private Meal getMealFromResultSet(java.sql.ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        LocalDate date = resultSet.getDate("date").toLocalDate();
        LocalTime time = resultSet.getTime("time").toLocalTime();
        MealTypes type = MealTypes.valueOf(resultSet.getString("type"));
        return new Meal(id, date, type, time);
    }

    @Override
    public List<Meal> getDayMeals(LocalDate date) {
        ArrayList<Meal> mealList = new ArrayList<>();
        String query = "SELECT id, date, time, type FROM MEAL WHERE date = ?";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            preparedStatement.setDate(1, java.sql.Date.valueOf(date));
            try (var resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    mealList.add(getMealFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            logger.logError("SQL Exception in getDayMeals(date): " + e.getMessage());
        }
        return mealList;
    }

    @Override
    public List<Meal> getDayMeals(Day day) {
        return getDayMeals(day.getDate());
    }

    @Override
    public List<Meal> getMealsBetweenDates(LocalDate start, LocalDate end) {
        ArrayList<Meal> mealList = new ArrayList<>();
        String query = "SELECT id, date, time, type FROM MEAL WHERE date BETWEEN ? AND ?";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            preparedStatement.setDate(1, java.sql.Date.valueOf(start));
            preparedStatement.setDate(2, java.sql.Date.valueOf(end));
            try (var resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    mealList.add(getMealFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            logger.logError("SQL Exception in getMealsBetweenDates(start, end): " + e.getMessage());
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
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            preparedStatement.setString(1, mealType.name());
            try (var resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    mealList.add(getMealFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            logger.logError("SQL Exception in getMealsByType(mealType): " + e.getMessage());
        }
        return mealList;
    }

    @Override
    public Meal updateMeal(int id, LocalDate date, LocalTime time, MealTypes type) {
        if (!contains(String.valueOf(id), "id")) {
            logger.logWarning(String.format("Meal with id '%d' does not exist. Cannot update non-existing meal.", id));
            return null;
        }
        String query = "UPDATE MEAL SET date = ?, time = ?, type = ? WHERE id = ?";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            preparedStatement.setDate(1, java.sql.Date.valueOf(date));
            preparedStatement.setTime(2, java.sql.Time.valueOf(time));
            preparedStatement.setString(3, type.name());
            preparedStatement.setInt(4, id);
            if (preparedStatement.executeUpdate() > 0) {
                return new Meal(id, date, type, time);
            } else {
                logger.logError("Failed to update meal: No rows affected.");
            }
        } catch (SQLException e) {
            logger.logError("SQL Exception in updateMeal(id, date, time, type): " + e.getMessage());
        }
        return null;
    }

    @Override
    public Meal updateMeal(Meal meal) {
        return updateMeal(meal.getId(), meal.getDate(), meal.getTime(), meal.getType());
    }
}
