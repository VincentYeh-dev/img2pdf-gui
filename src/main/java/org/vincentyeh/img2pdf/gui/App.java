package org.vincentyeh.img2pdf.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import org.vincentyeh.img2pdf.gui.ui.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
//        FlatDarkLaf.setup();
        JFrame frame = new JFrame(Constants.APP_TITLE);
//        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                frame.dispose();
                System.exit(0);
            }
        });

        frame.setContentPane(new MainFrame().getRootPanel());
        frame.pack();
        frame.setVisible(true);
    }
}
