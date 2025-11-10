package org.alexander.database.tables.food.dao;

import org.alexander.database.DatabaseComparer;
import org.alexander.database.QueryHelper;
import org.alexander.database.tables.TableDao;
import org.alexander.database.tables.food.Food;
import org.alexander.logging.CentralLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FoodDao implements FoodDaoInterface, TableDao, DatabaseComparer.ReadOnlyDao<Food> {
    private final Connection conn;

    public FoodDao() { this.conn = null; }
    public FoodDao(Connection conn) { this.conn = conn; }

    private Connection getConnection() throws SQLException {
        if (conn != null && !conn.isClosed()) return conn;
        return org.alexander.database.DatabaseManager.connect();
    }

    @Override
    public Food addFood(String name, Double serving_grams, Double serving_calories) {
        if (contains(name, "name")) return null;
        String query = "INSERT INTO food (name, serving_size_grams, serving_size_calories) VALUES (?, ?, ?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
            ps.setString(1, name);
            QueryHelper.checkNull(ps, 2, serving_grams);
            QueryHelper.checkNull(ps, 3, serving_calories);
            if (ps.executeUpdate() > 0) return new Food(name, serving_grams, serving_calories);
        } catch (SQLException e) {
            CentralLogger.getInstance().logError(e);
        }
        return null;
    }

    @Override
    public Food addFood(Food food) {
        return addFood(food.getName(), food.getServingGrams(), food.getServingCalories());
    }

    @Override
    public boolean deleteFood(String name) {
        if (!contains(name, "name")) return false;
        return QueryHelper.deleteEntity(name, "name", "food");
    }

    @Override
    public boolean deleteFood(Food food) {
        return deleteFood(food.getName());
    }

    @Override
    public List<Food> getFoodList() {
        List<Food> foodList = new ArrayList<>();
        String query = "SELECT name, serving_size_grams, serving_size_calories FROM food";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(query); var rs = ps.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("name");
                double sg = rs.getDouble("serving_size_grams");
                Double servingGrams = rs.wasNull() ? null : sg;
                double sc = rs.getDouble("serving_size_calories");
                Double servingCalories = rs.wasNull() ? null : sc;
                foodList.add(new Food(name, servingGrams, servingCalories));
            }
        } catch (SQLException e) {
            CentralLogger.getInstance().logError(e);
        }
        return foodList;
    }

    @Override
    public Food getFood(String name) {
        if (!contains(name, "name")) return null;
        String query = "SELECT name, serving_size_grams, serving_size_calories FROM food WHERE name = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
            ps.setString(1, name);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    double sg = rs.getDouble("serving_size_grams");
                    Double servingGrams = rs.wasNull() ? null : sg;
                    double sc = rs.getDouble("serving_size_calories");
                    Double servingCalories = rs.wasNull() ? null : sc;
                    return new Food(name, servingGrams, servingCalories);
                }
            }
        } catch (SQLException e) {
            CentralLogger.getInstance().logError(e);
        }
        return null;
    }

    @Override
    public Food updateFood(String name, Double serving_grams, Double serving_calories) {
        if (!contains(name, "name")) return null;
        String query = "UPDATE FOOD SET serving_size_grams = ?, serving_size_calories = ? WHERE name = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
            QueryHelper.checkNull(ps, 1, serving_grams);
            QueryHelper.checkNull(ps, 2, serving_calories);
            ps.setString(3, name);
            if (ps.executeUpdate() > 0) return new Food(name, serving_grams, serving_calories);
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
        if (!attribute.equals("name")) throw new IllegalArgumentException("Invalid attribute: " + attribute);
    }

    @Override
    public List<Food> getAll() {
        return getFoodList();
    }
}