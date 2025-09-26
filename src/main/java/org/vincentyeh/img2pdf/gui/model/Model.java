package org.vincentyeh.img2pdf.gui.model;

import org.vincentyeh.img2pdf.gui.model.util.file.FileNameFormatter;
import org.vincentyeh.img2pdf.gui.model.util.file.FileSorter;
import org.vincentyeh.img2pdf.gui.model.util.file.GlobbingFileFilter;
import org.vincentyeh.img2pdf.gui.model.util.interfaces.NameFormatter;
import org.vincentyeh.img2pdf.lib.pdf.framework.factory.ImagePDFFactoryListener;
import org.vincentyeh.img2pdf.lib.pdf.parameter.*;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class Model {
    private ModelListener listener;

    public List<Task> parseSourceFiles(File[] directories, String outputFormat, String fileFilterPattern) {
        List<Task> sources = new LinkedList<>();
        NameFormatter<File> formatter = new FileNameFormatter(outputFormat);
        FileFilter filter = new GlobbingFileFilter(fileFilterPattern);
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


//    public void convert(String owner_password, String user_password) {
//        try {
//            File tempFolder = Files.createTempDirectory("org.vincentyeh.img2pdf.gui").toFile();
//            tempFolder.deleteOnExit();
//            listener.onTotalConversionProgressUpdate(0, sources.size());
//
//            if (!output_folder.exists()) {
//                boolean success = output_folder.mkdirs();
//                if (!success)
//                    throw new IllegalStateException("Unable to create directories");
//            }
//            if (output_folder.isFile())
//                throw new IllegalArgumentException("Uestination should be folder");
//
//
//            Thread conversion_thread = new Thread(() -> {
//
//                ImagePDFFactory factory = Img2Pdf.createPDFBoxMaxPerformanceFactory();
//
//                DocumentArgument documentArgument = createDocumentArgument(owner_password, user_password);
//                PageArgument pageArgument = createPageArgument();
//
//                for (int i = 0; i < sources.size(); i++) {
//                    try {
//                        IDocument document = factory.start(
//                                sources.get(i).files,
//                                colorType,
//                                documentArgument,
//                                pageArgument,
//                                factoryListener);
//                        document.save(new File(output_folder, sources.get(i).destination.getName()));
//                        document.close();
//
//                        addLog(String.format("[OK] %s", sources.get(i).destination.getName()));
//                    } catch (PDFFactoryException e) {
//                        addLog(String.format("[ERROR] %s -> %s", sources.get(i).destination.getName(), e.getCause().getMessage()));
//                    } catch (IOException e) {
//                        addLog(String.format("[ERROR] %s -> %s", sources.get(i).destination.getName(), e.getCause().getMessage()));
//                    } finally {
//                        listener.onTotalConversionProgressUpdate(i + 1, sources.size());
//                    }
//                }
//                factory.shutdown();
//            });
//            conversion_thread.start();
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }


    private final ImagePDFFactoryListener factoryListener = new ImagePDFFactoryListener() {
        private int total;

        @Override
        public void initializing(int total) {
            this.total = total;
            listener.onPageConversionProgressUpdate(0, this.total);

        }

        @Override
        public void onConversionComplete() {

        }

        @Override
        public void onAppend(File file, int appended, int total) {
            listener.onPageConversionProgressUpdate(appended, this.total);
        }
    };



    public void setModelListener(ModelListener listener) {
        this.listener = listener;
    }


//    private PageArgument createPageArgument() {
//        return new PageArgument(new PageAlign(verticalAlign, horizontalAlign), pageSize, pageDirection, autoRotate);
//    }
//
//    private DocumentArgument createDocumentArgument(String owner_password, String user_password) {
//        DocumentArgument documentArgument = new DocumentArgument();
//        if (owner_password != null && user_password != null) {
//            documentArgument.setEncryption(owner_password, user_password, new Permission());
//        }
//        return documentArgument;
//    }
//
//    public PageSize getPageSize() {
//        return pageSize;
//    }
}
