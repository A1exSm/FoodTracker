package org.alexander.database.tables;

public interface TableDao {
    // TODO: Contains takes an entity which is an abstract class of an input to contains. Each table must have an input class that extends that abstract input class. Maybe changing the return type to some ints which map to constants like VALUE_NOT_FOUND, or type not recognised etc.
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
