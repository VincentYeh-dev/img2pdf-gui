package org.vincentyeh.img2pdf.gui.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
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

    // Verifies that getTasks() returns the exact list provided to setTask().
    @Test
    void setTask_stores_provided_tasks() {
        Task t1 = new Task(new File("a.pdf"), new File[0]);
        Task t2 = new Task(new File("b.pdf"), new File[0]);

        model.setTask(Arrays.asList(t1, t2));

        assertEquals(2, model.getTasks().size());
        assertTrue(model.getTasks().contains(t1));
        assertTrue(model.getTasks().contains(t2));
    }

    // Verifies that setTask() applies the default NAME_ASC sort order immediately.
    @Test
    void setTask_sorts_tasks_by_default_name_asc() {
        Task tz = new Task(new File("z.pdf"), new File[0]);
        Task ta = new Task(new File("a.pdf"), new File[0]);

        model.setTask(Arrays.asList(tz, ta));

        List<Task> tasks = model.getTasks();
        assertEquals("a.pdf", tasks.get(0).destination.getName());
        assertEquals("z.pdf", tasks.get(1).destination.getName());
    }

    // Verifies that setSortOrder(NAME_DESC) re-sorts an existing task list in reverse name order.
    @Test
    void setSortOrder_name_desc_reverses_name_order() {
        Task ta = new Task(new File("a.pdf"), new File[0]);
        Task tz = new Task(new File("z.pdf"), new File[0]);
        model.setTask(Arrays.asList(ta, tz));

        model.setSortOrder(TaskSortOrder.NAME_DESC);

        List<Task> tasks = model.getTasks();
        assertEquals("z.pdf", tasks.get(0).destination.getName());
        assertEquals("a.pdf", tasks.get(1).destination.getName());
    }

    // Verifies that setSortOrder(COUNT_DESC) places the task with more files first.
    @Test
    void setSortOrder_count_desc_orders_by_file_count_desc() {
        Task few  = new Task(new File("few.pdf"),  new File[]{new File("1.jpg")});
        Task many = new Task(new File("many.pdf"), new File[]{new File("1.jpg"), new File("2.jpg"), new File("3.jpg")});
        model.setTask(Arrays.asList(few, many));

        model.setSortOrder(TaskSortOrder.COUNT_DESC);

        List<Task> tasks = model.getTasks();
        assertEquals("many.pdf", tasks.get(0).destination.getName());
        assertEquals("few.pdf",  tasks.get(1).destination.getName());
    }

    // Verifies that setSortOrder() takes effect immediately on the already-stored task list.
    @Test
    void setSortOrder_immediately_resorts_existing_tasks() {
        Task ta = new Task(new File("a.pdf"), new File[0]);
        Task tz = new Task(new File("z.pdf"), new File[0]);
        model.setTask(Arrays.asList(ta, tz));
        assertEquals("a.pdf", model.getTasks().get(0).destination.getName());

        model.setSortOrder(TaskSortOrder.NAME_DESC);

        assertEquals("z.pdf", model.getTasks().get(0).destination.getName());
    }

    // Verifies that removeTask() eliminates the target task from the list.
    @Test
    void removeTask_removes_specified_task_from_list() {
        Task t1 = new Task(new File("a.pdf"), new File[0]);
        Task t2 = new Task(new File("b.pdf"), new File[0]);
        model.setTask(Arrays.asList(t1, t2));

        model.removeTask(t1);

        assertFalse(model.getTasks().contains(t1));
    }

    // Verifies that removeTask() leaves all other tasks untouched.
    @Test
    void removeTask_does_not_affect_other_tasks() {
        Task t1 = new Task(new File("a.pdf"), new File[0]);
        Task t2 = new Task(new File("b.pdf"), new File[0]);
        model.setTask(Arrays.asList(t1, t2));

        model.removeTask(t1);

        assertTrue(model.getTasks().contains(t2));
        assertEquals(1, model.getTasks().size());
    }

    // Verifies that removeTaskFromDisk() also removes the task from the in-memory list.
    @Test
    void removeTaskFromDisk_removes_task_from_in_memory_list(@TempDir Path tempDir) throws IOException {
        File dir = tempDir.resolve("album").toFile();
        dir.mkdirs();
        File img = new File(dir, "1.jpg");
        img.createNewFile();
        Task task = new Task(new File("album.pdf"), new File[]{img});
        model.setTask(Collections.singletonList(task));

        model.removeTaskFromDisk(task);

        assertFalse(model.getTasks().contains(task));
    }

    // Verifies that removeTaskFromDisk() physically deletes the source folder and its contents.
    @Test
    void removeTaskFromDisk_deletes_source_folder_on_disk(@TempDir Path tempDir) throws IOException {
        File dir = tempDir.resolve("to_delete").toFile();
        dir.mkdirs();
        File img = new File(dir, "photo.jpg");
        img.createNewFile();
        Task task = new Task(new File("to_delete.pdf"), new File[]{img});
        model.setTask(Collections.singletonList(task));

        model.removeTaskFromDisk(task);

        assertFalse(dir.exists());
    }

    // Verifies that convert() throws IllegalArgumentException synchronously when the
    // destination path points to an existing file rather than a directory.
    @Test
    void convert_throws_illegal_argument_when_destination_is_a_file(@TempDir Path tempDir) throws IOException {
        File existingFile = tempDir.resolve("output.txt").toFile();
        existingFile.createNewFile();

        ModelListener listener = mock(ModelListener.class);
        model.setModelListener(listener);

        ConversionConfig config = new ConversionConfig(
                existingFile, false, "", "",
                null, null, null, null, null, false
        );

        assertThrows(IllegalArgumentException.class, () -> model.convert(config));
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
        model.setTask(Collections.emptyList());

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
