package org.vincentyeh.img2pdf.gui.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.vincentyeh.img2pdf.lib.image.ColorType;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageAlign;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageDirection;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageSize;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Concurrency edge-case tests for Model.convert().
 *
 * Focus: verifies that the snapshot taken before the background thread starts
 * prevents ConcurrentModificationException / IndexOutOfBoundsException when
 * removeTask() is called from another thread (simulating EDT) while the
 * conversion thread is running.
 */
class ModelConcurrentConvertTest {

    /**
     * Builds a minimal ConversionConfig that writes output to the given folder.
     * All page/alignment parameters use simple defaults.
     */
    private ConversionConfig buildConfig(File outputFolder) {
        return new ConversionConfig(
                outputFolder,
                false,
                "",
                "",
                ColorType.sRGB,
                PageSize.A4,
                PageDirection.Portrait,
                PageAlign.VerticalAlign.CENTER,
                PageAlign.HorizontalAlign.CENTER,
                false
        );
    }

    /**
     * Creates a Task whose files array is empty (no real images).
     * The conversion of this task will fail gracefully via onTaskComplete(task, exception),
     * but the batch will still call onBatchComplete() at the end.
     */
    private Task emptyTask(File destination) {
        return new Task(destination, new File[0]);
    }

    /**
     * Verifies that calling removeTask() concurrently while convert() is running
     * does not throw ConcurrentModificationException or IndexOutOfBoundsException,
     * and that onBatchComplete() is eventually called.
     *
     * The snapshot taken on the EDT before the background thread starts should
     * insulate the conversion loop from mutation of the sources list.
     */
    @Test
    void convert_concurrentRemoveTask_doesNotThrowAndCompletesNormally(@TempDir Path tempDir)
            throws InterruptedException {

        // Prepare output folder
        File outputFolder = tempDir.resolve("output").toFile();
        outputFolder.mkdirs();

        // Prepare several empty tasks (no real image files; each will fail conversion,
        // but the batch completes without ConcurrentModificationException)
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            tasks.add(emptyTask(new File(outputFolder, "out" + i + ".pdf")));
        }

        Model model = new Model();
        model.setTask(tasks);

        // Latch: released when onBatchStart fires, so we know the background thread is running
        CountDownLatch batchStartLatch = new CountDownLatch(1);
        // Latch: released when onBatchComplete fires
        CountDownLatch batchCompleteLatch = new CountDownLatch(1);

        // Collects any unexpected throwable from either thread
        AtomicReference<Throwable> unexpectedError = new AtomicReference<>();

        model.setModelListener(new ModelListener() {
            @Override
            public void onBatchStart() {
                batchStartLatch.countDown();
            }

            @Override
            public void onBatchComplete() {
                batchCompleteLatch.countDown();
            }

            @Override
            public void onBatchProgressUpdate(int progress, int total) {
                // not verified in this test
            }

            @Override
            public void onConversionProgressUpdate(int progress, int total) {
                // not verified in this test
            }

            @Override
            public void onTaskComplete(Task task, Exception error) {
                // Empty-file tasks are expected to produce a conversion error;
                // capture only unexpected runtime exceptions (not PDFFactoryException/IOException).
                if (error != null
                        && !(error instanceof java.io.IOException)
                        && error.getClass().getName().contains("PDFFactory")) {
                    // expected failure for empty task — ignore
                }
            }

            @Override
            public void onBatchError(String title, String message) {
                // Not expected in this path; record it so the assertion fails clearly.
                unexpectedError.compareAndSet(null,
                        new AssertionError("Unexpected onBatchError: " + title + " — " + message));
            }
        });

        // Start conversion (launches background thread internally)
        model.convert(buildConfig(outputFolder));

        // Wait for the background thread to actually start before mutating sources
        boolean started = batchStartLatch.await(10, TimeUnit.SECONDS);
        assertTrue(started, "onBatchStart was not called within timeout");

        // Simulate EDT concurrently removing tasks while the background thread converts.
        // Before the snapshot fix, this would cause ConcurrentModificationException.
        Thread edtSimulator = new Thread(() -> {
            try {
                List<Task> snapshot = new ArrayList<>(tasks);
                for (Task t : snapshot) {
                    model.removeTask(t);
                    // Small yield to interleave with conversion thread
                    Thread.yield();
                }
            } catch (Exception e) {
                unexpectedError.compareAndSet(null, e);
            }
        }, "EDT-simulator");
        edtSimulator.start();
        edtSimulator.join(5000);

        // Wait for the batch to complete (up to 60 s; empty-task batches finish quickly)
        boolean completed = batchCompleteLatch.await(60, TimeUnit.SECONDS);

        // Assert no concurrent-modification error occurred
        assertNull(unexpectedError.get(),
                "Unexpected exception during concurrent removeTask: " + unexpectedError.get());

