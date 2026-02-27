package org.vincentyeh.img2pdf.gui.view;

import org.vincentyeh.img2pdf.gui.model.Task;

import java.util.List;

/**
 * Mediator interface that abstracts all UI update and event-dispatch operations.
 * <p>
 * The concrete implementation ({@link JUIMediator}) manages Swing component state,
 * while the Controller calls these methods to reflect model changes in the UI without
 * depending on any Swing types directly.
 * </p>
 */
public interface UIMediator {

    /**
     * Dispatches a named UI event with optional payload data.
     * <p>
     * Used internally by Swing component listeners to feed user actions through
     * a single, centralised entry point.
     * </p>
     *
     * @param event the name of the event (e.g. {@code "outputFolder"}, {@code "convert"})
     * @param data  optional additional data associated with the event
     */
    void notifyUI(String event, Object... data);

    /**
     * Replaces the displayed task list with the supplied tasks and refreshes the UI tree.
     *
     * @param tasks the new list of tasks to display
     */
    void updateTasks(List<Task> tasks);

    /**
     * Switches the UI between its "running" (conversion in progress) and "idle" states,
     * enabling or disabling controls accordingly.
     *
     * @param running {@code true} to enter the running state; {@code false} to return to idle
     */
    void setRunningState(boolean running);

    /**
     * Updates the batch (overall) progress bar and its associated label.
     *
     * @param progress the number of tasks completed so far
     * @param total    the total number of tasks in the batch
     */
    void setBatchProgress(int progress, int total);

    /**
     * Updates the per-page (current task) progress bar and its associated label.
     *
     * @param progress the number of pages/images converted so far in the current task
     * @param total    the total number of pages/images in the current task
     */
    void setConversionProgress(int progress, int total);

    /**
     * Initialises all UI components with their default values (combo-box items,
     * initial selections, empty task tree, etc.).
     * Must be called once after all components have been linked.
     */
    void initialize();

    /**
     * Registers the listener that will receive user-interaction events from this mediator.
     *
     * @param listener the listener to register
     */
    void setListener(MediatorListener listener);

    /**
     * Updates the visual status indicator of a task node in the tree after it finishes.
     *
     * @param task    the task that has completed
     * @param success {@code true} if the task succeeded; {@code false} if it failed
     */
    void updateTaskStatus(Task task, boolean success);

}
