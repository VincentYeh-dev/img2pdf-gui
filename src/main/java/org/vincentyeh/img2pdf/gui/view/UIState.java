package org.vincentyeh.img2pdf.gui.view;

import org.vincentyeh.img2pdf.lib.pdf.parameter.PageAlign;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageSize;

import javax.swing.plaf.synth.ColorType;
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
    private File destination = null;
    private ColorType colorType = null;
    private PageSize pageSize = null;
    boolean autoRotate = false;
    PageAlign pageAlign = null;
    private String fileFilterPattern = null;


    private UIState() {

    }

    public void setRunning(boolean running) {
        this.running = running;
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

    public void setDestination(File destination) {
        this.destination = destination;
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

    public void setPageAlign(PageAlign pageAlign) {
        this.pageAlign = pageAlign;
    }

    public void setFileFilterPattern(String fileFilterPattern) {
        this.fileFilterPattern = fileFilterPattern;
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

    public File getDestination() {
        return destination;
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

    public PageAlign getPageAlign() {
        return pageAlign;
    }

    public String getFileFilterPattern() {
        return fileFilterPattern;
    }
}
