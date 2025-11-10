package org.alexander.gui.dialogs;

import org.alexander.database.tables.day.Day;
import org.alexander.database.tables.day.dao.DayDao;
import org.alexander.database.tables.meal.MealTypes;
import org.alexander.database.tables.meal.dao.MealDao;
import org.alexander.database.tables.week.Week;
import org.alexander.logging.CentralLogger;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public class MealDialog extends JDialog {
    private final DayDao dayDao = new DayDao();
    private LocalDate date;
    private final Week week;
    private final JButton dayButton = new JButton();
    private final JComboBox<MealTypes> dayComboBox = new JComboBox<>();
    private final JComboBox<LocalTime> timeComboBox = new JComboBox<>();
    private final JLabel infoLabel = new JLabel("Select the meal type and time.");

    /**
     * Constructor used from the toolbar, requires the user to select a day.
     * @param owner The parent window.
     * @param week The current week context.
     */
    public MealDialog(Window owner, Week week) {
        super(owner, "New Meal");
        this.week = week;
        setupUI(owner, false); // Day selection is enabled
        dayButton.addActionListener(e -> {
            DayOfWeek tempDOW = new SelectDayDialog(owner).getSelectedDay();
            if (tempDOW != null) {
                setDay(tempDOW);
            }
        });
        setDay(LocalDate.now().getDayOfWeek()); // Default to today
        setVisible(true);
    }

    /**
     * Constructor used from the DayPanel context menu, with the day pre-selected.
     * @param owner The parent window.
     * @param week The current week context.
     * @param initialDay The pre-selected day of the week.
     */
    public MealDialog(Window owner, Week week, DayOfWeek initialDay) {
        super(owner, "New Meal");
        this.week = week;
        setupUI(owner, true); // Day selection is disabled
        setDay(initialDay);
        setVisible(true);
    }

    private void setupUI(Window owner, boolean dayIsLocked) {
        setModalityType(ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(400, 90));
        setLocationRelativeTo(owner);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(4,2));
        add(contentPanel, BorderLayout.CENTER);

        contentPanel.add(new JLabel("Selected Day: "));
        contentPanel.add(dayButton);
        dayButton.setEnabled(!dayIsLocked);

        contentPanel.add(new JLabel("Meal: "));
        for (MealTypes mealType : MealTypes.values()) {
            dayComboBox.addItem(mealType);
        }
        contentPanel.add(dayComboBox);
        dayComboBox.addActionListener(e -> setTime());

        contentPanel.add(new JLabel("Time: "));
        contentPanel.add(timeComboBox);
        setTime();

        JButton addMealButton = new JButton("Add Meal");
        contentPanel.add(addMealButton);
        contentPanel.add(infoLabel);

        addMealButton.addActionListener(e -> onAddMeal());
        pack();
    }

    private void onAddMeal() {
        Day day = dayDao.getDay(this.date);
        if (day == null) {
            day = dayDao.addDay(this.date, week.getId());
        }

        if (day == null) {
            infoLabel.setText("Error creating day.");
            infoLabel.setForeground(Color.RED);
            CentralLogger.getInstance().logError("Failed to get or create a Day object for date: " + this.date);
            return;
        }

        MealDao mealDao = new MealDao();
        mealDao.addMeal(day, (MealTypes) dayComboBox.getSelectedItem(), (LocalTime) timeComboBox.getSelectedItem());
        dispose();
    }

    private void setDay(DayOfWeek dayOfWeek) {
        this.date = week.getStartDate().plusDays(dayOfWeek.getValue() - 1);
        dayButton.setText(dayOfWeek.toString());
    }

    private void setTime() {
        timeComboBox.removeAllItems();
        MealTypes mealType = (MealTypes) dayComboBox.getSelectedItem();
        if (mealType == null) return;
        LocalTime startTime = mealType.getStartTime();
        LocalTime endTime = mealType.getEndTime();
        LocalTime now = LocalTime.now();
        LocalTime time = startTime;
        while (time.isBefore(endTime)) {
            timeComboBox.addItem(time);
            time = time.plusMinutes(30);
        }
        timeComboBox.setSelectedItem(mealType.defaultTime());
    }
}