package org.alexander.database.tables.food;

import org.alexander.database.QueryHelper;
import org.alexander.database.tables.TableDao;
import org.alexander.logging.CentralLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class FoodDao implements FoodDaoInterface, TableDao {
    @Override
    public Food addFood(String name, Double serving_grams, Double serving_calories) {
        if (contains(name, "name")) {
            System.out.println("Debug: Food with name '" + name + "' already exists.");
            return null;
        }
        String query = "INSERT INTO food (name, serving_size_grams, serving_size_calories) VALUES (?, ?, ?)";
        try (
                var conn = org.alexander.database.DatabaseManager.connect();
                PreparedStatement preparedStatement = conn.prepareStatement(query);
        ) {
            preparedStatement.setString(1, name);
            QueryHelper.checkNull(preparedStatement, 2, serving_grams);
            QueryHelper.checkNull(preparedStatement, 3, serving_calories);
            if (preparedStatement.executeUpdate() > 0) {
                return new Food(name, serving_grams, serving_calories);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    @Override
    public Food addFood(Food food) {
        return addFood(food.getName(), food.getServingGrams(), food.getServingCalories());
    }

    @Override
    public boolean deleteFood(String name) {
        if (!contains(name, "name")) {
            return false;
        }
        return org.alexander.database.QueryHelper.deleteEntity(name, "name", "food");
    }

    @Override
    public boolean deleteFood(Food food) {
        return deleteFood(food.getName());
    }

    @Override
    public List<Food> getFoodList() {
        List<Food> foodList = new java.util.ArrayList<>();
        String query = "SELECT name, serving_size_grams, serving_size_calories FROM food";
        try (
                Connection conn = org.alexander.database.DatabaseManager.connect();
                PreparedStatement preparedStatement = conn.prepareStatement(query);
                var resultSet = preparedStatement.executeQuery()
        ) {
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                Double servingGrams = resultSet.getObject("serving_size_grams", Double.class);
                Double servingCalories = resultSet.getObject("serving_size_calories", Double.class);
                foodList.add(new Food(name, servingGrams, servingCalories));
            }
        } catch (SQLException e) {
            CentralLogger.getInstance().logError(e);
        }
        return foodList;
    }

    @Override
    public Food getFood(String name) {
        if (!contains(name, "name")) {
            CentralLogger.getInstance().logWarning(String.format("Food with name '%s' does not exist.", name));
            return null;
        }
        String query = "SELECT name, serving_size_grams, serving_size_calories FROM food WHERE name = ?";
        try (
                Connection conn = org.alexander.database.DatabaseManager.connect();
                PreparedStatement preparedStatement = conn.prepareStatement(query)
        ) {
            preparedStatement.setString(1, name);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Double servingGrams = resultSet.getObject("serving_size_grams", Double.class);
                Double servingCalories = resultSet.getObject("serving_size_calories", Double.class);
                return new Food(name, servingGrams, servingCalories);
            }
        } catch (SQLException e) {
            CentralLogger.getInstance().logError(e);
        }
        return null;
    }

    @Override
    public Food updateFood(String name, Double serving_grams, Double serving_calories) {
        if (!contains(name, "name")) {
            CentralLogger.getInstance().logWarning(String.format("Food with name '%s' does not exist.", name));
            return null;
        }
        String query = "UPDATE FOOD SET serving_size_grams = ?, serving_size_calories = ? WHERE name = ?";
        try (
                var conn = org.alexander.database.DatabaseManager.connect();
                PreparedStatement preparedStatement = conn.prepareStatement(query);
        ) {
            QueryHelper.checkNull(preparedStatement, 1, serving_grams);
            QueryHelper.checkNull(preparedStatement, 2, serving_calories);
            preparedStatement.setString(3, name);
            if (preparedStatement.executeUpdate() > 0) {
                return new Food(name, serving_grams, serving_calories);
            }
        } catch (SQLException e) {
            CentralLogger.getInstance().logError(e);
        }
        return null;
    }

    @Override
    public boolean contains(String entity, String attribute) {
        validateAttribute(attribute);
        return QueryHelper.entityExists(entity, attribute, "FOOD");
    }

    @Override
    public void validateAttribute(String attribute) {
        if (!attribute.equals("name")) {
            throw new IllegalArgumentException("Invalid attribute: " + attribute);
        }
    }

}
