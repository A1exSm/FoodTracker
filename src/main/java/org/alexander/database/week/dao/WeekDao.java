package org.alexander.database.week.dao;

import org.alexander.database.DatabaseManager;
import org.alexander.database.QueryHelper;
import org.alexander.database.TableDao;
import org.alexander.database.week.Week;
import org.alexander.logging.CentralLogger;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WeekDao implements WeekDaoInterface, TableDao {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    @Override
    public boolean contains(String entity, String attribute) {
        validateAttribute(attribute);
        String query = "SELECT 1 FROM WEEK WHERE " + attribute + " = ?";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
                ) {
            if (attribute.equals("week_id")) {
                preparedStatement.setInt(1, Integer.parseInt(entity));
            } else if (attribute.equals("start_date") || attribute.equals("end_date")) {
                if (!isDateFormatedCorrectly(entity)) {
                    throw new IllegalArgumentException("Date is not formatted correctly: " + entity + ". Expected format: dd-MM-yyyy");
                }
                preparedStatement.setDate(1, java.sql.Date.valueOf(entity)); // assuming entity is in ISO_LOCAL_DATE format
            } else {
                throw new IllegalArgumentException("Invalid attribute: " + attribute + ", passed validation but not handled.");
            }
            return preparedStatement.executeQuery().next();

        } catch (SQLException e) {
            CentralLogger.getInstance().logError(e);
        }
        return false;
    }

    private boolean isDateFormatedCorrectly(String date) {
       return date.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    private LocalDate sqlDateToLocalDate(java.sql.Date sqlDate) {
        if (sqlDate == null) {
            return null;
        }
        return sqlDate.toLocalDate();
    }

    @Override
    public void validateAttribute(String attribute) {
        if (!attribute.equals("week_id") && !attribute.equals("start_date") && !attribute.equals("end_date")) {
            throw new IllegalArgumentException("Invalid attribute: " + attribute);
        }
    }

    @Override
    public Week addWeek(LocalDate startDate, LocalDate endDate) {
        if (contains(startDate.toString(), "start_date")) {
            CentralLogger.getInstance().logWarning(String.format("Week with start date '%s' already exists.", startDate));
            return null;
        }
        if (!isDateFormatedCorrectly(startDate.toString()) && !isDateFormatedCorrectly(endDate.toString())) {
            throw new IllegalArgumentException("Date is not formatted correctly. Expected format: dd-MM-yyyy");
        }
        String query = "INSERT INTO WEEK (start_date, end_date) VALUES (?, ?)";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            preparedStatement.setDate(1, java.sql.Date.valueOf(startDate));
            preparedStatement.setDate(2, java.sql.Date.valueOf(endDate));
            if (preparedStatement.executeUpdate() > 0) {
                try (var rs = preparedStatement.getGeneratedKeys()) {
                    if (rs.next()) {
                        int week_id = rs.getInt(1);
                        return new Week(week_id, startDate, endDate);
                    }
                    throw new SQLException("Creating week failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            CentralLogger.getInstance().logError(e);
        }
        return null;
    }

    @Override
    public Week addWeek(LocalDate startDate) {
        return addWeek(startDate, startDate.plusDays(6));
    }

    @Override
    public Week addWeek(Week week) {
        if (contains(String.valueOf(week.getId()), "week_id")) {
            CentralLogger.getInstance().logWarning(String.format("Week with id '%d' already exists.", week.getId()));
            return null;
        }
        return addWeek(week.getStartDate(), week.getEndDate());
    }

    @Override
    public boolean deleteWeek(Week week) {
        return deleteWeek(week.getId());
    }

    @Override
    public boolean deleteWeek(int id) {
        if (!contains(String.valueOf(id), "week_id")) {
            CentralLogger.getInstance().logWarning(String.format("Week with id: '%d', does not exist.", id));
            return false;
        }
        return QueryHelper.deleteEntity(id, "week_id", "WEEK");
    }

    @Override
    public List<Week> getWeekList() {
        List<Week> weekList = new ArrayList<>();
        String query = "SELECT * FROM WEEK";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query);
                var resultSet = preparedStatement.executeQuery()
        ) {
            while (resultSet.next()) {
                int id = resultSet.getInt("week_id");
                LocalDate startDate = sqlDateToLocalDate(resultSet.getDate("start_date"));
                LocalDate endDate = sqlDateToLocalDate(resultSet.getDate("end_date"));
                weekList.add(new Week(id, startDate, endDate));
            }
        } catch (SQLException e) {
            CentralLogger.getInstance().logError(e);
        }
        return weekList;
    }

    @Override
    public Week getWeek(LocalDate startDate) {
        if (!contains(startDate.toString(), "start_date")) {
            CentralLogger.getInstance().logWarning(String.format("Week with start date '%s' does not exist.", startDate));
            return null;
        }
        String query = "SELECT * FROM WEEK WHERE start_date = ? LIMIT 1";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            preparedStatement.setDate(1, java.sql.Date.valueOf(startDate));
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int id = resultSet.getInt("week_id");
                LocalDate endDate = sqlDateToLocalDate(resultSet.getDate("end_date"));
                return new Week(id, startDate, endDate);
            }
        } catch (SQLException e) {
            CentralLogger.getInstance().logError(e);
        }
        return null;
    }
}
