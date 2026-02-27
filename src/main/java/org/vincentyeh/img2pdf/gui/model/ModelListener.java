package org.vincentyeh.img2pdf.gui.model;

public interface ModelListener{

    void onBatchProgressUpdate(int progress, int total);
    void onConversionProgressUpdate(int progress, int total);
    void onBatchStart();
    void onBatchComplete();

    void onTaskComplete(Task task, boolean success);


}
