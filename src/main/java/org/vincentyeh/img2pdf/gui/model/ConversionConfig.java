package org.vincentyeh.img2pdf.gui.model;

import org.vincentyeh.img2pdf.lib.image.ColorType;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageAlign;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageDirection;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageSize;

import java.io.File;

/**
 * Immutable data-transfer object (DTO) that carries all PDF conversion parameters
 * from the UI layer to {@link Model#convert(ConversionConfig)}.
 */
public class ConversionConfig {

    /** The directory where generated PDF files will be written. */
    public final File destinationFolder;

    /** Whether PDF encryption (password protection) is enabled. */
    public final boolean encrypted;

    /** The owner (permissions) password; only used when {@link #encrypted} is {@code true}. */
    public final String ownerPassword;

    /** The user (open) password; only used when {@link #encrypted} is {@code true}. */
    public final String userPassword;

    /** The colour space used when rendering images into the PDF. */
    public final ColorType colorType;

    /** The page size applied to each PDF page. */
    public final PageSize pageSize;

    /** The page orientation (portrait or landscape). */
    public final PageDirection pageDirection;

    /** The vertical alignment of the image within the PDF page. */
    public final PageAlign.VerticalAlign verticalAlign;

    /** The horizontal alignment of the image within the PDF page. */
    public final PageAlign.HorizontalAlign horizontalAlign;

    /** Whether images should be automatically rotated to best fit the page. */
    public final boolean autoRotate;

    /**
     * Creates an immutable conversion configuration.
     *
     * @param destinationFolder the output directory for generated PDFs
     * @param encrypted         {@code true} to enable PDF password protection
     * @param ownerPassword     the owner (permissions) password; ignored when not encrypted
     * @param userPassword      the user (open) password; ignored when not encrypted
     * @param colorType         the colour space for image rendering
     * @param pageSize          the target PDF page size
     * @param pageDirection     the page orientation
     * @param verticalAlign     the vertical image alignment on the page
     * @param horizontalAlign   the horizontal image alignment on the page
     * @param autoRotate        {@code true} to auto-rotate images to fit the page
     */
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
