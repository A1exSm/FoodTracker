package org.alexander.database;

import org.alexander.logging.CentralLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class QueryHelper {
    private static final CentralLogger logger = CentralLogger.getInstance();
    /**
     * adds an entity of type String to a table, given the attribute to insert into.
     * Validation/whitelisting of table and attribute names is the responsibility of the caller.
     * Should NOT be called on user input without validation/whitelisting
     * @param entity the entity to add
     * @param attribute the attribute/column to insert into
     * @param table the table to insert into
     * @return true if the entity was added, false otherwise
     */
    public static boolean addEntity(String entity, String attribute, String table) {
        String query = "INSERT INTO " + table + " (" + attribute + ") VALUES (?)";
        try (
                var conn = DatabaseManager.connect();
                PreparedStatement stmt = conn.prepareStatement(query)
        ) {
            stmt.setString(1, entity);
            return stmt.executeUpdate() > 0; // affected rows > 0
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
    /**
     * adds an integer entity to a table, given the attribute to insert into.
     * Validation/whitelisting of table and attribute names is the responsibility of the caller.
     * Should NOT be called on user input without validation/whitelisting
     * @param entity the entity to add
     * @param attribute the attribute/column to insert into
     * @param table the table to insert into
     * @return true if the entity was added, false otherwise
     */
    public static boolean addEntity(int entity, String attribute, String table) {
        String query = "INSERT INTO " + table + " (" + attribute + ") VALUES (?)";
        try (
                var conn = DatabaseManager.connect();
                PreparedStatement stmt = conn.prepareStatement(query)
        ) {
            stmt.setInt(1, entity);
            return stmt.executeUpdate() > 0; // affected rows > 0
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    /**
     * deletes an integer entity from a table, given the attribute to match.
     * Validation/whitelisting of table and attribute names is the responsibility of the caller.
     * Should NOT be called on user input without validation/whitelisting
     * @param entity the entity to delete
     * @param attribute the attribute/column to match
     * @param table the table to delete from
     * @return true if the entity was deleted, false otherwise
     */
    public static <T> boolean deleteEntity(T entity, String attribute, String table) {
        String query = "DELETE FROM " + table + " WHERE " + attribute + " = ?";
        try (
                var conn = DatabaseManager.connect();
                PreparedStatement stmt = conn.prepareStatement(query)
        ) {
            if (typeSwitcher(stmt, entity, 1)) {
                if (stmt.executeUpdate() > 0) {
                    return true; // affected rows > 0
                } else {
                    logger.logWarning("No rows affected when trying to delete entity: " + entity);
                    return false;
                }
            } else {
                logger.logError("Unsupported entity type: " + entity.getClass().getName());
                return false;
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    /**
     * checks if a table exists in the database
     * @param tableName the name of the table to check
     * @return true if the table exists, false otherwise
     */
    public static boolean tableExists(String tableName) {
        String query = "SELECT name AS table_name FROM sqlite_master WHERE type='table' AND name=?";
        try (
                Connection conn = DatabaseManager.connect();
                java.sql.PreparedStatement stmt = conn.prepareStatement(query)
        ) {
            stmt.setString(1, tableName);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
    /**
     * checks if an entity exists in a table, given the attribute to match.
     * Validation/whitelisting of table and attribute names is the responsibility of the caller.
     * Should NOT be called on user input without validation/whitelisting
     * @param entityName the entity to check
     * @param colName the attribute/column to match
     * @param tableName the table to check in
     * @return true if the entity exists, false otherwise
     * @throws IllegalArgumentException if the table does not exist
     */
    public static <T> boolean entityExists(T entityName, String colName, String tableName) {
        if (!tableExists(tableName)) {
            throw new IllegalArgumentException("Table " + tableName + " does not exist");
        }
        String query = String.format("SELECT 1 FROM %s WHERE %s = ? LIMIT 1", tableName, colName);
        try (
                Connection conn = DatabaseManager.connect();
                PreparedStatement stmt = conn.prepareStatement(query)
        ) {
            if (typeSwitcher(stmt, entityName, 1)) {
                return stmt.executeQuery().next();
            } else {
                logger.logError("Unsupported entity type: " + entityName.getClass().getName());
                return false;
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    private static <T> boolean typeSwitcher(PreparedStatement preparedStatement, T entityName, int parameterIndex) throws SQLException {
        switch (entityName) {
            case Integer intName -> preparedStatement.setInt(parameterIndex, intName);
            case String stringName -> preparedStatement.setString(parameterIndex, stringName);
            case Double doubleName -> preparedStatement.setDouble(parameterIndex, doubleName);
            case LocalDate date -> {
                regexCheckDateFormat(date);
                preparedStatement.setDate(parameterIndex, java.sql.Date.valueOf(date));
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    /**
     * checks if a date string is formatted correctly as yyyy-MM-dd
     * @param date the date string to check
     * @throws IllegalArgumentException if the date is not formatted correctly
     */
    public static void regexCheckDateFormat(String date) {
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("Date is not formatted correctly: " + date + ". Expected format: yyyy-MM-dd");
        }
    }
    /**
     * checks if a LocalDate is formatted correctly as yyyy-MM-dd
     * @param date the LocalDate to check
     * @throws IllegalArgumentException if the date is not formatted correctly
     */
    public static void regexCheckDateFormat(LocalDate date) {
        regexCheckDateFormat(date.toString());
    }

    /**
     * retrieves all entities from a table for a given attribute/column.
     * Validation/whitelisting of table and attribute names is the responsibility of the caller.
     * Should NOT be called on user input without validation/whitelisting
     * @param attribute the attribute/column to retrieve
     * @param tableName the table to retrieve from
     * @return a list of entities, or an empty list if none found
     * @throws IllegalArgumentException if the table does not exist
     */
    public static List<String> getEntities(String attribute, String tableName) {
        if (!tableExists(tableName)) {
            throw new IllegalArgumentException("Table " + tableName + " does not exist");
        }
        List<String> resultsList = new java.util.ArrayList<>();
        try (
                var conn = DatabaseManager.connect();
                Statement stmt = conn.createStatement()
        ) {
            var rs = stmt.executeQuery("SELECT " + attribute + " FROM " + tableName);
            while (rs.next()) {
                resultsList.add( rs.getString(attribute));
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return resultsList;
    }

    public static void checkNull( PreparedStatement statement, int index, Double value) throws SQLException {
        if (value == null) {
            statement.setNull(index, java.sql.Types.DOUBLE);
        } else {
            statement.setDouble(index, value);
        }
    }

}
