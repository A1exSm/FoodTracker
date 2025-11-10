package org.alexander.gui.dialogs;

import org.alexander.database.tables.food.Food;
import org.alexander.database.tables.food.dao.FoodDao;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

/**
 * Dialog for selecting an existing food item from a searchable list.
 * Provides functionality to view/edit the selected food's details.
 */
public class SelectFoodDialog extends JDialog {
    private final JTextField searchField = new JTextField();
    private final DefaultListModel<Food> listModel = new DefaultListModel<>();
    private final JList<Food> foodList;
    private final List<Food> allFoods;
    private final FoodDao foodDao = new FoodDao();

    /**
     * Constructs a SelectFoodDialog for searching and selecting food items.
     *
     * @param owner The parent window
     */
    public SelectFoodDialog(Window owner) {
        super(owner, "Select Food", ModalityType.APPLICATION_MODAL);
        
        // Load and sort all foods alphabetically
        allFoods = foodDao.getFoodList();
        allFoods.sort(Comparator.comparing(Food::getName, String.CASE_INSENSITIVE_ORDER));
        
        // Initialize the list with all foods
        allFoods.forEach(listModel::addElement);
        
        // Create the food list with custom renderer
        foodList = new JList<>(listModel);
        foodList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        foodList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                Component renderer = super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof Food food) {
                    setText(food.getName());
                }
                return renderer;
            }
        });
        
        setupUI(owner);
    }

    /**
     * Sets up the user interface components.
     *
     * @param owner The parent window for positioning
     */
    private void setupUI(Window owner) {
        setLayout(new BorderLayout(10, 10));
        
        // Search panel at the top
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        // Add search listener for real-time filtering
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterFoodList(); }
            @Override public void removeUpdate(DocumentEvent e) { filterFoodList(); }
            @Override public void changedUpdate(DocumentEvent e) { filterFoodList(); }
        });
        
        // Food list in the center with scroll pane
        JScrollPane scrollPane = new JScrollPane(foodList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // Button panel at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton viewEditButton = new JButton("View/Edit");
        JButton cancelButton = new JButton("Cancel");
        
        viewEditButton.addActionListener(e -> onViewEdit());
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(viewEditButton);
        buttonPanel.add(cancelButton);
        
        // Add all components to dialog
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        setSize(400, 500);
        setLocationRelativeTo(owner);
    }

    /**
     * Filters the food list based on the search field text.
     * Uses case-insensitive substring matching.
     */
    private void filterFoodList() {
        String filter = searchField.getText().toLowerCase();
        listModel.clear();
        for (Food food : allFoods) {
            if (food.getName().toLowerCase().contains(filter)) {
                listModel.addElement(food);
            }
        }
    }

    /**
     * Opens the FoodDetailsDialog for the selected food item.
     * Shows a warning if no food is selected.
     */
    private void onViewEdit() {
        Food selectedFood = foodList.getSelectedValue();
        if (selectedFood != null) {
            new FoodDetailsDialog(this, selectedFood);
            // Refresh the food list in case it was edited
            refreshFoodList();
        } else {
            JOptionPane.showMessageDialog(this, 
                    "Please select a food item to view/edit.", 
                    "No Selection", 
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Refreshes the food list from the database.
     * Maintains the current search filter.
     */
    private void refreshFoodList() {
        allFoods.clear();
        allFoods.addAll(foodDao.getFoodList());
        allFoods.sort(Comparator.comparing(Food::getName, String.CASE_INSENSITIVE_ORDER));
        filterFoodList();
    }
}
