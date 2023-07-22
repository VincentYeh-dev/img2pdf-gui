package org.vincentyeh.img2pdf.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import org.vincentyeh.img2pdf.gui.ui.View;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Hello world!
 */
public class App {


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
        Controller controller = new Controller(model, view);
        controller.initialize();

        frame.setContentPane(view.getRootPanel());
        frame.pack();
        frame.setVisible(true);
    }
}
