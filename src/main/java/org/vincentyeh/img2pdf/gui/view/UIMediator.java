package org.vincentyeh.img2pdf.gui.view;

import org.vincentyeh.img2pdf.gui.model.Task;

import java.io.File;
import java.util.List;

public interface UIMediator {
    void notifyUI(String event, Object... data);
    void setTasks(List<Task> tasks);
    void setBatchProgress(int progress, int total);
    void setConversionProgress(int progress, int total);
    void addLog(String log);
    void clearLog();
    void initialize();
    void setListener(MediatorListener listener);

}
