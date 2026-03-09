package org.vincentyeh.img2pdf.gui.view;

import org.vincentyeh.img2pdf.gui.model.TaskSortOrder;

import java.io.File;
import java.util.List;

/**
 * Callback interface for receiving user-interaction events fired by the
 * {@link UIMediator}.
 * <p>
 * The {@link org.vincentyeh.img2pdf.gui.controller.Controller} implements this
 * interface to bridge UI actions to the business-logic layer ({@link
 * org.vincentyeh.img2pdf.gui.model.Model}).
 * All callbacks are invoked on the Swing Event Dispatch Thread.
 * </p>
 */
public interface MediatorListener {

    /**
     * Called when the user adds one or more source directories (via the file
     * browser or drag-and-drop). The supplied directories are appended to the
     * existing task list rather than replacing it.
     *
     * @param sources the newly added source directories; never {@code null}
     */
void onAddSourcesRequested(List<File> sources);

    /**
     * Called when the user clicks the Convert button to start PDF conversion.
     *
     * @param mediator the mediator that fired the event
     * @param state    the current UI state containing all conversion parameters
     */
    void onConvertRequested(UIMediator mediator, UIState state);

    /**
     * Called when the user clicks the Stop button to request early termination of
     * the running conversion batch.
     */
    void onStopButtonRequested();

    /**
     * Called when the user requests removal of selected tasks from the task list
     * (files on disk are kept).
     *
     * @param indices the zero-based positions of the selected tasks in the current list
     */
    void onTaskRemoveRequested(List<Integer> indices);

    /**
     * Called when the user requests deletion of selected tasks together with their
     * source files on disk.
     *
     * @param indices the zero-based positions of the selected tasks in the current list
     */
    void onTaskRemoveFromDiskRequest(List<Integer> indices);

    /**
     * Called when the user selects a new sort order for the task list.
     *
     * @param order the newly selected sort order
     */
    void onSortOrderChangeRequested(TaskSortOrder order);

    /**
     * Called when the user requests to clear the entire task list.
     */
    void onTaskClearRequested();
}
