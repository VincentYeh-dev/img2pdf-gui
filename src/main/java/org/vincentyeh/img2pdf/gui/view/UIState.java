package org.vincentyeh.img2pdf.gui.view;

import org.vincentyeh.img2pdf.lib.image.ColorType;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageAlign;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageDirection;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageSize;

import java.io.File;

/**
 * Singleton that holds the current state of all UI controls.
 * <p>
 * All Swing components write their values here via the mediator; the Controller
 * reads from this object when creating a {@link org.vincentyeh.img2pdf.gui.model.ConversionConfig}.
 * </p>
 */
public class UIState {
    private static UIState instance = null;

    /**
     * Returns the singleton instance, creating it on first call.
     *
     * @return the global {@code UIState} instance
     */
    public static UIState getInstance() {
        if (instance == null) {
            instance = new UIState();
        }
        return instance;
    }

    private boolean running = false;
    private boolean encrypted = false;
    private String ownerPassword = null;
    private String userPassword = null;

    private File[] sourceFiles = null;
    private File destinationFolder = null;
    private ColorType colorType = null;
    private PageSize pageSize = null;
    boolean autoRotate = false;

    private PageAlign.HorizontalAlign horizontalAlign;
    private PageAlign.VerticalAlign verticalAlign;
    private PageDirection pageDirection;


    /** Prevents external instantiation; use {@link #getInstance()}. */
    private UIState() {

    }

    /**
     * Resets the singleton to a fresh instance.
     * <p>
     * Package-private: only accessible from within the {@code view} package,
     * intended for use in unit tests via {@code UIState.resetForTesting()}.
     * </p>
     */
    static void resetForTesting() {
        instance = new UIState();
    }


    /**
     * Sets the page orientation.
     *
     * @param pageDirection the new page direction
     */
    public void setPageDirection(PageDirection pageDirection) {
        this.pageDirection = pageDirection;
    }

    /**
     * Sets the horizontal image alignment within the page.
     *
     * @param horizontalAlign the new horizontal alignment
     */
    public void setHorizontalAlign(PageAlign.HorizontalAlign horizontalAlign) {
        this.horizontalAlign = horizontalAlign;
    }

    /**
     * Sets the vertical image alignment within the page.
     *
     * @param verticalAlign the new vertical alignment
     */
    public void setVerticalAlign(PageAlign.VerticalAlign verticalAlign) {
        this.verticalAlign = verticalAlign;
    }

    /**
     * Sets whether PDF encryption is enabled.
     *
     * @param encrypted {@code true} to enable encryption
     */
    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    /**
     * Sets the owner (permissions) password used when encryption is enabled.
     *
     * @param ownerPassword the owner password string
     */
    public void setOwnerPassword(String ownerPassword) {
        this.ownerPassword = ownerPassword;
    }

    /**
     * Sets the user (open) password used when encryption is enabled.
     *
     * @param userPassword the user password string
     */
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    /**
     * Sets the source directories selected by the user.
     *
     * @param sourceFiles the array of selected source directories
     */
    public void setSourceFiles(File[] sourceFiles) {
        this.sourceFiles = sourceFiles;
    }

    /**
     * Sets the output folder where generated PDFs will be saved.
     *
     * @param destinationFolder the output directory
     */
    public void setDestinationFolder(File destinationFolder) {
        this.destinationFolder = destinationFolder;
    }

    /**
     * Sets the colour type used for image rendering.
     *
     * @param colorType the selected colour type
     */
    public void setColorType(ColorType colorType) {
        this.colorType = colorType;
    }

    /**
     * Sets the target PDF page size.
     *
     * @param pageSize the selected page size
     */
    public void setPageSize(PageSize pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Sets whether images should be auto-rotated to best fit the page.
     *
     * @param autoRotate {@code true} to enable auto-rotation
     */
    public void setAutoRotate(boolean autoRotate) {
        this.autoRotate = autoRotate;
    }

    /**
     * Returns whether PDF encryption is currently enabled.
     *
     * @return {@code true} if encryption is enabled
     */
    public boolean isEncrypted() {
        return encrypted;
    }

    /**
     * Returns the owner (permissions) password.
     *
     * @return the owner password, or {@code null} if not set
     */
    public String getOwnerPassword() {
        return ownerPassword;
    }

    /**
     * Returns the user (open) password.
     *
     * @return the user password, or {@code null} if not set
     */
    public String getUserPassword() {
        return userPassword;
    }

    /**
     * Returns the currently selected source directories.
     *
     * @return the source file array, or {@code null} if none selected
     */
    public File[] getSourceFiles() {
        return sourceFiles;
    }

    /**
     * Returns the output folder for generated PDFs.
     *
     * @return the destination folder, or {@code null} if not set
     */
    public File getDestinationFolder() {
        return destinationFolder;
    }

    /**
     * Returns the selected colour type for image rendering.
     *
     * @return the colour type, or {@code null} if not set
     */
    public ColorType getColorType() {
        return colorType;
    }

    /**
     * Returns the target PDF page size.
     *
     * @return the page size, or {@code null} if not set
     */
    public PageSize getPageSize() {
        return pageSize;
    }

    /**
     * Returns whether auto-rotation of images is enabled.
     *
     * @return {@code true} if auto-rotate is enabled
     */
    public boolean isAutoRotate() {
        return autoRotate;
    }

    /**
     * Returns the horizontal image alignment within the page.
     *
     * @return the horizontal alignment
     */
    public PageAlign.HorizontalAlign getHorizontalAlign() {
        return horizontalAlign;
    }

    /**
     * Returns the vertical image alignment within the page.
     *
     * @return the vertical alignment
     */
    public PageAlign.VerticalAlign getVerticalAlign() {
        return verticalAlign;
    }

    /**
     * Returns the page orientation.
     *
     * @return the page direction
     */
    public PageDirection getPageDirection() {
        return pageDirection;
    }

}
