package org.vincentyeh.img2pdf.gui.view;


import java.io.File;

public interface MediatorListener {

    void onSourcesUpdate(UIMediator mediator, UIState state);

    void onConversionStart(UIMediator mediator, UIState state);

    void onConversionStop(UIMediator mediator);

}
