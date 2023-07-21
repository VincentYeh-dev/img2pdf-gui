package org.vincentyeh.img2pdf.gui.ui;


import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.vincentyeh.img2pdf.gui.ViewListener;
import org.vincentyeh.img2pdf.gui.ui.components.Task;
import org.vincentyeh.img2pdf.lib.image.ColorType;
import org.vincentyeh.img2pdf.lib.pdf.parameter.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class View {

    private JButton button_source_browse;

    private JPanel root;
    private JComboBox<PageSize> combo_size;
    private JComboBox<PageAlign.HorizontalAlign> combo_horizontal;
    private JComboBox<PageAlign.VerticalAlign> combo_vertical;
    private JTextField field_destination_format;
    private JPasswordField pwd_owner_password;
    private JPasswordField pwd_user_password;
    private JButton button_convert;
    private JTextField field_filter;
    private JComboBox<PageDirection> combo_direction;
    private JCheckBox check_auto;
    private JProgressBar progressBar_total;
    private JComboBox<ColorType> comboBox_color;
    private JTree tree_sources;
    private JLabel label_progress;
    private JButton clearAllButton;
    private JProgressBar progressBar_sub;
    private JLabel label_sub_progress;
    private JButton stopButton;
    private JLabel imagePane;
    private JButton button_destination_browse;
    private JTextField field_destination_folder;
    private JList<String> logList;

    private JFileChooser sourceFilesChooser;
    private JFileChooser destinationFolderChooser;


    private final DefaultListModel<String> listModel = new DefaultListModel<>();

    private ViewListener viewListener;

    public View(ViewListener viewListener) {
        $$$setupUI$$$();
        this.viewListener = viewListener;
    }

    public void initialize() {
        for (PageDirection direction : PageDirection.values()) {
            combo_direction.addItem(direction);
        }
        combo_direction.addItemListener(e -> viewListener.onComboDirectionChanged((PageDirection) combo_direction.getSelectedItem()));
        viewListener.onComboDirectionChanged((PageDirection) combo_direction.getSelectedItem());

        for (PageAlign.VerticalAlign align : PageAlign.VerticalAlign.values()) {
            combo_vertical.addItem(align);
        }
        combo_vertical.addItemListener(e -> viewListener.onComboVerticalAlignChanged((PageAlign.VerticalAlign) combo_vertical.getSelectedItem()));
        viewListener.onComboVerticalAlignChanged((PageAlign.VerticalAlign) combo_vertical.getSelectedItem());

        for (PageAlign.HorizontalAlign align : PageAlign.HorizontalAlign.values()) {
            combo_horizontal.addItem(align);
        }
        combo_horizontal.addItemListener(e -> viewListener.onComboHorizontalAlignChanged((PageAlign.HorizontalAlign) combo_horizontal.getSelectedItem()));
        viewListener.onComboHorizontalAlignChanged((PageAlign.HorizontalAlign) combo_horizontal.getSelectedItem());

        for (PageSize size : PageSize.values()) {
            combo_size.addItem(size);
        }
        combo_size.addItemListener(e -> viewListener.onSizeComboChanged((PageSize) combo_size.getSelectedItem()));
        viewListener.onSizeComboChanged((PageSize) combo_size.getSelectedItem());

        for (ColorType color : ColorType.values()) {
            comboBox_color.addItem(color);
        }
        comboBox_color.addItemListener(e -> viewListener.onComboColorChanged((ColorType) comboBox_color.getSelectedItem()));
        viewListener.onComboColorChanged((ColorType) comboBox_color.getSelectedItem());

        check_auto.addActionListener(e -> viewListener.onAutoRotateCheckBoxChanged(check_auto.isSelected()));
        viewListener.onAutoRotateCheckBoxChanged(check_auto.isSelected());

        button_convert.addActionListener(e -> viewListener.onConvertButtonClicked());
        clearAllButton.addActionListener(e -> viewListener.onClearAllButtonClicked());

        sourceFilesChooser = createSourceFilesChooser();
        destinationFolderChooser = createDestinationFolderChooser();

        stopButton.addActionListener(e -> viewListener.onStopButtonClicked());

        button_source_browse.addActionListener(e -> browseSources());
        button_destination_browse.addActionListener(e -> browseOutputFolder());
        field_filter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {

            }

            @Override
            public void removeUpdate(DocumentEvent e) {

            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                viewListener.onFileFilterFieldChange(field_filter.getText());
            }
        });

        viewListener.onFileFilterFieldChange(field_filter.getText());
        field_destination_format.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {

            }

            @Override
            public void removeUpdate(DocumentEvent e) {

            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                viewListener.onOutputFormatFieldChange(field_destination_format.getText());
            }
        });
        viewListener.onOutputFormatFieldChange(field_destination_format.getText());

