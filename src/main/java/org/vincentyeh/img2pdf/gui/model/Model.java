//package org.vincentyeh.img2pdf.gui.model;
//
//import org.vincentyeh.img2pdf.gui.model.util.file.FileNameFormatter;
//import org.vincentyeh.img2pdf.gui.model.util.file.FileSorter;
//import org.vincentyeh.img2pdf.gui.model.util.file.GlobbingFileFilter;
//import org.vincentyeh.img2pdf.gui.model.util.interfaces.NameFormatter;
//import org.vincentyeh.img2pdf.lib.Img2Pdf;
//import org.vincentyeh.img2pdf.lib.image.ColorType;
//import org.vincentyeh.img2pdf.lib.pdf.framework.factory.IDocument;
//import org.vincentyeh.img2pdf.lib.pdf.framework.factory.ImagePDFFactory;
//import org.vincentyeh.img2pdf.lib.pdf.framework.factory.ImagePDFFactoryListener;
//import org.vincentyeh.img2pdf.lib.pdf.framework.factory.exception.PDFFactoryException;
//import org.vincentyeh.img2pdf.lib.pdf.parameter.*;
//
//import java.io.File;
//import java.io.FileFilter;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.util.Arrays;
//import java.util.Comparator;
//import java.util.LinkedList;
//import java.util.List;
//
//public class Model {
//    private ModelListener listener;
//    private File output_folder = new File("").getAbsoluteFile();
//    private final List<Task> sources = new LinkedList<>();
//    private ColorType colorType;
//
//    private PageAlign.HorizontalAlign horizontalAlign;
//    private PageAlign.VerticalAlign verticalAlign;
//
//    private PageDirection pageDirection;
//    private boolean autoRotate;
//    private PageSize pageSize;
//    private final List<String> logList = new LinkedList<>();
//
//
//    public void setOutputFolder(File output_folder) {
//        this.output_folder = output_folder;
//    }
//
//    public void addSource(File[] directories, String output_format, String file_filter_pattern) {
//        NameFormatter<File> formatter = new FileNameFormatter(output_format);
//        FileFilter filter = new GlobbingFileFilter(file_filter_pattern);
//        Comparator<File> sorter = new FileSorter(FileSorter.Sortby.NUMERIC, FileSorter.Sequence.INCREASE);
//
//        if (directories == null)
//            return;
//        Arrays.stream(directories).forEach(
//                (directory) -> {
//                    try {
//                        File[] files = directory.listFiles(filter);
//                        if (files == null)
//                            return;
//                        files = Arrays.stream(files).map(File::getAbsoluteFile).toArray(File[]::new);
//                        Arrays.sort(files, sorter);
//                        sources.add(new Task(new File(formatter.format(directory)), files));
//                    } catch (NameFormatter.FormatException e) {
////                            JOptionPane.showMessageDialog(null, e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//                        e.printStackTrace();
//                    }
//                });
//        listener.onSourcesUpdate(sources);
//    }
//
//    public List<Task> getSources() {
//        return sources;
//    }
//
//    public void clearSource() {
//        sources.clear();
//        listener.onSourcesUpdate(sources);
//    }
//
//
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
//
//    private void addLog(String text) {
//        logList.add(text);
//        listener.onLogUpdate(logList);
//    }
//
//    private final ImagePDFFactoryListener factoryListener = new ImagePDFFactoryListener() {
//        private int total;
//
//        @Override
//        public void initializing(int total) {
//            this.total = total;
//            listener.onPageConversionProgressUpdate(0, this.total);
//
//        }
//
//        @Override
//        public void onConversionComplete() {
//
//        }
//
//        @Override
//        public void onAppend(File file, int appended, int total) {
//            listener.onPageConversionProgressUpdate(appended, this.total);
//        }
//    };
//
//
//    public void setColorType(ColorType colorType) {
//        this.colorType = colorType;
//    }
//
//    public ColorType getColorType() {
//        return colorType;
//    }
//
//    public void setPageDirection(PageDirection pageDirection) {
//        this.pageDirection = pageDirection;
//    }
//
//    public PageDirection getPageDirection() {
//        return pageDirection;
//    }
//
//    public void setAutoRotate(boolean autoRotate) {
//        this.autoRotate = autoRotate;
//    }
//
//    public boolean isAutoRotate() {
//        return autoRotate;
//
//    }
//
//    public void setPageSize(PageSize pageSize) {
//        this.pageSize = pageSize;
//    }
//
//
//    public void setHorizontalAlign(PageAlign.HorizontalAlign horizontalAlign) {
//        this.horizontalAlign = horizontalAlign;
//    }
//
//    public PageAlign.HorizontalAlign getHorizontalAlign() {
//        return horizontalAlign;
//    }
//
//    public void setVerticalAlign(PageAlign.VerticalAlign verticalAlign) {
//        this.verticalAlign = verticalAlign;
//    }
//
//    public PageAlign.VerticalAlign getVerticalAlign() {
//        return verticalAlign;
//    }
//
//    public void setModelListener(ModelListener listener) {
//        this.listener = listener;
//    }
//
//
//    public File getOutputFolder() {
//        return output_folder;
//    }
//
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
//}
