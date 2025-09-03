package org.alexander.database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * FileManager is responsible for managing startup file operations.
 * @since 1.0.0
 */
public class FileManager {
    protected static final String FOLDER_NAME = "FoodTracker";
    protected static final String FILE_NAME = "data.sqlite";
    protected static boolean initialised = false;

    public static boolean isInitialised() {
        return initialised;
    }

    protected static void initialise() {
        boolean isFileNew = startHandler();
        if (isFileNew) {
            System.out.println("Debug: New file created, initializing fresh data...");
            DatabaseManager.createFreshData();
        } else {
            copyToLiveDatabase();
        }
        initialised = true;
    }
    /**
     * Handles the folder and file verification and creation process.
     * @return returns true if the file is new, and false if it already existed.
     */
    private static boolean startHandler() {
        String userHome = System.getProperty("user.home");
        boolean isFolderNew = folderHandler(userHome);
        return fileHandler(userHome, isFolderNew);
    }

    /**
     * Checks if the folder exists, if not creates it.
     * @return if folder was created, returns true, if it already existed, returns false.
     */
    private static boolean folderHandler(String userHome) {
        Path folderPath = Paths.get(userHome, FOLDER_NAME);
        File folder = folderPath.toFile();
        if (!folder.exists()) {
            System.out.println("Debug: Folder does not exist, creating...");
            if (!folder.mkdir()) {
                throw new RuntimeException("Error creating folder");
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if the file exists, if not creates it. If a backup file exists, it copies it to the main file.
     * @return returns true if the file is new, and false if it already existed.
     */
    private static boolean fileHandler(String userHome, boolean isFolderNew) {
        Path filePath = Paths.get(userHome, FOLDER_NAME, FILE_NAME);
        File file = filePath.toFile();
        if (!file.exists()) {
            // creates a new file if one does not exist
            System.out.println("Debug: File does not exist, creating...");
            try {
                if (!file.createNewFile()) {
                    throw new RuntimeException("Error creating file");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (!isFolderNew) {
                System.out.println("Debug: File does not exist, attempting to find backup...");
                Path backupFolderPath = Paths.get(userHome, FOLDER_NAME, "backup");
                if (backupFolderPath.toFile().exists()) {
                    Path backupFilePath = Paths.get(userHome, FOLDER_NAME, "backup", FILE_NAME);
                    File backupFile = backupFilePath.toFile();
                    if (backupFile.exists()) {
                        try {
                            Files.copy(backupFilePath, filePath, StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("Debug: Backup file restored successfully");
                            return false; // Returns false since the file data is not new but restored from backup
                        } catch (IOException e) {
                            System.err.println("Error copying backup file: " + e.getMessage());
                        }
                    }
                }
            }
            return true; // File was created
        }
        return false; // File already existed
    }

    private static void copyToLiveDatabase() {
        String userHome = System.getProperty("user.home");
        Path sourcePath = Paths.get(userHome, FOLDER_NAME, FILE_NAME);
        Path destinationPath = Paths.get("src/main/resources/data/data.sqlite");
        try {
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Debug: Data copied to live database successfully.");
            if (destinationPath.toFile().length() == 0) {
                System.out.println("Debug: Warning - Live database file is empty after copy.");
                DatabaseManager.createFreshData();
            }
        } catch (IOException e) {
            System.err.println("Error copying to live database: " + e.getMessage());
        }

    }
    /**
     * Saves the current live database to the user's directory.
     * Overwrites any existing file.
     */
    public static void save() {
        if (!initialised) {
            throw new IllegalStateException("FileManager not initialised");
        }
        String userHome = System.getProperty("user.home");
        Path sourcePath = Paths.get("src/main/resources/data/data.sqlite");
        Path destinationPath = Paths.get(userHome, FOLDER_NAME, FILE_NAME);
        try {
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Debug: Live database saved to user directory successfully.");
        } catch (IOException e) {
            System.err.println("Error saving live database: " + e.getMessage());
        }
    }
}
