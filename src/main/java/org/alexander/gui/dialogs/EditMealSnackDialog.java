package org.alexander.gui.dialogs;

import org.alexander.database.tables.day.Day;
import org.alexander.database.tables.day.dao.DayDao;
import org.alexander.database.tables.meal.Meal;
import org.alexander.database.tables.meal.MealTypes;
import org.alexander.database.tables.meal.dao.MealDao;
import org.alexander.database.tables.snack.Snack;
import org.alexander.database.tables.snack.dao.SnackDao;
import org.alexander.database.tables.week.Week;
import org.alexander.gui.tab.WeekScrollTab;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for editing meal or snack properties.
 * Allows changing the day, time, and meal type (for meals only).
 * Handles moving meals/snacks between days and refreshing affected panels.
 */
public class EditMealSnackDialog extends JDialog {
    private final Object mealOrSnack;
    private final Week week;
    private final WeekScrollTab weekScrollTab;
    private final JButton dayButton;
    private final JComboBox<LocalTime> timeComboBox;
    private final JComboBox<MealTypes> mealTypeComboBox;
    private final JLabel errorLabel = new JLabel(" ");
    
    private LocalDate selectedDate;
    private final DayDao dayDao = new DayDao();
    private final MealDao mealDao = new MealDao();
    private final SnackDao snackDao = new SnackDao();

    /**
     * Constructs an EditMealSnackDialog for editing a meal's properties.
     *
     * @param owner         The parent window
     * @param meal          The meal to edit
     * @param week          The week context
     * @param weekScrollTab The week scroll tab for refreshing
     */
    public EditMealSnackDialog(Window owner, Meal meal, Week week, WeekScrollTab weekScrollTab) {
        super(owner, "Edit Meal", ModalityType.APPLICATION_MODAL);
        this.mealOrSnack = meal;
        this.week = week;
        this.weekScrollTab = weekScrollTab;
        this.selectedDate = meal.getDate();
        
        dayButton = new JButton();
        timeComboBox = new JComboBox<>();
        mealTypeComboBox = new JComboBox<>();
        
        // Populate meal types
        for (MealTypes type : MealTypes.values()) {
            mealTypeComboBox.addItem(type);
        }
        mealTypeComboBox.setSelectedItem(meal.getType());
        mealTypeComboBox.addActionListener(e -> populateTimeComboBox());
        
        setupUI(owner, true);
        updateDayButton();
        populateTimeComboBox();
        timeComboBox.setSelectedItem(meal.getTime());
    }

    /**
     * Constructs an EditMealSnackDialog for editing a snack's properties.
     *
     * @param owner         The parent window
     * @param snack         The snack to edit
     * @param week          The week context
     * @param weekScrollTab The week scroll tab for refreshing
     */
    public EditMealSnackDialog(Window owner, Snack snack, Week week, WeekScrollTab weekScrollTab) {
        super(owner, "Edit Snack", ModalityType.APPLICATION_MODAL);
        this.mealOrSnack = snack;
        this.week = week;
        this.weekScrollTab = weekScrollTab;
        this.selectedDate = snack.getDate();
        
        dayButton = new JButton();
        timeComboBox = new JComboBox<>();
        mealTypeComboBox = new JComboBox<>();
        
        setupUI(owner, false);
        updateDayButton();
        populateTimeComboBoxForSnack();
        timeComboBox.setSelectedItem(snack.getTime());
    }

