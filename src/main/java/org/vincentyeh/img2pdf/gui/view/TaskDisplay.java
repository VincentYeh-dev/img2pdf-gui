package org.vincentyeh.img2pdf.gui.view;

import java.io.File;

/**
 * Display-only DTO carrying the minimum task information that the View layer
 * needs to render a task node in the source tree.
 * <p>
 * Created by the Controller from a {@link org.vincentyeh.img2pdf.gui.model.Task}
 * so that the View never needs to import model types.
 * </p>
 */
public class TaskDisplay {

    /** The output PDF file name derived from {@code task.destination.getName()}. */
    public final String destinationName;

    /**
     * The ordered source image files for this task.
     * Uses {@code java.io.File} (standard library), not a model type.
     */
    public final File[] sourceFiles;

    public TaskDisplay(String destinationName, File[] sourceFiles) {
        this.destinationName = destinationName;
        this.sourceFiles = sourceFiles;
    }
}
