package org.vincentyeh.img2pdf.gui.model;

import java.util.List;

public interface ModelListener{

    void onBatchProgressUpdate(int progress, int total);
    void onConversionProgressUpdate(int progress, int total);
    void onBatchStart();
    void onBatchComplete();
    void onSourcesUpdate(List<Task> source);
    void onTaskComplete(Task task, boolean success);


}
