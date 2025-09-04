package org.alexander.database.dao;

import org.alexander.database.DatabaseManager;
import org.alexander.database.Tables;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class FoodTypeDao implements  FoodTypeDaoInterface{
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean createFoodType(String name) {
        if (!DatabaseManager.tableExists(Tables.FOOD_TYPE.name()) || DatabaseManager.entityExists(name, "name", Tables.FOOD_TYPE.name())) {
            return false;
        }
        String query = "INSERT INTO FOOD_TYPE (name) VALUES (?)";
        try (
                var conn = DatabaseManager.connect();
                PreparedStatement stmt = conn.prepareStatement(query)
                ) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteFoodType(String name) {
        return false;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getFoodTypes() throws SQLException {
        return List.of();
    }
}
