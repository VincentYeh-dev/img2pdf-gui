package org.vincentyeh.img2pdf.gui.view;

import org.vincentyeh.img2pdf.lib.image.ColorType;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageAlign;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageDirection;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageSize;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;

public class JUIMediator implements UIMediator {
    private MediatorListener listener;
    private JButton sourceBrowseButton;
    private JComboBox<PageSize> pageSizeComboBox;
    private JComboBox<PageAlign.HorizontalAlign> horizontalAlignComboBox;
    private JComboBox<PageAlign.VerticalAlign> verticalAlignComboBox;
    private JTextField outputFormatField;
    private JPasswordField ownerPasswordField;
    private JPasswordField userPasswordField;
    private JButton convertButton;
    private JTextField fileFilterField;
    private JComboBox<PageDirection> directionComboBox;
    private JCheckBox autoRotateCheckBox;
    private JProgressBar totalConversionProgressBar;
    private JComboBox<ColorType> colorTypeComboBox;
    private JTree sourceTree;
    private JLabel totalConversionLabel;
    private JButton clearAllButton;
    private JProgressBar pageConversionProgressBar;
    private JLabel pageConversionLabel;
    private JButton stopButton;
    private JLabel imagePane;
    private JButton outputFolderBrowseButton;
    private JTextField outputFolderField;
    private JList<String> logList;
    private JFileChooser sourceFilesChooser;
    private JFileChooser outputFolderChooser;

    public static class Builder {
        private final JUIMediator mediator = new JUIMediator();

