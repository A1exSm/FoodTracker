package org.alexander.gui;

import javax.swing.*;

public class ToolBar extends JToolBar {
    JButton exitButton;
    public ToolBar() {
        super();
        exitButton = new JButton("Exit");
        exitButton.addActionListener(e->{
            int response = JOptionPane.showConfirmDialog(this.getParent(), "Exiting Application!", "", JOptionPane.OK_CANCEL_OPTION);
            if (response == JOptionPane.OK_OPTION) {
                System.exit(0);
            }
        });
        add(exitButton);
    }
}
