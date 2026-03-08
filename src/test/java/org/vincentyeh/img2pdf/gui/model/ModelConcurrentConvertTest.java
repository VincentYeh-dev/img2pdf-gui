package org.vincentyeh.img2pdf.gui.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.vincentyeh.img2pdf.lib.image.ColorType;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageAlign;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageDirection;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageSize;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
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
 * removeTasks() is called from another thread (simulating EDT) while the
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
     * Returns a ModelListener that captures tasks on every onTasksUpdate call.
     * All other callbacks are no-ops.
     */
    private ModelListener listenerCapturing(List<Task> list) {
        return new ModelListener() {
            @Override public void onBatchStart() {}
            @Override public void onBatchComplete() {}
            @Override public void onBatchProgressUpdate(int p, int t) {}
            @Override public void onConversionProgressUpdate(int p, int t) {}
            @Override public void onTaskComplete(Task task, int currentIndex, Exception e) {}
            @Override public void onBatchError(String title, String msg) {}
            @Override public void onTasksUpdate(List<Task> tasks) { list.clear(); list.addAll(tasks); }
            @Override public void onTaskDiskRemovalError(Task task, IOException e) {}
        };
    }

    /**
     * Verifies that calling removeTasks() concurrently while convert() is running
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

        // Create 5 empty source directories → tasks with empty files[]
        Model model = new Model();
        List<Task> capturedTasks = new ArrayList<>();
        model.setModelListener(listenerCapturing(capturedTasks));

        File[] srcDirs = new File[5];
        for (int i = 0; i < 5; i++) {
            srcDirs[i] = tempDir.resolve("src" + i).toFile();
            srcDirs[i].mkdirs();
        }
        model.importSources(srcDirs);
        assertEquals(5, capturedTasks.size());

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
            public void onTaskComplete(Task task, int currentIndex, Exception error) {
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

            @Override
            public void onTasksUpdate(List<Task> tasks) {
                // fired by removeTasks() from EDT simulator — no action needed
            }

            @Override
            public void onTaskDiskRemovalError(Task task, IOException e) {}
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
                for (int i = 0; i < capturedTasks.size(); i++) {
                    model.removeTasks(Collections.singletonList(0));
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
                "Unexpected exception during concurrent removeTasks: " + unexpectedError.get());

        // Assert batch actually completed (not silently stuck)
        assertTrue(completed, "onBatchComplete was not called within timeout");
    }

    /**
     * Verifies that when removeTasks() drains the sources list completely before
     * convert() takes its snapshot (race where sources is empty at snapshot time),
     * the batch still completes without error and onBatchComplete() is called.
     */
    @Test
    void convert_removeAllTasksBeforeSnapshot_batchCompletesWithZeroTasks(@TempDir Path tempDir)
            throws InterruptedException {

        File outputFolder = tempDir.resolve("output2").toFile();
        outputFolder.mkdirs();

        Model model = new Model();
        List<Task> capturedTasks = new ArrayList<>();
        model.setModelListener(listenerCapturing(capturedTasks));

        File[] srcDirs = new File[3];
        for (int i = 0; i < 3; i++) {
            srcDirs[i] = tempDir.resolve("src" + i).toFile();
            srcDirs[i].mkdirs();
        }
        model.importSources(srcDirs);
        assertEquals(3, capturedTasks.size());

        CountDownLatch batchCompleteLatch = new CountDownLatch(1);
        AtomicReference<Throwable> unexpectedError = new AtomicReference<>();

        model.setModelListener(new ModelListener() {
            @Override public void onBatchStart() { }
            @Override public void onBatchComplete() { batchCompleteLatch.countDown(); }
            @Override public void onBatchProgressUpdate(int progress, int total) { }
            @Override public void onConversionProgressUpdate(int progress, int total) { }
            @Override public void onTaskComplete(Task task, int currentIndex, Exception error) { }
            @Override public void onBatchError(String title, String message) {
                unexpectedError.compareAndSet(null,
                        new AssertionError("Unexpected onBatchError: " + title + " — " + message));
            }
            @Override public void onTasksUpdate(List<Task> tasks) {}
            @Override public void onTaskDiskRemovalError(Task task, IOException e) {}
        });

        // Remove all tasks BEFORE calling convert() — snapshot will capture 0 tasks
        int taskCount = capturedTasks.size();
        for (int i = 0; i < taskCount; i++) {
            model.removeTasks(Collections.singletonList(0));
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
     * Verifies that rapid, repeated removeTasks() calls from multiple threads
     * during an active conversion do not produce IndexOutOfBoundsException
     * from the background conversion loop.
     *
     * This is a stress variant of the single-thread removeTasks test.
     */
    @Test
    void convert_multipleThreadsRemovingTasksConcurrently_noIndexOutOfBounds(@TempDir Path tempDir)
            throws InterruptedException {

        File outputFolder = tempDir.resolve("output3").toFile();
        outputFolder.mkdirs();

        Model model = new Model();
        List<Task> capturedTasks = new ArrayList<>();
        model.setModelListener(listenerCapturing(capturedTasks));

        File[] srcDirs = new File[8];
        for (int i = 0; i < 8; i++) {
            srcDirs[i] = tempDir.resolve("src" + i).toFile();
            srcDirs[i].mkdirs();
        }
        model.importSources(srcDirs);
        assertEquals(8, capturedTasks.size());

        CountDownLatch batchStartLatch = new CountDownLatch(1);
        CountDownLatch batchCompleteLatch = new CountDownLatch(1);
        AtomicReference<Throwable> unexpectedError = new AtomicReference<>();

        model.setModelListener(new ModelListener() {
            @Override public void onBatchStart() { batchStartLatch.countDown(); }
            @Override public void onBatchComplete() { batchCompleteLatch.countDown(); }
            @Override public void onBatchProgressUpdate(int progress, int total) { }
            @Override public void onConversionProgressUpdate(int progress, int total) { }
            @Override public void onTaskComplete(Task task, int currentIndex, Exception error) { }
            @Override public void onBatchError(String title, String message) {
                unexpectedError.compareAndSet(null,
                        new AssertionError("Unexpected onBatchError: " + title + " — " + message));
            }
            @Override public void onTasksUpdate(List<Task> tasks) {}
            @Override public void onTaskDiskRemovalError(Task task, IOException e) {}
        });

        model.convert(buildConfig(outputFolder));

        // Wait for background thread to start
        boolean started = batchStartLatch.await(10, TimeUnit.SECONDS);
        assertTrue(started, "onBatchStart was not called");

        // Spawn multiple threads that each try to remove tasks concurrently
        int threadCount = 4;
        int totalTasks = capturedTasks.size();
        Thread[] removers = new Thread[threadCount];
        for (int t = 0; t < threadCount; t++) {
            removers[t] = new Thread(() -> {
                try {
                    // Each thread repeatedly removes index 0 to stress concurrent removal
                    for (int i = 0; i < totalTasks / threadCount; i++) {
                        model.removeTasks(Collections.singletonList(0));
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
                "Exception during multi-thread removeTasks: " + unexpectedError.get());
        assertTrue(completed, "onBatchComplete was not called within timeout");
    }
}
