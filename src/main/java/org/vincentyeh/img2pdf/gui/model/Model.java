package org.vincentyeh.img2pdf.gui.model;

import org.vincentyeh.img2pdf.gui.AppLogger;
import org.vincentyeh.img2pdf.gui.model.util.file.FileNameFormatter;
import org.vincentyeh.img2pdf.gui.model.util.file.FileSorter;
import org.vincentyeh.img2pdf.gui.model.util.file.GlobbingFileFilter;
import org.vincentyeh.img2pdf.gui.model.util.interfaces.NameFormatter;
import org.vincentyeh.img2pdf.lib.Img2Pdf;
import org.vincentyeh.img2pdf.lib.image.ColorType;
import org.vincentyeh.img2pdf.lib.pdf.framework.factory.IDocument;
import org.vincentyeh.img2pdf.lib.pdf.framework.factory.ImagePDFFactory;
import org.vincentyeh.img2pdf.lib.pdf.framework.factory.ImagePDFFactoryListener;
import org.vincentyeh.img2pdf.lib.pdf.framework.factory.exception.PDFFactoryException;
import org.vincentyeh.img2pdf.lib.pdf.parameter.*;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

/**
 * Core business-logic layer of the MVC architecture.
 * <p>
 * Responsible for:
 * <ul>
 *   <li>Scanning source directories and building a list of {@link Task} objects
 *       via {@link #parseSourceFiles(File[])}.</li>
 *   <li>Maintaining the in-memory task list and its sort order.</li>
 *   <li>Executing batch PDF conversion in a background thread via
 *       {@link #convert(ConversionConfig)}.</li>
 * </ul>
 * Progress and lifecycle events are forwarded to the registered {@link ModelListener}.
 * </p>
 */
public class Model {
    private List<Task> sources = new LinkedList<>();
    private ModelListener listener = null;
    private TaskSortOrder sortOrder = TaskSortOrder.NAME_ASC;
    private volatile boolean stopRequested = false;

    /**
     * Signals the running conversion to stop after the current task completes.
     * Subsequent tasks in the batch will be skipped.
     */
    public void requestStop() {
        stopRequested = true;
    }

    /**
     * Scans each supplied directory for supported image files and creates one
     * {@link Task} per directory.
     * <p>
     * Images are filtered by extension (JPG, JPEG, PNG, BMP, WEBP, case-insensitive),
     * sorted in numeric ascending order, and the output PDF name is derived from the
     * directory name via {@link FileNameFormatter}.
     * </p>
     *
     * @param directories the source directories to scan; must not be {@code null}
     * @return an ordered list of {@link Task} objects, one per directory
     * @throws IllegalArgumentException if {@code directories} is {@code null}
     */
    public static List<Task> parseSourceFiles(File[] directories) {
        List<Task> sources = new LinkedList<>();
        NameFormatter<File> formatter = new FileNameFormatter("<NAME>.pdf");
        FileFilter filter = new GlobbingFileFilter("*.{JPG,jpg,JPEG,jpeg,PNG,png,BMP,bmp,webp,WEBP}");
        Comparator<File> sorter = new FileSorter(FileSorter.Sortby.NUMERIC, FileSorter.Sequence.INCREASE);

        if (directories == null)
            throw new IllegalArgumentException("directories==null");

        Arrays.stream(directories).forEach(
                (directory) -> {
                    try {
                        File[] files = directory.listFiles(filter);
                        if (files == null)
                            return;
                        files = Arrays.stream(files).map(File::getAbsoluteFile).toArray(File[]::new);
                        Arrays.sort(files, sorter);
                        sources.add(new Task(new File(formatter.format(directory)), files));
                    } catch (NameFormatter.FormatException e) {
                        AppLogger.get().log(Level.WARNING,
                                "Skipping directory due to name formatting error: "
                                        + directory.getAbsolutePath(), e);
                    }
                });
        return sources;
    }

    /**
     * Parses the given source directories into tasks, replaces the current task
     * list, re-sorts by the active sort order, and notifies the listener once.
     *
     * @param directories the source directories to scan; must not be {@code null}
     */
    public void importSources(File[] directories) {
        List<Task> tasks = parseSourceFiles(directories);
        this.sources = new ArrayList<>(tasks);
        this.sources.sort(sortOrder.getComparator());
        notifyTasksUpdate();
    }

