package org.vincentyeh.img2pdf.gui.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModelTest {

    private Model model;

    @BeforeEach
    void setUp() {
        model = new Model();
    }

    // Returns a ModelListener that clears and repopulates the given list on each onTasksUpdate.
    private ModelListener listenerCapturing(List<Task> list) {
        return new ModelListener() {
            @Override public void onBatchStart() {}
            @Override public void onBatchComplete() {}
            @Override public void onBatchProgressUpdate(int p, int t) {}
            @Override public void onConversionProgressUpdate(int p, int t) {}
            @Override public void onTaskComplete(Task task, Exception e) {}
            @Override public void onBatchError(String title, String msg) {}
            @Override public void onTasksUpdate(List<Task> tasks) { list.clear(); list.addAll(tasks); }
            @Override public void onTaskDiskRemovalError(Task task, IOException e) {}
        };
    }

    // Verifies that importSources() creates one task per directory and fires onTasksUpdate.
    @Test
    void importSources_stores_tasks_from_directories(@TempDir Path tempDir) throws Exception {
        File dir1 = tempDir.resolve("a").toFile(); dir1.mkdirs();
        File dir2 = tempDir.resolve("b").toFile(); dir2.mkdirs();
        new File(dir1, "1.jpg").createNewFile();
        new File(dir2, "2.jpg").createNewFile();

        List<Task> captured = new ArrayList<>();
        model.setModelListener(listenerCapturing(captured));
        model.importSources(new File[]{dir1, dir2});

        assertEquals(2, captured.size());
    }

    // Verifies that importSources() applies the default NAME_ASC sort order immediately.
    @Test
    void importSources_sorts_tasks_by_default_name_asc(@TempDir Path tempDir) throws Exception {
        File tz = tempDir.resolve("z").toFile(); tz.mkdirs();
        File ta = tempDir.resolve("a").toFile(); ta.mkdirs();

        List<Task> captured = new ArrayList<>();
        model.setModelListener(listenerCapturing(captured));
        model.importSources(new File[]{tz, ta});

        assertEquals("a.pdf", captured.get(0).destination.getName());
        assertEquals("z.pdf", captured.get(1).destination.getName());
    }

    // Verifies that setSortOrder(NAME_DESC) re-sorts an existing task list in reverse name order.
    @Test
    void setSortOrder_name_desc_reverses_name_order(@TempDir Path tempDir) throws Exception {
        File ta = tempDir.resolve("a").toFile(); ta.mkdirs();
        File tz = tempDir.resolve("z").toFile(); tz.mkdirs();

        List<Task> captured = new ArrayList<>();
        model.setModelListener(listenerCapturing(captured));
        model.importSources(new File[]{ta, tz});

        model.setSortOrder(TaskSortOrder.NAME_DESC);

        assertEquals("z.pdf", captured.get(0).destination.getName());
        assertEquals("a.pdf", captured.get(1).destination.getName());
    }

    // Verifies that setSortOrder(COUNT_DESC) places the task with more files first.
    @Test
    void setSortOrder_count_desc_orders_by_file_count_desc(@TempDir Path tempDir) throws Exception {
        File fewDir = tempDir.resolve("few").toFile(); fewDir.mkdirs();
        new File(fewDir, "1.jpg").createNewFile();

        File manyDir = tempDir.resolve("many").toFile(); manyDir.mkdirs();
        new File(manyDir, "1.jpg").createNewFile();
        new File(manyDir, "2.jpg").createNewFile();
        new File(manyDir, "3.jpg").createNewFile();

        List<Task> captured = new ArrayList<>();
        model.setModelListener(listenerCapturing(captured));
        model.importSources(new File[]{fewDir, manyDir});

        model.setSortOrder(TaskSortOrder.COUNT_DESC);

        assertEquals("many.pdf", captured.get(0).destination.getName());
        assertEquals("few.pdf",  captured.get(1).destination.getName());
    }

    // Verifies that setSortOrder() takes effect immediately on the already-stored task list.
    @Test
    void setSortOrder_immediately_resorts_existing_tasks(@TempDir Path tempDir) throws Exception {
        File ta = tempDir.resolve("a").toFile(); ta.mkdirs();
        File tz = tempDir.resolve("z").toFile(); tz.mkdirs();

        List<Task> captured = new ArrayList<>();
        model.setModelListener(listenerCapturing(captured));
        model.importSources(new File[]{ta, tz});
        assertEquals("a.pdf", captured.get(0).destination.getName()); // NAME_ASC default

        model.setSortOrder(TaskSortOrder.NAME_DESC);

        assertEquals("z.pdf", captured.get(0).destination.getName());
    }

    // Verifies that removeTasks() eliminates the target task from the list.
    @Test
    void removeTasks_removes_specified_task_from_list(@TempDir Path tempDir) throws Exception {
        File dir1 = tempDir.resolve("a").toFile(); dir1.mkdirs();
        File dir2 = tempDir.resolve("b").toFile(); dir2.mkdirs();
        new File(dir1, "1.jpg").createNewFile();
        new File(dir2, "2.jpg").createNewFile();

        List<Task> captured = new ArrayList<>();
        model.setModelListener(listenerCapturing(captured));
        model.importSources(new File[]{dir1, dir2});
        // captured = [a.pdf, b.pdf]

        Task toRemove = captured.get(0);
        model.removeTasks(Collections.singletonList(toRemove));

        assertEquals(1, captured.size());
        assertFalse(captured.stream().anyMatch(t -> t == toRemove));
    }

    // Verifies that removeTasks() leaves all other tasks untouched.
    @Test
    void removeTasks_does_not_affect_other_tasks(@TempDir Path tempDir) throws Exception {
        File dir1 = tempDir.resolve("a").toFile(); dir1.mkdirs();
        File dir2 = tempDir.resolve("b").toFile(); dir2.mkdirs();
        new File(dir1, "1.jpg").createNewFile();
        new File(dir2, "2.jpg").createNewFile();

        List<Task> captured = new ArrayList<>();
        model.setModelListener(listenerCapturing(captured));
        model.importSources(new File[]{dir1, dir2});

        Task toRemove = captured.get(0); // "a.pdf"
        model.removeTasks(Collections.singletonList(toRemove));

        assertEquals(1, captured.size());
        assertEquals("b.pdf", captured.get(0).destination.getName());
    }

    // Verifies that removeTasksFromDisk() also removes the task from the in-memory list.
    @Test
    void removeTasksFromDisk_removes_task_from_in_memory_list(@TempDir Path tempDir) throws Exception {
        File dir = tempDir.resolve("album").toFile();
        dir.mkdirs();
        new File(dir, "1.jpg").createNewFile();

        List<Task> captured = new ArrayList<>();
        model.setModelListener(listenerCapturing(captured));
        model.importSources(new File[]{dir});
        assertEquals(1, captured.size());

        Task task = captured.get(0);
        model.removeTasksFromDisk(Collections.singletonList(task));

        assertTrue(captured.isEmpty());
    }

    // Verifies that removeTasksFromDisk() physically deletes the source folder and its contents.
    @Test
    void removeTasksFromDisk_deletes_source_folder_on_disk(@TempDir Path tempDir) throws Exception {
        File dir = tempDir.resolve("to_delete").toFile();
        dir.mkdirs();
        File img = new File(dir, "photo.jpg");
        img.createNewFile();

        List<Task> captured = new ArrayList<>();
        model.setModelListener(listenerCapturing(captured));
        model.importSources(new File[]{dir});
        assertEquals(1, captured.size());

        Task task = captured.get(0);
        model.removeTasksFromDisk(Collections.singletonList(task));

        assertFalse(dir.exists());
    }

    // Verifies that convert() calls listener.onBatchError() when the destination path
    // points to an existing file, and does NOT start the conversion thread.
    @Test
    void convert_calls_onBatchError_when_destination_is_a_file(@TempDir Path tempDir) throws IOException {
        File existingFile = tempDir.resolve("output.txt").toFile();
        existingFile.createNewFile();

        ModelListener listener = mock(ModelListener.class);
        model.setModelListener(listener);

        ConversionConfig config = new ConversionConfig(
                existingFile, false, "", "",
                null, null, null, null, null, false
        );

        model.convert(config);

        verify(listener).onBatchError(anyString(), anyString());
        verify(listener, never()).onBatchStart();
    }

    // Verifies that convert() calls onBatchStart() and then onBatchComplete() on the listener
    // when the task list is empty (no img2pdf.lib I/O required).
    // [White-box] Installs a mock ModelListener and uses CountDownLatch to synchronise with
    // the background conversion thread.
    // Justification: convert() runs on a background thread; there is no public API to observe
    // thread lifecycle or completion. CountDownLatch + mock listener is the only reliable way
    // to assert that both onBatchStart and onBatchComplete are invoked.
    @Test
    void convert_with_empty_task_list_calls_onBatchStart_and_onBatchComplete(@TempDir Path tempDir)
            throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ModelListener listener = mock(ModelListener.class);
        doAnswer(inv -> { latch.countDown(); return null; }).when(listener).onBatchComplete();
        model.setModelListener(listener);
        // model starts with empty task list — no setTask needed

        File outDir = tempDir.resolve("output").toFile();
        ConversionConfig config = new ConversionConfig(
                outDir, false, "", "",
                null, null, null, null, null, false
        );

        model.convert(config);
        boolean completed = latch.await(5, TimeUnit.SECONDS);

        assertTrue(completed, "onBatchComplete was not called within timeout");
        verify(listener).onBatchStart();
        verify(listener).onBatchComplete();
    }
}