    /**
     * Sets up the user interface components.
     *
     * @param owner  The parent window for positioning
     * @param isMeal True if editing a meal, false if editing a snack
     */
    private void setupUI(Window owner, boolean isMeal) {
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        add(contentPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Day selector
        gbc.gridx = 0; gbc.gridy = row;
        contentPanel.add(new JLabel("Day:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        dayButton.addActionListener(e -> selectDay());
        contentPanel.add(dayButton, gbc);
        row++;

        // Time selector
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        contentPanel.add(new JLabel("Time:"), gbc);
        gbc.gridx = 1; gbc.gridy = row;
        contentPanel.add(timeComboBox, gbc);
        row++;

        // Meal type selector (only for meals)
        if (isMeal) {
            gbc.gridx = 0; gbc.gridy = row;
            contentPanel.add(new JLabel("Meal Type:"), gbc);
            gbc.gridx = 1; gbc.gridy = row;
            contentPanel.add(mealTypeComboBox, gbc);
            row++;
        }

        // Error label
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        errorLabel.setForeground(Color.RED);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(errorLabel, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    /**
     * Opens a SelectDayDialog to choose a new day.
     */
    private void selectDay() {
        DayOfWeek selectedDayOfWeek = new SelectDayDialog(this).getSelectedDay();
        if (selectedDayOfWeek != null) {
            selectedDate = week.getStartDate().plusDays(selectedDayOfWeek.getValue() - 1);
            updateDayButton();
            populateTimeComboBox();
        }
    }

    /**
     * Updates the day button text to show the selected day of week.
     */
    private void updateDayButton() {
        dayButton.setText(selectedDate.getDayOfWeek().toString());
    }

    /**
     * Populates the time combo box based on the selected meal type.
     * Only applicable for meals.
     */
    private void populateTimeComboBox() {
        if (mealOrSnack instanceof Meal) {
            MealTypes selectedType = (MealTypes) mealTypeComboBox.getSelectedItem();
            if (selectedType == null) return;
            
            LocalTime currentSelection = (LocalTime) timeComboBox.getSelectedItem();
            timeComboBox.removeAllItems();
            
            LocalTime startTime = selectedType.getStartTime();
            LocalTime endTime = selectedType.getEndTime();
            LocalTime time = startTime;
            
            while (time.isBefore(endTime)) {
                timeComboBox.addItem(time);
                time = time.plusMinutes(30);
            }
            
            // Try to restore previous selection if it's still valid
            if (currentSelection != null && isTimeInRange(currentSelection, startTime, endTime)) {
                timeComboBox.setSelectedItem(currentSelection);
            } else {
                timeComboBox.setSelectedItem(selectedType.defaultTime());
            }
        }
    }

    /**
     * Populates the time combo box for snacks with 30-minute intervals throughout the day.
     */
    private void populateTimeComboBoxForSnack() {
        LocalTime currentSelection = (LocalTime) timeComboBox.getSelectedItem();
        timeComboBox.removeAllItems();
        
        LocalTime time = LocalTime.of(0, 0);
        LocalTime endTime = LocalTime.of(23, 30);
        
        while (!time.isAfter(endTime)) {
            timeComboBox.addItem(time);
            time = time.plusMinutes(30);
        }
        
        if (currentSelection != null) {
            timeComboBox.setSelectedItem(currentSelection);
        }
    }

    /**
     * Checks if a time is within the given range.
     *
     * @param time      The time to check
     * @param startTime The start of the range
     * @param endTime   The end of the range
     * @return True if the time is within range
     */
    private boolean isTimeInRange(LocalTime time, LocalTime startTime, LocalTime endTime) {
        return !time.isBefore(startTime) && time.isBefore(endTime);
    }

    /**
     * Handles the save action, updating the meal/snack in the database.
     */
    private void onSave() {
        errorLabel.setText(" "); // Reset error message
        
        LocalTime selectedTime = (LocalTime) timeComboBox.getSelectedItem();
        if (selectedTime == null) {
            errorLabel.setText("Please select a time.");
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        try {
            if (mealOrSnack instanceof Meal meal) {
                saveMeal(meal, selectedTime);
            } else if (mealOrSnack instanceof Snack snack) {
                saveSnack(snack, selectedTime);
            }
            dispose();
        } catch (Exception ex) {
            errorLabel.setText("Error: " + ex.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Saves the updated meal to the database.
     *
     * @param meal         The meal to update
     * @param selectedTime The new time
     */
    private void saveMeal(Meal meal, LocalTime selectedTime) {
        MealTypes selectedType = (MealTypes) mealTypeComboBox.getSelectedItem();
        LocalDate originalDate = meal.getDate();
        DayOfWeek originalDayOfWeek = originalDate.getDayOfWeek();
        DayOfWeek newDayOfWeek = selectedDate.getDayOfWeek();
        
        // Update the meal
        Meal updatedMeal = mealDao.updateMeal(meal.getId(), selectedDate, selectedTime, selectedType);
        
        if (updatedMeal == null) {
            errorLabel.setText("Error: Could not update meal in database.");
            return;
        }
        
        // Ensure the new day exists in the database
        if (!selectedDate.equals(originalDate)) {
            Day newDay = dayDao.getDay(selectedDate);
            if (newDay == null) {
                newDay = dayDao.addDay(selectedDate, week.getId());
                if (newDay == null) {
                    errorLabel.setText("Error: Could not create new day.");
                    return;
                }
            }
        }
        
        // Refresh affected day panels
        if (weekScrollTab != null) {
            weekScrollTab.refreshDay(originalDayOfWeek);
            if (!originalDayOfWeek.equals(newDayOfWeek)) {
                weekScrollTab.refreshDay(newDayOfWeek);
            }
        }
    }

    /**
     * Saves the updated snack to the database.
     *
     * @param snack        The snack to update
     * @param selectedTime The new time
     */
    private void saveSnack(Snack snack, LocalTime selectedTime) {
        LocalDate originalDate = snack.getDate();
        DayOfWeek originalDayOfWeek = originalDate.getDayOfWeek();
        DayOfWeek newDayOfWeek = selectedDate.getDayOfWeek();
        
        // Update the snack
        Snack updatedSnack = snackDao.updateSnack(snack.getId(), selectedDate, selectedTime);
        
        if (updatedSnack == null) {
            errorLabel.setText("Error: Could not update snack in database.");
            return;
        }
        
        // Ensure the new day exists in the database
        if (!selectedDate.equals(originalDate)) {
            Day newDay = dayDao.getDay(selectedDate);
            if (newDay == null) {
                newDay = dayDao.addDay(selectedDate, week.getId());
                if (newDay == null) {
                    errorLabel.setText("Error: Could not create new day.");
                    return;
                }
            }
        }
        
        // Refresh affected day panels
        if (weekScrollTab != null) {
            weekScrollTab.refreshDay(originalDayOfWeek);
            if (!originalDayOfWeek.equals(newDayOfWeek)) {
                weekScrollTab.refreshDay(newDayOfWeek);
            }
        }
    }
}
