package org.alexander.gui;

import org.alexander.AppState;
import org.alexander.database.DatabaseComparer;
import org.alexander.database.DatabaseManager;
import org.alexander.gui.dialogs.ChangesSummaryDialog;
import org.alexander.logging.CentralLogger;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AppFrame extends JFrame {
    public AppFrame() {
        setTitle("Food Tracker");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                closeOperation();
            }
        });
    }

    /**
     * Handles the application closing sequence. It closes the database connection,
     * compares the live and saved databases, shows a summary of changes if any exist,
     * and prompts the user to save, discard, or cancel.
     */
    public void closeOperation() {
        // Ensure the database connection is closed to release the file lock
        DatabaseManager.closeConnection();

        Path workingDbPath = Paths.get(System.getProperty("user.home"), "FoodTracker", "database.sqlite");
        Path savedDbPath = Paths.get(System.getProperty("user.home"), "FoodTracker", "data.sqlite");

        try {
            DatabaseComparer comparer = new DatabaseComparer(workingDbPath, savedDbPath);
            comparer.compare();

            ChangesSummaryDialog summaryDialog = new ChangesSummaryDialog(this, comparer.getAdditions(), comparer.getDeletions());
            int result = summaryDialog.showDialog();

            if (result == JOptionPane.YES_OPTION) {
                AppState.saveDB = true;
                dispose();
                System.exit(0);
            } else if (result == JOptionPane.NO_OPTION) {
                AppState.saveDB = false;
                dispose();
                System.exit(0);
            }
                // If CANCEL_OPTION, do nothing and keep the app open.

        } catch (Exception ex) {
            CentralLogger.getInstance().logError("Failed to compare databases on exit: " + ex.getMessage());
            // Fallback to the original simple dialog on error
            int confirmed = JOptionPane.showConfirmDialog(
                    this,
                    "Could not determine changes. Save before exit?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_CANCEL_OPTION
            );
            if (confirmed != JOptionPane.CANCEL_OPTION) {
                AppState.saveDB = (confirmed == JOptionPane.YES_OPTION);
                dispose();
                System.exit(0);
            }
        }
    }

    public void maximize() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
}