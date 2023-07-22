package org.vincentyeh.img2pdf.gui;

import org.vincentyeh.img2pdf.gui.ui.View;
import org.vincentyeh.img2pdf.gui.ui.components.Task;
import org.vincentyeh.img2pdf.lib.image.ColorType;
import org.vincentyeh.img2pdf.lib.pdf.parameter.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.util.List;

public class Controller {
    private final Model model;
    private final View view;

    private final DefaultListModel<String> listModel = new DefaultListModel<>();

    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;
        model.setModelListener(new ModelListener() {

            @Override
            public void onTotalConversionProgressUpdate(int progress, int total) {
                setConversionProgress(progress, total);
            }

            @Override
            public void onPageConversionProgressUpdate(int progress, int total) {
                setPageConversionProgress(progress, total);
            }

            @Override
            public void onSourcesUpdate(List<Task> source) {
                updateSourceTree(model.getSources());
            }

            @Override
            public void onLogUpdate(List<String> log) {
                view.getLogList().setModel(convertToModel(model.getLogList()));
            }
        });
    }

    private ListModel<String> convertToModel(List<String> list) {
        listModel.clear();
        for (int i = 0; i < list.size(); i++) {
            listModel.add(i, list.get(i));
        }
        return listModel;
    }

    public void initialize() {
        JComboBox<PageDirection> combo_direction = view.getDirectionComboBox();

        for (PageDirection direction : PageDirection.values()) {
            combo_direction.addItem(direction);
        }
        combo_direction.addItemListener(e -> model.setPageDirection((PageDirection) combo_direction.getSelectedItem()));
        model.setPageDirection((PageDirection) combo_direction.getSelectedItem());

        JComboBox<PageAlign.VerticalAlign> combo_vertical = view.getVerticalAlignComboBox();
        for (PageAlign.VerticalAlign align : PageAlign.VerticalAlign.values()) {
            combo_vertical.addItem(align);
        }

        combo_vertical.addItemListener(e -> model.setVerticalAlign((PageAlign.VerticalAlign) combo_vertical.getSelectedItem()));
        model.setVerticalAlign((PageAlign.VerticalAlign) combo_vertical.getSelectedItem());

        JComboBox<PageAlign.HorizontalAlign> combo_horizontal = view.getHorizontalComboBox();

        for (PageAlign.HorizontalAlign align : PageAlign.HorizontalAlign.values()) {
            combo_horizontal.addItem(align);
        }
        combo_horizontal.addItemListener(e -> model.setHorizontalAlign((PageAlign.HorizontalAlign) combo_horizontal.getSelectedItem()));
        model.setHorizontalAlign((PageAlign.HorizontalAlign) combo_horizontal.getSelectedItem());

        JComboBox<PageSize> combo_size = view.getSizeComboBox();
        for (PageSize size : PageSize.values()) {
            combo_size.addItem(size);
        }
        combo_size.addItemListener(e -> {
                    onSizeChange((PageSize) combo_size.getSelectedItem());
                }
        );
        model.setPageSize((PageSize) combo_size.getSelectedItem());

        JComboBox<ColorType> comboBox_color = view.getColorComboBox();
        for (ColorType color : ColorType.values()) {
            comboBox_color.addItem(color);
        }
        comboBox_color.addItemListener(e -> model.setColorType((ColorType) comboBox_color.getSelectedItem()));
        model.setColorType((ColorType) comboBox_color.getSelectedItem());

        JCheckBox check_auto = view.getAutoRotateCheckBox();
        check_auto.addActionListener(e -> onAutoRotateChange());
        model.setAutoRotate(check_auto.isSelected());

        JButton button_convert = view.getConvertButton();
        button_convert.addActionListener(e -> convert());
        JButton clearAllButton = view.getClearAllButton();
        clearAllButton.addActionListener(e -> model.clearSource());

        JButton button_source_browse = view.getSourceBrowseButton();
        button_source_browse.addActionListener(e -> browseSources());

        JButton button_output_folder_browse = view.getOutputFolderBrowseButton();
        button_output_folder_browse.addActionListener(e -> browseOutputFolder());

        view.getOutputFolderField().setText(model.getOutputFolder().getAbsolutePath());
        view.getOutputFolderChooser().setCurrentDirectory(model.getOutputFolder());

        updateSourceTree(model.getSources());
    }

    private void onSizeChange(PageSize selectedItem) {
        if (selectedItem == null)
            return;
        model.setPageSize(selectedItem);

        if (model.getPageSize() == PageSize.DEPEND_ON_IMG) {
            model.setPageDirection(PageDirection.Portrait);
            model.setAutoRotate(false);
            model.setVerticalAlign(PageAlign.VerticalAlign.CENTER);
            model.setHorizontalAlign(PageAlign.HorizontalAlign.CENTER);

        }
        view.getDirectionComboBox().setEnabled(!model.isAutoRotate() && model.getPageSize() != PageSize.DEPEND_ON_IMG);

        view.getAutoRotateCheckBox().setEnabled(model.getPageSize() != PageSize.DEPEND_ON_IMG);
        view.getVerticalAlignComboBox().setEnabled(model.getPageSize() != PageSize.DEPEND_ON_IMG);
        view.getHorizontalComboBox().setEnabled(model.getPageSize() != PageSize.DEPEND_ON_IMG);
        view.getDirectionComboBox().setSelectedItem(model.getPageDirection());
        view.getAutoRotateCheckBox().setSelected(model.isAutoRotate());
        view.getVerticalAlignComboBox().setSelectedItem(model.getVerticalAlign());
        view.getHorizontalComboBox().setSelectedItem(model.getHorizontalAlign());

    }

    private void onAutoRotateChange() {
        model.setAutoRotate(view.getAutoRotateCheckBox().isSelected());
        model.setPageDirection(PageDirection.Portrait);

        view.getDirectionComboBox().setEnabled(!view.getAutoRotateCheckBox().isSelected());
        view.getDirectionComboBox().setSelectedItem(model.getPageDirection());
    }

    private void convert() {
        String owner_password = String.valueOf(view.getOwnerPasswordField().getPassword());
        owner_password = owner_password.length() > 0 ? owner_password : null;
        String user_password = String.valueOf(view.getUserPasswordField().getPassword());
        user_password = user_password.length() > 0 ? user_password : null;
        model.convert(owner_password, user_password);
    }


    private void browseOutputFolder() {
        JFileChooser outputFolderChooser = view.getOutputFolderChooser();
        if (outputFolderChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            model.setOutputFolder(outputFolderChooser.getSelectedFile());
            view.getOutputFolderField().setText(model.getOutputFolder().getPath());
            outputFolderChooser.setCurrentDirectory(model.getOutputFolder());
        }
    }

    private void browseSources() {
        JFileChooser sourceFilesChooser = view.getSourceFilesChooser();
        if (sourceFilesChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            String output_format = view.getOutputFormatField().getText();
            String file_filter = view.getFileFilterField().getText();
            if (output_format == null || file_filter == null)
                return;
            model.addSource(sourceFilesChooser.getSelectedFiles(), output_format, file_filter);
        }
        updateSourceTree(model.getSources());
    }

    public void updateSourceTree(List<Task> tasks) {
        DefaultTreeModel model = (DefaultTreeModel) view.getSourcesTree().getModel();
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
        view.getConvertButton().setEnabled(tasks.size() != 0);
        view.getClearAllButton().setEnabled(tasks.size() != 0);
        model.reload();
    }

    public void setConversionProgress(int value, int max) {
        view.getTotalConversionProgressBar().setMaximum(max);
        view.getTotalConversionProgressBar().setValue(value);
        view.getTotalConversionLabel().setText(value + "/" + max);
    }

    public void setPageConversionProgress(int value, int max) {
        view.getPageConversionProgressBar().setMaximum(max);
        view.getPageConversionProgressBar().setValue(value);
        view.getPageConversionLabel().setText(value + "/" + max);
    }

}
