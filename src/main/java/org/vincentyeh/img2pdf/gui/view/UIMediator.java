package org.vincentyeh.img2pdf.gui.view;

import org.vincentyeh.img2pdf.gui.model.Task;

import java.util.List;

public interface UIMediator {
    void notifyUI(String event, Object... data);
    void updateTasks(List<Task> tasks);
    void setRunningState(boolean running);
    void setBatchProgress(int progress, int total);
    void setConversionProgress(int progress, int total);
    void initialize();
    void setListener(MediatorListener listener);
    void updateTaskStatus(Task task, boolean success);

}
