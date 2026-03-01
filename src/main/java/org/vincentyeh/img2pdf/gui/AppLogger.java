package org.vincentyeh.img2pdf.gui;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Application-wide logger backed by {@link java.util.logging}.
 * <p>
 * Writes all log records to {@code app.log} (append mode) in the working
 * directory and to the console (for IDE development). If the log file cannot
 * be created the console handler is still active so no records are lost.
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

    private static final Logger LOGGER =
            Logger.getLogger("org.vincentyeh.img2pdf.gui");

    static {
        LOGGER.setUseParentHandlers(false);

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        LOGGER.addHandler(consoleHandler);

        try {
            FileHandler fileHandler = new FileHandler("app.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);
            LOGGER.addHandler(fileHandler);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot open app.log for writing; logging to console only", e);
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
}
