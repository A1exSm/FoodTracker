package org.alexander.database;

import java.sql.*;

class TableConstructor {
    public void construct() {
        try (Connection conn = DatabaseManager.connect()) {
            // Check connection
            if (conn == null) {
                throw new SQLException("Error creating database connection, connection is null");
            }
            // Function Execution
            dropTables(conn);
            createTables(conn);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void dropTables(Connection conn) throws SQLException {
        try(java.sql.Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT name AS table_name FROM sqlite_master WHERE type='table'");
            while (rs.next()) {
                String tableName = rs.getString("table_name");
                System.out.println("Debug: Dropped table " + tableName);
                stmt.executeUpdate("DROP TABLE IF EXISTS " + tableName);
            }
        }
    }
    // Creates all necessary tables through a single batch execution
    private void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.addBatch(createWeekTable());
            stmt.addBatch(createDayTable());
            stmt.addBatch(createMealTable());
            stmt.addBatch(createSnackTable());
            stmt.addBatch(createFoodTable());
            stmt.addBatch(createFoodTypeTable());
            stmt.addBatch(createFoodTypeJunctionFoodTable());
            stmt.addBatch(createFoodMealTable());
            stmt.addBatch(createFoodSnackTable());
            stmt.executeBatch();
        }
    }
    // Tables seperated into their own methods for readability and maintainability
    private String createWeekTable(){
        return """
            CREATE TABLE IF NOT EXISTS WEEK (
                week_id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,
                start_date DATE NOT NULL UNIQUE,
                end_date DATE NOT NULL UNIQUE
                );
            """;
    }
    private String createDayTable() {
        return """
            CREATE TABLE IF NOT EXISTS DAY (
                date DATE PRIMARY KEY UNIQUE,
                week_id INTEGER NOT NULL,
                body_weight REAL,
                FOREIGN KEY (week_id) REFERENCES WEEK(week_id)
                );
            """;
    }
    private String createMealTable() {
        return """
            CREATE TABLE IF NOT EXISTS MEAL (
                id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,
                date DATE NOT NULL,
                type VARCHAR(9) NOT NULL,
                time TIME,
                FOREIGN KEY (date) REFERENCES DAY(date)
                );
            """;
    }
    private String createSnackTable() {
        return """
            CREATE TABLE IF NOT EXISTS SNACK (
                id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,
                date DATE NOT NULL,
                time TIME,
                FOREIGN KEY (date) REFERENCES DAY(date)
                );
            """;
    }
    private String createFoodTable() {
        return """
            CREATE TABLE IF NOT EXISTS FOOD (
                name VARCHAR(250) PRIMARY KEY NOT NULL UNIQUE,
                serving_size_grams REAL,
                serving_size_calories REAL
                );
            """;
    }
    private String createFoodTypeTable() {
        return """
            CREATE TABLE IF NOT EXISTS FOOD_TYPE (
                name VARCHAR(12) PRIMARY KEY NOT NULL UNIQUE
                );
            """;
    }
    private String createFoodTypeJunctionFoodTable() {
        return """
            CREATE TABLE IF NOT EXISTS FOOD_TYPE_JUNCTION_FOOD (
                name VARCHAR(250) NOT NULL,
                type VARCHAR(12) NOT NULL,
                PRIMARY KEY (name, type),
                FOREIGN KEY (name) REFERENCES FOOD(name),
                FOREIGN KEY (type) REFERENCES FOOD_TYPE(name)
                );
            """;
    }
    private String createFoodMealTable(){
        return """
            CREATE TABLE IF NOT EXISTS FOOD_MEAL (
                name VARCHAR(250) NOT NULL,
                meal_id INTEGER NOT NULL,
                num_servings REAL NOT NULL default 1,
                PRIMARY KEY (name, meal_id),
                FOREIGN KEY (name) REFERENCES FOOD(name),
                FOREIGN KEY (meal_id) REFERENCES MEAL(id)
                );
            """;
    }
    private String createFoodSnackTable(){
        return """
            CREATE TABLE IF NOT EXISTS FOOD_SNACK (
                name VARCHAR(250) NOT NULL,
                snack_id INTEGER NOT NULL,
                num_servings REAL NOT NULL default 1,
                PRIMARY KEY (name, snack_id),
                FOREIGN KEY (name) REFERENCES FOOD(name),
                FOREIGN KEY (snack_id) REFERENCES SNACK(id)
                );
            """;
    }
}
