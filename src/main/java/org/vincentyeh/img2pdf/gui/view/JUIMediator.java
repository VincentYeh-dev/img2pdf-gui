package org.vincentyeh.img2pdf.gui.view;

import org.vincentyeh.img2pdf.gui.model.Task;
import org.vincentyeh.img2pdf.gui.model.TaskSortOrder;
import org.vincentyeh.img2pdf.lib.image.ColorType;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageAlign;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageDirection;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageSize;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Concrete {@link UIMediator} implementation that manages all Swing component
 * interactions for the img2pdf-gui application.
 * <p>
 * Components are wired together via the inner {@link Builder} class.
 * All UI event processing is centralised in {@link #notifyUI(String, Object...)}
 * using string-based event identifiers. State is persisted in the singleton
 * {@link UIState}; user-action events are forwarded to the registered
 * {@link MediatorListener}.
 * </p>
 */
public class JUIMediator implements UIMediator {

    /**
     * Represents the display status of a task node in the source tree.
     */
    public enum TaskStatus {
        /** Task has not yet been processed. */
        PENDING,
        /** Task completed successfully. */
        SUCCESS,
        /** Task failed during conversion. */
        FAILED
    }

    /** Side length (in pixels) of the thumbnail icons shown in the source tree. */
    private static final int THUMBNAIL_SIZE = 48;

    /** Cache of scaled thumbnail icons keyed by the first image file of each task. */
    private final Map<File, ImageIcon> thumbnailCache = new HashMap<>();

    /** Maps each task to its current display status (visible in the tree). */
    final Map<Task, TaskStatus> taskStatusMap = new HashMap<>();

    private MediatorListener listener;
    private JButton sourceBrowseButton;
    private JComboBox<PageSize> pageSizeComboBox;
    private JComboBox<PageAlign.HorizontalAlign> horizontalAlignComboBox;
    private JComboBox<PageAlign.VerticalAlign> verticalAlignComboBox;
    private JPasswordField ownerPasswordField;
    private JPasswordField userPasswordField;
    private JButton convertButton;
    private JComboBox<PageDirection> directionComboBox;
    private JCheckBox autoRotateCheckBox;
    private JCheckBox encryptCheckBox;
    private JProgressBar totalConversionProgressBar;
    private JComboBox<ColorType> colorTypeComboBox;
    private JTree sourceTree;
    private JLabel totalConversionLabel;
    private JButton clearAllButton;
    private JProgressBar pageConversionProgressBar;
    private JLabel pageConversionLabel;
    private JButton stopButton;
    private JButton outputFolderBrowseButton;
    private JTextField outputFolderField;

    private List<Task> currentTasks = new ArrayList<>();

    private final UIState state = UIState.getInstance();

    /**
     * Builder that links individual Swing components to a {@link JUIMediator}
     * instance, then returns the fully configured mediator via {@link #build()}.
     * <p>
     * Every {@code link*()} method assigns the component name (for AssertJ Swing
     * lookup), stores the reference in the mediator, and attaches the appropriate
     * event listener.
     * </p>
     */
    public static class Builder {
        private final JUIMediator mediator = new JUIMediator();

        /**
         * Links the source-folder browse button to the mediator.
         *
         * @param button the button that opens the source-directory chooser
         */
        public void linkSourceBrowseButton(JButton button) {
            button.setName("sourceBrowseButton");
            mediator.sourceBrowseButton = button;
            mediator.sourceBrowseButton.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mediator.notifyUI("source_browse_button_click");
                }
            });
        }

        /**
         * Links the page-size combo-box to the mediator.
         *
         * @param comboBox the combo-box containing {@link PageSize} options
         */
        public void linkPageSizeComboBox(JComboBox<PageSize> comboBox) {
            comboBox.setName("pageSizeComboBox");
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

        /**
         * Links the horizontal-alignment combo-box to the mediator.
         *
         * @param comboBox the combo-box containing {@link PageAlign.HorizontalAlign} options
         */
        public void linkHorizontalAlignComboBox(JComboBox<PageAlign.HorizontalAlign> comboBox) {
            comboBox.setName("horizontalAlignComboBox");
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

        /**
         * Links the vertical-alignment combo-box to the mediator.
         *
         * @param comboBox the combo-box containing {@link PageAlign.VerticalAlign} options
         */
        public void linkVerticalAlignComboBox(JComboBox<PageAlign.VerticalAlign> comboBox) {
            comboBox.setName("verticalAlignComboBox");
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

        /**
         * Links the owner-password field to the mediator.
         *
         * @param passwordField the password field for the PDF owner (permissions) password
         */
        public void linkOwnerPasswordField(JPasswordField passwordField) {
            passwordField.setName("ownerPasswordField");
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

        /**
         * Links the user-password field to the mediator.
         *
         * @param passwordField the password field for the PDF user (open) password
         */
        public void linkUserPasswordField(JPasswordField passwordField) {
            passwordField.setName("userPasswordField");
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

        /**
         * Links the Convert button to the mediator.
         *
         * @param button the button that triggers batch PDF conversion
         */
        public void linkConvertButton(JButton button) {
            button.setName("convertButton");
            mediator.convertButton = button;
            mediator.convertButton.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mediator.notifyUI("convert_button_click");
                }
            });
        }

        /**
         * Links the page-direction combo-box to the mediator.
         *
         * @param comboBox the combo-box containing {@link PageDirection} options
         */
        public void linkDirectionComboBox(JComboBox<PageDirection> comboBox) {
            comboBox.setName("directionComboBox");
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

        /**
         * Links the auto-rotate check-box to the mediator.
         *
         * @param checkBox the check-box that enables automatic image rotation
         */
        public void linkAutoRotateCheckBox(JCheckBox checkBox) {
            checkBox.setName("autoRotateCheckBox");
            mediator.autoRotateCheckBox = checkBox;
            mediator.autoRotateCheckBox.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean selected = mediator.autoRotateCheckBox.isSelected();
                    mediator.notifyUI("auto_rotate_change", selected);
                }
            });
        }

        /**
         * Links the overall batch conversion progress bar to the mediator.
         *
         * @param progressBar the progress bar that shows how many tasks have been completed
         */
        public void linkTotalConversionProgressBar(JProgressBar progressBar) {
            progressBar.setName("totalConversionProgressBar");
            mediator.totalConversionProgressBar = progressBar;
        }


        /**
         * Links the colour-type combo-box to the mediator.
         *
         * @param comboBox the combo-box containing {@link ColorType} options
         */
        public void linkColorTypeComboBox(JComboBox<ColorType> comboBox) {
            comboBox.setName("colorTypeComboBox");
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

        /**
         * Links the source task tree to the mediator.
         * <p>
         * Also configures:
         * <ul>
         *   <li>A custom cell renderer that shows task status icons and thumbnails.</li>
         *   <li>A right-click context menu for removing tasks or deleting source folders.</li>
         *   <li>A drag-and-drop {@link TransferHandler} for dropping folders onto the tree.</li>
         * </ul>
         *
         * @param tree the tree used to display the list of conversion tasks
         */
        public void linkSourceTree(JTree tree) {
            tree.setName("sourceTree");
            mediator.sourceTree = tree;
            tree.setRowHeight(0); // 讓各列根據 renderer 自動計算高度
            tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

            // 4-B: Cell Renderer，讓 Task 節點顯示縮圖、狀態圖示與 destination 名稱
            tree.setCellRenderer(new DefaultTreeCellRenderer() {
                @Override
                public Component getTreeCellRendererComponent(
                        JTree tree, Object value, boolean sel, boolean expanded,
                        boolean leaf, int row, boolean hasFocus) {
                    Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
                    if (userObject instanceof Task) {
                        Task task = (Task) userObject;
                        TaskStatus status = mediator.taskStatusMap.getOrDefault(task, TaskStatus.PENDING);
                        String prefix = status == TaskStatus.SUCCESS ? "\u2713 " :
                                        status == TaskStatus.FAILED  ? "\u2717 " : "";
                        value = prefix + task.destination.getName();
                        Component c = super.getTreeCellRendererComponent(
                                tree, value, sel, expanded, leaf, row, hasFocus);
                        if (!sel) {
                            if (status == TaskStatus.SUCCESS)
                                c.setForeground(new Color(76, 175, 80));
                            else if (status == TaskStatus.FAILED)
                                c.setForeground(new Color(244, 67, 54));
                        }
                        if (task.files != null && task.files.length > 0) {
                            ImageIcon icon = mediator.thumbnailCache.get(task.files[0]);
                            if (icon != null) {
                                setIcon(icon);
                            }
                        }
                        return c;
                    }
                    return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                }
            });

            // 4-C: 右鍵 MouseListener
            tree.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        int row = tree.getRowForLocation(e.getX(), e.getY());
                        if (row < 0) {
                            row = tree.getClosestRowForLocation(e.getX(), e.getY());
                            if (row < 0) return;
                            java.awt.Rectangle bounds = tree.getRowBounds(row);
                            if (bounds == null || e.getY() < bounds.y || e.getY() > bounds.y + bounds.height) return;
                        }

                        // 若右鍵點擊的 row 不在目前選擇中，才切換選擇
                        if (!tree.isRowSelected(row)) {
                            tree.setSelectionRow(row);
                        }

                        // 收集所有選中的 Task 節點
                        TreePath[] selectionPaths = tree.getSelectionPaths();
                        if (selectionPaths == null) return;
                        List<Task> selectedTasks = new ArrayList<>();
                        for (TreePath path : selectionPaths) {
                            Object last = path.getLastPathComponent();
                            if (last instanceof DefaultMutableTreeNode) {
                                Object userObj = ((DefaultMutableTreeNode) last).getUserObject();
                                if (userObj instanceof Task) {
                                    selectedTasks.add((Task) userObj);
                                }
                            }
                        }
                        if (selectedTasks.isEmpty()) return;

                        JPopupMenu popup = new JPopupMenu();

                        JMenuItem removeItem = new JMenuItem("Remove from list");
                        removeItem.addActionListener(ev -> mediator.notifyUI("remove_tasks", selectedTasks));
                        popup.add(removeItem);

                        JMenuItem removeFromDiskItem = new JMenuItem("Remove from disk");
                        removeFromDiskItem.addActionListener(ev -> {
                            StringBuilder sb = new StringBuilder();
                            for (Task t : selectedTasks) {
                                sb.append(t.files[0].getParentFile().getAbsolutePath()).append("\n");
                            }
                            Object[] options = {"Delete", "Cancel"};
                            int confirm = JOptionPane.showOptionDialog(
                                    tree,
                                    "This will permanently delete the following folder(s):\n\n"
                                            + sb.toString()
                                            + "\nand all files inside them. This action cannot be undone.",
                                    "Warning",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.WARNING_MESSAGE,
                                    null,
                                    options,
                                    options[1]
                            );
                            if (confirm == JOptionPane.YES_OPTION) {
                                mediator.notifyUI("remove_tasks_from_disk", selectedTasks);
                            }
                        });
                        popup.add(removeFromDiskItem);

                        popup.show(tree, e.getX(), e.getY());
                    }
                }
            });

            // 4-D: 拖曳 TransferHandler
            tree.setDropMode(DropMode.ON_OR_INSERT);
            tree.setTransferHandler(new TransferHandler() {
                @Override
                public boolean canImport(TransferSupport support) {
                    return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
                }

                @Override
                @SuppressWarnings("unchecked")
                public boolean importData(TransferSupport support) {
                    if (!canImport(support)) return false;
                    try {
                        List<File> dropped = (List<File>) support.getTransferable()
                                .getTransferData(DataFlavor.javaFileListFlavor);
                        List<File> dirs = dropped.stream()
                                .filter(File::isDirectory)
                                .collect(Collectors.toList());
                        if (dirs.isEmpty()) return false;

                        File[] existing = mediator.state.getSourceFiles();
                        List<File> merged = new ArrayList<>();
                        if (existing != null) merged.addAll(Arrays.asList(existing));
                        for (File dir : dirs) {
                            if (!merged.contains(dir)) merged.add(dir);
                        }
                        mediator.state.setSourceFiles(merged.toArray(new File[0]));
                        if (mediator.listener != null)
                            mediator.listener.onSourcesUpdate(mediator, mediator.state);
                        return true;
                    } catch (Exception ex) {
                        return false;
                    }
                }
            });
        }

        /**
         * Links the batch progress label to the mediator.
         *
         * @param label the label that shows the current/total task count (e.g. {@code "2/5"})
         */
        public void linkTotalConversionLabel(JLabel label) {
            label.setName("totalConversionLabel");
            mediator.totalConversionLabel = label;
        }

        /**
         * Links the Clear All button to the mediator.
         *
         * @param button the button that clears all tasks from the list
         */
        public void linkClearAllButton(JButton button) {
            button.setName("clearAllButton");
            mediator.clearAllButton = button;
            mediator.clearAllButton.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mediator.notifyUI("clear_all_button_click");
                }
            });
        }

        /**
         * Links the per-page conversion progress bar to the mediator.
         *
         * @param progressBar the progress bar that shows page-level progress for the current task
         */
        public void linkPageConversionProgressBar(JProgressBar progressBar) {
            progressBar.setName("pageConversionProgressBar");
            mediator.pageConversionProgressBar = progressBar;
        }

        /**
         * Links the per-page progress label to the mediator.
         *
         * @param label the label that shows the current/total page count within the active task
         */
        public void linkPageConversionLabel(JLabel label) {
            label.setName("pageConversionLabel");
            mediator.pageConversionLabel = label;
        }

        /**
         * Links the Stop button to the mediator.
         *
         * @param button the button that requests early termination of the running conversion
         */
        public void linkStopButton(JButton button) {
            button.setName("stopButton");
            mediator.stopButton = button;
            mediator.stopButton.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mediator.notifyUI("stop_button_click");
                }
            });
        }

        /**
         * Links the output-folder browse button to the mediator.
         *
         * @param button the button that opens the output-directory chooser dialog
         */
        public void linkOutputFolderBrowseButton(JButton button) {
            button.setName("outputFolderBrowseButton");
            mediator.outputFolderBrowseButton = button;
            mediator.outputFolderBrowseButton.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mediator.notifyUI("output_folder_browse_button_click");
                }
            });
        }

        /**
         * Links the output-folder text field to the mediator.
         * The field is set to read-only; its text is updated programmatically.
         *
         * @param textField the text field displaying the selected output directory path
         */
        public void linkOutputFolderField(JTextField textField) {
            textField.setName("outputFolderField");
            textField.setEditable(false);
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

        /**
         * Links the sort-order combo-box to the mediator and populates its items.
         *
         * @param comboBox the combo-box containing {@link TaskSortOrder} options
         * @return {@code this} builder, for method chaining
         */
        public Builder linkSortComboBox(JComboBox<TaskSortOrder> comboBox) {
            comboBox.setName("sortComboBox");
            for (TaskSortOrder order : TaskSortOrder.values()) {
                comboBox.addItem(order);
            }
            comboBox.addActionListener(e -> {
                TaskSortOrder order = (TaskSortOrder) comboBox.getSelectedItem();
                if (order != null && mediator.listener != null)
                    mediator.listener.onSortOrderChange(mediator, order);
            });
            return this;
        }

        /**
         * Links the encryption enable check-box to the mediator.
         *
         * @param checkBox the check-box that toggles PDF encryption on or off
         */
        public void linkEncryptionCheckBox(JCheckBox checkBox) {
            checkBox.setName("encryptCheckBox");
            mediator.encryptCheckBox = checkBox;
            mediator.encryptCheckBox.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean selected = mediator.encryptCheckBox.isSelected();
                    mediator.notifyUI("encryption_change", selected);
                }
            });
        }

        /**
         * Builds and returns the fully configured {@link UIMediator}.
         *
         * @return the constructed mediator, ready to use after calling
         *         {@link UIMediator#initialize()}
         */
        public UIMediator build() {
            return mediator;
        }
    }

    /** Private constructor — instances are created exclusively by {@link Builder}. */
    private JUIMediator() {

    }

    /**
     * Central event-dispatch method.
     * <p>
     * Accepts string-keyed events fired by Swing component listeners and routes
     * them to the appropriate state update, UI refresh, or listener notification.
     * </p>
     * Supported event names include:
     * <ul>
     *   <li>{@code "output_folder_change"} — updates the destination folder in {@link UIState}</li>
     *   <li>{@code "owner_password_change"} / {@code "user_password_change"} — updates passwords</li>
     *   <li>{@code "auto_rotate_change"} — toggles auto-rotate and page-direction state</li>
     *   <li>{@code "page_size_change"}, {@code "horizontal_align_change"}, etc. — updates page settings</li>
     *   <li>{@code "source_browse_button_click"} — opens the source-folder chooser</li>
     *   <li>{@code "convert_button_click"} — delegates to {@link MediatorListener#onConvertButtonClick}</li>
     *   <li>{@code "stop_button_click"} — delegates to {@link MediatorListener#onStopButtonClick}</li>
     *   <li>{@code "remove_tasks"} / {@code "remove_tasks_from_disk"} — task removal events</li>
     *   <li>{@code "encryption_change"} — enables/disables encryption and password fields</li>
     * </ul>
     *
     * @param event the name identifying the UI event
     * @param data  optional payload values associated with the event
     */
    @Override
    public void notifyUI(String event, Object... data) {
        if (event.equals("output_folder_change")) {
            String folder = (String) data[0];
            System.out.printf("Output Folder changed: %s\n", folder);
            state.setDestinationFolder(new File(folder));
        }
        if (event.equals("owner_password_change")) {
            if(!state.isEncrypted())
                return;
            String password = (String) data[0];
            System.out.printf("Owner Password changed: %s\n", password.isEmpty() ? "<empty>" : password);
            state.setOwnerPassword(password);
            refreshConvertButton();
        }
        if (event.equals("user_password_change")) {
            if(!state.isEncrypted())
                return;
            String password = (String) data[0];
            System.out.printf("User Password changed: %s\n", password.isEmpty() ? "<empty>" : password);
            state.setUserPassword(password);
            refreshConvertButton();
        }
        if (event.equals("auto_rotate_change")) {
            boolean selected = (boolean) data[0];
            System.out.printf("Auto Rotate changed: %s\n", selected);
            state.setAutoRotate(selected);
            state.setPageDirection(PageDirection.Portrait);
            directionComboBox.setEnabled(!selected);
        }
        if (event.equals("page_size_change")) {
            PageSize size = (PageSize) data[0];
            System.out.printf("Page Size changed: %s\n", size);
            state.setPageSize(size);
            horizontalAlignComboBox.setEnabled(size != PageSize.DEPEND_ON_IMG);
            verticalAlignComboBox.setEnabled(size != PageSize.DEPEND_ON_IMG);
            if (size == PageSize.DEPEND_ON_IMG) {
                horizontalAlignComboBox.setSelectedItem(PageAlign.HorizontalAlign.CENTER);
                verticalAlignComboBox.setSelectedItem(PageAlign.VerticalAlign.CENTER);
            }
        }
        if (event.equals("horizontal_align_change")) {
            PageAlign.HorizontalAlign align = (PageAlign.HorizontalAlign) data[0];
            System.out.printf("Horizontal Align changed: %s\n", align);
            state.setHorizontalAlign(align);
        }
        if (event.equals("vertical_align_change")) {
            PageAlign.VerticalAlign align = (PageAlign.VerticalAlign) data[0];
            System.out.printf("Vertical Align changed: %s\n", align);
            state.setVerticalAlign(align);
        }
        if (event.equals("page_direction_change")) {
            PageDirection direction = (PageDirection) data[0];
            System.out.printf("Page Direction changed: %s\n", direction);
            state.setPageDirection(direction);
        }
        if (event.equals("color_type_change")) {
            ColorType color = (ColorType) data[0];
            System.out.printf("Color Type changed: %s\n", color);
            state.setColorType(color);
        }

        if (event.equals("source_browse_button_click")) {
            System.out.printf("Source Browse Button clicked\n");
            browseSources();
        }
        if (event.equals("output_folder_browse_button_click")) {
            System.out.printf("Output Folder Browse Button clicked\n");
            browseOutputFolder();
        }
        if (event.equals("convert_button_click")) {
            System.out.printf("Convert Button clicked\n");
            if (listener != null)
                listener.onConvertButtonClick(this, state);
        }
        if (event.equals("clear_all_button_click")) {
            System.out.printf("Clear All Button clicked\n");
            state.setSourceFiles(new File[]{});
            if (listener != null)
                listener.onSourcesUpdate(this, state);
        }
        if (event.equals("stop_button_click")) {
            if (listener != null) listener.onStopButtonClick(this);
        }

        if (event.equals("remove_tasks")) {
            @SuppressWarnings("unchecked")
            List<Task> tasks = (List<Task>) data[0];
            if (listener != null) listener.onTaskRemove(this, tasks);
        }

        if (event.equals("remove_tasks_from_disk")) {
            @SuppressWarnings("unchecked")
            List<Task> tasks = (List<Task>) data[0];
            if (listener != null) listener.onTaskRemoveFromDisk(this, tasks);
        }

        if(event.equals("encryption_change")){
            boolean selected = (boolean) data[0];
            System.out.printf("Encryption changed: %s\n", selected);
            state.setEncrypted(selected);
            ownerPasswordField.setEnabled(selected);
            userPasswordField.setEnabled(selected);
            if(!selected){
                state.setOwnerPassword("");
                state.setUserPassword("");
                ownerPasswordField.setText("");
                userPasswordField.setText("");
            }
            refreshConvertButton();
        }

    }

    @Override
    public void updateTasks(List<Task> tasks) {
        currentTasks = new ArrayList<>(tasks);
        taskStatusMap.clear();
        updateSourceTree(currentTasks);
        refreshConvertButton();
    }

    /**
     * Re-evaluates whether the Convert button should be enabled.
     * <p>
     * The button is enabled only when there is at least one task and, if encryption
     * is active, both owner and user passwords are non-empty.
     * </p>
     */
    private void refreshConvertButton() {
        if (currentTasks.isEmpty()) {
            convertButton.setEnabled(false);
            return;
        }
        if (state.isEncrypted()) {
            String owner = state.getOwnerPassword();
            String user  = state.getUserPassword();
            boolean ownerOk = owner != null && !owner.isEmpty();
            boolean userOk  = user != null && !user.isEmpty();
            convertButton.setEnabled(ownerOk && userOk);
        } else {
            convertButton.setEnabled(true);
        }
    }

    @Override
    public void setRunningState(boolean running) {
        if (running) {
            taskStatusMap.clear();
            sourceTree.repaint();
            convertButton.setEnabled(false);
            stopButton.setEnabled(true);
            clearAllButton.setEnabled(false);
            sourceBrowseButton.setEnabled(false);
            outputFolderBrowseButton.setEnabled(false);
        } else {
            convertButton.setEnabled(true);
            stopButton.setEnabled(false);
            clearAllButton.setEnabled(true);
            sourceBrowseButton.setEnabled(true);
            outputFolderBrowseButton.setEnabled(true);

        }
    }


    @Override
    public void updateTaskStatus(Task task, boolean success) {
        taskStatusMap.put(task, success ? TaskStatus.SUCCESS : TaskStatus.FAILED);
        SwingUtilities.invokeLater(() -> {
            DefaultTreeModel model = (DefaultTreeModel) sourceTree.getModel();
            model.nodeChanged((TreeNode) model.getRoot());
            sourceTree.repaint();
        });
    }

    /**
     * Rebuilds the source {@link JTree} from the given task list.
     * <p>
     * Stale thumbnail cache entries are evicted, and asynchronous thumbnail
     * loading is triggered for any first-image files not yet cached.
     * </p>
     *
     * @param tasks the current list of tasks to display in the tree
     */
    public void updateSourceTree(List<Task> tasks) {
        DefaultTreeModel model = (DefaultTreeModel) sourceTree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        root.removeAllChildren();

        for (Task task : tasks) {
            DefaultMutableTreeNode node1 = new DefaultMutableTreeNode(task);
            for (File file : task.files) {
                DefaultMutableTreeNode node2 = new DefaultMutableTreeNode(file.getName());
                node1.add(node2);
            }
            root.add(node1);
        }
        model.reload();

        // 清除不再使用的快取（已移除的任務）
        Set<File> activeFiles = tasks.stream()
                .filter(t -> t.files != null && t.files.length > 0)
                .map(t -> t.files[0])
                .collect(Collectors.toSet());
        thumbnailCache.keySet().retainAll(activeFiles);

        // 為尚未快取的第一張圖啟動非同步縮圖載入
        for (Task task : tasks) {
            if (task.files != null && task.files.length > 0) {
                File firstFile = task.files[0];
                if (!thumbnailCache.containsKey(firstFile)) {
                    loadThumbnailAsync(firstFile);
                }
            }
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
        directionComboBox.setSelectedItem(PageDirection.Portrait);

        for (PageAlign.VerticalAlign align : PageAlign.VerticalAlign.values()) {
            verticalAlignComboBox.addItem(align);
        }
        verticalAlignComboBox.setSelectedItem(PageAlign.VerticalAlign.CENTER);

        for (PageAlign.HorizontalAlign align : PageAlign.HorizontalAlign.values()) {
            horizontalAlignComboBox.addItem(align);
        }
        horizontalAlignComboBox.setSelectedItem(PageAlign.HorizontalAlign.CENTER);

        for (PageSize size : PageSize.values()) {
            pageSizeComboBox.addItem(size);
        }
        pageSizeComboBox.setSelectedItem(PageSize.A4);

        for (ColorType color : ColorType.values()) {
            colorTypeComboBox.addItem(color);
        }
        colorTypeComboBox.setSelectedItem(ColorType.sRGB);

        autoRotateCheckBox.setSelected(false);
        state.setAutoRotate(false);

        encryptCheckBox.setSelected(false);

        updateSourceTree(new LinkedList<>());

        outputFolderField.setText(new File(".").getAbsolutePath());

        stopButton.setEnabled(false);
        refreshConvertButton();
    }

    /**
     * Opens a directory-chooser dialog for the output folder and updates the
     * output folder field and {@link UIState} if the user confirms a selection.
     */
    private void browseOutputFolder() {
        JFileChooser outputFolderChooser = createOutputFolderChooser();
        String currentText = outputFolderField.getText().trim();
        if (!currentText.isEmpty()) {
            File current = new File(currentText);
            if (current.exists() && current.isDirectory()) {
                outputFolderChooser.setCurrentDirectory(current);
            }
        }
        if (outputFolderChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            String selectedPath = outputFolderChooser.getSelectedFile().getAbsolutePath();
            outputFolderField.setText(selectedPath);
            state.setDestinationFolder(new File(selectedPath));
        }
    }

    /**
     * Opens a multi-selection directory-chooser dialog for source folders and
     * notifies the listener when the user confirms a selection.
     */
    private void browseSources() {
        JFileChooser sourceFilesChooser = createSourceFilesChooser();
        if (sourceFilesChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            if (listener != null) {
                state.setSourceFiles(sourceFilesChooser.getSelectedFiles());
                listener.onSourcesUpdate(this, state);
            }
        }
    }

    /**
     * Creates a {@link JFileChooser} configured for selecting multiple source directories.
     *
     * @return a chooser set to multi-selection, directories-only mode
     */
    private JFileChooser createSourceFilesChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        return chooser;
    }

    /**
     * Creates a {@link JFileChooser} configured for selecting a single output directory.
     *
     * @return a chooser set to single-selection, directories-only mode
     */
    private JFileChooser createOutputFolderChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        return chooser;
    }

    /**
     * Starts a background {@link SwingWorker} to load and scale a thumbnail for the
     * given image file, then stores the result in {@link #thumbnailCache} and
     * triggers a tree node repaint when done.
     *
     * @param imageFile the image file for which to generate a thumbnail
     */
    private void loadThumbnailAsync(File imageFile) {
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(imageFile);
                if (img == null) return null;
                double scale = Math.min(
                        (double) THUMBNAIL_SIZE / img.getWidth(),
                        (double) THUMBNAIL_SIZE / img.getHeight());
                int newW = Math.max(1, (int) (img.getWidth() * scale));
                int newH = Math.max(1, (int) (img.getHeight() * scale));
                java.awt.Image scaled = img.getScaledInstance(newW, newH, java.awt.Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        thumbnailCache.put(imageFile, icon);
                        // 找到對應節點，通知 model 使其重新計算該 row 高度
                        DefaultTreeModel treeModel = (DefaultTreeModel) sourceTree.getModel();
                        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
                        for (int i = 0; i < root.getChildCount(); i++) {
                            DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
                            Object userObj = child.getUserObject();
                            if (userObj instanceof Task) {
                                Task t = (Task) userObj;
                                if (t.files != null && t.files.length > 0 && imageFile.equals(t.files[0])) {
                                    treeModel.nodeChanged(child);
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

}
