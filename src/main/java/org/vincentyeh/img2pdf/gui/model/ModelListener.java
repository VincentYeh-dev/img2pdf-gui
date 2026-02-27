package org.vincentyeh.img2pdf.gui.model;

/**
 * Callback interface that receives progress and lifecycle events from {@link Model}
 * during a batch PDF conversion.
 * <p>
 * All callbacks are invoked from the Model's background conversion thread; callers
 * must dispatch UI updates to the Event Dispatch Thread themselves if required.
 * </p>
 */
public interface ModelListener{

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
     * @param task    the task that has just completed
     * @param success {@code true} if the PDF was created successfully; {@code false} on error
     */
    void onTaskComplete(Task task, boolean success);


}
