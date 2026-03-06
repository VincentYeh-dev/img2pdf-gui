package org.vincentyeh.img2pdf.gui.model;

import java.io.IOException;
import java.util.List;

/**
 * Callback interface that receives progress and lifecycle events from {@link Model}.
 * <p>
 * Task-list mutation callbacks ({@link #onTasksUpdate}) are invoked on the EDT.
 * Conversion-progress callbacks are invoked from the Model's background thread;
 * callers must dispatch UI updates to the EDT themselves if required.
 * </p>
 */
public interface ModelListener{

    /**
     * Called once after any task-list change (import, sort, or remove).
     * Always invoked on the calling thread (EDT for user-triggered operations).
     *
     * @param tasks an unmodifiable snapshot of the current task list
     */
    void onTasksUpdate(List<Task> tasks);

    /**
     * Called per-task when a disk-removal operation fails.
     * Invoked before {@link #onTasksUpdate} at the end of a batch removal.
     *
     * @param task  the task whose source folder could not be deleted
     * @param error the underlying IO failure
     */
    void onTaskDiskRemovalError(Task task, IOException error);

    /**
     * Called whenever the overall batch progress changes.
     *
     * @param progress the number of tasks completed so far
     * @param total    the total number of tasks in the batch
     */
    void onBatchProgressUpdate(int progress, int total);

    /**
     * Called whenever the per-page (image) conversion progress changes within
     * the currently processing task.
     *
     * @param progress the number of pages/images converted so far in the current task
     * @param total    the total number of pages/images in the current task
     */
    void onConversionProgressUpdate(int progress, int total);

    /**
     * Called once when the batch conversion starts, before any task is processed.
     */
    void onBatchStart();

    /**
     * Called once when all tasks in the batch have been processed (or conversion
     * was stopped early).
     */
    void onBatchComplete();

    /**
     * Called after each individual task finishes, regardless of success or failure.
     *
     * @param task  the task that has just completed
     * @param error {@code null} if the PDF was created successfully;
     *              the exception that caused the failure otherwise
     */
    void onTaskComplete(Task task, Exception error);

    /**
     * Called when a pre-conversion validation step fails (e.g. the output folder
     * cannot be created or is not a directory).
     * <p>
     * This callback is always invoked on the Event Dispatch Thread.
     * </p>
     *
     * @param title   a short, human-readable error title
     * @param message a detailed description of the error suitable for display to the user
     */
    void onBatchError(String title, String message);

}
