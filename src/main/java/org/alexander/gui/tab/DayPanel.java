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

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
        titledBorder.setTitleColor(Color.white);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void init() {
        setBorder(titledBorder);
        setBackground(Color.black);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(Short.MAX_VALUE, 200));
    }

    private void initTable() {
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    Point point = e.getPoint();
                    int row = table.rowAtPoint(point);
                    int column = table.columnAtPoint(point);
                    if (column != -1) {
                        table.setColumnSelectionInterval(column, column);
                        if (row != -1) table.setRowSelectionInterval(row, row);
                        showPopupMenu(e, row, column);
                    }
                }
            }
        });
    }

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

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        dialog.add(searchPanel, BorderLayout.NORTH);
        dialog.add(new JScrollPane(foodJList), BorderLayout.CENTER);
        dialog.add(addButton, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
    }

    private void addFoodToMealOrSnack(Food food, Object mealOrSnackObject) {
        if (mealOrSnackObject instanceof Meal meal) {
            foodMealDao.addFoodMeal(food, meal, 1.0);
        } else if (mealOrSnackObject instanceof Snack snack) {
            foodSnackDao.addFoodSnack(food, snack, 1.0);
        }
        refreshTable();
    }

    private void addNewFoodToMealOrSnack(Object mealOrSnackObject) {
        FoodDialog dialog = new FoodDialog(SwingUtilities.getWindowAncestor(this));
        // This requires FoodDialog to be modal and to return the new food.
        // For now, just refreshing the table will pick up the new food if added.
        refreshTable();
    }

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

    private void deleteMealOrSnack(Object mealOrSnackObject) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this item and all associated foods?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (mealOrSnackObject instanceof Meal meal) {
                new MealDao().deleteMeal(meal);
            } else if (mealOrSnackObject instanceof Snack snack) {
                new SnackDao().deleteSnack(snack);
            }
            refreshTable();
        }
    }

    private void removeFoodFromMealOrSnack(Food food, Object mealOrSnackObject) {
        if (mealOrSnackObject instanceof Meal meal) {
            foodMealDao.deleteFoodMeal(food.getName(), meal.getId());
        } else if (mealOrSnackObject instanceof Snack snack) {
            foodSnackDao.deleteFoodSnack(food.getName(), snack.getId());
        }
        refreshTable();
    }

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
        List<List<Food>> foodsForColumns = new ArrayList<>();

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


        table.setDefaultRenderer(Object.class, (table, value, isSelected, hasFocus, row, column) -> {
            JLabel label = new JLabel();
            if (value instanceof Food food) {
                label.setText(food.getName());
            } else if (value != null) {
                label.setText(value.toString());
            }
            return label;
        });

        table.getTableHeader().repaint();
    }
}