//        viewListener.onOutputFolderChanged(new File(field_destination_folder.getText()));


    }

    public void setFileFilterField(String filter) {
        field_filter.setText(filter);
    }

    public String getFileFilterField() {
        return field_filter.getText();
    }


    private JFileChooser createSourceFilesChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        return chooser;
    }

    private JFileChooser createDestinationFolderChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        return chooser;
    }

//    private void onSizeChange(PageSize selectedItem) {
//        if (selectedItem == null)
//            return;
//        combo_direction.setEnabled(selectedItem != PageSize.DEPEND_ON_IMG);
//        check_auto.setEnabled(selectedItem != PageSize.DEPEND_ON_IMG);
//        combo_vertical.setEnabled(selectedItem != PageSize.DEPEND_ON_IMG);
//        combo_horizontal.setEnabled(selectedItem != PageSize.DEPEND_ON_IMG);
//    }


//    private void startConversion() {
//        interrupt_flag = false;
//        clearLog();
//        try {
//            File tempFolder = Files.createTempDirectory("org.vincentyeh.img2pdf.gui").toFile();
//            tempFolder.deleteOnExit();
//
//            ImagePDFFactory factory = Img2Pdf.createFactory(getPageArgument(), getDocumentArgument(), getColorType(), true);
//            setMaxProgress(tasks.size());
//            setProgress(0);
//
//            File destination_folder = new File(field_destination_folder.getText());
//            if (!destination_folder.exists()) {
//                boolean success = destination_folder.mkdirs();
//                if (!success)
//                    throw new IllegalStateException("Unable to create directories");
//            }
//            if (destination_folder.isFile())
//                throw new IllegalArgumentException("Uestination should be folder");
//
//
//            conversion_thread = new Thread(() -> {
//                for (int i = 0; i < tasks.size(); i++) {
//                    if (interrupt_flag)
//                        break;
//                    try {
//                        factory.start(i,
//                                tasks.get(i).files,
//                                new File(destination_folder, tasks.get(i).destination.getName()),
//                                factoryListener);
//                        recordNewLog(i, String.format("[OK] %s", tasks.get(i).destination.getName()));
//                    } catch (PDFFactoryException e) {
//                        recordNewLog(i, String.format("[ERROR] %s -> %s", tasks.get(i).destination.getName(), e.getCause().getMessage()));
//                    } finally {
//                        setProgress(i + 1);
//                    }
//                }
//
//            });
//            conversion_thread.start();
//        } catch (IOException ex) {
//            throw new RuntimeException(ex);
//        }
//    }

    public void recordNewLog(int index, String log) {
        listModel.add(index, log);
        logList.setModel(listModel);
    }

    public void clearLog() {
        listModel.clear();
        logList.setModel(listModel);
    }

//    private final ImageFactoryListener factoryListener = new ImageFactoryListener() {
//        @Override
//        public void initializing(int procedure_id, int total) {
//            setMaxSubProgress(total);
//        }
//
//        @Override
//        public void onSaved(int procedure_id, File file) {
//
//        }
//
//        @Override
//        public void onConversionComplete(int procedure_id) {
//
//        }
//
//        @Override
//        public void onAppend(int i, File file, int i1, int i2) {
//            setSubProgress(i1 + 1);
//        }
//    };

