package org.alexander.gui;

import org.alexander.gui.dialogs.FoodDialog;
import org.alexander.gui.tab.WeekManager;

import javax.swing.*;

public class ToolBar extends JToolBar {
    private final JButton exitButton;
    private final JButton nextButton;
    private final JButton previousButton;
    private final JButton addDay;
    private final JButton addFood;
    private final WeekManager weekManager;
    private final AppFrame appFrame;

    /**
     * Constructs the main toolbar for the application.
     * @param weekManager The manager responsible for handling week tabs.
     * @param appFrame The main application frame.
     */
    public ToolBar(WeekManager weekManager, AppFrame appFrame) {
        super();
        this.appFrame = appFrame;
        this.weekManager = weekManager;
        exitButton = new JButton("Exit");
        nextButton = new JButton("Next Week");
        previousButton = new JButton("Previous Week");
        addDay = new JButton("Add Day");
        addFood = new JButton("Add New Food");
        addListeners();
        add(exitButton);
        add(previousButton);
        add(nextButton);
        add(addDay);
        add(addFood);
    }

    /**
     * Adds action listeners to the toolbar buttons.
     */
    protected void addListeners() {
        exitButton.addActionListener(e -> appFrame.closeOperation());
        previousButton.addActionListener(e -> weekManager.openPreviousWeek());
        nextButton.addActionListener(e -> weekManager.openNextWeek());
        addDay.addActionListener(e -> weekManager.getOpenTab().selectDay());
        addFood.addActionListener(e -> new FoodDialog(SwingUtilities.getWindowAncestor(appFrame)));
    }
}