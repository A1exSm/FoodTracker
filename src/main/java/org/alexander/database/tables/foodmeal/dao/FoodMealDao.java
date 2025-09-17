package org.alexander.database.tables.foodmeal.dao;

import org.alexander.database.tables.TableDaoTwo;
import org.alexander.database.tables.food.Food;
import org.alexander.database.tables.food.dao.FoodDao;
import org.alexander.database.tables.foodmeal.FoodMeal;
import org.alexander.database.tables.meal.Meal;
import org.alexander.database.tables.meal.dao.MealDao;
import org.alexander.logging.CentralLogger;
import org.jetbrains.annotations.NotNull;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FoodMealDao implements FoodMealDaoInterface, TableDaoTwo {
    private final CentralLogger logger = CentralLogger.getInstance();

    @Override
    public <T> boolean contains(T foodName, T mealId) {
        if (typeInvalid(foodName, mealId)) {
            throw new IllegalArgumentException("Invalid Type present in parameters for FoodMealDao.contains method, foodName must be String and mealId must be Integer, inputted types: " + foodName.getClass() + " and " + mealId.getClass());
        }
        String query = "SELECT 1 FROM FOOD_MEAL WHERE name = ? AND meal_id = ? LIMIT 1";
        try (
                var conn = org.alexander.database.DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            preparedStatement.setString(1, (String) foodName);
            preparedStatement.setInt(2, (Integer) mealId);
            return preparedStatement.executeQuery().next();
        } catch (SQLException e) {
            logger.logError(e);
        }
        return false;
    }

    private <T> boolean typeInvalid(T foodName, T mealId) {
        return !((foodName instanceof String) && (mealId instanceof Integer));
    }

    @Override
    public FoodMeal addFoodMeal(String foodName, int mealId, Double num_servings) {
        if (paramsInvalid(foodName, mealId)) {
            return null;
        } else if (contains(foodName, mealId)) {
            logger.logWarning("Junction already exists. Cannot add duplicate.");
            return null;
        }
        String query;
        if (num_servings == null) {
            query = "INSERT INTO FOOD_MEAL (name, meal_id) VALUES (?, ?)";
        } else {
            query = "INSERT INTO FOOD_MEAL (name, meal_id, num_servings) VALUES (?, ?, ?)";
        }
        try (
                var conn = org.alexander.database.DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            preparedStatement.setString(1, foodName);
            preparedStatement.setInt(2, mealId);
            if (num_servings != null) {
                preparedStatement.setDouble(3, num_servings);
            }
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 1) {
                return new FoodMeal(foodName, mealId, num_servings);
            }
            logger.logError(new SQLException("Error to insert FoodMeal junction rowAffected != 1, " + rowsAffected + " rows affected."));
        } catch (SQLException e) {
            logger.logError(e);
        }
        return null;
    }

    @Override
    public FoodMeal addFoodMeal(@NotNull Food food, @NotNull Meal meal, Double num_servings) {
        return addFoodMeal(food.getName(), meal.getId(), num_servings);
    }

    @Override
    public FoodMeal addFoodMeal(String foodName, int mealId) {
        return addFoodMeal(foodName, mealId, null);
    }

    @Override
    public FoodMeal addFoodMeal(@NotNull Food food, @NotNull Meal meal) {
        return addFoodMeal(food.getName(), meal.getId());
    }

    @Override
    public boolean deleteFoodMeal(String foodName, int mealId) {
        if (!contains(foodName, mealId)) {
            logger.logWarning("Junction does not exist. Cannot delete non-existent junction.");
            return false;
        }
        String query = "DELETE FROM FOOD_MEAL WHERE name = ? AND meal_id = ?";
        try (
                var conn = org.alexander.database.DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            preparedStatement.setString(1, foodName);
            preparedStatement.setInt(2, mealId);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 1) {
                return true;
            }
            logger.logError(new SQLException("Error to delete FoodMeal junction, rows affected: " + rowsAffected));
        } catch (SQLException e) {
            logger.logError(e);
        }
        return false;
    }

    @Override
    public boolean deleteFoodMeal(@NotNull Food food, @NotNull Meal meal) {
        return deleteFoodMeal(food.getName(), meal.getId());
    }

    @Override
    public boolean deleteFoodMeal(@NotNull FoodMeal foodMeal) {
        return deleteFoodMeal(foodMeal.getFoodName(), foodMeal.getMealId());
    }

    @Override
    public FoodMeal getFoodMeal(String foodName, int mealId) {
        if (!contains(foodName, mealId)) {
            logger.logWarning("Junction does not exist. Cannot get non-existent junction.");
            return null;
        }
        String query = "SELECT name, meal_id, num_servings FROM FOOD_MEAL WHERE name = ? AND meal_id = ?";
        try (
                var conn = org.alexander.database.DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            preparedStatement.setString(1, foodName);
            preparedStatement.setInt(2, mealId);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return getFoodMealFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            logger.logError(e);
        }
        return null;
    }

    @Override
    public FoodMeal getFoodMeal(Food food, Meal meal) {
        return getFoodMeal(food.getName(), meal.getId());
    }

    @Override
    public FoodMeal getFoodMeal(FoodMeal foodMeal) {
        return getFoodMeal(foodMeal.getFoodName(), foodMeal.getMealId());
    }

    @Override
    public List<FoodMeal> getFoodMealList() {
        ArrayList<FoodMeal> foodMeals = new ArrayList<>();
        String query = "SELECT name, meal_id, num_servings FROM FOOD_MEAL";
        try (
                var conn = org.alexander.database.DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query);
                var resultSet = preparedStatement.executeQuery()
        ) {
            while (resultSet.next()) {
                foodMeals.add(getFoodMealFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            logger.logError(e);
        }
        return foodMeals;
    }

    private FoodMeal getFoodMealFromResultSet(java.sql.ResultSet resultSet) throws SQLException {
        String foodName = resultSet.getString("name");
        int mealId = resultSet.getInt("meal_id");
        Double num_servings = resultSet.getObject("num_servings", Double.class);
        return new FoodMeal(foodName, mealId, num_servings);
    }

    private boolean paramsInvalid(String foodName, int mealId) {
        FoodDao foodDao = new FoodDao();
        MealDao mealDao = new MealDao();
        boolean foodExists = foodDao.contains(foodName, "name");
        boolean mealExists = mealDao.contains(String.valueOf(mealId), "id");
        if (!foodExists || !mealExists) {
            if (!foodExists && !mealExists) {
                logger.logError(new IllegalArgumentException("Food and Meal do not exist. Cannot create junction."));
            } else if (!foodExists) {
                logger.logError(new IllegalArgumentException("Food does not exist. Cannot create junction."));
            } else {
                logger.logError(new IllegalArgumentException("Meal does not exist. Cannot create junction."));
            }
            return true;
        }
        return false;
    }
}
