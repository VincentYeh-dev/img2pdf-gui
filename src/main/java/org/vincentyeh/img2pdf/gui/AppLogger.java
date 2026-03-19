package org.vincentyeh.img2pdf.gui;

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Application-wide logger backed by {@link java.util.logging}.
 * <p>
 * Writes all log records to {@link #LOG_FILE_PATH} (append mode) in the user's
 * home directory and to the console (for IDE development). If the log file cannot
 * be created the console handler is still active so no records are lost.
 * </p>
 * <p>
 * Callers can check {@link #isFileLoggingActive()} to determine whether the log
 * file was opened successfully and optionally warn the user at startup.
 * </p>
 * <p>
 * Severity convention:
 * <ul>
 *   <li>{@link Level#SEVERE} — programming bugs (unexpected state, NPE, etc.)</li>
 *   <li>{@link Level#WARNING} — user / environment errors (disk full, bad path, etc.)</li>
 * </ul>
 * </p>
 */
public final class AppLogger {

    /** Absolute path of the log file written to the user's home directory. */
    private static final String LOG_FILE_PATH =
            System.getProperty("user.home") + File.separator + "img2pdf-gui.log";

    private static final Logger LOGGER =
            Logger.getLogger("org.vincentyeh.img2pdf.gui");

    private static boolean fileLoggingActive = false;

    static {
        LOGGER.setUseParentHandlers(false);

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        LOGGER.addHandler(consoleHandler);

        try {
            FileHandler fileHandler = new FileHandler(LOG_FILE_PATH, true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);
            LOGGER.addHandler(fileHandler);
            fileLoggingActive = true;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING,
                    "Cannot open log file for writing; logging to console only. Path: " + LOG_FILE_PATH, e);
        }

        LOGGER.setLevel(Level.ALL);
    }

    private AppLogger() {}

    /**
     * Returns the shared application {@link Logger} instance.
     *
     * @return the logger
     */
    public static Logger get() {
        return LOGGER;
    }

    /**
     * Returns {@code true} if the log file was opened successfully at startup.
     * When {@code false}, only the {@link ConsoleHandler} is active and log
     * records are not persisted to disk.
     *
     * @return whether file logging is currently active
     */
    public static boolean isFileLoggingActive() {
        return fileLoggingActive;
    }

    /**
     * Returns the absolute path of the log file that this logger attempts to write.
     * The file may not exist if {@link #isFileLoggingActive()} returns {@code false}.
     *
     * @return the log file path
     */
    public static String getLogFilePath() {
        return LOG_FILE_PATH;
    }
}
