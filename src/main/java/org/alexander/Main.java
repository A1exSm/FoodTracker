package org.alexander;

import org.alexander.database.DatabaseManager;

 /**
 * Main class to test the database connection and operations.
 * @since 1.0.0
 */
public class Main {
    public static void main(String[] args) {
        DatabaseManager.initialise();
        DatabaseManager.save();
    }
}