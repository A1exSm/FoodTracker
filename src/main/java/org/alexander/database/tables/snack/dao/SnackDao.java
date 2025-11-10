package org.alexander.database.tables.snack.dao;

import org.alexander.database.DatabaseComparer;
import org.alexander.database.DatabaseManager;
import org.alexander.database.QueryHelper;
import org.alexander.database.tables.TableDao;
import org.alexander.database.tables.day.Day;
import org.alexander.database.tables.snack.Snack;
import org.alexander.logging.CentralLogger;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SnackDao implements SnackDaoInterface, TableDao, DatabaseComparer.ReadOnlyDao<Snack> {
    private final CentralLogger logger = CentralLogger.getInstance();
    private final Connection conn;

    public SnackDao() { this.conn = null; }
    public SnackDao(Connection conn) { this.conn = conn; }

    private Connection getConnection() throws SQLException {
        if (conn != null && !conn.isClosed()) return conn;
        return DatabaseManager.connect();
    }

    @Override
    public boolean contains(String entity, String attribute) {
        if (!attribute.equals("id")) throw new UnsupportedOperationException("Only contains by ID is supported for Snack");
        return contains(Integer.parseInt(entity));
    }

    public boolean contains(Integer id) {
        if (id == null) return false;
        return QueryHelper.entityExists(id, "id", "SNACK");
    }

    public boolean contains(LocalDate date, LocalTime time) {
        if (date == null || time == null) return false;
        String query = "SELECT 1 FROM SNACK WHERE date = ? AND time = ? LIMIT 1";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            ps.setTime(2, java.sql.Time.valueOf(time));
            return ps.executeQuery().next();
        } catch (SQLException e) {
            logger.logError(e);
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
    public Snack addSnack(Snack snack) { return addSnack(snack.getDate(), snack.getTime()); }

    @Override
    public Snack addSnack(LocalDate date, LocalTime time) {
        if (contains(date, time)) return null;
        String query = "INSERT INTO SNACK (date, time) VALUES (?, ?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            ps.setTime(2, java.sql.Time.valueOf(time));
            if (ps.executeUpdate() > 0) {
                try (var rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return new Snack(rs.getInt(1), date, time);
                }
            }
        } catch (SQLException e) {
            logger.logError(e);
        }
        return null;
    }

    @Override
    public Snack addSnack(Day day, LocalTime time) { return addSnack(day.getDate(), time); }

    @Override
    public boolean deleteSnack(int id) {
        if (!contains(id)) return false;
        return QueryHelper.deleteEntity(id, "id", "SNACK");
    }

    @Override
    public boolean deleteSnack(Snack snack) {
        if (snack.getId() == null) return false;
        return deleteSnack(snack.getId());
    }

    @Override
    public Snack getSnack(int id) {
        String query = "SELECT id, date, time FROM SNACK WHERE id = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
            ps.setInt(1, id);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) return getSnackFromResultSet(rs);
            }
        } catch (SQLException e) {
            logger.logError(e);
        }
        return null;
    }

    @Override
    public Snack getSnack(LocalDate date, LocalTime time) {
        String query = "SELECT id, date, time FROM SNACK WHERE date = ? AND time = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            ps.setTime(2, java.sql.Time.valueOf(time));
            try (var rs = ps.executeQuery()) {
                if (rs.next()) return getSnackFromResultSet(rs);
            }
        } catch (SQLException e) {
            logger.logError(e);
        }
        return null;
    }

    @Override
    public Snack updateSnack(int id, LocalDate date, LocalTime time) {
        if (!contains(id)) return null;
        String query = "UPDATE SNACK SET date = ?, time = ? WHERE id = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            ps.setTime(2, java.sql.Time.valueOf(time));
            ps.setInt(3, id);
            if (ps.executeUpdate() > 0) return new Snack(id, date, time);
        } catch (SQLException e) {
            logger.logError(e);
        }
        return null;
    }

    @Override
    public Snack updateSnack(Snack snack) {
        return updateSnack(snack.getId(), snack.getDate(), snack.getTime());
    }

    @Override
    public List<Snack> getSnacks() { return getAll(); }

    @Override
    public List<Snack> getDaySnacks(Day day) {
        ArrayList<Snack> snacks = new ArrayList<>();
        String query = "SELECT id, date, time FROM SNACK WHERE date = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(query)) {
            ps.setDate(1, java.sql.Date.valueOf(day.getDate()));
            try (var rs = ps.executeQuery()) {
                while (rs.next()) snacks.add(getSnackFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.logError(e);
        }
        return snacks;
    }

    private Snack getSnackFromResultSet(ResultSet rs) throws SQLException {
        return new Snack(rs.getInt("id"), rs.getDate("date").toLocalDate(), rs.getTime("time").toLocalTime());
    }

    @Override
    public List<Snack> getAll() {
        ArrayList<Snack> snacks = new ArrayList<>();
        String query = "SELECT id, date, time FROM SNACK";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(query); var rs = ps.executeQuery()) {
            while (rs.next()) snacks.add(getSnackFromResultSet(rs));
        } catch (SQLException e) {
            logger.logError(e);
        }
        return snacks;
    }
}