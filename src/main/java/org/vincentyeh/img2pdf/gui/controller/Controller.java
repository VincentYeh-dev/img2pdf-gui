package org.vincentyeh.img2pdf.gui.controller;

import org.vincentyeh.img2pdf.gui.model.Model;
import org.vincentyeh.img2pdf.gui.model.ModelListener;
import org.vincentyeh.img2pdf.gui.model.Task;
import org.vincentyeh.img2pdf.gui.view.MediatorListener;
import org.vincentyeh.img2pdf.gui.view.UIMediator;
import org.vincentyeh.img2pdf.gui.view.UIState;

import javax.swing.*;
import java.io.File;
import java.util.List;

public class Controller implements MediatorListener, ModelListener {
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final Model model;
    private final UIMediator mediator;

    public Controller(Model model, UIMediator mediator) {
        this.model = model;
        this.mediator = mediator;
        mediator.setListener(this);
        mediator.initialize();
        model.setModelListener(this);
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
        File[] sources = state.getSourceFiles();
        if (sources == null)
            return;

        List<Task> tasks = Model.parseSourceFiles(sources);
        mediator.updateTasks(tasks);
        model.setTask(tasks);
    }

    @Override
    public void onConvertButtonClick(UIMediator mediator, UIState state) {
        model.convert(state);

    }

    @Override
    public void onStopButtonClick(UIMediator mediator) {

    }

    @Override
    public void onTaskRemove(UIMediator mediator, List<Task> tasks) {
        for (Task task : tasks) model.removeTask(task);
        mediator.updateTasks(model.getTasks());
    }

    @Override
    public void onTaskRemoveFromDisk(UIMediator mediator, List<Task> tasks) {
        for (Task task : tasks) model.removeTaskFromDisk(task);
        mediator.updateTasks(model.getTasks());
    }

    @Override
    public void onBatchProgressUpdate(int progress, int total) {
        mediator.setBatchProgress(progress, total);
    }

    @Override
    public void onConversionProgressUpdate(int progress, int total) {
        mediator.setConversionProgress(progress, total);
    }

    @Override
    public void onBatchStart() {
        mediator.setRunningState(true);
        mediator.clearLog();
    }

    @Override
    public void onBatchComplete() {
        mediator.setRunningState(false);
    }

    @Override
    public void onSourcesUpdate(List<Task> source) {
        model.setTask(source);
    }

    @Override
    public void onLogUpdate(List<String> log) {

    }

    @Override
    public void onLogAppend(String log) {
        mediator.addLog(log);
    }
}
