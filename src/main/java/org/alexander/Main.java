package org.alexander;

import org.alexander.database.DatabaseManager;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Main class to test the database connection and operations.
 * @since 1.0.0
 */
public class Main {
    public static void main(String[] args) {
        DatabaseManager.initialise();
        try (var conn = DatabaseManager.connect()) {
            if (conn == null) {
                throw new SQLException("Cannot connect to database");
            }
            Statement stmt = conn.createStatement();
            var rs = stmt.executeQuery("SELECT name FROM FOOD_TYPE");
            while (rs.next()) {
                System.out.println(rs.getString("name"));
            }

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        DatabaseManager.save();
    }
}