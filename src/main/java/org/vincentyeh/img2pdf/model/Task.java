package org.vincentyeh.img2pdf.model;

import java.io.File;

public class Task {
    public final File destination;
    public final File[] files;

    public Task(File destination, File[] files) {
        this.destination = destination;
        this.files = files;
    }
}
