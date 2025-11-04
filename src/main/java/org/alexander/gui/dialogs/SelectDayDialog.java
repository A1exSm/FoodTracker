package org.alexander.gui.dialogs;

import org.alexander.gui.tab.WeekScrollTab;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.DayOfWeek;
import java.util.HashMap;

public class SelectDayDialog extends JDialog {
    private final JPanel daysPanel = new JPanel();
    private final HashMap<DayOfWeek, JButton> dayButtons = new HashMap<>();
    private DayOfWeek selectedDay;
    public SelectDayDialog(WeekScrollTab weekScrollTab) {
        super(SwingUtilities.getWindowAncestor(weekScrollTab), "Select Day");
        setModalityType(ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(getMinimumSize().width, 90));
        setLocationRelativeTo(SwingUtilities.getWindowAncestor(weekScrollTab));
        daysPanel.setLayout(new FlowLayout());
        daysPanel.setBackground(Color.BLACK);
        add(new Label("Select Day:"), BorderLayout.NORTH);
        add(daysPanel, BorderLayout.CENTER);
        buttonsInit(weekScrollTab);
        componentListener();
        pack();
        this.setVisible(true);
    }

    public SelectDayDialog(Window owner) {
        super(owner, "Select Day");
        setModalityType(ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(getMinimumSize().width, 90));
        setLocationRelativeTo(owner);
        daysPanel.setLayout(new FlowLayout());
        daysPanel.setBackground(Color.BLACK);
        add(new Label("Select Day:"), BorderLayout.NORTH);
        add(daysPanel, BorderLayout.CENTER);
        buttonsInit2();
        componentListener();
        pack();
        this.setVisible(true);
    }

    private void componentListener() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (getWidth() < 681) {
                    setMinimumSize(new Dimension(getMinimumSize().width, 95 + (7 - (getWidth()/95))*32));
                } else {
                    if (getMinimumSize().height != 95) setMinimumSize(new Dimension(getMinimumSize().width, 95));
                    if (getSize().width != 681) setSize(681, getSize().height);
                }
            }
        });
    }

    public DayOfWeek getSelectedDay() {
        return selectedDay;
    }


    private void buttonsInit2() {
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            JButton dayButton = new JButton(dayOfWeek.name());
            dayButtons.put(dayOfWeek, dayButton);
            dayButton.addActionListener(e -> {
                this.selectedDay = dayOfWeek;
                this.dispose();
            });
            daysPanel.add(dayButton);
        }
    }

    private void buttonsInit(WeekScrollTab weekScrollTab) {
        boolean[] availableDays = weekScrollTab.getAvailableDays();
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            JButton dayButton = new JButton(dayOfWeek.name());
            dayButton.setEnabled(availableDays[dayOfWeek.getValue() - 1]);
            dayButtons.put(dayOfWeek, dayButton);
            dayButton.addActionListener(e -> {
                this.selectedDay = dayOfWeek;
                this.dispose();
            });
           daysPanel.add(dayButton);
        }
    }
}
