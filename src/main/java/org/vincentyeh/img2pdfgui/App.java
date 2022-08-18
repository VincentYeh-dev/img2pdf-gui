package org.vincentyeh.img2pdfgui;

import com.formdev.flatlaf.FlatDarkLaf;
import org.vincentyeh.img2pdfgui.ui.MainFrame;

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
