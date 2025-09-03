package org.alexander;

import org.alexander.database.DatabaseManager;
import org.alexander.database.FileManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Main class to test the database connection and operations.
 * @since 1.0.0
 */
public class Main {
    public static void main(String[] args) {
//        try (Connection conn = DatabaseManager.connect()) {
//            java.sql.Statement stmt = conn.createStatement();
//            stmt.addBatch("INSERT INTO DATA (name) VALUES ('Cabbage');");
//            stmt.executeBatch();
//
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }
        DatabaseManager.printTable("DATA");
//        FileManager.save();
    }
}