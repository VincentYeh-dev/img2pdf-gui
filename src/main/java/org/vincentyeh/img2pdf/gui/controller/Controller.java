package org.vincentyeh.img2pdf.gui.controller;

import org.vincentyeh.img2pdf.gui.AppLogger;
import org.vincentyeh.img2pdf.gui.model.ConversionConfig;
import org.vincentyeh.img2pdf.gui.model.Model;
import org.vincentyeh.img2pdf.gui.model.ModelListener;
import org.vincentyeh.img2pdf.gui.model.Task;
import org.vincentyeh.img2pdf.gui.model.TaskSortOrder;
import org.vincentyeh.img2pdf.gui.view.MediatorListener;
import org.vincentyeh.img2pdf.gui.view.UIMediator;
import org.vincentyeh.img2pdf.gui.view.UIState;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

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
     * <p>
     * Any unexpected runtime error that escapes from {@link Model#convert} is caught,
     * logged as {@code SEVERE}, and presented to the user as an error dialog.
     * </p>
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
        try {
            model.convert(config);
        } catch (RuntimeException e) {
            AppLogger.get().log(Level.SEVERE, "Unexpected error during convert() setup", e);
            mediator.showError("Unexpected Error",
                    "An unexpected error occurred while starting the conversion:\n" + e.getMessage());
        }
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
     * <p>
     * If deletion of a specific folder fails, an error dialog is shown for that
     * task and processing continues with the remaining tasks.
     * </p>
     *
     * @param mediator the mediator that fired the event
     * @param tasks    the tasks whose source directories should be deleted
     */
    @Override
    public void onTaskRemoveFromDisk(UIMediator mediator, List<Task> tasks) {
        for (Task task : tasks) {
            try {
                model.removeTaskFromDisk(task);
            } catch (IOException e) {
                String folderPath = (task.files != null && task.files.length > 0)
                        ? task.files[0].getParentFile().getAbsolutePath()
                        : "unknown";
                AppLogger.get().log(Level.WARNING,
                        "Failed to delete source folder: " + folderPath, e);
                mediator.showError("Delete Failed",
                        "Cannot delete source folder:\n" + folderPath);
            }
        }
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
     * <p>
     * A non-{@code null} {@code error} is logged at {@code WARNING} level; the
     * user sees the task marked as failed in the tree rather than a dialog.
     * </p>
     *
     * @param task  the task that has just completed
     * @param error {@code null} on success; the causing exception on failure
     */
    @Override
    public void onTaskComplete(Task task, Exception error) {
        if (error != null) {
            AppLogger.get().log(Level.WARNING,
                    "Task failed: " + task.destination.getName(), error);
        }
        mediator.updateTaskStatus(task, error == null);
    }

    /**
     * Displays a user-visible error dialog for pre-conversion validation failures
     * and logs the event at {@code WARNING} level.
     *
     * @param title   a short, human-readable error title
     * @param message a detailed description of the error
     */
    @Override
    public void onBatchError(String title, String message) {
        AppLogger.get().log(Level.WARNING, "Batch error [{0}]: {1}",
                new Object[]{title, message});
        mediator.showError(title, message);
    }
}
