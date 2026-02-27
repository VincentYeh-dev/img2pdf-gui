package org.vincentyeh.img2pdf.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import org.vincentyeh.img2pdf.gui.controller.Controller;
import org.vincentyeh.img2pdf.gui.model.Model;
import org.vincentyeh.img2pdf.gui.view.View;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
    }
}
