package org.vincentyeh.img2pdf.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import org.vincentyeh.img2pdf.gui.ui.MainFrame;

import javax.swing.*;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        FlatDarkLaf.setup();
        JFrame frame= new JFrame("");
        frame.setContentPane(new MainFrame().getRootPanel());
        frame.pack();
        frame.setVisible(true);
    }
}
