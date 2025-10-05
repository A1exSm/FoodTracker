package org.alexander;

import org.alexander.database.DatabaseManager;
import org.alexander.gui.AppFrame;
import org.alexander.gui.GUIHandler;

import javax.swing.*;

/**
 * Main class to test the database connection and operations.
 * @since 1.0.0
 */
public class Main {
    public static void main(String[] args) {
        DatabaseManager.initialise();
        AppFrame appFrame = new AppFrame();
        GUIHandler handler = new GUIHandler(appFrame);
        appFrame.setVisible(true);
        wait(2000);
        appFrame.maximize();
        wait(3000);
        appFrame.setSize(800,600);

//        DatabaseManager.save();
    }

    public static void wait(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException _) {}
    }
}