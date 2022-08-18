package org.vincentyeh.img2pdf.gui.ui;


import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.vincentyeh.IMG2PDF.lib.image.helper.concrete.DirectionImageHelper;
import org.vincentyeh.IMG2PDF.lib.pdf.concrete.appender.ExecutorPageAppender;
import org.vincentyeh.IMG2PDF.lib.pdf.concrete.calculation.strategy.StandardImagePageCalculationStrategy;
import org.vincentyeh.IMG2PDF.lib.pdf.concrete.converter.ImageHelperPDFCreatorImpl;
import org.vincentyeh.IMG2PDF.lib.pdf.concrete.converter.PDFBoxCreatorImpl;
import org.vincentyeh.IMG2PDF.lib.pdf.function.converter.ImagePDFCreator;
import org.vincentyeh.IMG2PDF.lib.pdf.parameter.*;
import org.vincentyeh.IMG2PDF.lib.task.concrete.factory.DirectoryTaskFactory;
import org.vincentyeh.IMG2PDF.lib.task.framework.Task;
import org.vincentyeh.IMG2PDF.lib.task.framework.factory.TaskFactory;
import org.vincentyeh.IMG2PDF.lib.task.framework.factory.exception.TaskFactoryProcessException;
import org.vincentyeh.IMG2PDF.lib.util.file.FileNameFormatter;
import org.vincentyeh.IMG2PDF.lib.util.file.FileSorter;
import org.vincentyeh.IMG2PDF.lib.util.file.GlobbingFileFilter;
import org.vincentyeh.IMG2PDF.lib.util.file.exception.MakeDirectoryException;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class MainFrame {
    private File[] files;
    private JButton button_browse;
    private JPanel root;
    private JTextField field_sources;
    private JComboBox<PageSize> combo_size;
    private JComboBox<PageAlign.HorizontalAlign> combo_horizontal;
    private JComboBox<PageAlign.VerticalAlign> combo_vertical;
    private JTextField field_destination;
    private JPasswordField pwd_owner_password;
    private JPasswordField pwd_user_password;
    private JButton button_convert;
    private JTextField field_filter;
    private JComboBox<PageDirection> combo_direction;
    private JCheckBox check_auto;

    public MainFrame() {
        createUIComponents();
        initializeListener();
    }

    private void initializeListener() {
        button_browse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(true);

            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int option = chooser.showOpenDialog(null);
            if (option == JFileChooser.APPROVE_OPTION) {
                files = chooser.getSelectedFiles();
            }
        });
        button_convert.addActionListener(e -> {
            try {
                List<Task> tasks = toTasks(files);

                ImagePDFCreator creator = new ImagePDFCreator(new PDFBoxCreatorImpl(new File("temp"), 1024 * 1024 * 100),
                        new ImageHelperPDFCreatorImpl(new DirectionImageHelper(null)),
                        new ExecutorPageAppender(10), true, new StandardImagePageCalculationStrategy());


                for (Task task : tasks) {
                    creator.start(task);
                }

            } catch (TaskFactoryProcessException | MakeDirectoryException ex) {
                ex.printStackTrace();
            }
        });

    }

    private void createUIComponents() {
        for (PageDirection direction : PageDirection.values()) {
            combo_direction.addItem(direction);
        }
        for (PageAlign.VerticalAlign align : PageAlign.VerticalAlign.values()) {
            combo_vertical.addItem(align);
        }
        for (PageAlign.HorizontalAlign align : PageAlign.HorizontalAlign.values()) {
            combo_horizontal.addItem(align);
        }
        for (PageSize size : PageSize.values()) {
            combo_size.addItem(size);
        }
    }

    private List<Task> toTasks(File[] directories) throws TaskFactoryProcessException {
        TaskFactory<File> factory = new DirectoryTaskFactory(
                getDocumentArgument(), getPageArgument(), new GlobbingFileFilter(field_filter.getText()),
                new FileSorter(FileSorter.Sortby.NAME, FileSorter.Sequence.INCREASE),
                new FileNameFormatter(field_destination.getText()));

        List<Task> tasks = new LinkedList<>();
        for (File directory : directories) {
            tasks.add(factory.create(directory));
        }
        return tasks;
    }

    private PageArgument getPageArgument() {
        PageArgument argument = new PageArgument();
        PageAlign.HorizontalAlign horizontalAlign = combo_horizontal.getItemAt(combo_horizontal.getSelectedIndex());
        PageAlign.VerticalAlign verticalAlign = combo_vertical.getItemAt(combo_vertical.getSelectedIndex());

        argument.setAlign(new PageAlign(verticalAlign, horizontalAlign));
        argument.setSize(combo_size.getItemAt(combo_size.getSelectedIndex()));
        argument.setDirection(combo_direction.getItemAt(combo_direction.getSelectedIndex()));
        argument.setAutoRotate(check_auto.isSelected());
        return argument;
    }

    private DocumentArgument getDocumentArgument() {
        DocumentArgument argument = new DocumentArgument();
        argument.setInformation(null);
        String owner, user;

        if (pwd_owner_password.getPassword().length == 0) {
            owner = new String(pwd_owner_password.getPassword());
        } else {
            owner = null;
        }

        if (pwd_user_password.getPassword().length == 0) {
            user = new String(pwd_user_password.getPassword());
        } else {
            user = null;
        }

        argument.setOwnerPassword(owner);
        argument.setUserPassword(user);
        argument.setPermission(new Permission());
        return argument;
    }

    public JPanel getRootPanel() {
        return root;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
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
        root.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(9, 1, new Insets(5, 5, 5, 5), -1, -1));
        root.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        button_browse = new JButton();
        button_browse.setText("browse");
        panel2.add(button_browse, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Sources");
        panel2.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        field_sources = new JTextField();
        panel2.add(field_sources, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Size");
        panel3.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        combo_size = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        combo_size.setModel(defaultComboBoxModel1);
        panel3.add(combo_size, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel4, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Owner Password");
        panel4.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pwd_owner_password = new JPasswordField();
        panel4.add(pwd_owner_password, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel5, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("User Password");
        panel5.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pwd_user_password = new JPasswordField();
        panel5.add(pwd_user_password, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel6, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Destination");
        panel6.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        field_destination = new JTextField();
        field_destination.setText("");
        panel6.add(field_destination, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel7, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Horizontal Alignment");
        panel8.add(label6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        combo_horizontal = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        combo_horizontal.setModel(defaultComboBoxModel2);
        panel8.add(combo_horizontal, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel9, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Vertical Alignment");
        panel9.add(label7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        combo_vertical = new JComboBox();
        panel9.add(combo_vertical, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        button_convert = new JButton();
        button_convert.setText("Convert");
        panel1.add(button_convert, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel10, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Filter");
        panel10.add(label8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        field_filter = new JTextField();
        field_filter.setText("*.{PNG,png,JPG,jpg}");
        panel10.add(field_filter, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel11, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Direction");
        panel11.add(label9, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        combo_direction = new JComboBox();
        panel11.add(combo_direction, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        check_auto = new JCheckBox();
        check_auto.setText("Auto Rotate");
        panel11.add(check_auto, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return root;
    }
}
