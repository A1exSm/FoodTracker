package org.alexander.gui;

import org.alexander.AppState;

import javax.swing.*;
import java.awt.event.WindowAdapter;

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

    public void closeOperation() {
        int confirmed = JOptionPane.showConfirmDialog(
                null,
                "Save before exit?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION
        );
        AppState.saveDB = (confirmed == JOptionPane.YES_OPTION);
        System.out.println(AppState.saveDB);
        dispose();
        System.exit(0);
    }

    public void maximize() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
}
