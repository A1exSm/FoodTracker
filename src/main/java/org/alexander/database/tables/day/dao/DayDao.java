package org.alexander.database.tables.day.dao;

import org.alexander.database.DatabaseComparer;
import org.alexander.database.DatabaseManager;
import org.alexander.database.QueryHelper;
import org.alexander.database.tables.TableDao;
import org.alexander.database.tables.day.Day;
import org.alexander.database.tables.week.dao.WeekDao;
import org.alexander.logging.CentralLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DayDao implements DayDaoInterface, TableDao, DatabaseComparer.ReadOnlyDao<Day> {
    private final CentralLogger logger = CentralLogger.getInstance();
    private final Connection conn;

    public DayDao() { this.conn = null; }
    public DayDao(Connection conn) { this.conn = conn; }

    private Connection getConnection() throws SQLException {
        if (conn != null && !conn.isClosed()) return conn;
        return DatabaseManager.connect();
    }

    @Override
    public boolean contains(String entity, String attribute) {
        validateAttribute(attribute);
        return QueryHelper.entityExists(LocalDate.parse(entity), "date", "DAY");
    }

    @Override
    public void validateAttribute(String attribute) {
        if (!attribute.equals("date") && !attribute.equals("week_id") && !attribute.equals("body_weight")) {
            throw new IllegalArgumentException("Invalid attribute: " + attribute);
        }
    }

    @Override
    public Day addDay(LocalDate date, int week_id, Double bodyWeight) {
        if (contains(date.toString(), "date")) return null;
        if (!new WeekDao().contains(String.valueOf(week_id), "week_id")) return null;

        String query = "INSERT INTO DAY (date, week_id, body_weight) VALUES (?, ?, ?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            ps.setInt(2, week_id);
            QueryHelper.checkNull(ps, 3, bodyWeight);
            if (ps.executeUpdate() > 0) return new Day(date, week_id, bodyWeight);
        } catch (SQLException e) {
            logger.logError(e);
        }
        return null;
    }

    @Override
    public Day addDay(LocalDate date) {
        WeekDao weekDao = new WeekDao();
        if (weekDao.contains(String.valueOf(weekDao.getClosestMonday(date)), "start_date")) {
            int week_id = weekDao.getWeek(weekDao.getClosestMonday(date)).getId();
            return addDay(date, week_id, null);
        }
        return null;
    }

    @Override
    public Day addDay(LocalDate date, int week_id) { return addDay(date, week_id, null); }
    @Override
    public Day addDay(Day day) { return addDay(day.getDate(), day.getWeek_id(), day.getBodyWeight()); }
    @Override
    public boolean deleteDay(LocalDate date) { return QueryHelper.deleteEntity(date, "date", "DAY"); }
    @Override
    public boolean deleteDay(Day day) { return deleteDay(day.getDate()); }

    @Override
    public List<Day> getDayList() {
        ArrayList<Day> dayList = new ArrayList<>();
        String query = "SELECT date, week_id, body_weight FROM DAY";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LocalDate date = rs.getDate("date").toLocalDate();
                int week_id = rs.getInt("week_id");
                Double bodyWeight = rs.getObject("body_weight") != null ? rs.getDouble("body_weight") : null;
                dayList.add(new Day(date, week_id, bodyWeight));
            }
        } catch (SQLException e) {
            logger.logError(e);
        }
        return dayList;
    }

    @Override
    public List<Day> getDaysInWeek(int week_id) {
        ArrayList<Day> dayList = new ArrayList<>();
        String query = "SELECT date, week_id, body_weight FROM DAY WHERE week_id = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
            ps.setInt(1, week_id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = rs.getDate("date").toLocalDate();
                    Double bodyWeight = rs.getObject("body_weight") != null ? rs.getDouble("body_weight") : null;
                    dayList.add(new Day(date, week_id, bodyWeight));
                }
            }
        } catch (SQLException e) {
            logger.logError(e);
        }
        return dayList;
    }

    @Override
    public List<Day> getDaysInWeek(org.alexander.database.tables.week.Week week) {
        return getDaysInWeek(week.getId());
    }

    @Override
    public Day getDay(LocalDate date) {
        if (!contains(date.toString(), "date")) return null;
        String query = "SELECT date, week_id, body_weight FROM DAY WHERE date = ? LIMIT 1";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int week_id = rs.getInt("week_id");
                    Double bodyWeight = rs.getObject("body_weight") != null ? rs.getDouble("body_weight") : null;
                    return new Day(date, week_id, bodyWeight);
                }
            }
        } catch (SQLException e) {
            logger.logError(e);
        }
        return null;
    }

    @Override
    public Day updateDay(LocalDate date, int week_id, Double bodyWeight) {
        if (!contains(date.toString(), "date")) return null;
        if (!new WeekDao().contains(String.valueOf(week_id), "week_id")) return null;
        String query = "UPDATE DAY SET week_id = ?, body_weight = ? WHERE date = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
            ps.setInt(1, week_id);
            QueryHelper.checkNull(ps, 2, bodyWeight);
            ps.setDate(3, java.sql.Date.valueOf(date));
            if (ps.executeUpdate() > 0) return new Day(date, week_id, bodyWeight);
        } catch (SQLException e) {
            logger.logError(e);
        }
        return null;
    }

    @Override
    public Day updateDay(Day day) {
        return updateDay(day.getDate(), day.getWeek_id(), day.getBodyWeight());
    }

    @Override
    public List<Day> getAll() {
        return getDayList();
    }
}