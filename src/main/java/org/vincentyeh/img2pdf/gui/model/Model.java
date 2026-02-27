package org.vincentyeh.img2pdf.gui.model;

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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class Model {
    private List<Task> sources = new LinkedList<>();
    private ModelListener listener = null;
    private TaskSortOrder sortOrder = TaskSortOrder.NAME_ASC;
    private volatile boolean stopRequested = false;

    public void requestStop() {
        stopRequested = true;
    }

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
//                            JOptionPane.showMessageDialog(null, e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                });
        return sources;
    }

    public void setTask(List<Task> tasks) {
        this.sources = new ArrayList<>(tasks);
        this.sources.sort(sortOrder.getComparator());
    }

    public List<Task> getTasks() {
        return sources;
    }

    public void setSortOrder(TaskSortOrder order) {
        this.sortOrder = order;
        sources.sort(order.getComparator());
    }

    public void removeTask(Task task) {
        this.sources.remove(task);
    }

    public void removeTaskFromDisk(Task task) {
        if (task.files != null && task.files.length > 0) {
            File folder = task.files[0].getParentFile();
            try {
                Files.walk(folder.toPath())
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.sources.remove(task);
    }


    public void convert(ConversionConfig config) {
        try {
            File tempFolder = Files.createTempDirectory("org.vincentyeh.img2pdf.gui").toFile();
            tempFolder.deleteOnExit();
            listener.onBatchProgressUpdate(0, sources.size());
            File output_folder = config.destinationFolder;
            boolean encryption = config.encrypted;
            String owner_password = config.ownerPassword;
            String user_password = config.userPassword;
            ColorType colorType = config.colorType;

            if (!output_folder.exists()) {
                boolean success = output_folder.mkdirs();
                if (!success)
                    throw new IllegalStateException("Unable to create directories");
            }
            if (output_folder.isFile())
                throw new IllegalArgumentException("Uestination should be folder");


            Thread conversion_thread = new Thread(() -> {
                stopRequested = false;
                listener.onBatchStart();
                ImagePDFFactory factory = Img2Pdf.createPDFBoxMaxPerformanceFactory();

                DocumentArgument documentArgument = createDocumentArgument(encryption, owner_password, user_password);
                PageArgument pageArgument = createPageArgument(
                        config.verticalAlign,
                        config.horizontalAlign,
                        config.pageSize,
                        config.pageDirection,
                        config.autoRotate
                );


                for (int i = 0; i < sources.size(); i++) {
                    Task currentTask = sources.get(i);
                    if (stopRequested) break;
                    try {
                        IDocument document = factory.start(
                                currentTask.files,
                                colorType,
                                documentArgument,
                                pageArgument,
                                factoryListener);
                        document.save(new File(output_folder, currentTask.destination.getName()));
                        document.close();
                        listener.onTaskComplete(currentTask, true);
                    } catch (PDFFactoryException | IOException e) {
                        listener.onTaskComplete(currentTask, false);
                    } finally {
                        listener.onBatchProgressUpdate(i + 1, sources.size());
                    }
                }
                factory.shutdown();
                listener.onBatchComplete();
            });
            conversion_thread.start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private final ImagePDFFactoryListener factoryListener = new ImagePDFFactoryListener() {
        private int total;

        @Override
        public void initializing(int total) {
            this.total = total;
            listener.onConversionProgressUpdate(0, this.total);
        }

        @Override
        public void onConversionComplete() {

        }

        @Override
        public void onAppend(File file, int appended, int total) {
            listener.onConversionProgressUpdate(appended, this.total);
        }
    };


    public void setModelListener(ModelListener listener) {
        this.listener = listener;
    }


    private PageArgument createPageArgument(PageAlign.VerticalAlign verticalAlign,
                                            PageAlign.HorizontalAlign horizontalAlign,
                                            PageSize pageSize,
                                            PageDirection pageDirection,
                                            boolean autoRotate) {
        return new PageArgument(new PageAlign(verticalAlign, horizontalAlign), pageSize, pageDirection, autoRotate);
    }

    private DocumentArgument createDocumentArgument(boolean encryption, String owner_password, String user_password) {
        DocumentArgument documentArgument = new DocumentArgument();
        if (encryption) {
            documentArgument.setEncryption(owner_password, user_password, new Permission());
        }
        return documentArgument;
    }

}
