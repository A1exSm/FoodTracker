package org.alexander.database.tables.week.dao;

import org.alexander.database.DatabaseManager;
import org.alexander.database.QueryHelper;
import org.alexander.database.tables.TableDao;
import org.alexander.database.tables.week.Week;
import org.alexander.logging.CentralLogger;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WeekDao implements WeekDaoInterface, TableDao {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    @Override
    public boolean contains(String entity, String attribute) {
        validateAttribute(attribute);
        if (attribute.equals("week_id")) {
            return QueryHelper.entityExists(Integer.parseInt(entity), "week_id", "WEEK");
        } else if (attribute.equals("start_date") || attribute.equals("end_date")) {
            QueryHelper.regexCheckDateFormat(entity);
            return QueryHelper.entityExists(LocalDate.parse(entity), attribute, "WEEK");
        } else {
            throw new IllegalArgumentException("Invalid attribute: " + attribute + ", passed validation but not handled.");
        }
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
        // need to ensure the start day is a Monday and end day is a Sunday
        if (startDate.getDayOfWeek().getValue() != 1 || endDate.getDayOfWeek().getValue() != 7) {
            String message = String.format("Invalid week range: start date '%s' is not a Monday or end date '%s' is not a Sunday. StartDate is a '%s' and EndDate is a '%s'.", startDate, endDate, startDate.getDayOfWeek(), endDate.getDayOfWeek());
            CentralLogger.getInstance().logError(message);
            return null;
        }
        if (contains(startDate.toString(), "start_date")) {
            CentralLogger.getInstance().logWarning(String.format("Week with start date '%s' already exists.", startDate));
            return null;
        }
        // removed regex check since LocalDate parsing already enforces correct format
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

    @Override
    public LocalDate getClosestMonday(LocalDate date) {
        if (date.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
            return date;
        }
        LocalDate returnDate;
        int distance = date.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        returnDate = date.minusDays(distance);
       if (returnDate.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
           return returnDate;
       } else {
              CentralLogger.getInstance().logError(String.format("Failed to calculate closest Monday for date: %s, calculated date is: %s which is a %s", date.format(formatter), returnDate.format(formatter), returnDate.getDayOfWeek()));
              return null;
       }
    }
}
