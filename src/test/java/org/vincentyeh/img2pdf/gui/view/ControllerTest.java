package org.vincentyeh.img2pdf.gui.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.vincentyeh.img2pdf.gui.AppLogger;
import org.vincentyeh.img2pdf.gui.controller.Controller;
import org.vincentyeh.img2pdf.gui.model.ConversionConfig;
import org.vincentyeh.img2pdf.gui.model.Model;
import org.vincentyeh.img2pdf.gui.model.Task;
import org.vincentyeh.img2pdf.gui.model.TaskSortOrder;
import org.vincentyeh.img2pdf.lib.image.ColorType;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageAlign;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageDirection;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageSize;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControllerTest {

    private Model model;
    private UIMediator mediator;
    private Controller controller;

    @BeforeEach
    void setUp() {
        UIState.resetForTesting();
        model    = mock(Model.class);
        mediator = mock(UIMediator.class);
        when(model.getTasks()).thenReturn(Collections.emptyList());
        controller = new Controller(model, mediator);
    }

    // Verifies that the Controller constructor registers itself as the mediator's listener.
    // [White-box] Uses verify() to check mediator.setListener(controller) was called.
    // Justification: wiring correctness is not observable through the public API;
    // if the listener is not registered, UI events will never reach the controller.
    @Test
    void constructor_registers_controller_as_mediator_listener() {
        verify(mediator).setListener(controller);
    }

    // Verifies that the Controller constructor calls mediator.initialize().
    // [White-box] Uses verify() to confirm the call happened during construction.
    // Justification: initialize() bootstraps all UI component defaults; skipping it
    // would leave the UI in an undefined state and cannot be detected from outside.
    @Test
    void constructor_calls_mediator_initialize() {
        verify(mediator).initialize();
    }

    // Verifies that the Controller constructor registers itself as the model's listener.
    // [White-box] Uses verify() on the mock Model.
    // Justification: if model.setModelListener() is not called, progress callbacks
    // (onBatchStart, onTaskComplete, etc.) will never be forwarded to the UI.
    @Test
    void constructor_registers_controller_as_model_listener() {
        verify(model).setModelListener(controller);
    }

    // Verifies that onSourcesUpdate() does nothing when UIState.sourceFiles is null.
    @Test
    void onSourcesUpdate_with_null_sources_does_not_call_model_setTask() {
        UIState state = UIState.getInstance();
        // sourceFiles is null by default after reset

        controller.onSourcesUpdate(mediator, state);

        verify(model, never()).setTask(any());
    }

    // Verifies that onSourcesUpdate() calls model.setTask() and mediator.updateTasks()
    // when UIState contains valid source directories.
    @Test
    void onSourcesUpdate_with_valid_sources_calls_setTask_and_mediator_updateTasks(
            @TempDir Path tempDir) throws Exception {
        File dir = tempDir.resolve("album").toFile();
        dir.mkdirs();
        new File(dir, "photo.jpg").createNewFile();

        UIState state = UIState.getInstance();
        state.setSourceFiles(new File[]{dir});

        controller.onSourcesUpdate(mediator, state);

        verify(model).setTask(argThat(list -> !list.isEmpty()));
        verify(mediator, atLeastOnce()).updateTasks(any());
    }

    // Verifies that onSortOrderChange() updates the model sort order and refreshes the UI.
    @Test
    void onSortOrderChange_calls_model_setSortOrder_and_mediator_updateTasks() {
        controller.onSortOrderChange(mediator, TaskSortOrder.NAME_DESC);

        verify(model).setSortOrder(TaskSortOrder.NAME_DESC);
        verify(mediator, atLeastOnce()).updateTasks(any());
    }

    // Verifies that onConvertButtonClick() builds a ConversionConfig from UIState values
    // and passes it to model.convert() with all fields matching the state.
    @Test
    void onConvertButtonClick_builds_config_from_ui_state_and_calls_model_convert(
            @TempDir Path tempDir) {
        File dest = tempDir.resolve("out").toFile();
        UIState state = UIState.getInstance();
        state.setDestinationFolder(dest);
        state.setEncrypted(true);
        state.setOwnerPassword("owner123");
        state.setUserPassword("user456");
        state.setColorType(ColorType.sRGB);
        state.setPageSize(PageSize.A4);
        state.setPageDirection(PageDirection.Landscape);
        state.setVerticalAlign(PageAlign.VerticalAlign.CENTER);
        state.setHorizontalAlign(PageAlign.HorizontalAlign.CENTER);
        state.setAutoRotate(true);

        controller.onConvertButtonClick(mediator, state);

        ArgumentCaptor<ConversionConfig> captor = ArgumentCaptor.forClass(ConversionConfig.class);
        verify(model).convert(captor.capture());
        ConversionConfig cfg = captor.getValue();
        assertEquals(dest,                            cfg.destinationFolder);
        assertTrue(cfg.encrypted);
        assertEquals("owner123",                      cfg.ownerPassword);
        assertEquals("user456",                       cfg.userPassword);
        assertEquals(ColorType.sRGB,                   cfg.colorType);
        assertEquals(PageSize.A4,                     cfg.pageSize);
        assertEquals(PageDirection.Landscape,         cfg.pageDirection);
        assertEquals(PageAlign.VerticalAlign.CENTER,  cfg.verticalAlign);
        assertEquals(PageAlign.HorizontalAlign.CENTER, cfg.horizontalAlign);
        assertTrue(cfg.autoRotate);
    }

    // Verifies that onStopButtonClick() delegates to model.requestStop().
    @Test
    void onStopButtonClick_calls_model_requestStop() {
        controller.onStopButtonClick(mediator);

        verify(model).requestStop();
    }

    // Verifies that onTaskRemove() calls model.removeTask() for each task in the list
    // and then calls mediator.updateTasks() exactly once.
    @Test
    void onTaskRemove_calls_removeTask_for_each_task_and_updates_mediator() {
        Task t1 = new Task(new File("a.pdf"), new File[0]);
        Task t2 = new Task(new File("b.pdf"), new File[0]);
        List<Task> toRemove = Arrays.asList(t1, t2);

        controller.onTaskRemove(mediator, toRemove);

        verify(model).removeTask(t1);
        verify(model).removeTask(t2);
        verify(mediator, atLeastOnce()).updateTasks(any());
    }

    // Verifies that onTaskRemoveFromDisk() calls model.removeTaskFromDisk() for each task
    // and then calls mediator.updateTasks() exactly once.
    @Test
    void onTaskRemoveFromDisk_calls_removeTaskFromDisk_for_each_and_updates_mediator() throws IOException {
        Task t1 = new Task(new File("a.pdf"), new File[0]);
        Task t2 = new Task(new File("b.pdf"), new File[0]);
        List<Task> toRemove = Arrays.asList(t1, t2);
        doNothing().when(model).removeTaskFromDisk(any());

        controller.onTaskRemoveFromDisk(mediator, toRemove);

        verify(model).removeTaskFromDisk(t1);
        verify(model).removeTaskFromDisk(t2);
        verify(mediator, atLeastOnce()).updateTasks(any());
    }

    // Verifies that onTaskRemoveFromDisk() shows an error dialog and keeps the task
    // when model.removeTaskFromDisk() throws IOException.
    @Test
    void onTaskRemoveFromDisk_when_IOException_calls_showError_and_keeps_other_tasks() throws IOException {
        File img = new File("folder/1.jpg");
        Task t1 = new Task(new File("a.pdf"), new File[]{img});
        doThrow(new IOException("disk error")).when(model).removeTaskFromDisk(t1);

        controller.onTaskRemoveFromDisk(mediator, Collections.singletonList(t1));

        verify(mediator).showError(anyString(), anyString());
        verify(mediator, atLeastOnce()).updateTasks(any());
    }

    // Verifies that onBatchProgressUpdate() forwards both arguments to mediator.setBatchProgress().
    @Test
    void onBatchProgressUpdate_delegates_to_mediator_setBatchProgress() {
        controller.onBatchProgressUpdate(3, 10);

        verify(mediator).setBatchProgress(3, 10);
    }

    // Verifies that onConversionProgressUpdate() forwards both arguments to mediator.setConversionProgress().
    @Test
    void onConversionProgressUpdate_delegates_to_mediator_setConversionProgress() {
        controller.onConversionProgressUpdate(5, 20);

        verify(mediator).setConversionProgress(5, 20);
    }

    // Verifies that onBatchStart() calls mediator.setRunningState(true).
    @Test
    void onBatchStart_calls_mediator_setRunningState_true() {
        controller.onBatchStart();

        verify(mediator).setRunningState(true);
    }

    // Verifies that onBatchComplete() calls mediator.setRunningState(false).
    @Test
    void onBatchComplete_calls_mediator_setRunningState_false() {
        controller.onBatchComplete();

        verify(mediator).setRunningState(false);
    }

    // Verifies that onTaskComplete() with error=null calls mediator.updateTaskStatus(task, true).
    @Test
    void onTaskComplete_success_calls_mediator_updateTaskStatus_with_true() {
        Task task = new Task(new File("a.pdf"), new File[0]);

        controller.onTaskComplete(task, null);

        verify(mediator).updateTaskStatus(task, true);
    }

    // Verifies that onTaskComplete() with a non-null exception calls mediator.updateTaskStatus(task, false).
    @Test
    void onTaskComplete_failure_calls_mediator_updateTaskStatus_with_false() {
        Task task = new Task(new File("a.pdf"), new File[0]);

        controller.onTaskComplete(task, new RuntimeException("test error"));

        verify(mediator).updateTaskStatus(task, false);
    }

    // Verifies that onBatchError() forwards the error to mediator.showError().
    @Test
    void onBatchError_calls_mediator_showError() {
        controller.onBatchError("Test Title", "Test Message");

        verify(mediator).showError("Test Title", "Test Message");
    }

    // Verifies that onTaskComplete() with a non-null error logs exactly one WARNING
    // record that contains the task's destination file name.
    // [White-box] Installs a custom java.util.logging.Handler on AppLogger.get() to
    // capture log records.
    // Justification: there is no public API to observe what was logged; direct Handler
    // capture is the only reliable way to assert Logger output without external libraries.
    @Test
    void onTaskComplete_failure_logs_warning_with_task_name() {
        Logger appLogger = AppLogger.get();
        List<LogRecord> records = new ArrayList<>();
        Handler captureHandler = new Handler() {
            @Override public void publish(LogRecord record) { records.add(record); }
            @Override public void flush() {}
            @Override public void close() {}
        };
        captureHandler.setLevel(Level.ALL);
        appLogger.addHandler(captureHandler);

        try {
            Task task = new Task(new File("failed_task.pdf"), new File[0]);
            controller.onTaskComplete(task, new RuntimeException("conversion error"));

            long warnings = records.stream()
                    .filter(r -> r.getLevel() == Level.WARNING)
                    .count();
            assertEquals(1, warnings, "Expected exactly one WARNING log record");

            boolean containsName = records.stream()
                    .filter(r -> r.getLevel() == Level.WARNING)
                    .anyMatch(r -> r.getMessage().contains("failed_task.pdf"));
            assertTrue(containsName, "WARNING message should contain the task destination file name");
        } finally {
            appLogger.removeHandler(captureHandler);
        }
    }

    // Verifies that onTaskComplete() with error=null does NOT produce any WARNING log record.
    // [White-box] Installs a custom java.util.logging.Handler on AppLogger.get() to
    // capture log records.
    // Justification: successful completion should be silent; verifying absence of WARNING
    // records requires direct Handler capture.
    @Test
    void onTaskComplete_success_does_not_log_warning() {
        Logger appLogger = AppLogger.get();
        List<LogRecord> records = new ArrayList<>();
        Handler captureHandler = new Handler() {
            @Override public void publish(LogRecord record) { records.add(record); }
            @Override public void flush() {}
            @Override public void close() {}
        };
        captureHandler.setLevel(Level.ALL);
        appLogger.addHandler(captureHandler);

        try {
            Task task = new Task(new File("success_task.pdf"), new File[0]);
            controller.onTaskComplete(task, null);

            long warnings = records.stream()
                    .filter(r -> r.getLevel() == Level.WARNING)
                    .count();
            assertEquals(0, warnings, "No WARNING log record should be emitted on success");
        } finally {
            appLogger.removeHandler(captureHandler);
        }
    }

    // Verifies that onBatchError() logs a WARNING record whose message contains
    // both the title and message arguments passed to the method.
    // [White-box] Installs a custom java.util.logging.Handler on AppLogger.get() to
    // capture log records.
    // Justification: the log message format "Batch error [title]: message" is not
    // observable through any public API; Handler capture is necessary.
    @Test
    void onBatchError_logs_warning_with_title_and_message() {
        Logger appLogger = AppLogger.get();
        List<LogRecord> records = new ArrayList<>();
        Handler captureHandler = new Handler() {
            @Override public void publish(LogRecord record) { records.add(record); }
            @Override public void flush() {}
            @Override public void close() {}
        };
        captureHandler.setLevel(Level.ALL);
        appLogger.addHandler(captureHandler);

        try {
            controller.onBatchError("ErrorTitle", "ErrorMessage");

            long warnings = records.stream()
                    .filter(r -> r.getLevel() == Level.WARNING)
                    .count();
            assertEquals(1, warnings, "Expected exactly one WARNING log record for onBatchError");

            boolean containsTitle = records.stream()
                    .filter(r -> r.getLevel() == Level.WARNING)
                    .anyMatch(r -> r.getMessage().contains("ErrorTitle"));
            boolean containsMessage = records.stream()
                    .filter(r -> r.getLevel() == Level.WARNING)
                    .anyMatch(r -> r.getMessage().contains("ErrorMessage"));
            assertTrue(containsTitle,   "WARNING message should contain the error title");
            assertTrue(containsMessage, "WARNING message should contain the error message");
        } finally {
            appLogger.removeHandler(captureHandler);
        }
    }
}
