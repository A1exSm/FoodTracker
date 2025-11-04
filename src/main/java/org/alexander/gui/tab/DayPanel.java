package org.alexander.gui.tab;

import org.alexander.database.tables.day.Day;
import org.alexander.database.tables.meal.Meal;
import org.alexander.database.tables.meal.dao.MealDao;
import org.alexander.database.tables.snack.Snack;
import org.alexander.database.tables.snack.dao.SnackDao;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.ArrayList;

class DayPanel extends JPanel {
    private JTable table;
    private TableModel model;
    private final TitledBorder titledBorder;
    private final Day day;

    DayPanel(Day day) {
        super();
        this.day = day;
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
        setPreferredSize(new Dimension(Short.MAX_VALUE, getPreferredSize().height));
    }

    private void initTable() {

        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//        JTable table = new JTable(new String[][]{{}}, new String[]{"Column1"});
    }

    public void refreshTable() {
        MealDao mealDao = new MealDao();
        SnackDao snackDao = new SnackDao();
        ArrayList<Meal> meals = new ArrayList<>(mealDao.getDayMeals(day));
        ArrayList<Snack> snacks = new ArrayList<>(snackDao.getDaySnacks(day));
        model = new DefaultTableModel(1, getCol()) {
            // ok so each columns first row is the mea type or just called snack, under each of these is the food items for that meal/snack in the remaining rows.
            // will use a JPopupMenu to add food items to each meal/snack. where we detect a mouse click on a cell and can show a pop up menu to do with that
            // for example: click on snack/meal popup menu shows "Add Food Item", "Remove Meal/Snack", click on food item popup menu shows "Remove Food Item", click on empty cell below meal/snack shows "Add Food Item" (means we need rowsFilled + 1)
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setModel(model);
        for (int i = 0; i < meals.size(); i++) {
            Meal meal = meals.get(i);
            table.setValueAt(meal.getType(), 0, i);
            table.getColumnModel().getColumn(i).setHeaderValue(meal.getTime());
        }
        for (int i = 0; i < snacks.size(); i++) {
            int columnIndex = meals.size() + i;
            Snack snack = snacks.get(i);
            table.setValueAt("Snack" + snack.getId(), 0, columnIndex);
            table.getColumnModel().getColumn(columnIndex).setHeaderValue(snack.getTime());
        }
    }

    private int getCol() {
        int cols = 0;
        MealDao mealDao = new MealDao();
        SnackDao snackDao = new SnackDao();
        ArrayList<Meal> meals = new ArrayList<>(mealDao.getDayMeals(day));
        ArrayList<Snack> snacks = new ArrayList<>(snackDao.getDaySnacks(day));
        cols += meals.size();
        cols += snacks.size();
        return cols;
    }
}
