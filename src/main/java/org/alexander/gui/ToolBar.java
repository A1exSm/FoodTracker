package org.alexander.gui;

import org.alexander.gui.tab.WeekManager;

import javax.swing.*;

public class ToolBar extends JToolBar {
    private final JButton exitButton;
    private final JButton nextButton;
    private final JButton previousButton;
    private final WeekManager weekManager;
    // TODO: Make a button in the toolbar to sort the tabs by date.
    public ToolBar(WeekManager weekManager) {
        super();
        this.weekManager = weekManager;
        exitButton = new JButton("Exit");
        nextButton = new JButton("Next Week");
        previousButton = new JButton("Previous Week");
        addListeners();
        add(exitButton);
        add(previousButton);
        add(nextButton);
    }

    protected void addListeners() {
        exitButton.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(this.getParent(), "Exiting Application!", "", JOptionPane.OK_CANCEL_OPTION);
            if (response == JOptionPane.OK_OPTION) {
                System.exit(0);
            }
        });
        previousButton.addActionListener(e -> {
            weekManager.openPreviousWeek();
        });

        nextButton.addActionListener(e -> {
            weekManager.openNextWeek();
        });
    }
}
