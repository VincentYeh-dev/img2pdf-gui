package org.vincentyeh.img2pdf.gui.view;

import org.vincentyeh.img2pdf.lib.image.ColorType;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageAlign;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageDirection;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageSize;

import java.io.File;

public class UIState {
    private static UIState instance = null;

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


    private UIState() {

    }

    static void resetForTesting() {
        instance = new UIState();
    }

    public void setRunning(boolean running) {
        this.running = running;
    }


    public void setPageDirection(PageDirection pageDirection) {
        this.pageDirection = pageDirection;
    }

    public void setHorizontalAlign(PageAlign.HorizontalAlign horizontalAlign) {
        this.horizontalAlign = horizontalAlign;
    }

    public void setVerticalAlign(PageAlign.VerticalAlign verticalAlign) {
        this.verticalAlign = verticalAlign;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public void setOwnerPassword(String ownerPassword) {
        this.ownerPassword = ownerPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public void setSourceFiles(File[] sourceFiles) {
        this.sourceFiles = sourceFiles;
    }

    public void setDestinationFolder(File destinationFolder) {
        this.destinationFolder = destinationFolder;
    }

    public void setColorType(ColorType colorType) {
        this.colorType = colorType;
    }

    public void setPageSize(PageSize pageSize) {
        this.pageSize = pageSize;
    }

    public void setAutoRotate(boolean autoRotate) {
        this.autoRotate = autoRotate;
    }



    public boolean isRunning() {
        return running;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public String getOwnerPassword() {
        return ownerPassword;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public File[] getSourceFiles() {
        return sourceFiles;
    }

    public File getDestinationFolder() {
        return destinationFolder;
    }

    public ColorType getColorType() {
        return colorType;
    }

    public PageSize getPageSize() {
        return pageSize;
    }

    public boolean isAutoRotate() {
        return autoRotate;
    }



    public PageAlign.HorizontalAlign getHorizontalAlign() {
        return horizontalAlign;
    }

    public PageAlign.VerticalAlign getVerticalAlign() {
        return verticalAlign;
    }

    public PageDirection getPageDirection() {
        return pageDirection;
    }

}
