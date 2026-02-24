package org.vincentyeh.img2pdf.gui.view;

import org.vincentyeh.img2pdf.gui.model.Task;

public interface MediatorListener {
    void onSourcesUpdate(UIMediator mediator, UIState state);

    void onConvertButtonClick(UIMediator mediator, UIState state);

    void onStopButtonClick(UIMediator mediator);

    void onTaskRemove(UIMediator mediator, Task task);
}
