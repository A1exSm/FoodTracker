package org.alexander.gui.dialogs;

import org.alexander.database.tables.food.Food;
import org.alexander.database.tables.food.dao.FoodDao;
import org.alexander.database.tables.foodtype.FoodType;
import org.alexander.database.tables.foodtype.dao.FoodTypeDao;
import org.alexander.database.tables.foodtypefood.dao.FoodJunctionTypeDao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class FoodDialog extends JDialog {
    private final JTextField newFoodName = new JTextField();
    private final JTextField newFoodServing = new JTextField();
    private final JTextField newFoodCalories = new JTextField();
    private final JButton addFoodButton = new JButton("Add Food");
    private final JLabel addFoodNotification = new JLabel(" ");
    private final ArrayList<JCheckBox> foodCheckboxes = new ArrayList<>();
    private final ArrayList<FoodType> foodTypes = new ArrayList<>();
    private final FoodDao foodDao = new FoodDao();
    private Food newFood = null; // To store the newly created food

    /**
     * Constructs a modal dialog for creating a new food item.
     * The dialog is designed with a clean, spacious layout.
     *
     * @param owner The Window from which the dialog is displayed.
     */
    public FoodDialog(Window owner) {
        super(owner, "Create New Food");
        setModalityType(ModalityType.APPLICATION_MODAL);
        setLocationRelativeTo(owner);
        // Use GridBagLayout for flexible and clean alignment
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15)); // Add padding around the dialog
        add(contentPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Spacing between components
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Food Name
        gbc.gridx = 0; gbc.gridy = 0;
        contentPanel.add(new JLabel("Food Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        contentPanel.add(newFoodName, gbc);

        // Serving Grams
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        contentPanel.add(new JLabel("Serving (grams):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        contentPanel.add(newFoodServing, gbc);

        // Calories
        gbc.gridx = 0; gbc.gridy = 2;
        contentPanel.add(new JLabel("Calories per serving:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        contentPanel.add(newFoodCalories, gbc);

        // Food Types Checkboxes
        gbc.gridx = 0; gbc.gridy = 3;
        contentPanel.add(new JLabel("Food Type(s):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        JPanel selectTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        FoodTypeDao foodTypeDao = new FoodTypeDao();
        for (FoodType type : foodTypeDao.getFoodTypeList()) {
            JCheckBox checkbox = new JCheckBox(type.getName());
            foodCheckboxes.add(checkbox);
            foodTypes.add(type);
            selectTypePanel.add(checkbox);
        }
        contentPanel.add(selectTypePanel, gbc);

        // Notification Label
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        addFoodNotification.setForeground(Color.RED);
        addFoodNotification.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(addFoodNotification, gbc);

        // Add Food Button
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(addFoodButton, gbc);

        addListeners();
        pack();
        setResizable(false);
        setVisible(true); // This will block until the dialog is disposed
    }

    /**
     * Returns the food item that was created in the dialog.
     * @return The created {@link Food} object, or null if no food was created or the dialog was cancelled.
     */
    public Food getNewFood() {
        return newFood;
    }

    private ArrayList<FoodType> getSelectedFoodTypes() {
        ArrayList<FoodType> selectedTypes = new ArrayList<>();
        for (int i = 0; i < foodCheckboxes.size(); i++) {
            if (foodCheckboxes.get(i).isSelected()) {
                selectedTypes.add(foodTypes.get(i));
            }
        }
        return selectedTypes;
    }


    private void addListeners() {
        addFoodButton.addActionListener(e -> {
            addFoodNotification.setText(" "); // Reset notification
            String name = newFoodName.getText().trim().toLowerCase();

            if (name.isEmpty()) {
                addFoodNotification.setText("Food name is required.");
                return;
            }
            if (foodDao.contains(name, "name")) {
                addFoodNotification.setText("Food with this name already exists.");
                return;
            }
            if (getSelectedFoodTypes().isEmpty()) {
                addFoodNotification.setText("At least one food type must be selected.");
                return;
            }

            Double calories = null;
            try {
                if (!newFoodCalories.getText().trim().isEmpty()) {
                    calories = Double.parseDouble(newFoodCalories.getText().trim());
                }
            } catch (NumberFormatException ex) {
                addFoodNotification.setText("Calories must be a valid number.");
                return;
            }

            Double servingGrams = null;
            try {
                if (!newFoodServing.getText().trim().isEmpty()) {
                    servingGrams = Double.parseDouble(newFoodServing.getText().trim());
                }
            } catch (NumberFormatException ex) {
                addFoodNotification.setText("Serving size must be a valid number.");
                return;
            }

            this.newFood = foodDao.addFood(name, servingGrams, calories);

            if (this.newFood != null) {
                FoodJunctionTypeDao foodJunctionTypeDao = new FoodJunctionTypeDao();
                for (FoodType type : getSelectedFoodTypes()) {
                    foodJunctionTypeDao.addFoodTypeFood(this.newFood, type);
                }
                this.dispose(); // Close the dialog on success
            } else {
                addFoodNotification.setText("Error: Could not save food to the database.");
            }
        });
    }
}