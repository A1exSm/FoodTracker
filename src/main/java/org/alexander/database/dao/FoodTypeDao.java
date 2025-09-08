package org.alexander.database.dao;

import org.alexander.database.DatabaseManager;
import org.alexander.database.QueryHelper;
import org.alexander.database.Tables;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class FoodTypeDao implements  FoodTypeDaoInterface, tableDao {
    private final String TABLE_NAME = Tables.FOOD_TYPE.name();
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean createFoodType(String name) {
        if (contains(name, "name")) {
            return false;
        }
       return QueryHelper.addEntity(name, "name", TABLE_NAME);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteFoodType(String name) {
        if (!contains(name, "name")) {
            return false;
        }
        return QueryHelper.deleteEntity(name, "name", TABLE_NAME);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getFoodTypes() {
        return QueryHelper.getEntities("name", TABLE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(String entity, String attribute) {
        validateAttribute(attribute);
        return QueryHelper.entityExists(entity, attribute, TABLE_NAME);
    }

    @Override
    public void validateAttribute(String attribute) {
        if (!attribute.equals("name")) {
            throw new IllegalArgumentException("Attribute " + attribute + " is not whitelisted for table " + TABLE_NAME);
        }
    }
}
