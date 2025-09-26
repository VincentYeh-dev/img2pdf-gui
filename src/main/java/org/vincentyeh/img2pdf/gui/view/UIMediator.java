package org.vincentyeh.img2pdf.gui.view;

public interface UIMediator {
    void notifyUI(String event, Object... data);
    void setBatchProgress(int progress, int total);
    void setConversionProgress(int progress, int total);
    void initialize();

}
