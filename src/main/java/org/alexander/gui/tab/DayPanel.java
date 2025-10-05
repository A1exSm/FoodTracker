package org.alexander.gui.tab;

import org.alexander.database.tables.day.Day;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;

class DayPanel extends JPanel {
    private final JTable table;
    private final TitledBorder titledBorder;

    DayPanel(Day day) {
        super();
        titledBorder = BorderFactory.createTitledBorder(day.dayOfWeek.toString());
        init();
        TableModel model = new DefaultTableModel(100, 100) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        initTable();
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
}
