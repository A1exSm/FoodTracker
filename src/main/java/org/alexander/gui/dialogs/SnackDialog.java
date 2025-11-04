package org.alexander.gui.dialogs;

import org.alexander.database.tables.day.Day;
import org.alexander.database.tables.day.dao.DayDao;
import org.alexander.database.tables.meal.MealTypes;
import org.alexander.database.tables.snack.dao.SnackDao;
import org.alexander.database.tables.week.Week;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public class SnackDialog extends JDialog {
    private final DayDao dayDao = new DayDao();
    private Day day;
    private final Week week;
    private final JButton dayButton = new JButton();
    private final JComboBox<LocalTime> timeComboBox = new JComboBox<>();
    private final JLabel infoLabel = new JLabel("Select the day and time for the new snack.");
    public SnackDialog(Window owner, Week week) {
        super(owner, "New Snack");
        setModalityType(ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(400, 90));
        setLocationRelativeTo(owner);
        this.week = week;
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(3,2));
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

        JLabel timeLabel = new JLabel("Time: ");
        contentPanel.add(timeLabel);
        contentPanel.add(timeComboBox);
        setTime();
        JButton addSnackButton = new JButton("Add Snack");
        contentPanel.add(addSnackButton);
        contentPanel.add(infoLabel);
        addSnackButton.addActionListener(e -> {
            SnackDao snackDao = new SnackDao();
            snackDao.addSnack(day, (LocalTime) timeComboBox.getSelectedItem());
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
        LocalTime current = LocalTime.MIDNIGHT.plusMinutes(15);
        timeComboBox.addItem(LocalTime.MIDNIGHT);
        while (current != LocalTime.MIDNIGHT) {
            timeComboBox.addItem(current);
            current = current.plusMinutes(15);
        }
        for (int i = 0; i < timeComboBox.getItemCount(); i++) {
            if (timeComboBox.getItemAt(i).isAfter(LocalTime.now())) {
                timeComboBox.setSelectedIndex(i);
                return;
            }
        }
    }
}
