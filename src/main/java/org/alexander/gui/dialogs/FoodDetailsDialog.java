package org.alexander.gui.dialogs;

import org.alexander.database.tables.food.Food;
import org.alexander.database.tables.food.dao.FoodDao;
import org.alexander.database.tables.foodtype.FoodType;
import org.alexander.database.tables.foodtype.dao.FoodTypeDao;
import org.alexander.database.tables.foodtypefood.dao.FoodJunctionTypeDao;
import org.alexander.database.tables.foodmeal.dao.FoodMealDao;
import org.alexander.database.tables.foodsnack.dao.FoodSnackDao;
import org.alexander.gui.tab.WeekScrollTab;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dialog for viewing, editing, and deleting food items.
 * Provides functionality to:
 * - View food details (name, serving size, calories, food types)
 * - Edit food properties
 * - Delete food with cascade deletion of all associations
 */
public class FoodDetailsDialog extends JDialog {
    private final Food food;
    private final JTextField nameField;
    private final JTextField servingGramsField;
    private final JTextField servingCaloriesField;
    private final Map<FoodType, JCheckBox> foodTypeCheckboxes = new HashMap<>();
    private final JLabel errorLabel = new JLabel(" ");
    
    private final FoodDao foodDao = new FoodDao();
    private final FoodTypeDao foodTypeDao = new FoodTypeDao();
    private final FoodJunctionTypeDao foodJunctionTypeDao = new FoodJunctionTypeDao();
    private final FoodMealDao foodMealDao = new FoodMealDao();
    private final FoodSnackDao foodSnackDao = new FoodSnackDao();

    /**
     * Constructs a FoodDetailsDialog for viewing and editing a food item.
     *
     * @param owner The parent window
     * @param food  The food item to view/edit
     */
    public FoodDetailsDialog(Window owner, Food food) {
        super(owner, "Food Details", ModalityType.APPLICATION_MODAL);
        this.food = food;
        
        // Initialize text fields with current values
        nameField = new JTextField(food.getName());
        nameField.setEditable(false); // Food name cannot be changed (it's the primary key)
        servingGramsField = new JTextField(
                food.getServingGrams() != null ? String.valueOf(food.getServingGrams()) : "");
        servingCaloriesField = new JTextField(
                food.getServingCalories() != null ? String.valueOf(food.getServingCalories()) : "");
        
        setupUI(owner);
        loadFoodTypes();
    }

