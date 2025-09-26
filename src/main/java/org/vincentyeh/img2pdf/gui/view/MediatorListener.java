package org.vincentyeh.img2pdf.gui.view;


public interface MediatorListener {
    void onSourcesUpdate(UIMediator mediator, UIState state);

    void onConvertButtonClick(UIMediator mediator, UIState state);

    void onStopButtonClick(UIMediator mediator);

}
