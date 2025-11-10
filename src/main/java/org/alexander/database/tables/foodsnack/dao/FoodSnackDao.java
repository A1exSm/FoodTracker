package org.alexander.database.tables.foodsnack.dao;
import org.alexander.database.DatabaseManager;
import org.alexander.database.QueryHelper;
import org.alexander.database.tables.TableDaoTwo;
import org.alexander.database.tables.food.Food;
import org.alexander.database.tables.food.dao.FoodDao;
import org.alexander.database.tables.foodsnack.FoodSnack;
import org.alexander.database.tables.snack.Snack;
import org.alexander.database.tables.snack.dao.SnackDao;
import org.alexander.logging.CentralLogger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FoodSnackDao implements FoodSnackDaoInterface, TableDaoTwo {
    private final CentralLogger logger = CentralLogger.getInstance();
    /**
     * Check if a FoodSnack entry exists based on foodName and snackId.
     * @param foodName String
     * @param snackId Integer
     * @return true if the entry exists, false otherwise
     * @param <T> Type parameter to enforce correct types
     * @throws IllegalArgumentException if the argument types are incorrect
     */
    @Override
    public <T> boolean contains(T foodName, T snackId) {
        if (!(foodName instanceof String) && !(snackId instanceof Integer)) {
            logger.logError(new IllegalArgumentException("Invalid argument types for FoodSnackDao.contains method, returning false"));
            return false;
        }
        String query = "SELECT 1 FROM FOOD_SNACK WHERE name = ? AND snack_id = ? LIMIT 1";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            assert foodName instanceof String;
            preparedStatement.setString(1, (String) foodName);
            preparedStatement.setInt(2, (Integer) snackId);
            return preparedStatement.executeQuery().next();
        } catch (SQLException e) {
            logger.logError(e);
        }
        return false;
    }

    @Override
    public FoodSnack addFoodSnack(String foodName, Integer snackId, Double num_servings) {
        if (snackId == null) {
            logger.logError(new IllegalArgumentException("Snack ID cannot be null."));
            return null;
        }
        if (!existenceCheck(foodName, snackId)) {
            return null;
        } else if (contains(foodName, snackId)) {
            logger.logWarning("Junction already exists. Cannot add duplicate.");
            return null;
        }
        String query;
        if (num_servings == null) {
            query = "INSERT INTO FOOD_SNACK (name, snack_id) VALUES (?, ?)";
        } else {
            query = "INSERT INTO FOOD_SNACK (name, snack_id, num_servings) VALUES (?, ?, ?)";
        }
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            preparedStatement.setString(1, foodName);
            preparedStatement.setInt(2, snackId);
            if (num_servings != null) {
                QueryHelper.checkNull(preparedStatement, 3, num_servings);
            }
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 1) {
                return new FoodSnack(foodName, snackId, num_servings);
            }
            throw new SQLException("FoodSnackDao.addSnack: INSERTION EFFECTED MULTIPLE ROWS ON INSERT, RETURNING NULL, ROWS AFFECTED: " + rowsAffected);
        } catch (SQLException e) {
            logger.logError(e);
        }
        return null;
    }

    @Override
    public FoodSnack addFoodSnack(String foodName, Integer snackId) {
        return addFoodSnack(foodName, snackId, 1.0); // Default to 1 serving
    }

    @Override
    public FoodSnack addFoodSnack(Food food, Snack snack, Double num_servings) {
        return addFoodSnack(food.getName(), snack.getId(), num_servings);
    }

    @Override
    public FoodSnack addFoodSnack(FoodSnack foodSnack) {
        return addFoodSnack(foodSnack.getFoodName(), foodSnack.getSnackId(), foodSnack.getNumServings());
    }

    @Override
    public boolean deleteFoodSnack(String foodName, Integer snackId) {
        if (snackId == null) {
            logger.logError(new IllegalArgumentException("Snack ID cannot be null."));
            return false;
        }
        if (!contains(foodName, snackId)) {
            logger.logWarning("FoodSnack does not exist. Cannot delete junction.");
            return false;
        }
        String query = "DELETE FROM FOOD_SNACK WHERE name = ? AND snack_id = ?";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            preparedStatement.setString(1, foodName);
            preparedStatement.setInt(2, snackId);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.logError(e);
        }
        return false;
    }

    @Override
    public boolean deleteFoodSnack(FoodSnack foodSnack) {
        return deleteFoodSnack(foodSnack.getFoodName(), foodSnack.getSnackId());
    }

    @Override
    public boolean deleteFoodSnack(Food food, Snack snack) {
        return deleteFoodSnack(food.getName(), snack.getId());
    }

    @Override
    public FoodSnack getFoodSnack(String foodName, Integer snackId) {
        if (snackId == null) {
            logger.logError(new IllegalArgumentException("Snack ID cannot be null."));
            return null;
        }
        if (!contains(foodName, snackId)) {
            logger.logWarning("FoodSnack does not exist. Cannot retrieve junction.");
            return null;
        }
        String query = "SELECT name, snack_id, num_servings FROM FOOD_SNACK WHERE name = ? AND snack_id = ? LIMIT 1";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            preparedStatement.setString(1, foodName);
            preparedStatement.setInt(2, snackId);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Double num_servings = resultSet.getObject("num_servings", Double.class);
                return new FoodSnack(foodName, snackId, num_servings);
            }
        } catch (SQLException e) {
            logger.logError(e);
        }
        return null;
    }

    @Override
    public FoodSnack updateFoodSnack(String foodName, Integer snackId, Double num_servings) {
        if (!contains(foodName, snackId)) {
            logger.logWarning("Junction does not exist. Cannot update non-existent junction.");
            return null;
        }
        String query = "UPDATE FOOD_SNACK SET num_servings = ? WHERE name = ? AND snack_id = ?";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            QueryHelper.checkNull(preparedStatement, 1, num_servings);
            preparedStatement.setString(2, foodName);
            preparedStatement.setInt(3, snackId);
            if (preparedStatement.executeUpdate() > 0) {
                return new FoodSnack(foodName, snackId, num_servings);
            }
        } catch (SQLException e) {
            logger.logError(e);
        }
        return null;
    }

    @Override
    public FoodSnack getFoodSnack(Food food, Snack snack) {
        return getFoodSnack(food.getName(), snack.getId());
    }

    @Override
    public FoodSnack getFoodSnack(FoodSnack foodSnack) {
        return getFoodSnack(foodSnack.getFoodName(), foodSnack.getSnackId());
    }

    @Override
    public List<FoodSnack> getFoodSnackList() {
        ArrayList<FoodSnack> foodSnackList = new ArrayList<>();
        String query = "SELECT name, snack_id, num_servings FROM FOOD_SNACK";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query);
                var resultSet = preparedStatement.executeQuery()
        ) {
            while (resultSet.next()) {
                foodSnackList.add(getFoodSnackFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            logger.logError(e);
        }
        return foodSnackList;
    }

    private boolean existenceCheck(String foodName, int snackId) {
        FoodDao foodDao = new FoodDao();
        SnackDao snackDao = new SnackDao();
        boolean foodExists = foodDao.contains(foodName, "name");
        boolean snackExists = snackDao.contains(snackId);
        if (!foodExists || !snackExists) {
            if (!foodExists && !snackExists) {
                logger.logError(new IllegalArgumentException("Food and Snack do not exist. Cannot create junction."));
            } else if (!foodExists) {
                logger.logError(new IllegalArgumentException("Food does not exist. Cannot create junction."));
            } else {
                logger.logError(new IllegalArgumentException("Snack does not exist. Cannot create junction."));
            }
            return false;
        }
        return true;
    }

    private FoodSnack getFoodSnackFromResultSet(java.sql.ResultSet rs) throws SQLException {
        String foodName = rs.getString("name");
        Integer snackId = rs.getInt("snack_id");
        Double numServings = rs.getObject("num_servings", Double.class);
        return new FoodSnack(foodName, snackId, numServings);

    }
}