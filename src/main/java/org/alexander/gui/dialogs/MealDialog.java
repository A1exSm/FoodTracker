package org.alexander.gui.dialogs;

import org.alexander.database.tables.day.Day;
import org.alexander.database.tables.day.dao.DayDao;
import org.alexander.database.tables.meal.MealTypes;
import org.alexander.database.tables.meal.dao.MealDao;
import org.alexander.database.tables.week.Week;
import org.alexander.gui.tab.WeekManager;
import org.alexander.gui.tab.WeekScrollTab;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public class MealDialog extends JDialog {
    private final DayDao dayDao = new DayDao();
    private Day day;
    private final Week week;
    private final JButton dayButton = new JButton();
    private final JComboBox<MealTypes> dayComboBox = new JComboBox<>();
    private final JComboBox<LocalTime> timeComboBox = new JComboBox<>();
    private final JLabel infoLabel = new JLabel("Select the day and meal type for the new meal.");
    public MealDialog(Window owner, Week week) {
        super(owner, "New Meal");
        setModalityType(ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(400, 90));
        setLocationRelativeTo(owner);
        this.week = week;
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(4,2));
        add(contentPanel, BorderLayout.CENTER);
        JLabel dayLabel = new JLabel("Selected Day: ");
        contentPanel.add(dayLabel);
        contentPanel.add(dayButton);
        setDayOfWeek(LocalDate.now().getDayOfWeek());
        dayButton.addActionListener(e -> {
            DayOfWeek tempDOW = new SelectDayDialog(owner).getSelectedDay();
            if (tempDOW == null) {
                dispose();
                return;
            }
            setDayOfWeek(tempDOW);
        });
        JLabel mealTypeLabel = new JLabel("Meal: ");
        contentPanel.add(mealTypeLabel);
        for (MealTypes mealType : MealTypes.values()) {
            dayComboBox.addItem(mealType);
        }
        contentPanel.add(dayComboBox);
        dayComboBox.addActionListener(e -> setTime());
        JLabel timeLabel = new JLabel("Time: ");
        contentPanel.add(timeLabel);
        setTime();
        contentPanel.add(timeComboBox);
        JButton addMealButton = new JButton("Add Meal");
        contentPanel.add(addMealButton);
        contentPanel.add(infoLabel);
        addMealButton.addActionListener(e -> {
            MealDao mealDao = new MealDao();
            mealDao.addMeal(day, (MealTypes) dayComboBox.getSelectedItem(), (LocalTime) timeComboBox.getSelectedItem());
            dispose();
        });
        pack();
        setVisible(true);
    }

    private void setDayOfWeek(DayOfWeek dayOfWeek) {
        LocalDate localDate = week.getStartDate().plusDays(dayOfWeek.getValue() - 1);
        if (dayDao.contains(localDate.toString(), "date")) {
            day = dayDao.getDay(localDate);
        } else {
            day = dayDao.addDay(localDate);
        }
        dayButton.setText(dayOfWeek.toString());
    }

    private void setTime() {
        timeComboBox.removeAllItems();
        MealTypes mealType = (MealTypes) dayComboBox.getSelectedItem();
        LocalTime startTime = mealType.getStartTime();
        LocalTime endTime = mealType.getEndTime();
        LocalTime now = LocalTime.now();
        LocalTime time = startTime;
        while (time.isBefore(endTime)) {
            timeComboBox.addItem(time);
            time = time.plusMinutes(30);
        }
        for (int i = 0; i < timeComboBox.getItemCount(); i++) {
            LocalTime t = timeComboBox.getItemAt(i);
            if (t.equals(now) || (t.isAfter(now) && i > 0 && timeComboBox.getItemAt(i - 1).isBefore(now))) {
                timeComboBox.setSelectedIndex(i);
                break;
            }
        }
    }
}
