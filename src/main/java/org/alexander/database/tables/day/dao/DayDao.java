package org.alexander.database.tables.day.dao;

import org.alexander.database.DatabaseManager;
import org.alexander.database.QueryHelper;
import org.alexander.database.TableDao;
import org.alexander.database.tables.day.Day;
import org.alexander.database.tables.week.dao.WeekDao;
import org.alexander.logging.CentralLogger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DayDao implements DayDaoInterface, TableDao {
    private final CentralLogger logger = CentralLogger.getInstance();

    @Override
    public boolean contains(String entity, String attribute) {
        validateAttribute(attribute);
        if (!attribute.equals("date")) {
            throw new IllegalArgumentException("Can only check existence by the primary key 'date'.");
        }
        LocalDate localDate = LocalDate.parse(entity);
        return QueryHelper.entityExists(localDate, "date", "DAY");
    }

    @Override
    public void validateAttribute(String attribute) {
        if (!attribute.equals("date") && !attribute.equals("week_id") && !attribute.equals("body_weight")) {
            throw new IllegalArgumentException("Invalid attribute: " + attribute);
        }
    }

    @Override
    public Day addDay(LocalDate date, int week_id, Double bodyWeight) {
        if (contains(date.toString(), "date")) {
            logger.logWarning("Add Day failed: Day with date '" + date + "' already exists. Returning null.");
            return null;
        }
        if (!new WeekDao().contains(String.valueOf(week_id), "week_id")) {
            logger.logError("Add Day failed: Week with id '" + week_id + "' does not exist. Cannot add Day.");
            return null;
        }
        String Query = "INSERT INTO DAY (date, week_id, body_weight) VALUES (?, ?, ?)";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(Query)
        ) {
            preparedStatement.setDate(1, java.sql.Date.valueOf(date));
            preparedStatement.setInt(2, week_id);
            QueryHelper.checkNull(preparedStatement, 3, bodyWeight);
            if (preparedStatement.executeUpdate() > 0) {
                return new Day(date, week_id, bodyWeight);
            } else {
                logger.logError("Add Day failed: No rows affected when inserting Day with date '" + date + "'.");
                return null;
            }
        } catch (SQLException e) {
            logger.logError("Add Day failed: Exception occurred while inserting Day with date '" + date + "'. Exception: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Day addDay(LocalDate date, int week_id) {
        return addDay(date, week_id, null);
    }

    @Override
    public Day addDay(Day day) {
        return addDay(day.getDate(), day.getWeek_id(), day.getBodyWeight());
    }

    @Override
    public boolean deleteDay(LocalDate date) {
        return QueryHelper.deleteEntity(date, "date", "DAY");
    }

    @Override
    public boolean deleteDay(Day day) {
        return deleteDay(day.getDate());
    }

    @Override
    public List<Day> getDayList() {
        ArrayList<Day> dayList = new ArrayList<>();
        String query = "SELECT date, week_id, body_weight FROM DAY";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query);
                var resultSet = preparedStatement.executeQuery()
        ) {
            while (resultSet.next()) {
                LocalDate date = resultSet.getDate("date").toLocalDate();
                int week_id = resultSet.getInt("week_id");
                Double bodyWeight = resultSet.getObject("body_weight") != null ? resultSet.getDouble("body_weight") : null;
                dayList.add(new Day(date, week_id, bodyWeight));
            }
            return dayList;
        } catch (SQLException e) {
            logger.logError("Get Day List failed: Exception occurred while retrieving Day list. Exception: " + e.getMessage());
        }
        return dayList;
    }

    @Override
    public List<Day> getDaysInWeek(int week_id) {
        ArrayList<Day> dayList = new ArrayList<>();
        if (!new WeekDao().contains(String.valueOf(week_id), "week_id")) {
            logger.logWarning("Get Days In Week failed: Week with id '" + week_id + "' does not exist. Returning empty list.");
            return dayList;
        }
        String query = "SELECT date, week_id, body_weight FROM DAY WHERE week_id = ?";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            preparedStatement.setInt(1, week_id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                LocalDate date = resultSet.getDate("date").toLocalDate();
                Double bodyWeight = resultSet.getObject("body_weight") != null ? resultSet.getDouble("body_weight") : null;
                dayList.add(new Day(date, week_id, bodyWeight));
            }
            return dayList;
        } catch (SQLException e) {
            logger.logError("Get Days In Week failed: Exception occurred while retrieving Days for week_id '" + week_id + "'. Exception: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Day> getDaysInWeek(org.alexander.database.tables.week.Week week) {
        return getDaysInWeek(week.getId());
    }

    @Override
    public Day getDay(LocalDate date) {
        if (!contains(date.toString(), "date")) {
            logger.logWarning("Get Day failed: Day with date '" + date + "' does not exist. Returning null.");
            return null;
        }
        String query = "SELECT date, week_id, body_weight FROM DAY WHERE date = ? LIMIT 1";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            preparedStatement.setDate(1, java.sql.Date.valueOf(date));
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int week_id = resultSet.getInt("week_id");
                Double bodyWeight = resultSet.getObject("body_weight") != null ? resultSet.getDouble("body_weight") : null;
                return new Day(date, week_id, bodyWeight);
            } else {
                logger.logError("Get Day failed: No data found for existing Day with date '" + date + "'. This should not happen.");
                return null;
            }
        } catch (SQLException e) {
            logger.logError("Get Day failed: Exception occurred while retrieving Day with date '" + date + "'. Exception: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Day updateDay(LocalDate date, int week_id, Double bodyWeight) {
        if (!contains(date.toString(), "date")) {
            logger.logWarning("Update Day failed: Day with date '" + date + "' does not exist. Returning null.");
            return null;
        }
        if (!new WeekDao().contains(String.valueOf(week_id), "week_id")) {
            logger.logError("Update Day failed: Week with id '" + week_id + "' does not exist. Cannot update Day.");
            return null;
        }
        String query = "UPDATE DAY SET week_id = ?, body_weight = ? WHERE date = ?";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            preparedStatement.setInt(1, week_id);
            QueryHelper.checkNull(preparedStatement, 2, bodyWeight);
            preparedStatement.setDate(3, java.sql.Date.valueOf(date));
            if (preparedStatement.executeUpdate() > 0) {
                return new Day(date, week_id, bodyWeight);
            } else {
                logger.logError("Update Day failed: No rows affected when updating Day with date '" + date + "'.");
                return null;
            }
        } catch (SQLException e) {
            logger.logError("Update Day failed: Exception occurred while updating Day with date '" + date + "'. Exception: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Day updateDay(Day day) {
        return updateDay(day.getDate(), day.getWeek_id(), day.getBodyWeight());
    }
}
