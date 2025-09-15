package org.alexander.database.tables.snack.dao;

import org.alexander.database.DatabaseManager;
import org.alexander.database.QueryHelper;
import org.alexander.database.tables.TableDao;
import org.alexander.database.tables.day.Day;
import org.alexander.database.tables.day.dao.DayDao;
import org.alexander.database.tables.snack.Snack;
import org.alexander.logging.CentralLogger;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SnackDao implements SnackDaoInterface, TableDao {
    private final CentralLogger logger = CentralLogger.getInstance();
    @Override
    public boolean contains(String entity, String attribute) {
        throw new UnsupportedOperationException("Unimplemented method 'contains' with string arguments");
    }

    public boolean contains(Integer id) {
        if (id == null) {
            logger.logWarning("SnackDao.contains: id is null");
            return false;
        }
        return QueryHelper.entityExists(id, "id", "SNACK");
    }

    public boolean contains(LocalDate date, LocalTime time) {
        if (date == null || time == null) {
            logger.logWarning("SnackDao.contains: date or time is null");
            return false;
        }
        String query = "SELECT 1 FROM SNACK WHERE date = ? AND time = ? LIMIT 1";
        try (var conn = DatabaseManager.connect();
             var preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, date.toString());
            preparedStatement.setString(2, time.toString());
            var rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException sqlException) {
            logger.logError(sqlException);
        }
        return false;
    }

    @Override
    public void validateAttribute(String attribute) {
        if (!attribute.equals("id") && !attribute.equals("date") && !attribute.equals("time")) {
            throw new IllegalArgumentException("Invalid attribute: " + attribute);
        }
    }

    @Override
    public Snack addSnack(Snack snack) {
        return addSnack(snack.getDate(), snack.getTime());
    }

    @Override
    public Snack addSnack(LocalDate  date, LocalTime time) {
        if (contains(date, time)) {
            logger.logWarning("SnackDao.addSnack: Snack already exists for date " + date + " and time " + time);
            return null;
        }
        String query = "INSERT INTO SNACK (date, time) VALUES (?, ?)";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS)
        ) {
            preparedStatement.setDate(1, java.sql.Date.valueOf(date));
            preparedStatement.setTime(2, java.sql.Time.valueOf(time));
            // Execute and handle result
            int affectedRows = preparedStatement.executeUpdate();
            if (QueryHelper.effectedRowsHandler(affectedRows)) {
                try (var generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        return new Snack(generatedId, date, time);
                    }
                    logger.logError(new SQLException("Creating snack failed, no ID obtained."));
                }
            }
        } catch (SQLException sqlException) {
            logger.logError(sqlException);
        }
        return null;
    }

    @Override
    public Snack addSnack(Day day, LocalTime time) {
        return addSnack(day.getDate(), time);
    }

    @Override
    public boolean deleteSnack(int id) {
        if (!contains(id)) {
            logger.logWarning(String.format("Snack with id: '%d', does not exist.", id));
            return false;
        }
        return QueryHelper.deleteEntity(id, "id", "SNACK");
    }

    @Override
    public boolean deleteSnack(Snack snack) {
        if (snack.getId() == null) {
            logger.logWarning("SnackDao.deleteSnack: Snack id is null (meaning it has not yet been added to the database)");
            return false;
        }
        return deleteSnack(snack.getId());
    }

    @Override
    public Snack getSnack(int id) {
        if (!contains(id)) {
            logger.logWarning(String.format("Snack with id: '%d', does not exist.", id));
            return null;
        }
        String query = "SELECT * FROM SNACK WHERE id = ?";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            preparedStatement.setInt(1, id);
            var rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return getSnackFromResultSet(rs);
            } else {
                logger.logError(new SQLException("Could not get snack with id " + id + " as resultSet.next() returned false."));
            }
        } catch (SQLException sqlException) {
            logger.logError(sqlException);
        }
        return null;
    }

    @Override
    public Snack getSnack(LocalDate date, LocalTime time) {
        if (!contains(date, time)) {
            logger.logWarning(String.format("Snack with date: '%s', and time: '%s' does not exist.", date, time));
            return null;
        }
        String query = "SELECT * FROM SNACK WHERE date = ? AND time = ?";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            preparedStatement.setDate(1, java.sql.Date.valueOf(date));
            preparedStatement.setTime(2, java.sql.Time.valueOf(time));
            var rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return getSnackFromResultSet(rs);
            } else {
                logger.logError(new SQLException("Could not get snack with date " + date + " and time " + time + " as resultSet.next() returned false."));
            }
        } catch (SQLException sqlException) {
            logger.logError(sqlException);
        }
        return null;
    }

    @Override
    public Snack updateSnack(int id, LocalDate date, LocalTime time) {
        if (!contains(id)) {
            logger.logWarning(String.format("Snack with id: '%d', does not exist.", id));
            return null;
        }
        Snack existingSnack = getSnack(id);
        if (existingSnack == null) {
            logger.logError(new SQLException("Could not get existing snack with id " + id + " for update."));
            return null;
        }
        if (existingSnack.getDate().equals(date) && existingSnack.getTime().equals(time)) {
            logger.logWarning("SnackDao.updateSnack: No changes detected, update aborted.");
            return existingSnack;
        }
        String query = "UPDATE SNACK SET date = ?, time = ? WHERE id = ?";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            preparedStatement.setDate(1, java.sql.Date.valueOf(date));
            preparedStatement.setTime(2, java.sql.Time.valueOf(time));
            preparedStatement.setInt(3, id);
            int affectedRows = preparedStatement.executeUpdate();
            if (QueryHelper.effectedRowsHandler(affectedRows)) {
                return new Snack(id, date, time);
            }
        } catch (SQLException sqlException) {
            logger.logError(sqlException);
        }
        return null;
    }

    @Override
    public Snack updateSnack(Snack snack) {
        return updateSnack(snack.getId(), snack.getDate(), snack.getTime());
    }

    @Override
    public List<Snack> getSnacks() {
        ArrayList<Snack> snacks = new ArrayList<>();
        String query = "SELECT * FROM SNACK";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            var rs = preparedStatement.executeQuery();
            while (rs.next()) {
                snacks.add(getSnackFromResultSet(rs));
            }
        } catch (SQLException sqlException) {
            logger.logError(sqlException);
        }
        return snacks;

    }

    @Override
    public List<Snack> getDaySnacks(Day day) {
        if (new DayDao().getDay(day.getDate()) == null) {
            logger.logWarning(String.format("Day with date: '%s' does not exist.", day.getDate()));
            return null;
        }
        ArrayList<Snack> snacks = new ArrayList<>();
        String query = "SELECT * FROM SNACK WHERE date = ?";
        try (
                var conn = DatabaseManager.connect();
                var preparedStatement = conn.prepareStatement(query)
        ) {
            preparedStatement.setDate(1, java.sql.Date.valueOf(day.getDate()));
            var rs = preparedStatement.executeQuery();
            while (rs.next()) {
                snacks.add(getSnackFromResultSet(rs));
            }
        } catch (SQLException sqlException) {
            logger.logError(sqlException);
        }
        return snacks;
    }

    private Snack getSnackFromResultSet(java.sql.ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        LocalDate date = rs.getDate("date").toLocalDate();
        LocalTime time = rs.getTime("time").toLocalTime();
        return new Snack(id, date, time);
    }
}
