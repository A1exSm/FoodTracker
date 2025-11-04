package org.alexander.gui;

import org.alexander.gui.dialogs.FoodDialog;
import org.alexander.gui.dialogs.MealDialog;
import org.alexander.gui.dialogs.SnackDialog;
import org.alexander.gui.tab.WeekManager;

import javax.swing.*;

public class ToolBar extends JToolBar {
    private final JButton exitButton;
    private final JButton nextButton;
    private final JButton previousButton;
    private final JButton addDay;
    private final JButton addFood;
    private final JButton addMeal;
    private final JButton addSnack;
    private final WeekManager weekManager;
    private final AppFrame appFrame;
    // TODO: Make a button in the toolbar to sort the tabs by date.
    public ToolBar(WeekManager weekManager, AppFrame appFrame) {
        super();
        this.appFrame = appFrame;
        this.weekManager = weekManager;
        exitButton = new JButton("Exit");
        nextButton = new JButton("Next Week");
        previousButton = new JButton("Previous Week");
        addDay = new JButton("Add Day");
        addFood = new JButton("Add New Food");
        addMeal = new JButton("Add New Meal");
        addSnack = new JButton("Add New Snack");
        addListeners();
        add(exitButton);
        add(previousButton);
        add(nextButton);
        add(addDay);
        add(addFood);
        add(addMeal);
        add(addSnack);
//        addDay.setEnabled(false);
    }

    protected void addListeners() {
        exitButton.addActionListener(e -> {
            appFrame.closeOperation();
        });
        previousButton.addActionListener(e -> {
            weekManager.openPreviousWeek();
        });

        nextButton.addActionListener(e -> {
            weekManager.openNextWeek();
        });

        addDay.addActionListener(e -> {
            weekManager.getOpenTab().selectDay();
        });

        addFood.addActionListener(e -> {
            new FoodDialog(SwingUtilities.getWindowAncestor(appFrame));
        });

        addMeal.addActionListener(e -> {
           new MealDialog(SwingUtilities.getWindowAncestor(appFrame), weekManager.getOpenTab().getWeek());
        });

        addSnack.addActionListener(e -> {
            new SnackDialog(SwingUtilities.getWindowAncestor(appFrame), weekManager.getOpenTab().getWeek());
        });
    }
}
