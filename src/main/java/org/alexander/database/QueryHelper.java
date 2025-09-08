package org.alexander.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class QueryHelper {
    /**
     * adds an entity to a table, given the attribute to insert into.
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
     * deletes an entity from a table, given the attribute to match.
     * Validation/whitelisting of table and attribute names is the responsibility of the caller.
     * Should NOT be called on user input without validation/whitelisting
     * @param entity the entity to delete
     * @param attribute the attribute/column to match
     * @param table the table to delete from
     * @return true if the entity was deleted, false otherwise
     */
    public static boolean deleteEntity(String entity, String attribute, String table) {
        String query = "DELETE FROM " + table + " WHERE " + attribute + " = ?";
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
    public static boolean entityExists(String entityName, String colName, String tableName) {
        if (!tableExists(tableName)) {
            throw new IllegalArgumentException("Table " + tableName + " does not exist");
        }
        String query = String.format("SELECT 1 FROM %s WHERE %s = ? LIMIT 1", tableName, colName);
        try (
                Connection conn = DatabaseManager.connect();
                PreparedStatement stmt = conn.prepareStatement(query)
        ) {
            stmt.setString(1, entityName);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
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

}