        public void linkSourceBrowseButton(JButton button) {
            mediator.sourceBrowseButton = button;
            mediator.sourceBrowseButton.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mediator.notifyUI("source_browse_button_click");
                }
            });
        }

        public void linkPageSizeComboBox(JComboBox<PageSize> comboBox) {
            mediator.pageSizeComboBox = comboBox;
            mediator.pageSizeComboBox.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!e.getActionCommand().equals("comboBoxChanged"))
                        return;
                    mediator.notifyUI("page_size_change",
                            mediator.pageSizeComboBox.getItemAt(mediator.pageSizeComboBox.getSelectedIndex()));
                }
            });
        }

        public void linkHorizontalAlignComboBox(JComboBox<PageAlign.HorizontalAlign> comboBox) {
            mediator.horizontalAlignComboBox = comboBox;
            mediator.horizontalAlignComboBox.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!e.getActionCommand().equals("comboBoxChanged"))
                        return;
                    mediator.notifyUI("horizontal_align_change",
                            mediator.horizontalAlignComboBox.getItemAt(mediator.horizontalAlignComboBox.getSelectedIndex()));
                }
            });

        }

        public void linkVerticalAlignComboBox(JComboBox<PageAlign.VerticalAlign> comboBox) {
            mediator.verticalAlignComboBox = comboBox;
            mediator.verticalAlignComboBox.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!e.getActionCommand().equals("comboBoxChanged"))
                        return;
                    mediator.notifyUI("vertical_align_change",
                            mediator.verticalAlignComboBox.getItemAt(mediator.verticalAlignComboBox.getSelectedIndex()));
                }
            });
        }

        public void linkOutputFormatField(JTextField textField) {
            mediator.outputFormatField = textField;
            mediator.outputFormatField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    String text = mediator.outputFormatField.getText();
                    mediator.notifyUI("output_format_change", text);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    String text = mediator.outputFormatField.getText();
                    mediator.notifyUI("output_format_change", text);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                }
            });

        }

        public void linkOwnerPasswordField(JPasswordField passwordField) {
            mediator.ownerPasswordField = passwordField;
            mediator.ownerPasswordField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    String text = new String(mediator.ownerPasswordField.getPassword());
                    mediator.notifyUI("owner_password_change", text);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    String text = new String(mediator.ownerPasswordField.getPassword());
                    mediator.notifyUI("owner_password_change", text);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {

                }
            });
        }

        public void linkUserPasswordField(JPasswordField passwordField) {
            mediator.userPasswordField = passwordField;
            mediator.userPasswordField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    String text = new String(mediator.userPasswordField.getPassword());
                    mediator.notifyUI("user_password_change", text);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    String text = new String(mediator.userPasswordField.getPassword());
                    mediator.notifyUI("user_password_change", text);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {

                }
            });
        }

        public void linkConvertButton(JButton button) {
            mediator.convertButton = button;
            mediator.convertButton.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mediator.notifyUI("convert_button_click");
                }
            });
        }

        public void linkFileFilterField(JTextField textField) {
            mediator.fileFilterField = textField;
            mediator.fileFilterField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    String text = mediator.fileFilterField.getText();
                    mediator.notifyUI("file_filter_change", text);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    String text = mediator.fileFilterField.getText();
                    mediator.notifyUI("file_filter_change", text);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {

                }
            });
        }

        public void linkDirectionComboBox(JComboBox<PageDirection> comboBox) {
            mediator.directionComboBox = comboBox;
            mediator.directionComboBox.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!e.getActionCommand().equals("comboBoxChanged"))
                        return;
                    mediator.notifyUI("page_direction_change",
                            mediator.directionComboBox.getItemAt(mediator.directionComboBox.getSelectedIndex()));
                }
            });
        }

        public void linkAutoRotateCheckBox(JCheckBox checkBox) {
            mediator.autoRotateCheckBox = checkBox;
            mediator.autoRotateCheckBox.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean selected = mediator.autoRotateCheckBox.isSelected();
                    mediator.notifyUI("auto_rotate_change", selected);
                }
            });
        }

        public void linkTotalConversionProgressBar(JProgressBar progressBar) {
            mediator.totalConversionProgressBar = progressBar;
        }


        public void linkColorTypeComboBox(JComboBox<ColorType> comboBox) {
            mediator.colorTypeComboBox = comboBox;
            mediator.colorTypeComboBox.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!e.getActionCommand().equals("comboBoxChanged"))
                        return;
                    mediator.notifyUI("color_type_change",
                            mediator.colorTypeComboBox.getItemAt(mediator.colorTypeComboBox.getSelectedIndex()));
                }
            });
        }

        public void linkSourceTree(JTree tree) {
            mediator.sourceTree = tree;
        }

        public void linkTotalConversionLabel(JLabel label) {
            mediator.totalConversionLabel = label;
        }

        public void linkClearAllButton(JButton button) {
            mediator.clearAllButton = button;
            mediator.clearAllButton.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mediator.notifyUI("clear_all_button_click");
                }
            });
        }

        public void linkPageConversionProgressBar(JProgressBar progressBar) {
            mediator.pageConversionProgressBar = progressBar;
        }

        public void linkPageConversionLabel(JLabel label) {
            mediator.pageConversionLabel = label;
        }

        public void linkStopButton(JButton button) {
            mediator.stopButton = button;
            mediator.stopButton.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mediator.notifyUI("stop_button_click");
                }
            });
        }

        public void linkImagePane(JLabel label) {
            mediator.imagePane = label;
        }

        public void linkOutputFolderBrowseButton(JButton button) {
            mediator.outputFolderBrowseButton = button;
            mediator.outputFolderBrowseButton.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mediator.notifyUI("output_folder_browse_button_click");
                }
            });
        }

        public void linkOutputFolderField(JTextField textField) {
            mediator.outputFolderField = textField;
            mediator.outputFolderField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    String text = mediator.outputFolderField.getText();
                    mediator.notifyUI("output_folder_change", text);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    String text = mediator.outputFolderField.getText();
                    mediator.notifyUI("output_folder_change", text);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {

                }
            });
        }

        public void linkLogList(JList<String> list) {
            mediator.logList = list;
        }

        public void linkSourceFilesChooser(JFileChooser fileChooser) {
            mediator.sourceFilesChooser = fileChooser;
        }

        public void linkOutputFolderChooser(JFileChooser fileChooser) {
            mediator.outputFolderChooser = fileChooser;
        }

        public UIMediator build() {
            return mediator;
        }
    }

    private JUIMediator() {

    }

    @Override
    public void notifyUI(String event, Object... data) {
//        System.out.println("Event:" + event);
        if (event.equals("output_format_change")) {
            String format = (String) data[0];
            System.out.printf("Output Format changed: %s\n", format);
            convertButton.setEnabled(!format.isEmpty());
        }
        if (event.equals("output_folder_change")) {
            String folder = (String) data[0];
            System.out.printf("Output Folder changed: %s\n", folder);
        }
        if (event.equals("file_filter_change")) {
            String filter = (String) data[0];
            System.out.printf("File Filter changed: %s\n", filter);
        }
        if (event.equals("owner_password_change")) {
            String password = (String) data[0];
            System.out.printf("Owner Password changed: %s\n", password.isEmpty() ? "<empty>" : password);
        }
        if (event.equals("user_password_change")) {
            String password = (String) data[0];
            System.out.printf("User Password changed: %s\n", password.isEmpty() ? "<empty>" : password);
        }
        if (event.equals("auto_rotate_change")) {
            boolean selected = (boolean) data[0];
            System.out.printf("Auto Rotate changed: %s\n", selected);
        }
        if (event.equals("page_size_change")) {
            PageSize size = (PageSize) data[0];
            System.out.printf("Page Size changed: %s\n", size);
        }
        if (event.equals("horizontal_align_change")) {
            PageAlign.HorizontalAlign align = (PageAlign.HorizontalAlign) data[0];
            System.out.printf("Horizontal Align changed: %s\n", align);
        }
        if (event.equals("vertical_align_change")) {
            PageAlign.VerticalAlign align = (PageAlign.VerticalAlign) data[0];
            System.out.printf("Vertical Align changed: %s\n", align);
        }
        if (event.equals("page_direction_change")) {
            PageDirection direction = (PageDirection) data[0];
            System.out.printf("Page Direction changed: %s\n", direction);
        }
        if (event.equals("color_type_change")) {
            ColorType color = (ColorType) data[0];
            System.out.printf("Color Type changed: %s\n", color);
        }

        if (event.equals("source_browse_button_click")) {
            System.out.printf("Source Browse Button clicked\n");
        }
        if (event.equals("output_folder_browse_button_click")) {
            System.out.printf("Output Folder Browse Button clicked\n");
        }
        if (event.equals("convert_button_click")) {
            System.out.printf("Convert Button clicked\n");
        }
        if (event.equals("clear_all_button_click")) {
            System.out.printf("Clear All Button clicked\n");
        }
        if (event.equals("stop_button_click")) {
            System.out.printf("Stop Button clicked\n");
        }

    }

    public void setBatchProgress(int progress, int total) {
        totalConversionProgressBar.setMaximum(total);
        totalConversionProgressBar.setValue(progress);
        totalConversionLabel.setText(progress + "/" + total);
    }

    @Override
    public void setConversionProgress(int progress, int total) {
        pageConversionProgressBar.setMaximum(total);
        pageConversionProgressBar.setValue(progress);
        pageConversionLabel.setText(progress + "/" + total);
    }

    public void setListener(MediatorListener listener) {
        this.listener = listener;
    }

    @Override
    public void initialize() {

        for (PageDirection direction : PageDirection.values()) {
            directionComboBox.addItem(direction);
        }

        for (PageAlign.VerticalAlign align : PageAlign.VerticalAlign.values()) {
            verticalAlignComboBox.addItem(align);
        }
        for (PageAlign.HorizontalAlign align : PageAlign.HorizontalAlign.values()) {
            horizontalAlignComboBox.addItem(align);
        }
        for (PageSize size : PageSize.values()) {
            pageSizeComboBox.addItem(size);
        }
        for (ColorType color : ColorType.values()) {
            colorTypeComboBox.addItem(color);
        }
        colorTypeComboBox.setSelectedItem(ColorType.sRGB);
        autoRotateCheckBox.setSelected(true);


//        view.getOutputFolderField().setText(model.getOutputFolder().getAbsolutePath());
//        view.getOutputFolderChooser().setCurrentDirectory(model.getOutputFolder());

//        updateSourceTree(model.getSources());
    }
}
