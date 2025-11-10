package org.alexander.gui.tab;

import org.alexander.database.tables.day.Day;
import org.alexander.database.tables.week.Week;
import org.alexander.database.tables.week.dao.WeekDao;
import org.alexander.gui.GUIHandler;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;

public class WeekManager {
    private static final org.alexander.logging.CentralLogger logger = org.alexander.logging.CentralLogger.getInstance();
    private final Week week;
    private final JTabbedPane tabbedPane;
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

    private void init() {
        openWeek(week);
        // ok we may switch to a JOption pane with 7 nested JOption panes each with one table, the nested JOptionPanes will each represent a day of the Week.
        // Ok new plan. JOptionPane with nested JPanels (which can have a title set to BorderLayoutNorth) each with a table inside a JScrollPane.
        // Ok some ideas (a couple of days later): lets not create days if they don't exist. make a button to add a day with the day names that are still available. Delete weeks if they have no days on quit.
    }

    private Week getTabbedPaneWeek() {
        GUIHandler.setCursor(tabbedPane, Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        int index = tabbedPane.getSelectedIndex();
        if (index == -1) {
            logger.logWarning("WeekManager getTabbedPaneWeek() called with no selected tab.");
            throw new IllegalStateException("No tab selected");
        }
        WeekScrollTab currentTab = (WeekScrollTab) tabbedPane.getComponentAt(index);
        return currentTab.getWeek();
    }

    public void openNextWeek() {
        Week currentTab = getTabbedPaneWeek();
        LocalDate nextWeekDate = getNextWeekDate(currentTab);
        Week nextWeek = getNextWeek(nextWeekDate);
        openWeek(nextWeek);
    }

    public void openPreviousWeek() {
        Week currentTab = getTabbedPaneWeek();
        LocalDate previousWeekDate = getPreviousWeekDate(currentTab);
        Week previousWeek = getNextWeek(previousWeekDate);
        openWeek(previousWeek);
    }

    private Week getNextWeek(LocalDate startDate) {
        if (weekDao.contains(startDate.toString(), "start_date")) {
            return weekDao.getWeek(startDate);
        } else {
            return weekDao.addWeek(startDate);
        }
    }

    private LocalDate getNextWeekDate(Week currentWeek) {
        return currentWeek.getStartDate().plusWeeks(1);
    }

    private LocalDate getPreviousWeekDate(Week currentWeek) {
        return currentWeek.getStartDate().minusWeeks(1);
    }

    public WeekScrollTab getOpenTab() {
        int index = tabbedPane.getSelectedIndex();
        if (index == -1) {
            logger.logWarning("WeekManager getOpenTab() called with no selected tab.");
            throw new IllegalStateException("No tab selected");
        }
        if (tabbedPane.getComponentAt(index) instanceof WeekScrollTab scrollTab) {
            return scrollTab;
        }
        throw new IllegalStateException("Selected tab is not a WeekScrollTab");
    }

    public void openWeek(Week week) {
        Week workingWeek;
        if (weekTabMap.containsKey(week)) {
            tabbedPane.setSelectedComponent(weekTabMap.get(week));
            return;
        } else if (weekDao.getWeek(week.getStartDate()) == null ) {
            workingWeek = weekDao.addWeek(week.getStartDate());
            if (workingWeek == null) {
                logger.logError("WeekManager openWeek() failed to add week: " + week.getStartDate());
                return;
            }
        } else {
            workingWeek = week;
        }
        WeekScrollTab weekScrollTab = new WeekScrollTab(workingWeek);
        tabbedPane.addTab(weekScrollTab.getTitle(), weekScrollTab);
        weekTabMap.put(workingWeek, weekScrollTab);
        tabbedPane.setSelectedComponent(weekScrollTab);
        cursorDefault(week);
    }

    private void cursorDefault(Week week) {
        new Thread(() -> {
            org.alexander.gui.tab.WeekScrollTab scrollTab = weekTabMap.get(week);
            while (scrollTab.getParent() == null) {
                try {
                    Thread.sleep(100); // Avoid busy waiting
                } catch (InterruptedException ignored) {}
            }
            SwingUtilities.invokeLater(() -> GUIHandler.setCursor(tabbedPane, Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)));
        }).start();
    }

    /**
     * Refreshes all day panels in all open week tabs.
     * This is useful after global operations like deleting a food item.
     */
    public void refreshAllDayPanels() {
        for (WeekScrollTab weekScrollTab : weekTabMap.values()) {
            for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                weekScrollTab.refreshDay(dayOfWeek);
            }
        }
    }
}
