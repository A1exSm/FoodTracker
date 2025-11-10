package org.alexander.database;

import org.alexander.logging.CentralLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * FileManager is responsible for managing startup file operations.
 * @since 1.0.0
 */
public class FileManager {
    private static final String FOLDER_NAME = "FoodTracker";
    private static final String SAVED_FILE_NAME = "data.sqlite";
    private static final String WORKING_FILE_NAME = "database.sqlite";
    private static boolean initialised = false;
    private static final CentralLogger logger = CentralLogger.getInstance();
    private final int BACKUP_RETENTION = 10;

    public void initialise() {
        if (initialised) {
            throw new IllegalStateException("FileManager already initialised");
        }
        boolean isFileNew = startHandler();
        if (isFileNew) {
            System.out.println("Debug: New file created, initializing fresh data...");
            DatabaseManager.createFreshData();
        } else {
            copyToWorkingDatabase();
        }
        initialised = true;
    }

    public boolean isRunning() {
        return !initialised;
    }

    /**
     * Handles the folder and file verification and creation process.
     * @return returns true if the file is new, and false if it already existed.
     */
    private boolean startHandler() {
        String userHome = System.getProperty("user.home");
        boolean isFolderNew = folderHandler(userHome);
        return fileHandler(userHome, isFolderNew);
    }

    /**
     * Checks if the folder exists, if not creates it.
     * @return if folder was created, returns true, if it already existed, returns false.
     */
    private boolean folderHandler(String userHome) {
        Path folderPath = Paths.get(userHome, FOLDER_NAME);
        Path backupPath = Paths.get(userHome, FOLDER_NAME, "backup");
        File folder = folderPath.toFile();
        File backupFolder = backupPath.toFile();
        if (!folder.exists()) {
            System.out.println("Debug: Folder does not exist, creating...");
            if (!folder.mkdir()) {
                throw new RuntimeException("Error creating folder");
            }
            if (!backupFolder.mkdir()) {
                throw new RuntimeException("Error creating backup folder");
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if the saved file exists, if not creates it. If a backup file exists, it copies it to the saved file.
     * @return returns true if the file is new, and false if it already existed.
     */
    private boolean fileHandler(String userHome, boolean isFolderNew) {
        Path savedFilePath = Paths.get(userHome, FOLDER_NAME, SAVED_FILE_NAME);
        File savedFile = savedFilePath.toFile();
        if (!savedFile.exists()) {
            // creates a new file if one does not exist
            logger.logInfo("[Debug] Saved file does not exist, creating...");
            try {
                if (!savedFile.createNewFile()) {
                    throw new RuntimeException("Error creating saved file");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (!isFolderNew) {
                logger.logWarning("[Debug] Saved file does not exist, attempting to find backup...");
                File[] backupFiles = listBackupFiles(userHome);
                if (backupFiles != null) {
                    File latestBackup = backupFiles[0];
                    try {
                        Files.copy(latestBackup.toPath(), savedFilePath, StandardCopyOption.REPLACE_EXISTING);
                        logger.logInfo("[Debug]: Most recent Backup file restored successfully: " + latestBackup.getName());
                        return false; // Returns false since the file data is not new but restored from backup
                    } catch (IOException e) {
                        logger.logError("Error copying backup file: " + e.getMessage());
                    }
                }
            }
            return true; // File was created
        }
        return false; // File already existed
    }

    /**
     * Lists all backup files in the backup folder, sorted by last modified date descending.
     * @param userHome the user's home directory
     * @return an array of backup files, or null if none exist
     */
    private File[] listBackupFiles(String userHome) {
        File backupFolder = Paths.get(userHome, FOLDER_NAME, "backup").toFile();
        File[] backupFiles = backupFolder.listFiles((dir, name) -> name.startsWith("backup_") && name.endsWith(".sqlite"));
        if (backupFiles != null && backupFiles.length > 0) {
            Arrays.sort(backupFiles, Comparator.comparingLong(File::lastModified).reversed());
            return backupFiles;
        } else {
            return null;
        }

    }

    /**
     * Copies the saved database to the working database at startup.
     */
    private void copyToWorkingDatabase() {
        String userHome = System.getProperty("user.home");
        Path savedPath = Paths.get(userHome, FOLDER_NAME, SAVED_FILE_NAME);
        Path workingPath = Paths.get(userHome, FOLDER_NAME, WORKING_FILE_NAME);
        File workingFile = workingPath.toFile();
        
        // Delete existing working database if it exists
        if (workingFile.exists()) {
            if (workingFile.delete()) {
                logger.logInfo("[Debug] Previous working database deleted successfully.");
            } else {
                logger.logError("Error deleting old working database.");
            }
        }
        
        try {
            if (!workingFile.createNewFile()) {
                throw new RuntimeException("Error creating working database file, file " + workingFile.getAbsolutePath() + " already exists.");
            }
            Files.copy(savedPath, workingPath, StandardCopyOption.REPLACE_EXISTING);
            logger.logInfo("[Debug] Data copied to working database successfully.");
            if (workingPath.toFile().length() == 0) {
                System.out.println("Debug: Warning - Working database file is empty after copy.");
                DatabaseManager.createFreshData();
            }
        } catch (IOException e) {
            System.err.println("Error copying to working database: " + e.getMessage());
        }
    }



    /**
     * Saves the current working database to the saved database file.
     * Creates a backup of the saved database before overwriting.
     */
    public void save() {
        if (!initialised) {
            System.err.println("Debug: FileManager not initialised, cannot save.");
            return;
        }
        String userHome = System.getProperty("user.home");
        Path workingPath = Paths.get(userHome, FOLDER_NAME, WORKING_FILE_NAME);
        Path savedPath = Paths.get(userHome, FOLDER_NAME, SAVED_FILE_NAME);
        Path backupPath = createBackup(userHome);
        try {
            // First backup the current saved database
            Files.copy(savedPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            // Then copy the working database to the saved database
            Files.copy(workingPath, savedPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Debug: Working database saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving working database: " + e.getMessage());
        }
    }

    private Path createBackup(String userHome) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String backupName = "backup_" + LocalDateTime.now().format(formatter) + ".sqlite";
        Path backupPath = Paths.get(userHome, FOLDER_NAME, "backup", backupName);
        File backupFile = backupPath.toFile();
        if (backupFile.exists()) {
            if (!backupFile.delete()) {
                logger.logError("Error deleting old backup file.");
            }
        }
        backupRetentionHandler(userHome);
        try {
            if (!backupFile.createNewFile()) {
                throw new RuntimeException("Error creating backup file, file " + backupFile.getAbsolutePath() + " already exists.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return backupPath;
    }

    private void backupRetentionHandler(String userHome) {
        File[] backupFiles = listBackupFiles(userHome);
        if (backupFiles != null) {
            ArrayList<File> backupFileList = new ArrayList<>(Arrays.asList(backupFiles));
            while (backupFileList.size() > BACKUP_RETENTION - 1) {
                if (backupFileList.getLast().delete()) {
                    backupFileList.removeLast(); // Remove reference to deleted file
                    logger.logInfo("[Debug] Oldest backup file deleted successfully to maintain retention policy.");
                } else {
                    logger.logError("Error deleting oldest backup file.");
                }
            }
        }
    }
}
