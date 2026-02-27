package org.vincentyeh.img2pdf.gui.model;

import java.io.File;

/**
 * Immutable data object representing a single PDF conversion task.
 * <p>
 * A task pairs a set of source image files with the output PDF path
 * that will be produced when the task is executed by {@link Model}.
 * </p>
 */
public class Task {

    /** The output PDF file that will be created for this task. */
    public final File destination;

    /** The ordered array of source image files to be merged into the PDF. */
    public final File[] files;

    /**
     * Creates a new conversion task.
     *
     * @param destination the output PDF file path
     * @param files       the ordered source image files to convert
     */
    public Task(File destination, File[] files) {
        this.destination = destination;
        this.files = files;
    }
}
