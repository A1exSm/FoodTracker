package org.alexander.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CentralLogger {
    // Singleton instance
    private static CentralLogger instance;
    private final Logger logger = LoggerFactory.getLogger(CentralLogger.class);

    // Private constructor to prevent instantiation
    private CentralLogger() {}

    // Public method to provide access to the singleton instance
    public static CentralLogger getInstance() {
        if (instance == null) {
            instance = new CentralLogger();
        }
        return instance;
    }

    // Example logging methods
    public void logInfo(String message) {
        logger.info(message);
    }

    public void logWarning(String message) {
        logger.warn(message);
    }

    public void logError(String message) {
        logger.error(message);
    }

    public void logError(Throwable t) {
        logger.error(t.getMessage(), t);
    }
}
