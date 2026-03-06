package org.vincentyeh.img2pdf.gui.controller;

import org.vincentyeh.img2pdf.gui.AppLogger;
import org.vincentyeh.img2pdf.gui.model.ConversionConfig;
import org.vincentyeh.img2pdf.gui.model.Model;
import org.vincentyeh.img2pdf.gui.model.ModelListener;
import org.vincentyeh.img2pdf.gui.model.Task;
import org.vincentyeh.img2pdf.gui.model.TaskSortOrder;
import org.vincentyeh.img2pdf.gui.view.MediatorListener;
import org.vincentyeh.img2pdf.gui.view.TaskDisplay;
import org.vincentyeh.img2pdf.gui.view.UIMediator;
import org.vincentyeh.img2pdf.gui.view.UIState;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

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

    /** Local snapshot of the current task list, updated by {@link #onTasksUpdate}. */
    private List<Task> currentTasks = new ArrayList<>();

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
        // importSources() internally calls parseSourceFiles() and fires onTasksUpdate
        model.importSources(sources);
    }

    /**
     * Responds to a sort-order change by updating the model's sort order.
     * The model fires {@link #onTasksUpdate} automatically after re-sorting.
     *
     * @param mediator the mediator that fired the event
     * @param order    the newly selected sort order
     */
    @Override
    public void onSortOrderChange(UIMediator mediator, TaskSortOrder order) {
        model.setSortOrder(order);
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
     * Responds to a task-removal request by translating indices to Task objects
     * and delegating to the model. The model fires {@link #onTasksUpdate} once
     * after all removals.
     *
     * @param mediator the mediator that fired the event
     * @param indices  the zero-based positions of tasks to remove
     */
    @Override
    public void onTaskRemove(UIMediator mediator, List<Integer> indices) {
        List<Task> toRemove = indices.stream()
                .filter(i -> i >= 0 && i < currentTasks.size())
                .map(currentTasks::get)
                .collect(Collectors.toList());
        model.removeTasks(toRemove);
    }

    /**
     * Responds to a disk-deletion request by translating indices to Task objects
     * and delegating to the model. Per-task errors are reported via
     * {@link #onTaskDiskRemovalError}; a single {@link #onTasksUpdate} is fired at
     * the end.
     *
     * @param mediator the mediator that fired the event
     * @param indices  the zero-based positions of tasks to delete from disk
     */
    @Override
    public void onTaskRemoveFromDisk(UIMediator mediator, List<Integer> indices) {
        List<Task> toRemove = indices.stream()
                .filter(i -> i >= 0 && i < currentTasks.size())
                .map(currentTasks::get)
                .collect(Collectors.toList());
        model.removeTasksFromDisk(toRemove);
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
        int index = currentTasks.indexOf(task);
        if (index >= 0) mediator.updateTaskStatus(index, error == null);
    }

    /**
     * Receives the updated task list from the model and pushes a converted
     * {@link TaskDisplay} list to the UI mediator.
     *
     * @param tasks an unmodifiable snapshot of the current task list
     */
    @Override
    public void onTasksUpdate(List<Task> tasks) {
        currentTasks = new ArrayList<>(tasks);
        List<TaskDisplay> displays = tasks.stream()
                .map(t -> new TaskDisplay(t.destination.getName(), t.files))
                .collect(Collectors.toList());
        mediator.updateTasks(displays);
    }

    /**
     * Shows an error dialog when a disk-removal operation fails for a specific task.
     *
     * @param task  the task whose source folder could not be deleted
     * @param error the underlying IO failure
     */
    @Override
    public void onTaskDiskRemovalError(Task task, IOException error) {
        String folderPath = (task.files != null && task.files.length > 0)
                ? task.files[0].getParentFile().getAbsolutePath()
                : "unknown";
        AppLogger.get().log(Level.WARNING,
                "Failed to delete source folder: " + folderPath, error);
        mediator.showError("Delete Failed", "Cannot delete source folder:\n" + folderPath);
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
        AppLogger.get().log(Level.WARNING, "Batch error [" + title + "]: " + message);
        mediator.showError(title, message);
    }
}
