package org.alexander.gui.dialogs;

import org.alexander.database.tables.food.Food;
import org.alexander.database.tables.food.dao.FoodDao;
import org.alexander.database.tables.foodtype.FoodType;
import org.alexander.database.tables.foodtype.dao.FoodTypeDao;
import org.alexander.database.tables.foodtypefood.dao.FoodJunctionTypeDao;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class FoodDialog extends JDialog {
    private final JLabel foodNameLabel = new  JLabel("Food Name");
    private final JTextField newFoodName = new JTextField("Required");
    private final JTextField newFoodServing = new JTextField("00.00");
    private final JTextField newFoodCalories = new JTextField("0");
    private final JButton addFoodButton = new JButton("Add Food");
    private final JLabel addFoodNotification = new JLabel("");
    private final JLabel selectFoodType = new JLabel("Select Food Type:");
    private ArrayList<Checkbox> foodCheckboxes = new ArrayList<>();
    private ArrayList<FoodType> foodTypes = new ArrayList<>();
    private final FoodDao foodDao = new FoodDao();
    public FoodDialog(Window owner) {
        super(owner, "New Food");
        setModalityType(ModalityType.APPLICATION_MODAL);
        setLocationRelativeTo(owner);
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(6,2));
        add(contentPanel, BorderLayout.CENTER);
        contentPanel.add(foodNameLabel);
        newFoodName.setPreferredSize(new Dimension(300, newFoodName.getPreferredSize().height));
        contentPanel.add(newFoodName);
        contentPanel.add(new JLabel("Serving Grams:"));
        newFoodServing.setPreferredSize(new Dimension(50, newFoodServing.getPreferredSize().height));
        contentPanel.add(newFoodServing);
        contentPanel.add(new JLabel("Calories per serving:"));
        newFoodCalories.setPreferredSize(new Dimension(50, newFoodCalories.getPreferredSize().height));
        contentPanel.add(newFoodCalories);
        contentPanel.add(selectFoodType);
        JPanel selectType  = new JPanel();;
        FoodTypeDao foodTypeDao = new FoodTypeDao();
        for (FoodType type : foodTypeDao.getFoodTypeList()) {
            Checkbox checkbox = new Checkbox(type.getName());
            foodCheckboxes.add(checkbox);
            foodTypes.add(type);
            selectType.add(checkbox);
        }
        selectType.setLayout(new GridLayout(1, foodCheckboxes.size()));
        contentPanel.add(selectType);
        contentPanel.add(addFoodButton);
        contentPanel.add(addFoodNotification);
        addListeners();
        pack();
        setVisible(true);
    }

    private ArrayList<FoodType> getSelectedFoodTypes() {
        ArrayList<FoodType> selectedTypes = new ArrayList<>();
        for (int i = 0; i < foodCheckboxes.size(); i++) {
            if (foodCheckboxes.get(i).getState()) {
                selectedTypes.add(foodTypes.get(i));
            }
        }
        return selectedTypes;
    }


    private void addListeners() {
        newFoodName.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (newFoodName.getText().equals("Required")) {
                    newFoodName.setText("");
                }
            }
        });

        newFoodServing.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (newFoodServing.getText().equals("00.00")) {
                    newFoodServing.setText("");
                }
            }
        });

        newFoodCalories.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (newFoodCalories.getText().equals("0")) {
                    newFoodCalories.setText("");
                }
            }
        });

        addFoodButton.addActionListener(e -> {
            if (newFoodName.getText().equals("Required") || newFoodName.getText().isBlank()) {
                addFoodNotification.setForeground(Color.RED);
                foodNameLabel.setForeground(Color.RED);
                addFoodNotification.setText("Food Name is Required");
                return;
            }
            String name = newFoodName.getText().toLowerCase();
            if (foodDao.contains(name, "name")) {
                addFoodNotification.setForeground(Color.RED);
                foodNameLabel.setForeground(Color.RED);
                addFoodNotification.setText("Food with name " + name + " already exists");
                return;
            }
            foodNameLabel.setForeground(Color.BLACK);
            if (getSelectedFoodTypes().isEmpty()) {
                addFoodNotification.setForeground(Color.RED);
                selectFoodType.setForeground(Color.RED);
                addFoodNotification.setText("Select at least one Food Type");
                return;
            }
            Double calories = newFoodCalories.getText().equals("0") ? null : Double.parseDouble(newFoodCalories.getText());
            Double servingGrams = newFoodServing.getText().equals("00.00") ? null : Double.parseDouble(newFoodServing.getText());
            Food food = foodDao.addFood(new Food(name, servingGrams, calories));
            FoodJunctionTypeDao foodJunctionTypeDao = new FoodJunctionTypeDao();
            for (FoodType type : getSelectedFoodTypes()) {
                foodJunctionTypeDao.addFoodTypeFood(food, type);
            }
            this.dispose();
        });
    }
}
