package org.alexander.database.tables.foodtypefood.dao;

import org.alexander.database.DatabaseManager;
import org.alexander.database.QueryHelper;
import org.alexander.database.tables.TableDao;
import org.alexander.database.tables.food.Food;
import org.alexander.database.tables.food.dao.FoodDao;
import org.alexander.database.tables.foodtype.FoodType;
import org.alexander.database.tables.foodtype.dao.FoodTypeDao;
import org.alexander.database.tables.foodtypefood.FoodJunctionType;
import org.alexander.logging.CentralLogger;

import java.sql.*;
import java.util.List;

public class FoodJunctionTypeDao implements FoodJunctionTypeDaoInterface, TableDao {
    @Override
    public boolean contains(String name, String type) {
        if (name == null || type == null) {
            CentralLogger.getInstance().logWarning("FoodJunctionTypeDao.contains: name or type is null");
            return false;
        }
        String query = "SELECT 1 FROM FOOD_TYPE_JUNCTION_FOOD WHERE name = ? AND type = ? LIMIT 1";
        try (var conn = DatabaseManager.connect();
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, type);
            ResultSet rs = preparedStatement.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            CentralLogger.getInstance().logError(e);
        }
        return false;
    }

    @Override
    public void validateAttribute(String attribute) {
        if (!attribute.equals("name") && !attribute.equals("type")) {
            throw new IllegalArgumentException("Invalid attribute: " + attribute);
        }
    }

    @Override
    public FoodJunctionType addFoodTypeFood(Food food, FoodType type) {
        if (contains(food.getName(), type.getName())) {
            CentralLogger.getInstance().logWarning("Junction already exists. Cannot add duplicate.");
            return null;
        }
        checkInputsExist(food, type);
        String query = "INSERT INTO FOOD_TYPE_JUNCTION_FOOD (name, type) VALUES (?, ?)";
        try (
                var conn = DatabaseManager.connect();
                PreparedStatement preparedStatement = conn.prepareStatement(query);
                ) {
            preparedStatement.setString(1, food.getName());
            preparedStatement.setString(2, type.getName());
            if (preparedStatement.executeUpdate() > 0) {
                return new FoodJunctionType(food, type);
            }
        } catch (SQLException e) {
            CentralLogger.getInstance().logError(e);
        }
        return null;
    }

    private void checkInputsExist(Food food, FoodType type) {
        FoodTypeDao foodTypeDao = new FoodTypeDao();
        FoodDao foodDao = new FoodDao();
        boolean foodExists = foodDao.contains(food.getName(), "name");
        boolean typeExists = foodTypeDao.contains(type.getName(), "name");
        if (!foodExists || !typeExists) {
            if (!foodExists && !typeExists) {
                throw new IllegalArgumentException("Food and FoodType do not exist. Cannot create junction.");
            } else if (!foodExists) {
                throw new IllegalArgumentException("Food does not exist. Cannot create junction.");
            } else {
                throw new IllegalArgumentException("FoodType does not exist. Cannot create junction.");
            }
        }
    }

    @Override
    public boolean deleteFoodTypeFood(String name, String type) {
        if (!contains(name, type)) {
            CentralLogger.getInstance().logWarning("FoodType does not exist. Cannot delete junction.");
            return false;
        }
        String Query = "DELETE FROM FOOD_TYPE_JUNCTION_FOOD WHERE name = ? AND type = ?";
        try (
                var conn = DatabaseManager.connect();
                PreparedStatement preparedStatement = conn.prepareStatement(Query)
        ) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, type);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            CentralLogger.getInstance().logError(e);
        }
        return false;
    }

    @Override
    public boolean deleteFoodTypeFood(Food food, FoodType type) {
        return deleteFoodTypeFood(food.getName(), type.getName());
    }

    @Override
    public List<FoodType> getTypes(Food food) {
        List<FoodType> foodTypeList = new java.util.ArrayList<>();
        String query = "SELECT type FROM FOOD_TYPE_JUNCTION_FOOD WHERE name = ?";
        try (
                var conn = DatabaseManager.connect();
                PreparedStatement preparedStatement = conn.prepareStatement(query);
        ) {
            preparedStatement.setString(1, food.getName());
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                foodTypeList.add(new FoodType(rs.getString("type")));
            }
        } catch (SQLException e) {
            CentralLogger.getInstance().logError(e);
        }
        return foodTypeList;
    }
}
