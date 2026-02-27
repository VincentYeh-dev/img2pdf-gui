package org.vincentyeh.img2pdf.gui.model;

import org.vincentyeh.img2pdf.lib.image.ColorType;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageAlign;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageDirection;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageSize;

import java.io.File;

public class ConversionConfig {
    public final File destinationFolder;
    public final boolean encrypted;
    public final String ownerPassword;
    public final String userPassword;
    public final ColorType colorType;
    public final PageSize pageSize;
    public final PageDirection pageDirection;
    public final PageAlign.VerticalAlign verticalAlign;
    public final PageAlign.HorizontalAlign horizontalAlign;
    public final boolean autoRotate;

    public ConversionConfig(File destinationFolder,
                            boolean encrypted,
                            String ownerPassword,
                            String userPassword,
                            ColorType colorType,
                            PageSize pageSize,
                            PageDirection pageDirection,
                            PageAlign.VerticalAlign verticalAlign,
                            PageAlign.HorizontalAlign horizontalAlign,
                            boolean autoRotate) {
        this.destinationFolder = destinationFolder;
        this.encrypted = encrypted;
        this.ownerPassword = ownerPassword;
        this.userPassword = userPassword;
        this.colorType = colorType;
        this.pageSize = pageSize;
        this.pageDirection = pageDirection;
        this.verticalAlign = verticalAlign;
        this.horizontalAlign = horizontalAlign;
        this.autoRotate = autoRotate;
    }
}
