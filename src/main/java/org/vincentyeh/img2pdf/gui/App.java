package org.vincentyeh.img2pdf.gui;

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

        model = new Model();
        controller = new Controller(model);
        view = new View(new ViewListener() {
            @Override
            public void onConvertButtonClicked() {
                controller.convert();
            }

            @Override
            public void onClearAllButtonClicked() {
                controller.clearSource();

            }

            @Override
            public void onAutoRotateCheckBoxChanged(boolean checked) {
                controller.setAutoRotate(checked);
            }

            @Override
            public void onSizeComboChanged(PageSize pageSize) {
                controller.setPageSize(pageSize);
            }

            @Override
            public void onStopButtonClicked() {

            }

            @Override
            public void onFileFilterFieldChange(String text) {
                controller.setFileFilter(text);
            }

            @Override
            public void onOutputFormatFieldChange(String text) {
                controller.setOutputFormat(text);
            }

            @Override
            public void onSourcesFileSelected(File[] selectedFiles) {
                controller.addSource(selectedFiles);
            }

            @Override
            public void onOutputFolderChanged(File selectedFile) {
                controller.setOutputFolder(selectedFile);
            }

            @Override
            public void onComboColorChanged(ColorType selectedItem) {
                controller.setColorType(selectedItem);
            }

            @Override
            public void onComboHorizontalAlignChanged(PageAlign.HorizontalAlign selectedItem) {
                controller.setHorizontalAlign(selectedItem);
            }

            @Override
            public void onComboVerticalAlignChanged(PageAlign.VerticalAlign selectedItem) {
                controller.setVerticalAlign(selectedItem);
            }

            @Override
            public void onComboDirectionChanged(PageDirection selectedItem) {
                controller.setPageDirection(selectedItem);
            }

        });
        model.setModelListener(new ModelListener() {
            @Override
            public void onUIUpdate(Model model) {
                view.updateSourceTree(model.getSources());
                view.setProgress(model.getConversionProgress(),model.getConversionProgressMax());
                view.setPageProgress(model.getPageConversionProgress(),model.getPageConversionProgressMax());
                view.setOutputFolderField(model.getOutputFolder().getAbsolutePath());

            }
        });

        view.initialize();

        frame.setContentPane(view.getRootPanel());
        frame.pack();
        frame.setVisible(true);
    }
}
