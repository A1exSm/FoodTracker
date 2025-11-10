package org.alexander.gui.tab;

import org.alexander.database.tables.day.Day;
import org.alexander.database.tables.food.Food;
import org.alexander.database.tables.food.dao.FoodDao;
import org.alexander.database.tables.foodmeal.FoodMeal;
import org.alexander.database.tables.foodmeal.dao.FoodMealDao;
import org.alexander.database.tables.foodsnack.FoodSnack;
import org.alexander.database.tables.foodsnack.dao.FoodSnackDao;
import org.alexander.database.tables.meal.Meal;
import org.alexander.database.tables.meal.dao.MealDao;
import org.alexander.database.tables.snack.Snack;
import org.alexander.database.tables.snack.dao.SnackDao;
import org.alexander.database.tables.week.Week;
import org.alexander.gui.dialogs.EditFoodDialog;
import org.alexander.gui.dialogs.FoodDialog;
import org.alexander.gui.dialogs.MealDialog;
import org.alexander.gui.dialogs.SnackDialog;
import org.alexander.gui.dialogs.ViewMealSnackDialog;
import org.alexander.gui.dialogs.EditMealSnackDialog;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a panel for a single day, displaying meals and snacks in a table.
 * It provides context menus for interacting with the data, such as adding or removing
 * meals, snacks, and food items.
 * The design is focused on a clean, spacious, and modern light theme.
 */
class DayPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private final TitledBorder titledBorder;
    private final Day day;
    private final Week week;
    private final FoodMealDao foodMealDao = new FoodMealDao();
    private final FoodSnackDao foodSnackDao = new FoodSnackDao();
    private final FoodDao foodDao = new FoodDao();
    private List<Object> mealAndSnackObjects = new ArrayList<>();
    private List<List<Food>> foodsForColumns = new ArrayList<>();

    /**
     * Constructs a DayPanel.
     * @param day The Day object this panel represents.
     * @param week The Week object this day belongs to, used for context in dialogs.
     */
    DayPanel(Day day, Week week) {
        super();
        this.day = day;
        this.week = week;
        titledBorder = BorderFactory.createTitledBorder(day.dayOfWeek.toString());
        init();
        table = new JTable();
        initTable();
        refreshTable();
        JScrollPane scrollPane = new JScrollPane(table);
        // Use a very light gray for the scroll pane background to blend with the panel
        scrollPane.getViewport().setBackground(new Color(250, 250, 250));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Initializes the panel's appearance and layout with generous padding for a spacious feel.
     */
    private void init() {
        titledBorder.setTitleFont(new Font("SansSerif", Font.BOLD, 18));
        titledBorder.setTitleColor(new Color(80, 80, 80));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15), // Increased padding
                titledBorder
        ));
        setBackground(new Color(250, 250, 250)); // A soft, off-white background
        setLayout(new BorderLayout());
    }

    /**
     * Initializes the JTable's properties, including renderers, increased row height,
     * and mouse listeners for context menus.
     */
    private void initTable() {
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);
        table.setBackground(new Color(255, 255, 255)); // White table background
        table.setForeground(new Color(51, 51, 51)); // Dark gray text for readability
        table.setGridColor(new Color(220, 220, 220)); // Light grid lines
        table.setRowHeight(30); // Increased row height for spacing
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setCellSelectionEnabled(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(200, 225, 255)); // A soft blue for selection
        table.setSelectionForeground(new Color(51, 51, 51));

        // Style the table header for a modern look
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(242, 242, 242)); // Light gray header
        header.setForeground(new Color(80, 80, 80)); // Darker gray text
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));


        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    Point point = e.getPoint();
                    int row = table.rowAtPoint(point);
                    int column = table.columnAtPoint(point);
                    if (column != -1 && row != -1) {
                        table.setColumnSelectionInterval(column, column);
                        table.setRowSelectionInterval(row, row);
                        showPopupMenu(e, row, column);
                    }
                }
            }
        });
    }

    /**
     * Displays a context-sensitive popup menu based on the clicked cell.
     * @param e The MouseEvent that triggered the menu.
     * @param row The row index of the clicked cell.
     * @param col The column index of the clicked cell.
     */
    private void showPopupMenu(MouseEvent e, int row, int col) {
        JPopupMenu popupMenu = new JPopupMenu();
        if (col >= mealAndSnackObjects.size()) {
            if (col == mealAndSnackObjects.size()) { // "Add New" column
                JMenuItem addMealItem = new JMenuItem("Add Meal");
                addMealItem.addActionListener(ae -> {
                    new MealDialog(SwingUtilities.getWindowAncestor(this), week, day.dayOfWeek);
                    refreshTable();
                });
                popupMenu.add(addMealItem);

                JMenuItem addSnackItem = new JMenuItem("Add Snack");
                addSnackItem.addActionListener(ae -> {
                    new SnackDialog(SwingUtilities.getWindowAncestor(this), week, day.dayOfWeek);
                    refreshTable();
                });
                popupMenu.add(addSnackItem);
            } else {
                return;
            }
        } else {
            Object mealOrSnackObject = mealAndSnackObjects.get(col);

            if (row == 0) { // Clicked on a Meal/Snack row
                JMenuItem viewDetailsItem = new JMenuItem("View Details");
                viewDetailsItem.addActionListener(ae -> viewMealSnackDetails(mealOrSnackObject));
                popupMenu.add(viewDetailsItem);

                JMenuItem editItem = new JMenuItem("Edit Meal/Snack");
                editItem.addActionListener(ae -> editMealSnack(mealOrSnackObject));
                popupMenu.add(editItem);

                JMenuItem deleteMealSnackItem = new JMenuItem("Delete Meal/Snack");
                deleteMealSnackItem.addActionListener(ae -> deleteMealOrSnack(mealOrSnackObject));
                popupMenu.add(deleteMealSnackItem);
            } else { // Clicked on a Food item or empty row
                Object value = table.getValueAt(row, col);
                if (value instanceof Food food) { // An existing food item
                    JMenuItem removeFoodItem = new JMenuItem("Remove Food");
                    removeFoodItem.addActionListener(ae -> removeFoodFromMealOrSnack(food, mealOrSnackObject));
                    popupMenu.add(removeFoodItem);

                    double numServings = getNumServings(food, mealOrSnackObject);

                    JMenuItem editDetailsItem = new JMenuItem("Details");
                    editDetailsItem.addActionListener(ae -> {
                        new EditFoodDialog(SwingUtilities.getWindowAncestor(this), food, mealOrSnackObject, numServings);
                        refreshTable(); // Refresh after dialog closes
                    });
                    popupMenu.add(editDetailsItem);
                } else { // An empty cell
                    JMenuItem addExistingFoodItem = new JMenuItem("Add Existing Food");
                    addExistingFoodItem.addActionListener(ae -> addExistingFoodToMealOrSnack(mealOrSnackObject));
                    popupMenu.add(addExistingFoodItem);

                    JMenuItem addNewFoodItem = new JMenuItem("Add New Food");
                    addNewFoodItem.addActionListener(ae -> addNewFoodToMealOrSnack(mealOrSnackObject));
                    popupMenu.add(addNewFoodItem);
                }
            }
        }
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Opens a dialog to select an existing food item to add to a meal or snack.
     * The search functionality is case-insensitive.
     * @param mealOrSnackObject The meal or snack to add the food to.
     */
    private void addExistingFoodToMealOrSnack(Object mealOrSnackObject) {
        List<Food> allFoods = foodDao.getFoodList();
        allFoods.sort(Comparator.comparing(Food::getName, String.CASE_INSENSITIVE_ORDER));

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Select Existing Food", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(300, 400);

        JTextField searchField = new JTextField();
        DefaultListModel<Food> listModel = new DefaultListModel<>();
        allFoods.forEach(listModel::addElement);

        JList<Food> foodJList = new JList<>(listModel);
        foodJList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Food food) {
                    setText(food.getName());
                }
                return renderer;
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }

            private void filter() {
                String filter = searchField.getText().toLowerCase();
                listModel.clear();
                for (Food food : allFoods) {
                    if (food.getName().toLowerCase().contains(filter)) {
                        listModel.addElement(food);
                    }
                }
            }
        });

        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> {
            Food selectedFood = foodJList.getSelectedValue();
            if (selectedFood != null) {
                addFoodToMealOrSnack(selectedFood, mealOrSnackObject);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select a food to add.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        dialog.add(searchPanel, BorderLayout.NORTH);
        dialog.add(new JScrollPane(foodJList), BorderLayout.CENTER);
        dialog.add(addButton, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
    }

    /**
     * Adds a food item to a meal or snack and refreshes the table.
     * @param food The food to add.
     * @param mealOrSnackObject The meal or snack to add the food to.
     */
    private void addFoodToMealOrSnack(Food food, Object mealOrSnackObject) {
        if (mealOrSnackObject instanceof Meal meal) {
            foodMealDao.addFoodMeal(food, meal, 1.0);
        } else if (mealOrSnackObject instanceof Snack snack) {
            foodSnackDao.addFoodSnack(food, snack, 1.0);
        }
        refreshTable();
    }

    /**
     * Opens a dialog to create a new food item and add it to the meal or snack.
     * @param mealOrSnackObject The meal or snack to add the new food to.
     */
    private void addNewFoodToMealOrSnack(Object mealOrSnackObject) {
        FoodDialog dialog = new FoodDialog(SwingUtilities.getWindowAncestor(this));
        Food newFood = dialog.getNewFood();
        if (newFood != null) {
            addFoodToMealOrSnack(newFood, mealOrSnackObject);
        }
    }
    /**
     * Retrieves the number of servings for a specific food within a meal or snack.
     * @param food The food item.
     * @param mealOrSnackObject The containing meal or snack.
     * @return The number of servings, or 1.0 as a default.
     */
    private double getNumServings(Food food, Object mealOrSnackObject) {
        if (mealOrSnackObject instanceof Meal meal) {
            FoodMeal fm = foodMealDao.getFoodMeal(food.getName(), meal.getId());
            if (fm != null && fm.getNumServings() != null) {
                return fm.getNumServings();
            }
        } else if (mealOrSnackObject instanceof Snack snack) {
            FoodSnack fs = foodSnackDao.getFoodSnack(food.getName(), snack.getId());
            if (fs != null && fs.getNumServings() != null) {
                return fs.getNumServings();
            }
        }
        return 1.0; // Default to 1.0 if not found
    }

    /**
     * Opens a dialog to view meal or snack details.
     * @param mealOrSnackObject The meal or snack to view.
     */
    private void viewMealSnackDetails(Object mealOrSnackObject) {
        if (mealOrSnackObject instanceof Meal meal) {
            new ViewMealSnackDialog(SwingUtilities.getWindowAncestor(this), meal);
        } else if (mealOrSnackObject instanceof Snack snack) {
            new ViewMealSnackDialog(SwingUtilities.getWindowAncestor(this), snack);
        }
    }

    /**
     * Opens a dialog to edit meal or snack properties.
     * @param mealOrSnackObject The meal or snack to edit.
     */
    private void editMealSnack(Object mealOrSnackObject) {
        // Find the WeekScrollTab parent
        Container parent = this.getParent();
        WeekScrollTab weekScrollTab = null;
        while (parent != null) {
            if (parent instanceof WeekScrollTab) {
                weekScrollTab = (WeekScrollTab) parent;
                break;
            }
            parent = parent.getParent();
        }

        if (weekScrollTab != null) {
            if (mealOrSnackObject instanceof Meal meal) {
                new EditMealSnackDialog(SwingUtilities.getWindowAncestor(this), meal, week, weekScrollTab);
            } else if (mealOrSnackObject instanceof Snack snack) {
                new EditMealSnackDialog(SwingUtilities.getWindowAncestor(this), snack, week, weekScrollTab);
            }
            refreshTable();
        }
    }

    /**
     * Deletes a meal or snack after user confirmation.
     * @param mealOrSnackObject The meal or snack to delete.
     */
    private void deleteMealOrSnack(Object mealOrSnackObject) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this item and all associated foods?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (mealOrSnackObject instanceof Meal meal) {
                // First delete associations in FOOD_MEAL
                foodMealDao.getFoodMealList().stream()
                        .filter(fm -> fm.getMealId() == meal.getId())
                        .forEach(foodMealDao::deleteFoodMeal);
                // Then delete the meal itself
                new MealDao().deleteMeal(meal);
            } else if (mealOrSnackObject instanceof Snack snack) {
                // First delete associations in FOOD_SNACK
                foodSnackDao.getFoodSnackList().stream()
                        .filter(fs -> fs.getSnackId() != null && fs.getSnackId().equals(snack.getId()))
                        .forEach(foodSnackDao::deleteFoodSnack);
                // Then delete the snack itself
                new SnackDao().deleteSnack(snack);
            }
            refreshTable();
        }
    }

    /**
     * Removes a food item from its containing meal or snack.
     * @param food The food to remove.
     * @param mealOrSnackObject The meal or snack to remove from.
     */
    private void removeFoodFromMealOrSnack(Food food, Object mealOrSnackObject) {
        if (mealOrSnackObject instanceof Meal meal) {
            foodMealDao.deleteFoodMeal(food.getName(), meal.getId());
        } else if (mealOrSnackObject instanceof Snack snack) {
            foodSnackDao.deleteFoodSnack(food.getName(), snack.getId());
        }
        refreshTable();
    }

    /**
     * Refreshes the entire table by re-fetching data from the database,
     * rebuilding the model, applying custom cell renderers, and resizing columns.
     */
    public void refreshTable() {
        MealDao mealDao = new MealDao();
        SnackDao snackDao = new SnackDao();

        List<Meal> meals = mealDao.getDayMeals(day);
        List<Snack> snacks = snackDao.getDaySnacks(day);

        mealAndSnackObjects = new ArrayList<>();
        mealAndSnackObjects.addAll(meals);
        mealAndSnackObjects.addAll(snacks);

        mealAndSnackObjects.sort(Comparator.comparing(o -> {
            if (o instanceof Meal) return ((Meal) o).getTime();
            if (o instanceof Snack) return ((Snack) o).getTime();
            return null;
        }, Comparator.nullsLast(Comparator.naturalOrder())));

        int colCount = mealAndSnackObjects.size() + 1; // +1 for the "Add New" column
        int maxFoods = 0;
        foodsForColumns = new ArrayList<>();

        for (Object obj : mealAndSnackObjects) {
            List<Food> foods = new ArrayList<>();
            if (obj instanceof Meal meal) {
                List<FoodMeal> foodMeals = foodMealDao.getFoodMealList().stream()
                        .filter(fm -> fm.getMealId() == meal.getId()).toList();
                for (FoodMeal fm : foodMeals) {
                    Food food = foodDao.getFood(fm.getFoodName());
                    if (food != null) foods.add(food);
                }
            } else if (obj instanceof Snack snack) {
                List<FoodSnack> foodSnacks = foodSnackDao.getFoodSnackList().stream()
                        .filter(fs -> fs.getSnackId() != null && fs.getSnackId().equals(snack.getId())).toList();
                for (FoodSnack fs : foodSnacks) {
                    Food food = foodDao.getFood(fs.getFoodName());
                    if (food != null) foods.add(food);
                }
            }
            foodsForColumns.add(foods);
            if (foods.size() > maxFoods) {
                maxFoods = foods.size();
            }
        }

        int rowCount = maxFoods + 2;
        model = new DefaultTableModel(rowCount, colCount) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setModel(model);

        for (int i = 0; i < mealAndSnackObjects.size(); i++) {
            Object obj = mealAndSnackObjects.get(i);
            List<Food> foods = foodsForColumns.get(i);

            if (obj instanceof Meal meal) {
                model.setValueAt(meal.getType().toString(), 0, i);
                table.getColumnModel().getColumn(i).setHeaderValue(meal.getTime().toString());
            } else if (obj instanceof Snack snack) {
                model.setValueAt("SNACK", 0, i);
                table.getColumnModel().getColumn(i).setHeaderValue(snack.getTime().toString());
            }

            for (int j = 0; j < foods.size(); j++) {
                model.setValueAt(foods.get(j), j + 1, i);
            }
        }

        table.getColumnModel().getColumn(colCount-1).setHeaderValue("Add New...");
        model.setValueAt("+", 0, colCount-1);


        table.setDefaultRenderer(Object.class, new DayTableCellRenderer(foodsForColumns));
        table.getTableHeader().repaint();

        // Adjust column widths to fit content
        packColumns(table);
    }

    /**
     * Adjusts the width of each column to fit the widest content, including the header.
     * Adds extra padding for a more spacious and readable layout.
     *
     * @param table The JTable whose columns are to be resized.
     */
    private void packColumns(JTable table) {
        for (int column = 0; column < table.getColumnCount(); column++) {
            TableColumn tableColumn = table.getColumnModel().getColumn(column);
            int preferredWidth = 0;

            // Get width of header
            TableCellRenderer headerRenderer = tableColumn.getHeaderRenderer();
            if (headerRenderer == null) {
                headerRenderer = table.getTableHeader().getDefaultRenderer();
            }
            Component headerComp = headerRenderer.getTableCellRendererComponent(
                    table, tableColumn.getHeaderValue(), false, false, 0, column);
            preferredWidth = headerComp.getPreferredSize().width;

            // Get width of cells
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                Component c = table.prepareRenderer(cellRenderer, row, column);
                int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
                preferredWidth = Math.max(preferredWidth, width);
            }

            // Set the final preferred width with some padding
            tableColumn.setPreferredWidth(preferredWidth + 15);
        }
    }
}

