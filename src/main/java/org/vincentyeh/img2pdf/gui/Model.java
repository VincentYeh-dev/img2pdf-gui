package org.vincentyeh.img2pdf.gui;

import org.vincentyeh.img2pdf.gui.ui.components.Task;
import org.vincentyeh.img2pdf.gui.util.file.FileNameFormatter;
import org.vincentyeh.img2pdf.gui.util.file.FileSorter;
import org.vincentyeh.img2pdf.gui.util.file.GlobbingFileFilter;
import org.vincentyeh.img2pdf.gui.util.interfaces.NameFormatter;
import org.vincentyeh.img2pdf.lib.Img2Pdf;
import org.vincentyeh.img2pdf.lib.image.ColorType;
import org.vincentyeh.img2pdf.lib.pdf.framework.factory.ImageFactoryListener;
import org.vincentyeh.img2pdf.lib.pdf.framework.factory.ImagePDFFactory;
import org.vincentyeh.img2pdf.lib.pdf.framework.factory.exception.PDFFactoryException;
import org.vincentyeh.img2pdf.lib.pdf.parameter.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class Model {
    private ModelListener listener;
    private String file_filter_pattern;
    private String output_format;
    private File output_folder=new File("").getAbsoluteFile();
    private final List<Task> sources = new LinkedList<>();

    private ColorType colorType;

    private PageArgument pageArgument;
    private DocumentArgument documentArgument;

    private int conversionProgress,conversionProgressMax;
    private int pageConversionProgress,pageConversionProgressMax;


    public String getFileFilter() {
        return file_filter_pattern;
    }

    public void setFileFilter(String file_filter_pattern) {
        this.file_filter_pattern = file_filter_pattern;
        listener.onUIUpdate(this);
    }

    public String getOutputFormat() {
        return output_format;
    }

    public void setOutputFormat(String output_format) {
        this.output_format = output_format;
        listener.onUIUpdate(this);
    }

    public File getOutputFolder() {
        return output_folder;
    }

    public void setOutputFolder(File output_folder) {
        this.output_folder = output_folder;
        listener.onUIUpdate(this);
    }

//    public void addSource(Task task){
//        sources.add(task);
//    }

    public void addSource(File[] directories) {
        NameFormatter<File> formatter = new FileNameFormatter(output_format);
        FileFilter filter = new GlobbingFileFilter(file_filter_pattern);
        Comparator<File> sorter = new FileSorter(FileSorter.Sortby.NUMERIC, FileSorter.Sequence.INCREASE);

        if (directories == null)
            return;
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
//                            e.printStackTrace();
                    }
                });
        listener.onUIUpdate(Model.this);
    }

    public List<Task> getSources() {
        return sources;
    }

    public void clearSource() {
        sources.clear();
        listener.onUIUpdate(Model.this);
    }


    public ColorType getColorType() {
        return colorType;
    }

    public void setColorType(ColorType colorType) {
        this.colorType = colorType;
        listener.onUIUpdate(this);
    }

    public void convert() {
        try {
            File tempFolder = Files.createTempDirectory("org.vincentyeh.img2pdf.gui").toFile();
            tempFolder.deleteOnExit();

            ImagePDFFactory factory = Img2Pdf.createFactory(pageArgument,documentArgument, colorType, true);

            conversionProgressMax=sources.size();
            conversionProgress=0;
            listener.onUIUpdate(Model.this);

            if (!output_folder.exists()) {
                boolean success = output_folder.mkdirs();
                if (!success)
                    throw new IllegalStateException("Unable to create directories");
            }
            if (output_folder.isFile())
                throw new IllegalArgumentException("Uestination should be folder");


            Thread conversion_thread = new Thread(() -> {
                for (int i = 0; i < sources.size(); i++) {
//                    if (interrupt_flag)
//                        break;
                    try {
                        factory.start(i,
                                sources.get(i).files,
                                new File(output_folder, sources.get(i).destination.getName()),
                                factoryListener);
//                        recordNewLog(i, String.format("[OK] %s", sources.get(i).destination.getName()));
                    } catch (PDFFactoryException e) {
//                        recordNewLog(i, String.format("[ERROR] %s -> %s", sources.get(i).destination.getName(), e.getCause().getMessage()));
                    } finally {
                        conversionProgress=i+1;
                        listener.onUIUpdate(Model.this);
                    }
                }

            });
            conversion_thread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final ImageFactoryListener factoryListener = new ImageFactoryListener() {
        @Override
        public void initializing(int procedure_id, int total) {
            pageConversionProgressMax=total;
            pageConversionProgress=0;
            listener.onUIUpdate(Model.this);
        }

        @Override
        public void onSaved(int procedure_id, File file) {

        }

        @Override
        public void onConversionComplete(int procedure_id) {

        }

        @Override
        public void onAppend(int procedure_id, File file, int i, int length){
            pageConversionProgress=i+1;
            listener.onUIUpdate(Model.this);
        }
    };

    public void setModelListener(ModelListener listener) {
        this.listener = listener;
    }

    public void setPageArgument(PageArgument pageArgument) {
        this.pageArgument = pageArgument;
    }

    public void setDocumentArgument(DocumentArgument documentArgument) {
        this.documentArgument = documentArgument;
    }

    public int getConversionProgress() {
        return conversionProgress;
    }

    public int getConversionProgressMax() {
        return conversionProgressMax;
    }

    public int getPageConversionProgress() {
        return pageConversionProgress;
    }

    public int getPageConversionProgressMax() {
        return pageConversionProgressMax;
    }
}
