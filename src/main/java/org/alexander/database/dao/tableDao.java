package org.alexander.database.dao;

public interface tableDao {
    /**
     * Checks if a value with the given name exists in the table.
     * @param entity the entity to check
     * @param attribute the attribute/column to check against
     * @return true if the value exists, false otherwise
     */
    boolean contains(String entity, String attribute);
    /**
     * Validates that the given attribute is whitelisted for the table.
     * Should throw an IllegalArgumentException if the attribute is not valid.
     * @param attribute the attribute/column to validate
     */
    void validateAttribute(String attribute);
}