/**
 * Custom cell renderer for the DayPanel's table to provide specific styling
 * for different cell types (headers, food items, etc.) in a light, modern theme.
 */
class DayTableCellRenderer extends DefaultTableCellRenderer {
    private final List<List<Food>> foodsForColumns;

    public DayTableCellRenderer(List<List<Food>> foodsForColumns) {
        this.foodsForColumns = foodsForColumns;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // Use a JLabel for more control over padding and alignment
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Add padding within cells
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        label.setForeground(new Color(51, 51, 51));

        // Style for the top row (Meal/Snack type)
        if (row == 0) {
            label.setFont(new Font("SansSerif", Font.BOLD, 14));
            label.setBackground(new Color(235, 235, 235)); // A slightly darker gray for the header row
            label.setForeground(new Color(60, 60, 60));
        } else {
            // Alternating row colors for better readability ("Zebra stripes")
            if (row % 2 == 0) {
                label.setBackground(new Color(248, 248, 248));
            } else {
                label.setBackground(Color.WHITE);
            }
        }

        // Override background for selected cells
        if (isSelected) {
            label.setBackground(new Color(200, 225, 255)); // Soft blue for selection
        }

        // Render Food objects with their names, otherwise use toString()
        if (value instanceof Food food) {
            label.setText(food.getName());
            label.setFont(table.getFont());
            label.setForeground(new Color(51, 51, 51));
        } else if (value != null) {
            label.setText(value.toString());
        } else {
            label.setText(""); // Default to empty
            // Check if this is the actionable "add" cell
            if (column < foodsForColumns.size() && row == foodsForColumns.get(column).size() + 1) {
                label.setText("+ Add Food");
                label.setFont(new Font("SansSerif", Font.ITALIC, 12));
                label.setForeground(Color.GRAY);
            }
        }

        return label;
    }
}