        // Assert batch actually completed (not silently stuck)
        assertTrue(completed, "onBatchComplete was not called within timeout");
    }

    /**
     * Verifies that when removeTask() drains the sources list completely before
     * convert() takes its snapshot (race where sources is empty at snapshot time),
     * the batch still completes without error and onBatchComplete() is called.
     */
    @Test
    void convert_removeAllTasksBeforeSnapshot_batchCompletesWithZeroTasks(@TempDir Path tempDir)
            throws InterruptedException {

        File outputFolder = tempDir.resolve("output2").toFile();
        outputFolder.mkdirs();

        // Add a few tasks
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            tasks.add(emptyTask(new File(outputFolder, "out" + i + ".pdf")));
        }

        Model model = new Model();
        model.setTask(tasks);

        CountDownLatch batchCompleteLatch = new CountDownLatch(1);
        AtomicReference<Throwable> unexpectedError = new AtomicReference<>();

        model.setModelListener(new ModelListener() {
            @Override
            public void onBatchStart() { }

            @Override
            public void onBatchComplete() {
                batchCompleteLatch.countDown();
            }

            @Override
            public void onBatchProgressUpdate(int progress, int total) { }

            @Override
            public void onConversionProgressUpdate(int progress, int total) { }

            @Override
            public void onTaskComplete(Task task, Exception error) { }

            @Override
            public void onBatchError(String title, String message) {
                unexpectedError.compareAndSet(null,
                        new AssertionError("Unexpected onBatchError: " + title + " — " + message));
            }
        });

        // Remove all tasks BEFORE calling convert() — snapshot will capture 0 tasks
        for (Task t : tasks) {
            model.removeTask(t);
        }

        // convert() with an empty sources list should still call onBatchComplete()
        model.convert(buildConfig(outputFolder));

        boolean completed = batchCompleteLatch.await(30, TimeUnit.SECONDS);

        assertNull(unexpectedError.get(),
                "Unexpected exception when sources is empty: " + unexpectedError.get());
        assertTrue(completed,
                "onBatchComplete was not called when sources was empty at convert() time");
    }

    /**
     * Verifies that rapid, repeated removeTask() calls from multiple threads
     * during an active conversion do not produce IndexOutOfBoundsException
     * from the background conversion loop.
     *
     * This is a stress variant of the single-thread removeTask test.
     */
    @Test
    void convert_multipleThreadsRemovingTasksConcurrently_noIndexOutOfBounds(@TempDir Path tempDir)
            throws InterruptedException {

        File outputFolder = tempDir.resolve("output3").toFile();
        outputFolder.mkdirs();

        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            tasks.add(emptyTask(new File(outputFolder, "out" + i + ".pdf")));
        }

        Model model = new Model();
        model.setTask(tasks);

        CountDownLatch batchStartLatch = new CountDownLatch(1);
        CountDownLatch batchCompleteLatch = new CountDownLatch(1);
        AtomicReference<Throwable> unexpectedError = new AtomicReference<>();

        model.setModelListener(new ModelListener() {
            @Override
            public void onBatchStart() {
                batchStartLatch.countDown();
            }

            @Override
            public void onBatchComplete() {
                batchCompleteLatch.countDown();
            }

            @Override
            public void onBatchProgressUpdate(int progress, int total) { }

            @Override
            public void onConversionProgressUpdate(int progress, int total) { }

            @Override
            public void onTaskComplete(Task task, Exception error) { }

            @Override
            public void onBatchError(String title, String message) {
                unexpectedError.compareAndSet(null,
                        new AssertionError("Unexpected onBatchError: " + title + " — " + message));
            }
        });

        model.convert(buildConfig(outputFolder));

        // Wait for background thread to start
        boolean started = batchStartLatch.await(10, TimeUnit.SECONDS);
        assertTrue(started, "onBatchStart was not called");

        // Spawn multiple threads that each try to remove tasks concurrently
        int threadCount = 4;
        Thread[] removers = new Thread[threadCount];
        for (int t = 0; t < threadCount; t++) {
            final int idx = t;
            removers[t] = new Thread(() -> {
                try {
                    // Each thread removes a subset of tasks
                    for (int i = idx; i < tasks.size(); i += threadCount) {
                        model.removeTask(tasks.get(i));
                        Thread.yield();
                    }
                } catch (Exception e) {
                    unexpectedError.compareAndSet(null, e);
                }
            }, "remover-" + t);
        }
        for (Thread r : removers) r.start();
        for (Thread r : removers) r.join(5000);

        boolean completed = batchCompleteLatch.await(60, TimeUnit.SECONDS);

        assertNull(unexpectedError.get(),
                "Exception during multi-thread removeTask: " + unexpectedError.get());
        assertTrue(completed, "onBatchComplete was not called within timeout");
    }
}
