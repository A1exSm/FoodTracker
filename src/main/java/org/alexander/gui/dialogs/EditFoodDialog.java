package org.alexander.gui.dialogs;

import org.alexander.database.tables.food.Food;
import org.alexander.database.tables.foodmeal.dao.FoodMealDao;
import org.alexander.database.tables.foodsnack.dao.FoodSnackDao;
import org.alexander.database.tables.meal.Meal;
import org.alexander.database.tables.snack.Snack;
import org.alexander.logging.CentralLogger;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * A dialog for editing the details of a food item within a meal or snack,
 * specifically the number of servings.
 */
public class EditFoodDialog extends JDialog {
    private final Food food;
    private final Object mealOrSnack;
    private final JTextField servingsField;
    private final JLabel calculatedCaloriesLabel;

    private final FoodMealDao foodMealDao = new FoodMealDao();
    private final FoodSnackDao foodSnackDao = new FoodSnackDao();

    /**
     * Constructs the dialog for editing food details.
     *
     * @param owner         The parent window.
     * @param food          The food item being edited.
     * @param mealOrSnack   The meal or snack context this food belongs to.
     * @param currentServings The current number of servings for the food item.
     */
    public EditFoodDialog(Window owner, Food food, Object mealOrSnack, double currentServings) {
        super(owner, "Food Details", ModalityType.APPLICATION_MODAL);
        this.food = food;
        this.mealOrSnack = mealOrSnack;

        setLayout(new BorderLayout(10, 10));
        JPanel contentPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Non-editable fields
        contentPanel.add(new JLabel("Food Name:"));
        contentPanel.add(new JLabel(food.getName()));
        contentPanel.add(new JLabel("Calories per Serving:"));
        contentPanel.add(new JLabel(String.format("%.2f", food.getServingCalories())));
        contentPanel.add(new JLabel("Serving Grams:"));
        contentPanel.add(new JLabel(String.format("%.2f", food.getServingGrams())));

        // Editable field
        contentPanel.add(new JLabel("Number of Servings:"));
        servingsField = new JTextField(String.valueOf(currentServings));
        contentPanel.add(servingsField);

        // Calculated field
        contentPanel.add(new JLabel("Total Calculated Calories:"));
        calculatedCaloriesLabel = new JLabel();
        contentPanel.add(calculatedCaloriesLabel);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton updateButton = new JButton("Update");
        JButton cancelButton = new JButton("Close");
        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);

        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add listeners
        servingsField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateCalculatedCalories(); }
            @Override public void removeUpdate(DocumentEvent e) { updateCalculatedCalories(); }
            @Override public void changedUpdate(DocumentEvent e) { updateCalculatedCalories(); }
        });

        updateButton.addActionListener(e -> onUpdate());
        cancelButton.addActionListener(e -> dispose());

        updateCalculatedCalories(); // Initial calculation
        pack();
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    /**
     * Updates the calculated calories label based on the current number of servings.
     */
    private void updateCalculatedCalories() {
        try {
            double servings = Double.parseDouble(servingsField.getText());
            double totalCalories = (food.getServingCalories() != null ? food.getServingCalories() : 0.0) * servings;
            calculatedCaloriesLabel.setText(String.format("%.2f", totalCalories));
        } catch (NumberFormatException e) {
            calculatedCaloriesLabel.setText("Invalid number");
        }
    }

    /**
     * Handles the update action, persisting the new number of servings to the database.
     */
    private void onUpdate() {
        try {
            double newServings = Double.parseDouble(servingsField.getText());
            if (mealOrSnack instanceof Meal meal) {
                foodMealDao.updateFoodMeal(food.getName(), meal.getId(), newServings);
            } else if (mealOrSnack instanceof Snack snack) {
                foodSnackDao.updateFoodSnack(food.getName(), snack.getId(), newServings);
            }
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for servings.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            CentralLogger.getInstance().logError("Failed to update serving size: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "An error occurred while updating.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}