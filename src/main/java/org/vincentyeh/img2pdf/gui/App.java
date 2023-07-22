package org.vincentyeh.img2pdf.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import org.vincentyeh.img2pdf.gui.ui.View;
import org.vincentyeh.img2pdf.lib.image.ColorType;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageAlign;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageDirection;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageSize;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * Hello world!
 */
public class App {

    private static Controller controller;
    private static Model model;


    private static View view;

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

        model = new Model();
        view = new View();
        controller = new Controller(model,view);
        controller.initialize();

        frame.setContentPane(view.getRootPanel());
        frame.pack();
        frame.setVisible(true);
    }
}
