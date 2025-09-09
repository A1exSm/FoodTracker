package org.alexander.database.foodtype.dao;

import org.alexander.database.QueryHelper;
import org.alexander.database.Tables;
import org.alexander.database.foodtype.FoodType;
import org.alexander.database.TableDao;

import java.util.ArrayList;
import java.util.List;

public class FoodTypeDao implements  FoodTypeDaoInterface, TableDao {
    private final String TABLE_NAME = Tables.FOOD_TYPE.name();
    /**
     * {@inheritDoc}
     */
    @Override
    public FoodType createFoodType(String name) {
        if (contains(name, "name")) {
            return null;
        }
       boolean queryResult = QueryHelper.addEntity(name, "name", TABLE_NAME);
        if (!queryResult) {
            System.err.println("QueryHelper.addEntity returned false when trying to add food type: " + name);
            return null;
        }
        return new FoodType(name);
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

    @Override
    public boolean deleteFoodType(FoodType foodType) {
        return deleteFoodType(foodType.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FoodType> getFoodTypeList() {
        List<FoodType> foodTypeList = new ArrayList<>();
        QueryHelper.getEntities("name", TABLE_NAME).forEach(key -> foodTypeList.add(new FoodType(key)));
        return foodTypeList;
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
