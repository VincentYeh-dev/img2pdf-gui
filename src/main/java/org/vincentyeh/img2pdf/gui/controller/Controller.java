package org.vincentyeh.img2pdf.gui.controller;

import org.vincentyeh.img2pdf.gui.model.ConversionConfig;
import org.vincentyeh.img2pdf.gui.model.Model;
import org.vincentyeh.img2pdf.gui.model.ModelListener;
import org.vincentyeh.img2pdf.gui.model.Task;
import org.vincentyeh.img2pdf.gui.model.TaskSortOrder;
import org.vincentyeh.img2pdf.gui.view.MediatorListener;
import org.vincentyeh.img2pdf.gui.view.UIMediator;
import org.vincentyeh.img2pdf.gui.view.UIState;

import java.io.File;
import java.util.List;

/**
 * The controller in the MVC + Mediator architecture.
 * <p>
 * Implements both {@link MediatorListener} (to receive UI-interaction events from
 * the {@link UIMediator}) and {@link ModelListener} (to receive progress/lifecycle
 * callbacks from the {@link Model}), acting as the bridge between the two layers.
 * </p>
 */
public class Controller implements MediatorListener, ModelListener {
    private final Model model;
    private final UIMediator mediator;

    /**
     * Constructs the controller, wiring itself as the listener for both the
     * mediator and the model, then initialises the UI.
     *
     * @param model    the business-logic model
     * @param mediator the UI mediator managing Swing components
     */
    public Controller(Model model, UIMediator mediator) {
        this.model = model;
        this.mediator = mediator;
        mediator.setListener(this);
        mediator.initialize();
        model.setModelListener(this);
    }

    /**
     * Responds to a source-directory selection change by re-scanning the directories
     * and updating the task list in both the model and the UI.
     *
     * @param mediator the mediator that fired the event
     * @param state    the current UI state containing the updated source directories
     */
    @Override
    public void onSourcesUpdate(UIMediator mediator, UIState state) {
        File[] sources = state.getSourceFiles();
        if (sources == null)
            return;

        List<Task> tasks = Model.parseSourceFiles(sources);
        model.setTask(tasks);
        mediator.updateTasks(model.getTasks());
    }

    /**
     * Responds to a sort-order change by updating the model's sort order and
     * refreshing the task list display in the UI.
     *
     * @param mediator the mediator that fired the event
     * @param order    the newly selected sort order
     */
    @Override
    public void onSortOrderChange(UIMediator mediator, TaskSortOrder order) {
        model.setSortOrder(order);
        mediator.updateTasks(model.getTasks());
    }

    /**
     * Responds to the Convert button click by building a {@link ConversionConfig}
     * from the current UI state and starting the batch conversion via the model.
     *
     * @param mediator the mediator that fired the event
     * @param state    the current UI state containing all conversion parameters
     */
    @Override
    public void onConvertButtonClick(UIMediator mediator, UIState state) {
        ConversionConfig config = new ConversionConfig(
                state.getDestinationFolder(),
                state.isEncrypted(),
                state.getOwnerPassword(),
                state.getUserPassword(),
                state.getColorType(),
                state.getPageSize(),
                state.getPageDirection(),
                state.getVerticalAlign(),
                state.getHorizontalAlign(),
                state.isAutoRotate()
        );
        model.convert(config);
    }

    /**
     * Responds to the Stop button click by requesting the model to halt the
     * conversion after the current task finishes.
     *
     * @param mediator the mediator that fired the event
     */
    @Override
    public void onStopButtonClick(UIMediator mediator) {
        model.requestStop();
    }

    /**
     * Responds to a task-removal request by removing each selected task from the
     * model and refreshing the UI task list.
     *
     * @param mediator the mediator that fired the event
     * @param tasks    the tasks to remove from the in-memory list
     */
    @Override
    public void onTaskRemove(UIMediator mediator, List<Task> tasks) {
        for (Task task : tasks) model.removeTask(task);
        mediator.updateTasks(model.getTasks());
    }

    /**
     * Responds to a disk-deletion request by deleting each task's source folder
     * from disk via the model and refreshing the UI task list.
     *
     * @param mediator the mediator that fired the event
     * @param tasks    the tasks whose source directories should be deleted
     */
    @Override
    public void onTaskRemoveFromDisk(UIMediator mediator, List<Task> tasks) {
        for (Task task : tasks) model.removeTaskFromDisk(task);
        mediator.updateTasks(model.getTasks());
    }

    /**
     * Forwards the batch progress update to the UI mediator.
     *
     * @param progress the number of tasks completed so far
     * @param total    the total number of tasks in the batch
     */
    @Override
    public void onBatchProgressUpdate(int progress, int total) {
        mediator.setBatchProgress(progress, total);
    }

    /**
     * Forwards the per-page conversion progress update to the UI mediator.
     *
     * @param progress the number of pages/images converted in the current task
     * @param total    the total number of pages/images in the current task
     */
    @Override
    public void onConversionProgressUpdate(int progress, int total) {
        mediator.setConversionProgress(progress, total);
    }

    /**
     * Notifies the UI that batch conversion has started, switching it to the
     * running state.
     */
    @Override
    public void onBatchStart() {
        mediator.setRunningState(true);
    }

    /**
     * Notifies the UI that the batch conversion has finished, returning it to the
     * idle state.
     */
    @Override
    public void onBatchComplete() {
        mediator.setRunningState(false);
    }

    /**
     * Forwards the per-task completion result to the UI mediator to update the
     * task status indicator in the tree.
     *
     * @param task    the task that has just completed
     * @param success {@code true} if the PDF was created successfully; {@code false} on error
     */
    @Override
    public void onTaskComplete(Task task, boolean success) {
        mediator.updateTaskStatus(task, success);
    }
}
