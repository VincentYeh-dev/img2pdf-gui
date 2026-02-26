package org.vincentyeh.img2pdf.gui.view;

import org.vincentyeh.img2pdf.gui.model.Task;
import org.vincentyeh.img2pdf.gui.model.TaskSortOrder;

import java.util.List;

public interface MediatorListener {
    void onSourcesUpdate(UIMediator mediator, UIState state);

    void onConvertButtonClick(UIMediator mediator, UIState state);

    void onStopButtonClick(UIMediator mediator);

    void onTaskRemove(UIMediator mediator, List<Task> tasks);

    void onTaskRemoveFromDisk(UIMediator mediator, List<Task> tasks);

    void onSortOrderChange(UIMediator mediator, TaskSortOrder order);
}
