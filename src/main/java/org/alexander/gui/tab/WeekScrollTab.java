package org.alexander.gui.tab;

import org.alexander.database.tables.day.Day;
import org.alexander.database.tables.day.dao.DayDao;
import org.alexander.database.tables.week.Week;
import org.alexander.logging.CentralLogger;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.time.DayOfWeek;
import java.time.LocalDate;
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
        Arrays.stream(getDays()).toList().forEach(this::addDay);
        thisFuncWillMakeTheAddDayField();
        setViewportView(mainPanel);
        dayPanelMap.values().forEach(this::addResizeListener);
//        someTableStuff();
    }

    private void thisFuncWillMakeTheAddDayField() {}

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

    public String getTitle() {
        return String.format("Week of %s to %s", week.getStartDate(), week.getEndDate());
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
