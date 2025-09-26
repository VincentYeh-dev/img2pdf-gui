package org.vincentyeh.img2pdf.gui.controller;

import org.vincentyeh.img2pdf.gui.model.Model;
import org.vincentyeh.img2pdf.gui.model.Task;
import org.vincentyeh.img2pdf.gui.view.MediatorListener;
import org.vincentyeh.img2pdf.gui.view.UIMediator;
import org.vincentyeh.img2pdf.gui.view.UIState;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Controller implements MediatorListener {
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final Model model;

    public Controller(Model model, UIMediator mediator) {
        this.model = model;
        mediator.setListener(this);
        mediator.initialize();
    }

    private ListModel<String> convertToModel(List<String> list) {
        listModel.clear();
        for (int i = 0; i < list.size(); i++) {
            listModel.add(i, list.get(i));
        }
        return listModel;
    }


    @Override
    public void onSourcesUpdate(UIMediator mediator, UIState state) {
        String outputFormat = state.getOutputFormat();
        String fileFilter = state.getFileFilterPattern();
        if (outputFormat == null || fileFilter == null)
            return;
        File[] sources = state.getSourceFiles();
        if (sources == null || sources.length == 0)
            return;
        List<Task> tasks = model.parseSourceFiles(sources, outputFormat, fileFilter );
        mediator.setTasks(tasks);
    }

    @Override
    public void onConversionStart(UIMediator mediator, UIState state) {

    }

    @Override
    public void onConversionStop(UIMediator mediator) {

    }
}
