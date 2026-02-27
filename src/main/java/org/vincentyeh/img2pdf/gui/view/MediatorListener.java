package org.vincentyeh.img2pdf.gui.view;

import org.vincentyeh.img2pdf.gui.model.Task;
import org.vincentyeh.img2pdf.gui.model.TaskSortOrder;

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
     * Called when the user changes the source directory selection, triggering a
     * re-scan and rebuild of the task list.
     *
     * @param mediator the mediator that fired the event
     * @param state    the current UI state containing the updated source files
     */
    void onSourcesUpdate(UIMediator mediator, UIState state);

    /**
     * Called when the user clicks the Convert button to start PDF conversion.
     *
     * @param mediator the mediator that fired the event
     * @param state    the current UI state containing all conversion parameters
     */
    void onConvertButtonClick(UIMediator mediator, UIState state);

    /**
     * Called when the user clicks the Stop button to request early termination of
     * the running conversion batch.
     *
     * @param mediator the mediator that fired the event
     */
    void onStopButtonClick(UIMediator mediator);

    /**
     * Called when the user requests removal of selected tasks from the task list
     * (files on disk are kept).
     *
     * @param mediator the mediator that fired the event
     * @param tasks    the list of tasks selected for removal
     */
    void onTaskRemove(UIMediator mediator, List<Task> tasks);

    /**
     * Called when the user requests deletion of selected tasks together with their
     * source files on disk.
     *
     * @param mediator the mediator that fired the event
     * @param tasks    the list of tasks whose source directories should be deleted
     */
    void onTaskRemoveFromDisk(UIMediator mediator, List<Task> tasks);

    /**
     * Called when the user selects a new sort order for the task list.
     *
     * @param mediator the mediator that fired the event
     * @param order    the newly selected sort order
     */
    void onSortOrderChange(UIMediator mediator, TaskSortOrder order);
}