    /**
     * Sets up the user interface components.
     *
     * @param owner The parent window for positioning
     */
    private void setupUI(Window owner) {
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        add(contentPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Food Name (read-only)
        gbc.gridx = 0; gbc.gridy = 0;
        contentPanel.add(new JLabel("Food Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        contentPanel.add(nameField, gbc);

        // Serving Grams
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        contentPanel.add(new JLabel("Serving (grams):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        contentPanel.add(servingGramsField, gbc);

        // Calories per serving
        gbc.gridx = 0; gbc.gridy = 2;
        contentPanel.add(new JLabel("Calories per serving:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        contentPanel.add(servingCaloriesField, gbc);

        // Food Types Checkboxes
        gbc.gridx = 0; gbc.gridy = 3;
        contentPanel.add(new JLabel("Food Type(s):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        JPanel selectTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        
        for (FoodType type : foodTypeDao.getFoodTypeList()) {
            JCheckBox checkbox = new JCheckBox(type.getName());
            foodTypeCheckboxes.put(type, checkbox);
            selectTypePanel.add(checkbox);
        }
        contentPanel.add(selectTypePanel, gbc);

        // Error Label
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        errorLabel.setForeground(Color.RED);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(errorLabel, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton saveButton = new JButton("Save");
        JButton deleteButton = new JButton("Delete");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> onSave());
        deleteButton.addActionListener(e -> onDelete());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    /**
     * Loads the current food types for the food and checks the appropriate checkboxes.
     */
    private void loadFoodTypes() {
        List<FoodType> currentTypes = foodJunctionTypeDao.getTypes(food);
        for (FoodType type : currentTypes) {
            JCheckBox checkbox = foodTypeCheckboxes.get(type);
            if (checkbox != null) {
                checkbox.setSelected(true);
            }
        }
    }

    /**
     * Gets the list of selected food types from the checkboxes.
     *
     * @return List of selected FoodType objects
     */
    private List<FoodType> getSelectedFoodTypes() {
        List<FoodType> selectedTypes = new ArrayList<>();
        for (Map.Entry<FoodType, JCheckBox> entry : foodTypeCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                selectedTypes.add(entry.getKey());
            }
        }
        return selectedTypes;
    }

    /**
     * Handles the save action, updating the food's properties in the database.
     */
    private void onSave() {
        errorLabel.setText(" "); // Reset error message
        
        // Validate at least one food type is selected
        List<FoodType> selectedTypes = getSelectedFoodTypes();
        if (selectedTypes.isEmpty()) {
            errorLabel.setText("At least one food type must be selected.");
            return;
        }

        // Parse and validate serving grams
        Double servingGrams = null;
        try {
            String gramsText = servingGramsField.getText().trim();
            if (!gramsText.isEmpty()) {
                servingGrams = Double.parseDouble(gramsText);
                if (servingGrams <= 0) {
                    errorLabel.setText("Serving size must be positive.");
                    return;
                }
            }
        } catch (NumberFormatException ex) {
            errorLabel.setText("Serving size must be a valid number.");
            return;
        }

        // Parse and validate serving calories
        Double servingCalories = null;
        try {
            String caloriesText = servingCaloriesField.getText().trim();
            if (!caloriesText.isEmpty()) {
                servingCalories = Double.parseDouble(caloriesText);
                if (servingCalories < 0) {
                    errorLabel.setText("Calories cannot be negative.");
                    return;
                }
            }
        } catch (NumberFormatException ex) {
            errorLabel.setText("Calories must be a valid number.");
            return;
        }

        // Update the food in the database
        Food updatedFood = foodDao.updateFood(food.getName(), servingGrams, servingCalories);
        if (updatedFood == null) {
            errorLabel.setText("Error: Could not update food in the database.");
            return;
        }

        // Update food types
        // First, get current types
        List<FoodType> currentTypes = foodJunctionTypeDao.getTypes(food);
        
        // Remove types that are no longer selected
        for (FoodType type : currentTypes) {
            if (!selectedTypes.contains(type)) {
                foodJunctionTypeDao.deleteFoodTypeFood(food.getName(), type.getName());
            }
        }
        
        // Add new selected types
        for (FoodType type : selectedTypes) {
            if (!currentTypes.contains(type)) {
                foodJunctionTypeDao.addFoodTypeFood(food, type);
            }
        }

        // Refresh all DayPanels if WeekManager is accessible
        refreshAllDayPanels();
        
        dispose();
    }

    /**
     * Handles the delete action with confirmation.
     * Performs cascade deletion of all food associations.
     */
    private void onDelete() {
        String message = String.format(
                "This will delete '%s' and remove it from all meals and snacks.\n" +
                "This cannot be undone. Are you sure?", 
                food.getName());
        
        int confirm = JOptionPane.showConfirmDialog(
                this,
                message,
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            try {
                // Delete all FOOD_MEAL associations
                foodMealDao.getFoodMealList().stream()
                        .filter(fm -> fm.getFoodName().equals(food.getName()))
                        .forEach(fm -> foodMealDao.deleteFoodMeal(fm.getFoodName(), fm.getMealId()));

                // Delete all FOOD_SNACK associations
                foodSnackDao.getFoodSnackList().stream()
                        .filter(fs -> fs.getFoodName().equals(food.getName()))
                        .forEach(fs -> foodSnackDao.deleteFoodSnack(fs.getFoodName(), fs.getSnackId()));

                // Delete all FOOD_TYPE_JUNCTION_FOOD associations
                List<FoodType> currentTypes = foodJunctionTypeDao.getTypes(food);
                for (FoodType type : currentTypes) {
                    foodJunctionTypeDao.deleteFoodTypeFood(food.getName(), type.getName());
                }

                // Finally, delete the food itself
                boolean deleted = foodDao.deleteFood(food.getName());
                
                if (!deleted) {
                    errorLabel.setText("Error: Could not delete food from database.");
                    return;
                }

                // Refresh all DayPanels
                refreshAllDayPanels();
                
                JOptionPane.showMessageDialog(
                        this,
                        "Food deleted successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                
                dispose();
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    /**
     * Attempts to refresh all visible DayPanels by finding open WeekScrollTabs.
     * This ensures the UI is updated after food changes.
     */
    private void refreshAllDayPanels() {
        // Try to find the JTabbedPane containing WeekScrollTabs
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (owner instanceof JFrame frame) {
            Container contentPane = frame.getContentPane();
            refreshDayPanelsRecursive(contentPane);
        }
    }

    /**
     * Recursively searches for JTabbedPane with WeekScrollTabs and refreshes their day panels.
     *
     * @param container The container to search
     */
    private void refreshDayPanelsRecursive(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTabbedPane tabbedPane) {
                // Found the tabbed pane, refresh all WeekScrollTabs
                for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                    Component tab = tabbedPane.getComponentAt(i);
                    if (tab instanceof WeekScrollTab weekScrollTab) {
                        // Refresh all days in this week tab
                        for (java.time.DayOfWeek dayOfWeek : java.time.DayOfWeek.values()) {
                            weekScrollTab.refreshDay(dayOfWeek);
                        }
                    }
                }
                return; // Found and processed, no need to continue
            } else if (comp instanceof Container childContainer) {
                refreshDayPanelsRecursive(childContainer);
            }
        }
    }
}
