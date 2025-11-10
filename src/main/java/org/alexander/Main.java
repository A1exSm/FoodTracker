package org.alexander;

import com.formdev.flatlaf.FlatIntelliJLaf;
import org.alexander.database.DatabaseManager;
import org.alexander.gui.AppFrame;
import org.alexander.gui.GUIHandler;

import javax.swing.*;

/**
 * Main class for the Food Tracker application.
 * Initializes the database, sets up the graphical user interface,
 * and handles application startup and shutdown procedures.
 * @since 1.0.0
 */
public class Main {

    /**
     * The entry point of the Food Tracker application.
     * <p>
     * This method performs the following steps:
     * 1. Sets up the modern FlatLaf light look and feel for the entire application.
     * 2. Initializes the {@link DatabaseManager} to prepare file and database access.
     * 3. Sets up a runtime shutdown hook to handle database saving on exit, based on {@link AppState}.
     * 4. Creates the main application window ({@link AppFrame}).
     * 5. Initializes the {@link GUIHandler} to populate the frame with UI components.
     * 6. Makes the application window visible to the user.
     * </p>
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        // Set up the modern IntelliJ light look and feel for a clean, professional appearance
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Failed to set FlatLaf look and feel.");
        }

        DatabaseManager.initialise();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (AppState.saveDB) {
                DatabaseManager.save();
            }
        }));

        SwingUtilities.invokeLater(() -> {
            AppFrame appFrame = new AppFrame();
            GUIHandler handler = new GUIHandler(appFrame);
            appFrame.setVisible(true);
        });
    }

    /**
     * Pauses the execution of the current thread for a specified duration.
     * This is a utility method, primarily for debugging purposes.
     *
     * @param ms The time to wait in milliseconds.
     */
    public static void wait(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt(); // Restore the interrupted status
        }
    }
}