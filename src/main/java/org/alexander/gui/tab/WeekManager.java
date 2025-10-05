package org.alexander.gui.tab;

import org.alexander.database.tables.week.Week;
import org.alexander.database.tables.week.dao.WeekDao;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.time.LocalDate;
import java.util.HashMap;

public class WeekManager {
    private Week week;
    private JTabbedPane tabbedPane;
    private TableModel model;
    private WeekDao weekDao = new WeekDao();
    private final HashMap<Week, WeekScrollTab> weekTabMap = new HashMap<>();


    public WeekManager(Week week, JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
        this.week = week;
        init();
    }
    public WeekManager(JTabbedPane tabbedPane) {
        LocalDate closestMonday = weekDao.getClosestMonday(LocalDate.now());
        Week week = weekDao.getWeek(closestMonday);
        if (week == null) {
            week = weekDao.addWeek(closestMonday);
        }
        this.tabbedPane = tabbedPane;
        this.week = week;
        init();
    }

    public JTabbedPane getJTabbedPane() {
        return tabbedPane;
    }

    public void init() {
        WeekScrollTab currentWeekTab = new WeekScrollTab(week);
        tabbedPane.addTab(currentWeekTab.getTitle(), currentWeekTab);
        weekTabMap.put(week, currentWeekTab);
        // ok we may switch to a Joption pane with 7 nested JOption panes each with one table, the nested JOptionPanes will each represent a day of the Week.
        // Ok new plan. JOptionPane with nested JPanels (which can have a title set to BorderLayoutNorth) each with a table inside a JScrollPane.
        // Ok some ideas (a couple of days later): lets not create days if they don't exist. make a button to add a day with the day names that are still available. Delete weeks if they have no days on quit.

    }

}