//    private void onAutoRotateChange(boolean selected) {
//        combo_direction.setSelectedItem(PageDirection.Portrait);
//        combo_direction.setEnabled(!selected);
//    }
//
//    private void clearAll() {
//        tasks.clear();
//        updateUISources();
//    }
//

    private void browseOutputFolder() {
        if (destinationFolderChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
            viewListener.onOutputFolderChanged(destinationFolderChooser.getSelectedFile());

//            File directories = destinationFolderChooser.getSelectedFile();
//            if (directories == null)
//                return;
//            field_destination_folder.setText(directories.getAbsolutePath());
//            updateUISources();
    }

    private void browseSources() {
        if (sourceFilesChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            viewListener.onSourcesFileSelected(sourceFilesChooser.getSelectedFiles());
    }


    public void updateSourceTree(List<Task> tasks) {
        DefaultTreeModel model = (DefaultTreeModel) tree_sources.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        root.removeAllChildren();

        for (Task task : tasks) {
            DefaultMutableTreeNode node1 = new DefaultMutableTreeNode(task.destination.getName());
            for (File file : task.files) {
                DefaultMutableTreeNode node2 = new DefaultMutableTreeNode(file.getName());
                node1.add(node2);
            }
            root.add(node1);
        }
        button_convert.setEnabled(tasks.size() != 0);
        clearAllButton.setEnabled(tasks.size() != 0);
        model.reload();
    }


//    public PageArgument getPageArgument() {
//        PageSize size = (PageSize) combo_size.getSelectedItem();
//        PageDirection direction = (PageDirection) combo_direction.getSelectedItem();
//        boolean auto_rotate = check_auto.isSelected();
//        PageAlign.VerticalAlign verticalAlign = (PageAlign.VerticalAlign) combo_vertical.getSelectedItem();
//        PageAlign.HorizontalAlign horizontalAlign = (PageAlign.HorizontalAlign) combo_horizontal.getSelectedItem();
//        return new PageArgument(verticalAlign, horizontalAlign, size, direction, auto_rotate);
//    }
//
//    public DocumentArgument getDocumentArgument() {
//        String owner_password = pwd_owner_password.getPassword().length > 0 ? Arrays.toString(pwd_owner_password.getPassword()) : null;
//        String user_password = pwd_user_password.getPassword().length > 0 ? Arrays.toString(pwd_user_password.getPassword()) : null;
//        return new DocumentArgument(owner_password, user_password);
//    }
//
//    public ColorType getColorType() {
//        return (ColorType) comboBox_color.getSelectedItem();
//    }
//
//    public FileFilter getFilter() {
//        return new GlobbingFileFilter(field_filter.getText());
//    }
//
//    public NameFormatter<File> getFormatter() {
//        String dest = field_destination_format.getText();
//        return new FileNameFormatter(dest);
//    }

    public void setProgress(int value, int max) {
        progressBar_total.setMaximum(max);
        progressBar_total.setValue(value);
        label_progress.setText(value + "/" + max);
    }

    public void setPageProgress(int value, int max) {
        progressBar_sub.setMaximum(max);
        progressBar_sub.setValue(value);
        label_sub_progress.setText(value + "/" + max);
    }


    public JPanel getRootPanel() {
        return root;
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        root = new JPanel();
        root.setLayout(new GridLayoutManager(9, 2, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(11, 1, new Insets(5, 5, 5, 5), -1, -1));
        root.add(panel1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        combo_size = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        combo_size.setModel(defaultComboBoxModel1);
        panel3.add(combo_size, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Size");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel4, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Color");
        panel4.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBox_color = new JComboBox();
        panel4.add(comboBox_color, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel5, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(253, 34), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Alignment");
        panel5.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        combo_horizontal = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        combo_horizontal.setModel(defaultComboBoxModel2);
        panel5.add(combo_horizontal, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        combo_vertical = new JComboBox();
        panel5.add(combo_vertical, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel6, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel6.add(panel7, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("User Password");
        panel7.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pwd_user_password = new JPasswordField();
        panel7.add(pwd_user_password, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel6.add(panel8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        pwd_owner_password = new JPasswordField();
        panel8.add(pwd_owner_password, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Owner Password");
        panel8.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel6.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel9, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        button_convert = new JButton();
        button_convert.setText("Convert");
        panel9.add(button_convert, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        progressBar_total = new JProgressBar();
        panel9.add(progressBar_total, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label_progress = new JLabel();
        label_progress.setText("");
        panel9.add(label_progress, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel9.add(panel10, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        progressBar_sub = new JProgressBar();
        panel10.add(progressBar_sub, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label_sub_progress = new JLabel();
        label_sub_progress.setText("");
        panel9.add(label_sub_progress, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        stopButton = new JButton();
        stopButton.setText("Stop");
        panel9.add(stopButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel11, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        field_filter = new JTextField();
        field_filter.setText("*.{PNG,png,JPG,jpg}");
        panel11.add(field_filter, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Filter");
        panel11.add(label6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        button_source_browse = new JButton();
        button_source_browse.setText("Browse");
        panel11.add(button_source_browse, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel12, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Direction");
        panel12.add(label7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel12.add(spacer2, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        combo_direction = new JComboBox();
        panel12.add(combo_direction, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        check_auto = new JCheckBox();
        check_auto.setEnabled(true);
        check_auto.setText("Auto Rotate");
        panel12.add(check_auto, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel13 = new JPanel();
        panel13.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel13, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        Font label8Font = this.$$$getFont$$$(null, -1, 16, label8.getFont());
        if (label8Font != null) label8.setFont(label8Font);
        label8.setText("File");
        panel13.add(label8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel14 = new JPanel();
        panel14.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel14, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        Font label9Font = this.$$$getFont$$$(null, -1, 16, label9.getFont());
        if (label9Font != null) label9.setFont(label9Font);
        label9.setText("Page setting");
        panel14.add(label9, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        panel14.add(separator1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel15 = new JPanel();
        panel15.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel15, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JSeparator separator2 = new JSeparator();
        panel15.add(separator2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        Font label10Font = this.$$$getFont$$$(null, -1, 16, label10.getFont());
        if (label10Font != null) label10.setFont(label10Font);
        label10.setText("Encrypt");
        panel15.add(label10, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator3 = new JSeparator();
        panel1.add(separator3, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel16 = new JPanel();
        panel16.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel16, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Output Format");
        panel16.add(label11, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        field_destination_format = new JTextField();
        field_destination_format.setText("<NAME>.pdf");
        panel16.add(field_destination_format, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel16.add(spacer3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel17 = new JPanel();
        panel17.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel17, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Output Folder");
        panel17.add(label12, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        button_destination_browse = new JButton();
        button_destination_browse.setText("Browse");
        panel17.add(button_destination_browse, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        field_destination_folder = new JTextField();
        field_destination_folder.setText(".");
        panel17.add(field_destination_folder, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel18 = new JPanel();
        panel18.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        root.add(panel18, new GridConstraints(1, 0, 6, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel18.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(400, -1), null, 0, false));
        tree_sources = new JTree();
        tree_sources.setEnabled(true);
        scrollPane1.setViewportView(tree_sources);
        imagePane = new JLabel();
        imagePane.setText("");
        root.add(imagePane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel19 = new JPanel();
        panel19.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        root.add(panel19, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        clearAllButton = new JButton();
        clearAllButton.setText("Clear All");
        panel19.add(clearAllButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        root.add(spacer4, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label13 = new JLabel();
        Font label13Font = this.$$$getFont$$$(null, -1, 16, label13.getFont());
        if (label13Font != null) label13.setFont(label13Font);
        label13.setText("Log");
        root.add(label13, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        root.add(scrollPane2, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        logList = new JList();
        scrollPane2.setViewportView(logList);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return root;
    }

    public void setViewListener(ViewListener viewListener) {
        this.viewListener = viewListener;
    }

    public void setOutputFolderField(String text) {
        field_destination_folder.setText(text);
    }
}
