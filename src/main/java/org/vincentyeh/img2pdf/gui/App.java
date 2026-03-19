package org.vincentyeh.img2pdf.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import org.vincentyeh.img2pdf.gui.controller.Controller;
import org.vincentyeh.img2pdf.gui.model.Model;
import org.vincentyeh.img2pdf.gui.view.View;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * Application entry point for img2pdf-gui.
 * <p>
 * Initialises the FlatDarkLaf theme, constructs the MVC triad
 * (Model, View, Controller), and displays the main application window.
 * </p>
 */
public class App {

    /**
     * Launches the application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        // Ensure all logging handlers (including FileHandler) are flushed and closed
        // on JVM exit, regardless of how the application terminates.
        Runtime.getRuntime().addShutdownHook(
                new Thread(LogManager.getLogManager()::reset, "log-shutdown"));

        // Force AppLogger initialisation before the window appears so that
        // isFileLoggingActive() returns a reliable result when checked below.
        AppLogger.get();

        try {
            FlatDarkLaf.setup();

            JFrame frame = new JFrame(Constants.APP_TITLE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);
                    frame.dispose();
                    System.exit(0);
                }
            });

            Model model = new Model();
            View view = new View();
            new Controller(model, view.getUIMediator());

            frame.setContentPane(view.getRootPanel());
            frame.pack();
            frame.setVisible(true);

            // Warn the user if the log file could not be opened at startup.
            SwingUtilities.invokeLater(() -> {
                if (!AppLogger.isFileLoggingActive()) {
                    JOptionPane.showMessageDialog(
                            frame,
                            "Unable to write log file. Logging to console only.\n"
                                    + "Attempted path: " + AppLogger.getLogFilePath(),
                            "Log File Warning",
                            JOptionPane.WARNING_MESSAGE);
                }
            });

        } catch (Exception e) {
            AppLogger.get().log(Level.SEVERE, "Fatal error during application startup", e);
            JOptionPane.showMessageDialog(
                    null,
                    "Application failed to start:\n" + e.getMessage(),
                    "Fatal Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}
