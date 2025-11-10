package org.alexander.gui.dialogs;

import org.alexander.database.tables.meal.Meal;
import org.alexander.database.tables.snack.Snack;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog for viewing meal or snack details in a read-only format.
 * Displays day of week, time, and type information.
 */
public class ViewMealSnackDialog extends JDialog {
    
    /**
     * Constructs a ViewMealSnackDialog for viewing a meal's details.
     *
     * @param owner The parent window
     * @param meal  The meal to view
     */
    public ViewMealSnackDialog(Window owner, Meal meal) {
        super(owner, "View Meal Details", ModalityType.APPLICATION_MODAL);
        setupUI(owner, meal.getDate().getDayOfWeek().toString(), 
                meal.getTime().toString(), meal.getType().toString());
    }

    /**
     * Constructs a ViewMealSnackDialog for viewing a snack's details.
     *
     * @param owner The parent window
     * @param snack The snack to view
     */
    public ViewMealSnackDialog(Window owner, Snack snack) {
        super(owner, "View Snack Details", ModalityType.APPLICATION_MODAL);
        setupUI(owner, snack.getDate().getDayOfWeek().toString(), 
                snack.getTime().toString(), "SNACK");
    }

    /**
     * Sets up the user interface components.
     *
     * @param owner     The parent window for positioning
     * @param dayOfWeek The day of week string
     * @param time      The time string
     * @param type      The type string (meal type or "SNACK")
     */
    private void setupUI(Window owner, String dayOfWeek, String time, String type) {
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        add(contentPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Day of Week
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel dayLabel = new JLabel("Day of Week:");
        dayLabel.setFont(dayLabel.getFont().deriveFont(Font.BOLD));
        contentPanel.add(dayLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        contentPanel.add(new JLabel(dayOfWeek), gbc);

        // Time
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel timeLabel = new JLabel("Time:");
        timeLabel.setFont(timeLabel.getFont().deriveFont(Font.BOLD));
        contentPanel.add(timeLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        contentPanel.add(new JLabel(time), gbc);

        // Type
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel typeLabel = new JLabel("Type:");
        typeLabel.setFont(typeLabel.getFont().deriveFont(Font.BOLD));
        contentPanel.add(typeLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        contentPanel.add(new JLabel(type), gbc);

        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
        setVisible(true);
    }
}
