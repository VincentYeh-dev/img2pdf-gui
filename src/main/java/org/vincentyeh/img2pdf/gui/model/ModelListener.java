package org.vincentyeh.img2pdf.gui.model;

import java.util.List;

public interface ModelListener{

    void onTotalConversionProgressUpdate(int progress,int total);
    void onPageConversionProgressUpdate(int progress,int total);

    void onSourcesUpdate(List<Task> source);

    void onLogUpdate(List<String> log);

}
