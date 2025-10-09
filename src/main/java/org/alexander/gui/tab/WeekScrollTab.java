package org.alexander.gui.tab;

import org.alexander.database.tables.day.Day;
import org.alexander.database.tables.day.dao.DayDao;
import org.alexander.database.tables.week.Week;
import org.alexander.gui.GUIHandler;
import org.alexander.logging.CentralLogger;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.HashMap;

public class WeekScrollTab extends JScrollPane {
    private final DayDao dayDao = new DayDao();
    private final Week week;
    private final JPanel mainPanel = new JPanel();
    private final JPanel addDayPanel = new JPanel();
    private final HashMap<DayOfWeek, JPanel> dayPanelMap = new HashMap<>();
    private static final CentralLogger logger = CentralLogger.getInstance();

    public WeekScrollTab(Week tabWeek) {
        super();
        week = tabWeek;
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.black);
        Arrays.stream(getDays()).toList().forEach(this::addDay);;
        setViewportView(mainPanel);
        dayPanelMap.values().forEach(this::addResizeListener);
    }

    private void refresh() {
        if (allDaysPresent()) {
            removeAddDayPanel();
            return;
        }
        setAddDayPanel();
    }

    private void setAddDayPanel() {}

    private void removeAddDayPanel() {}

    private boolean allDaysPresent() {
        return dayPanelMap.size() == 7;
    }

    public void selectDay() {
        DayOfWeek dayOfWeek = new SelectDayDialog(this).getSelectedDay();
        if (dayOfWeek == null) return;
        GUIHandler.setCursor(this, Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Day day = dayDao.addDay(week.getStartDate().plusDays(dayOfWeek.getValue() - 1));
        addDay(day);
        addResizeListener(dayPanelMap.get(dayOfWeek));
    }


    public boolean[] getAvailableDays() {
        boolean[] availableDays = new boolean[7];
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            availableDays[dayOfWeek.getValue() - 1] = !dayPanelMap.containsKey(dayOfWeek);
        }
        return availableDays;
    }

    private void addDay(Day day) {
        if (day == null) {
            return;
        }
        if (dayPanelMap.containsKey(day.dayOfWeek)) {
            logger.logWarning("WeekScrollTab addDay() called with duplicate day: " + day.dayOfWeek);
            return;
        }
        dayPanelMap.put(day.dayOfWeek, new DayPanel(day));
        mainPanel.add(dayPanelMap.get(day.dayOfWeek));
        mainPanel.revalidate();
        mainPanel.repaint();
        cursorDefault(day);
    }

    private void cursorDefault(Day day) {
        new Thread(() -> {
            JPanel panel = dayPanelMap.get(day.dayOfWeek);
            while (panel.getParent() == null) {
                try {
                    Thread.sleep(100); // Avoid busy waiting
                } catch (InterruptedException ignored) {}
            }
            SwingUtilities.invokeLater(() -> GUIHandler.setCursor(this, Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)));
        }).start();
    }

    private void addResizeListener(JPanel dayPanel) {
        mainPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int width = mainPanel.getWidth();
                dayPanel.setPreferredSize(new Dimension(width - width/10, 200));
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });
    }

    public Week getWeek() {
        return week;
    }

    public String getTitle() {
        LocalDate start = week.getStartDate();
        LocalDate end = week.getEndDate();
        int startDay = start.getDayOfMonth();
        int endDay = end.getDayOfMonth();
        int startYear = start.getYear();
        int endYear = end.getYear();
        String startMonth = start.getMonth().toString().substring(0, 3);
        String endMonth = end.getMonth().toString().substring(0, 3);
        StringBuilder title = new StringBuilder();
        if (!startMonth.equals(endMonth)) {
            title.append(String.format("%s %s", startDay, startMonth));
        } else {
            title.append(startDay);
        }
        if (startYear != endYear) {
            title.append(String.format(", %d", startYear));
        }
        title.append(String.format(" - %s %s %d", endDay, endMonth, endYear));
        return title.toString();
    }

    private Day[] getDays() {
        Day[] days = new Day[7];
        for (int i = 0; i < 7; i++) {
            LocalDate date = week.getStartDate().plusDays(i);
            Day day = dayDao.getDay(date);
            // ignore null values, since we are not creating new days.
            days[i] = day;
        }
        return days;
    }
}