    /**
     * Changes the active sort order, immediately re-sorts the task list, and
     * notifies the listener once.
     *
     * @param order the new sort order to apply
     */
    public void setSortOrder(TaskSortOrder order) {
        this.sortOrder = order;
        sources.sort(order.getComparator());
        notifyTasksUpdate();
    }

    /**
     * Removes all specified tasks from the in-memory list, then notifies the
     * listener once. Files on disk are not affected.
     *
     * @param tasks the tasks to remove
     */
    public void removeTasks(List<Task> tasks) {
        for (Task task : tasks) {
            removeTask(task);
        }
        notifyTasksUpdate();
    }

    /**
     * Deletes the source folder of each task from disk and removes it from the
     * in-memory list. A single {@link #onTasksUpdate} notification is sent at the
     * end. Per-task failures are reported via
     * {@link ModelListener#onTaskDiskRemovalError} before the final notification.
     *
     * @param tasks the tasks whose source directories should be deleted
     */
    public void removeTasksFromDisk(List<Task> tasks) {
        for (Task task : tasks) {
            try {
                removeTaskFromDisk(task);
            } catch (IOException e) {
                if (listener != null) listener.onTaskDiskRemovalError(task, e);
            }
        }
        notifyTasksUpdate();
    }

    /**
     * Removes the specified task from the in-memory task list.
     * The corresponding files on disk are not affected.
     */
    private void removeTask(Task task) {
        this.sources.remove(task);
    }

