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
     * @param mediator the mediator that fired the event
     * @param sources  the newly added source directories; never {@code null}
     */
    void onSourcesAdded(UIMediator mediator, List<File> sources);

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
     * @param indices  the zero-based positions of the selected tasks in the current list
     */
    void onTaskRemove(UIMediator mediator, List<Integer> indices);

    /**
     * Called when the user requests deletion of selected tasks together with their
     * source files on disk.
     *
     * @param mediator the mediator that fired the event
     * @param indices  the zero-based positions of the selected tasks in the current list
     */
    void onTaskRemoveFromDisk(UIMediator mediator, List<Integer> indices);

    /**
     * Called when the user selects a new sort order for the task list.
     *
     * @param mediator the mediator that fired the event
     * @param order    the newly selected sort order
     */
    void onSortOrderChange(UIMediator mediator, TaskSortOrder order);

    /**
     * Called when the user requests to clear the entire task list.
     *
     * @param mediator the mediator that fired the event
     */
    void onTaskClear(UIMediator mediator);
}
