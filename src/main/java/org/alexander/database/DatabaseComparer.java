package org.alexander.database;

import org.alexander.database.tables.day.Day;
import org.alexander.database.tables.day.dao.DayDao;
import org.alexander.database.tables.food.Food;
import org.alexander.database.tables.food.dao.FoodDao;
import org.alexander.database.tables.meal.Meal;
import org.alexander.database.tables.meal.dao.MealDao;
import org.alexander.database.tables.snack.Snack;
import org.alexander.database.tables.snack.dao.SnackDao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compares two database files to find differences in their data.
 * It identifies added and deleted entities like Meals, Snacks, Foods, and Days.
 */
public class DatabaseComparer {

    private final Path liveDbPath;
    private final Path userDbPath;
    private final List<String> additions = new ArrayList<>();
    private final List<String> deletions = new ArrayList<>();

    /**
     * An interface to unify DAO access for comparison purposes, requiring a method to get all entities.
     * @param <T> The type of the entity.
     */
    public interface ReadOnlyDao<T> {
        List<T> getAll();
    }

    public DatabaseComparer(Path liveDbPath, Path userDbPath) {
        this.liveDbPath = liveDbPath;
        this.userDbPath = userDbPath;
    }

    /**
     * Executes the comparison between the live database and the user's saved database.
     *
     * @throws SQLException if there is an error connecting to or querying the databases.
     * @throws IOException if there is an error creating or managing temporary database files.
     */
    public void compare() throws SQLException, IOException {
        if (!liveDbPath.toFile().exists()) {
            return; // Nothing to compare if live DB doesn't exist
        }

        // If the user's saved database doesn't exist, all items in the live DB are additions.
        if (!userDbPath.toFile().exists()) {
            try (Connection liveConn = DriverManager.getConnection("jdbc:sqlite:" + liveDbPath.toAbsolutePath())) {
                findAllAdditions(liveConn);
            }
            return;
        }

        // Use temporary copies to avoid file locking issues
        Path tempLiveDb = Files.createTempFile("live_db_copy", ".sqlite");
        Path tempUserDb = Files.createTempFile("user_db_copy", ".sqlite");
        try {
            Files.copy(liveDbPath, tempLiveDb, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(userDbPath, tempUserDb, StandardCopyOption.REPLACE_EXISTING);

            try (Connection liveConn = DriverManager.getConnection("jdbc:sqlite:" + tempLiveDb.toAbsolutePath());
                 Connection userConn = DriverManager.getConnection("jdbc:sqlite:" + tempUserDb.toAbsolutePath())) {

                compareEntities(new FoodDao(liveConn), new FoodDao(userConn), "Food");
                compareEntities(new DayDao(liveConn), new DayDao(userConn), "Day");
                compareEntities(new MealDao(liveConn), new MealDao(userConn), "Meal");
                compareEntities(new SnackDao(liveConn), new SnackDao(userConn), "Snack");
            }
        } finally {
            // Clean up temporary files
            Files.deleteIfExists(tempLiveDb);
            Files.deleteIfExists(tempUserDb);
        }
    }

    /**
     * Finds all entities in the live database and marks them as additions.
     * This is used when there is no pre-existing user database to compare against.
     * @param liveConn Connection to the live database.
     */
    private void findAllAdditions(Connection liveConn) {
        new FoodDao(liveConn).getAll().forEach(f -> additions.add("Added Food: " + f.getName()));
        new DayDao(liveConn).getAll().forEach(d -> additions.add("Added Day: " + d.getDate()));
        new MealDao(liveConn).getAll().forEach(m -> additions.add("Added " + m.getType() + " on " + m.getDate()));
        new SnackDao(liveConn).getAll().forEach(s -> additions.add("Added Snack on " + s.getDate()));
    }

    /**
     * Generic method to compare entities between two database connections using their respective DAOs.
     */
    private <T extends Entity<?>> void compareEntities(ReadOnlyDao<T> liveDao, ReadOnlyDao<T> userDao, String entityType) {
        List<T> liveEntities = liveDao.getAll();
        List<T> userEntities = userDao.getAll();

        List<Object> liveKeys = liveEntities.stream().map(Entity::getKey).collect(Collectors.toList());
        List<Object> userKeys = userEntities.stream().map(Entity::getKey).collect(Collectors.toList());

        // Find additions: entities present in live DB but not in user's saved DB
        for (T entity : liveEntities) {
            if (!userKeys.contains(entity.getKey())) {
                additions.add("Added " + entityType + ": " + getEntityDescription(entity));
            }
        }

        // Find deletions: entities present in user's saved DB but not in live DB
        for (T entity : userEntities) {
            if (!liveKeys.contains(entity.getKey())) {
                deletions.add("Removed " + entityType + ": " + getEntityDescription(entity));
            }
        }
    }

    /**
     * Creates a user-friendly description of an entity for display.
     * @param entity The entity to describe.
     * @return A string description.
     */
    private String getEntityDescription(Object entity) {
        if (entity instanceof Food) return ((Food) entity).getName();
        if (entity instanceof Day) return ((Day) entity).getDate().toString();
        if (entity instanceof Meal) return ((Meal) entity).getType() + " at " + ((Meal) entity).getTime() + " on " + ((Meal) entity).getDate();
        if (entity instanceof Snack) return "Snack at " + ((Snack) entity).getTime() + " on " + ((Snack) entity).getDate();
        return entity.toString();
    }

    public List<String> getAdditions() { return additions; }
    public List<String> getDeletions() { return deletions; }
    public boolean hasChanges() { return !additions.isEmpty() || !deletions.isEmpty(); }
}