    /**
     * Deletes the source folder (and all its contents) associated with the task
     * from disk, then removes the task from the in-memory list.
     * <p>
     * If an {@link IOException} occurs the task is <em>not</em> removed from the
     * in-memory list and the exception is propagated to the caller.
     * </p>
     */
    private void removeTaskFromDisk(Task task) throws IOException {
        if (task.files != null && task.files.length > 0) {
            File folder = task.files[0].getParentFile();
            try {
                Files.walk(folder.toPath())
                        .sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                throw new java.io.UncheckedIOException(e);
                            }
                        });
            } catch (java.io.UncheckedIOException e) {
                throw e.getCause();
            }
        }
        this.sources.remove(task);
    }


    /**
     * Starts batch PDF conversion on a dedicated background thread.
     * <p>
     * For each task in the current list, a PDF is created in the destination folder
     * defined by {@code config}. The registered {@link ModelListener} receives progress
     * and completion callbacks throughout the process. Call {@link #requestStop()} to
     * cancel remaining tasks after the current one finishes.
     * </p>
     * <p>
     * Pre-conversion validation failures (invalid output folder) are reported via
     * {@link ModelListener#onBatchError(String, String)} instead of throwing, so the
     * caller does not need to handle exceptions from this method.
     * </p>
     *
     * @param config the conversion parameters (output folder, page layout, encryption, etc.)
     */
    public void convert(ConversionConfig config) {
        File outputFolder = config.destinationFolder;

        if (outputFolder.isFile()) {
            if (listener != null)
                listener.onBatchError("Invalid Output Folder",
                        "The destination path is an existing file, not a folder:\n"
                                + outputFolder.getAbsolutePath());
            return;
        }
        if (!outputFolder.exists()) {
            boolean success = outputFolder.mkdirs();
            if (!success) {
                if (listener != null)
                    listener.onBatchError("Cannot Create Output Folder",
                            "Unable to create output directory:\n"
                                    + outputFolder.getAbsolutePath());
                return;
            }
        }

        final File tempFolder;
        try {
            tempFolder = Files.createTempDirectory("org.vincentyeh.img2pdf.gui").toFile();
            tempFolder.deleteOnExit();
        } catch (IOException e) {
            AppLogger.get().log(Level.SEVERE, "Cannot create temp directory", e);
            if (listener != null)
                listener.onBatchError("Internal Error",
                        "Cannot create temporary directory: " + e.getMessage());
            return;
        }

        if (listener != null) listener.onBatchProgressUpdate(0, sources.size());

        boolean encryption = config.encrypted;
        String ownerPassword = config.ownerPassword;
        String userPassword = config.userPassword;
        ColorType colorType = config.colorType;

        // Take a snapshot of the task list on the EDT before starting the background thread.
        // The background thread reads only this snapshot, avoiding concurrent modification
        // of sources by setTask(), removeTask(), or setSortOrder() on the EDT.
        final List<Task> snapshot = new ArrayList<>(sources);

        Thread conversionThread = new Thread(() -> {
            stopRequested = false;
            if (listener != null) listener.onBatchStart();
            ImagePDFFactory factory = Img2Pdf.createPDFBoxMaxPerformanceFactory();

            DocumentArgument documentArgument = createDocumentArgument(encryption, ownerPassword, userPassword);
            PageArgument pageArgument = createPageArgument(
                    config.verticalAlign,
                    config.horizontalAlign,
                    config.pageSize,
                    config.pageDirection,
                    config.autoRotate
            );

            try {
                for (int i = 0; i < snapshot.size(); i++) {
                    Task currentTask = snapshot.get(i);
                    if (stopRequested) break;
                    try {
                        IDocument document = factory.start(
                                currentTask.files,
                                colorType,
                                documentArgument,
                                pageArgument,
                                factoryListener);
                        // BUG-01 fix: use try-finally to guarantee document.close() is always called.
                        // BUG-05 fix: manage FileOutputStream ourselves with try-with-resources so the
                        //             file handle is always closed even when save() throws, preventing
                        //             the output PDF from being locked on Windows.
                        try {
                            File destination = new File(outputFolder, currentTask.destination.getName());
                            try (OutputStream out = new FileOutputStream(destination)) {
                                document.save(out);
                            }
                        } finally {
                            document.close();
                        }
                        if (listener != null) listener.onTaskComplete(currentTask, null);
                    } catch (PDFFactoryException | IOException e) {
                        if (listener != null) listener.onTaskComplete(currentTask, e);
                    } finally {
                        if (listener != null) listener.onBatchProgressUpdate(i + 1, snapshot.size());
                    }
                }
            } finally {
                factory.shutdown();
                if (listener != null) listener.onBatchComplete();
            }
        });
        conversionThread.start();
    }


    private final ImagePDFFactoryListener factoryListener = new ImagePDFFactoryListener() {
        private int total;

        @Override
        public void initializing(int total) {
            this.total = total;
            if (listener != null) listener.onConversionProgressUpdate(0, this.total);
        }

        @Override
        public void onConversionComplete() {

        }

        @Override
        public void onAppend(File file, int appended, int total) {
            if (listener != null) listener.onConversionProgressUpdate(appended, this.total);
        }
    };


    /**
     * Pushes an unmodifiable snapshot of the current task list to the listener.
     * No-op when no listener is registered.
     */
    private void notifyTasksUpdate() {
        if (listener != null) listener.onTasksUpdate(Collections.unmodifiableList(sources));
    }

    /**
     * Registers the listener that will receive progress and lifecycle events from
     * this model during conversion.
     *
     * @param listener the listener to notify; may be {@code null} to clear the listener
     */
    public void setModelListener(ModelListener listener) {
        this.listener = listener;
    }


    /**
     * Builds a {@code PageArgument} from individual layout parameters.
     *
     * @param verticalAlign   the vertical image alignment within the page
     * @param horizontalAlign the horizontal image alignment within the page
     * @param pageSize        the target page size
     * @param pageDirection   the page orientation
     * @param autoRotate      whether to auto-rotate images to best fit the page
     * @return the constructed {@code PageArgument}
     */
    private PageArgument createPageArgument(PageAlign.VerticalAlign verticalAlign,
                                            PageAlign.HorizontalAlign horizontalAlign,
                                            PageSize pageSize,
                                            PageDirection pageDirection,
                                            boolean autoRotate) {
        return new PageArgument(new PageAlign(verticalAlign, horizontalAlign), pageSize, pageDirection, autoRotate);
    }

    /**
     * Builds a {@code DocumentArgument}, optionally including encryption settings.
     *
     * @param encryption     whether the PDF should be encrypted
     * @param ownerPassword the owner (permissions) password; used only when {@code encryption} is {@code true}
     * @param userPassword  the user (open) password; used only when {@code encryption} is {@code true}
     * @return the constructed {@code DocumentArgument}
     */
    private DocumentArgument createDocumentArgument(boolean encryption, String ownerPassword, String userPassword) {
        DocumentArgument documentArgument = new DocumentArgument();
        if (encryption) {
            documentArgument.setEncryption(ownerPassword, userPassword, new Permission());
        }
        return documentArgument;
    }

}
