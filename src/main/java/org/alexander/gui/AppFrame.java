package org.alexander.gui;

import javax.swing.*;

public class AppFrame extends JFrame {
    public AppFrame() {
        setTitle("Food Tracker");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        setVisible(true);
    }
}
