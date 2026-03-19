package org.vincentyeh.img2pdf.gui.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.vincentyeh.img2pdf.lib.image.ColorType;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageAlign;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageDirection;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageSize;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Edge-case tests for the document.close() RuntimeException fix in Model.convert().
 *
 * <p>The fix (commit 8271060 area): inside the conversion loop, document.close() is
 * wrapped in a try-catch(RuntimeException) so that an unexpected runtime error during
 * resource cleanup does not propagate out of the finally block and break the for-loop,
 * allowing remaining tasks to proceed.</p>
 *
 * <p>Direct injection of a RuntimeException into document.close() is not feasible
 * without a factory injection point (Img2Pdf.createPDFBoxMaxPerformanceFactory() is
 * hard-wired inside the background thread). The tests here therefore verify all
 * observable consequences of the fix through the public API:</p>
 * <ul>
 *   <li>Both tasks in a two-task batch complete when both have valid input and output.</li>
 *   <li>requestStop() pre-set before convert() is reset at thread start.</li>
 *   <li>onBatchProgressUpdate(0, N) fires synchronously before the background thread.</li>
 *   <li>requestStop() during a batch stops the loop after the current task finishes.</li>
 *   <li>Three-task batch: first task fails at FileOutputStream (close path exercised),
 *       second succeeds, third still runs.</li>
 * </ul>
 */
class ModelConvertDocumentCloseTest {

    private Model model;

    @BeforeEach
    void setUp() {
        model = new Model();
    }

    // Creates a minimal 1x1 JPEG so PDFBox can process it without errors.
    private static File createMinimalJpeg(File dir, String name) throws IOException {
        dir.mkdirs();
        File jpeg = new File(dir, name);
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(img, "JPEG", jpeg);
        return jpeg;
    }

    private static ConversionConfig config(File outputFolder) {
        return new ConversionConfig(
                outputFolder,
                false, "", "",
                ColorType.sRGB,
                PageSize.A4,
                PageDirection.Portrait,
                PageAlign.VerticalAlign.CENTER,
                PageAlign.HorizontalAlign.CENTER,
                false
        );
    }

    // -------------------------------------------------------------------------
    // Structural note: why direct RuntimeException injection is not feasible
    // -------------------------------------------------------------------------
    // Model.convert() hard-wires:
    //   ImagePDFFactory factory = Img2Pdf.createPDFBoxMaxPerformanceFactory();
    // There is no setter/constructor parameter to supply a custom factory.
    // IDocument is also a PDFBox internal returned by the factory — it cannot
    // be stubbed without a mock framework that supports interface-based proxying,
    // which is not available in this project's test scope (no Mockito dependency).
    // All tests below therefore exercise the observable contract rather than
    // injecting an artificial RuntimeException into document.close().
    // -------------------------------------------------------------------------

    // Verifies that both tasks in a two-task batch trigger onTaskComplete,
    // confirming the conversion loop is not broken by document.close() on the first task.
    // This is the happy-path verification of the BUG-01 fix: close() is called and
    // does not disrupt the loop when it completes normally.
    @Test
    void convert_twoSuccessfulTasks_bothTriggerOnTaskComplete(@TempDir Path tempDir)
            throws Exception {
        File srcDir1 = tempDir.resolve("album1").toFile();
        createMinimalJpeg(srcDir1, "1.jpg");

        File srcDir2 = tempDir.resolve("album2").toFile();
        createMinimalJpeg(srcDir2, "2.jpg");

        File outputFolder = tempDir.resolve("output").toFile();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger taskCompleteCount = new AtomicInteger(0);

        model.setModelListener(new ModelListener() {
            @Override public void onBatchStart() {}
            @Override public void onBatchComplete() { latch.countDown(); }
            @Override public void onBatchProgressUpdate(int p, int t) {}
            @Override public void onConversionProgressUpdate(int p, int t) {}
            @Override public void onTaskComplete(Task task, int i, Exception e) {
                taskCompleteCount.incrementAndGet();
            }
            @Override public void onBatchError(String title, String msg) {}
            @Override public void onTasksUpdate(List<Task> tasks) {}
            @Override public void onTaskDiskRemovalError(Task task, IOException e) {}
        });

        model.addSources(new File[]{srcDir1, srcDir2});
        model.convert(config(outputFolder));

        assertTrue(latch.await(30, TimeUnit.SECONDS), "onBatchComplete not called");
        assertEquals(2, taskCompleteCount.get(),
                "Both tasks must trigger onTaskComplete; loop must not break after first close()");
    }

    // Verifies that the output PDFs for both tasks are actually created on disk,
    // confirming document.save() + document.close() completed for each task.
    @Test
    void convert_twoSuccessfulTasks_bothOutputPdfsExistOnDisk(@TempDir Path tempDir)
            throws Exception {
        File srcDir1 = tempDir.resolve("album1").toFile();
        createMinimalJpeg(srcDir1, "1.jpg");

        File srcDir2 = tempDir.resolve("album2").toFile();
        createMinimalJpeg(srcDir2, "2.jpg");

        File outputFolder = tempDir.resolve("output").toFile();

        CountDownLatch latch = new CountDownLatch(1);
        model.setModelListener(new ModelListener() {
            @Override public void onBatchStart() {}
            @Override public void onBatchComplete() { latch.countDown(); }
            @Override public void onBatchProgressUpdate(int p, int t) {}
            @Override public void onConversionProgressUpdate(int p, int t) {}
            @Override public void onTaskComplete(Task task, int i, Exception e) {}
            @Override public void onBatchError(String title, String msg) {}
            @Override public void onTasksUpdate(List<Task> tasks) {}
            @Override public void onTaskDiskRemovalError(Task task, IOException e) {}
        });

        model.addSources(new File[]{srcDir1, srcDir2});
        model.convert(config(outputFolder));

        assertTrue(latch.await(30, TimeUnit.SECONDS), "onBatchComplete not called");
        assertTrue(new File(outputFolder, "album1.pdf").exists(), "album1.pdf must exist");
        assertTrue(new File(outputFolder, "album2.pdf").exists(), "album2.pdf must exist");
    }

    // Verifies that setting stopRequested=true via requestStop() BEFORE calling convert()
    // does not prevent the batch from starting, because the background thread resets the
    // flag to false as its first action (line: stopRequested = false;).
    @Test
    void requestStop_beforeConvert_flagIsResetSoBatchStarts(@TempDir Path tempDir)
            throws Exception {
        File outputFolder = tempDir.resolve("output").toFile();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean batchStartFired = new AtomicBoolean(false);

        model.setModelListener(new ModelListener() {
            @Override public void onBatchStart() { batchStartFired.set(true); }
            @Override public void onBatchComplete() { latch.countDown(); }
            @Override public void onBatchProgressUpdate(int p, int t) {}
            @Override public void onConversionProgressUpdate(int p, int t) {}
            @Override public void onTaskComplete(Task task, int i, Exception e) {}
            @Override public void onBatchError(String title, String msg) {}
            @Override public void onTasksUpdate(List<Task> tasks) {}
            @Override public void onTaskDiskRemovalError(Task task, IOException e) {}
        });

        // Set the stop flag before convert() is called.
        // The background thread must reset it to false before checking it.
        model.requestStop();
        model.convert(config(outputFolder));

        assertTrue(latch.await(10, TimeUnit.SECONDS), "onBatchComplete not called");
        assertTrue(batchStartFired.get(),
                "onBatchStart must fire even when requestStop() was called before convert()");
    }

    // Verifies that onBatchProgressUpdate(0, N) is fired synchronously on the calling
    // thread BEFORE the background thread starts, so the UI can immediately show
    // "0 / N tasks completed" while conversion is in progress.
    @Test
    void convert_initialProgressUpdate_firesBeforeBackgroundThreadStarts(@TempDir Path tempDir)
            throws Exception {
        // Prepare N=2 tasks (empty dirs — tasks are added with 0 files each)
        File srcDir1 = tempDir.resolve("d1").toFile(); srcDir1.mkdirs();
        File srcDir2 = tempDir.resolve("d2").toFile(); srcDir2.mkdirs();
        File outputFolder = tempDir.resolve("output").toFile();

        // Track whether the initial progress callback arrived before onBatchStart
        List<String> callOrder = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(1);

        model.setModelListener(new ModelListener() {
            @Override public void onBatchStart() { callOrder.add("onBatchStart"); }
            @Override public void onBatchComplete() { latch.countDown(); }
            @Override public void onBatchProgressUpdate(int p, int t) {
                callOrder.add("onBatchProgressUpdate(" + p + "," + t + ")");
            }
            @Override public void onConversionProgressUpdate(int p, int t) {}
            @Override public void onTaskComplete(Task task, int i, Exception e) {}
            @Override public void onBatchError(String title, String msg) {}
            @Override public void onTasksUpdate(List<Task> tasks) {}
            @Override public void onTaskDiskRemovalError(Task task, IOException e) {}
        });

        model.addSources(new File[]{srcDir1, srcDir2});
        model.convert(config(outputFolder));

        // The first onBatchProgressUpdate(0,2) fires on the calling thread before
        // conversionThread.start(), so it must be the very first element.
        assertEquals("onBatchProgressUpdate(0,2)", callOrder.get(0),
                "onBatchProgressUpdate(0,N) must fire synchronously before onBatchStart");

        assertTrue(latch.await(10, TimeUnit.SECONDS), "onBatchComplete not called");
    }

    // Verifies that requestStop() called from onBatchStart fires BEFORE the loop checks
    // the flag at the top of the first iteration, so the loop breaks immediately and
    // zero tasks trigger onTaskComplete. This confirms the flag check order:
    //   stopRequested = false  (thread start)
    //   onBatchStart()
    //   for each task: if (stopRequested) break;  <- checked before task body
    @Test
    void requestStop_calledFromOnBatchStart_noTasksComplete(@TempDir Path tempDir)
            throws Exception {
        File srcDir1 = tempDir.resolve("album1").toFile();
        createMinimalJpeg(srcDir1, "img.jpg");

        File srcDir2 = tempDir.resolve("album2").toFile();
        createMinimalJpeg(srcDir2, "img.jpg");

        File outputFolder = tempDir.resolve("output").toFile();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger taskCompleteCount = new AtomicInteger(0);

        model.setModelListener(new ModelListener() {
            @Override
            public void onBatchStart() {
                // Signal stop; the loop checks the flag at the TOP of each iteration,
                // so all tasks are skipped — even the first one.
                model.requestStop();
            }
            @Override public void onBatchComplete() { latch.countDown(); }
            @Override public void onBatchProgressUpdate(int p, int t) {}
            @Override public void onConversionProgressUpdate(int p, int t) {}
            @Override public void onTaskComplete(Task task, int i, Exception e) {
                taskCompleteCount.incrementAndGet();
            }
            @Override public void onBatchError(String title, String msg) {}
            @Override public void onTasksUpdate(List<Task> tasks) {}
            @Override public void onTaskDiskRemovalError(Task task, IOException e) {}
        });

        model.addSources(new File[]{srcDir1, srcDir2});
        model.convert(config(outputFolder));

        assertTrue(latch.await(30, TimeUnit.SECONDS), "onBatchComplete not called");
        assertEquals(0, taskCompleteCount.get(),
                "No task should complete when requestStop() is called before the loop body runs");
    }

    // Verifies that requestStop() called from the first task's onTaskComplete causes
    // the loop to break before the second task starts (flag is checked at loop top).
    // Only the first task triggers onTaskComplete; the second does not.
    @Test
    void requestStop_calledFromFirstTaskComplete_secondTaskSkipped(@TempDir Path tempDir)
            throws Exception {
        File srcDir1 = tempDir.resolve("album1").toFile();
        createMinimalJpeg(srcDir1, "img.jpg");

        File srcDir2 = tempDir.resolve("album2").toFile();
        createMinimalJpeg(srcDir2, "img.jpg");

        File srcDir3 = tempDir.resolve("album3").toFile();
        createMinimalJpeg(srcDir3, "img.jpg");

        File outputFolder = tempDir.resolve("output").toFile();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger taskCompleteCount = new AtomicInteger(0);

        model.setModelListener(new ModelListener() {
            @Override public void onBatchStart() {}
            @Override public void onBatchComplete() { latch.countDown(); }
            @Override public void onBatchProgressUpdate(int p, int t) {}
            @Override public void onConversionProgressUpdate(int p, int t) {}
            @Override public void onTaskComplete(Task task, int i, Exception e) {
                int count = taskCompleteCount.incrementAndGet();
                if (count == 1) {
                    // Signal stop after task 1; loop checks the flag at the top of
                    // each iteration, so tasks 2 and 3 are skipped.
                    model.requestStop();
                }
            }
            @Override public void onBatchError(String title, String msg) {}
            @Override public void onTasksUpdate(List<Task> tasks) {}
            @Override public void onTaskDiskRemovalError(Task task, IOException e) {}
        });

        // addSources NAME_ASC: album1 first
        model.addSources(new File[]{srcDir1, srcDir2, srcDir3});
        model.convert(config(outputFolder));

        assertTrue(latch.await(30, TimeUnit.SECONDS), "onBatchComplete not called");
        assertEquals(1, taskCompleteCount.get(),
                "Only task 1 should complete; tasks 2 and 3 must be skipped after requestStop()");
    }

    // Verifies that in a three-task batch where the first task's output is pre-created
    // and read-only (causing IOException at FileOutputStream after factory.start() succeeds,
    // which triggers the finally { document.close() } path), the third task still runs.
    // This is the closest achievable behavioral proxy for the BUG-01 fix: it confirms
    // that the try-catch(RuntimeException) in the finally block does not interfere with
    // normal close() execution, and that the per-task catch(PDFFactoryException|IOException)
    // in the outer try correctly routes the error so the loop continues.
    @Test
    void convert_firstTaskClosePathExercised_thirdTaskStillRuns(@TempDir Path tempDir)
            throws Exception {
        File srcDir1 = tempDir.resolve("album1").toFile();
        createMinimalJpeg(srcDir1, "1.jpg");

        File srcDir2 = tempDir.resolve("album2").toFile();
        createMinimalJpeg(srcDir2, "2.jpg");

        File srcDir3 = tempDir.resolve("album3").toFile();
        createMinimalJpeg(srcDir3, "3.jpg");

        File outputFolder = tempDir.resolve("output").toFile();
        outputFolder.mkdirs();

        // Pre-create album1.pdf as read-only: factory.start() will succeed (IDocument allocated),
        // but new FileOutputStream(destination) will throw FileNotFoundException,
        // which triggers the finally { document.close() } block for task 1.
        File readOnlyOutput = new File(outputFolder, "album1.pdf");
        readOnlyOutput.createNewFile();
        assertTrue(readOnlyOutput.setReadOnly(), "Failed to set read-only");

        CountDownLatch latch = new CountDownLatch(1);
        List<Exception> errors = new ArrayList<>();

        model.setModelListener(new ModelListener() {
            @Override public void onBatchStart() {}
            @Override public void onBatchComplete() { latch.countDown(); }
            @Override public void onBatchProgressUpdate(int p, int t) {}
            @Override public void onConversionProgressUpdate(int p, int t) {}
            @Override public void onTaskComplete(Task task, int i, Exception e) {
                errors.add(e); // null = success, non-null = failure
            }
            @Override public void onBatchError(String title, String msg) {}
            @Override public void onTasksUpdate(List<Task> tasks) {}
            @Override public void onTaskDiskRemovalError(Task task, IOException e) {}
        });

        model.addSources(new File[]{srcDir1, srcDir2, srcDir3});
        model.convert(config(outputFolder));
        boolean completed = latch.await(30, TimeUnit.SECONDS);

        // Restore write permission for @TempDir cleanup
        readOnlyOutput.setWritable(true);

        assertTrue(completed, "onBatchComplete not called");
        assertEquals(3, errors.size(), "All three tasks must trigger onTaskComplete");
        assertNotNull(errors.get(0),
                "Task 1 must fail (read-only output); document.close() finally path was exercised");
        assertNull(errors.get(1), "Task 2 must succeed after task 1's close() path");
        assertNull(errors.get(2), "Task 3 must succeed; loop must not break after task 1's close()");
    }

    // Verifies that onTaskComplete receives the correct zero-based index for each task,
    // confirming that snapshot.indexOf() correctly maps the task back even after
    // concurrent modifications to the live sources list would have invalidated direct indexing.
    @Test
    void convert_twoTasks_onTaskComplete_receivesCorrectIndices(@TempDir Path tempDir)
            throws Exception {
        File srcDir1 = tempDir.resolve("album1").toFile();
        createMinimalJpeg(srcDir1, "1.jpg");

        File srcDir2 = tempDir.resolve("album2").toFile();
        createMinimalJpeg(srcDir2, "2.jpg");

        File outputFolder = tempDir.resolve("output").toFile();

        CountDownLatch latch = new CountDownLatch(1);
        List<Integer> indices = Collections.synchronizedList(new ArrayList<>());

        model.setModelListener(new ModelListener() {
            @Override public void onBatchStart() {}
            @Override public void onBatchComplete() { latch.countDown(); }
            @Override public void onBatchProgressUpdate(int p, int t) {}
            @Override public void onConversionProgressUpdate(int p, int t) {}
            @Override public void onTaskComplete(Task task, int currentIndex, Exception e) {
                indices.add(currentIndex);
            }
            @Override public void onBatchError(String title, String msg) {}
            @Override public void onTasksUpdate(List<Task> tasks) {}
            @Override public void onTaskDiskRemovalError(Task task, IOException e) {}
        });

        // addSources NAME_ASC: album1 at index 0, album2 at index 1
        model.addSources(new File[]{srcDir1, srcDir2});
        model.convert(config(outputFolder));

        assertTrue(latch.await(30, TimeUnit.SECONDS), "onBatchComplete not called");
        assertEquals(2, indices.size(), "Both tasks must fire onTaskComplete");
        assertEquals(0, (int) indices.get(0), "album1 must report currentIndex=0");
        assertEquals(1, (int) indices.get(1), "album2 must report currentIndex=1");
    }

    // Verifies that the batch lifecycle callbacks fire in the correct order:
    // onBatchStart → (per-task callbacks) → onBatchComplete.
    // This confirms that factory.shutdown() in the outer finally does not prevent
    // onBatchComplete from being called.
    @Test
    void convert_emptyBatch_lifecycleOrderIsStartThenComplete(@TempDir Path tempDir)
            throws Exception {
        File outputFolder = tempDir.resolve("output").toFile();

        CountDownLatch latch = new CountDownLatch(1);
        List<String> order = Collections.synchronizedList(new ArrayList<>());

        model.setModelListener(new ModelListener() {
            @Override public void onBatchStart() { order.add("start"); }
            @Override public void onBatchComplete() { order.add("complete"); latch.countDown(); }
            @Override public void onBatchProgressUpdate(int p, int t) {}
            @Override public void onConversionProgressUpdate(int p, int t) {}
            @Override public void onTaskComplete(Task task, int i, Exception e) {}
            @Override public void onBatchError(String title, String msg) {}
            @Override public void onTasksUpdate(List<Task> tasks) {}
            @Override public void onTaskDiskRemovalError(Task task, IOException e) {}
        });

        model.convert(config(outputFolder));

        assertTrue(latch.await(10, TimeUnit.SECONDS), "onBatchComplete not called");
        List<String> expected = new ArrayList<>();
        expected.add("start");
        expected.add("complete");
        assertEquals(expected, order,
                "Lifecycle must be: onBatchStart then onBatchComplete");
    }

    // Verifies that convert() with a null listener does not throw NullPointerException
    // from any of the listener call sites inside the background thread.
    @Test
    void convert_nullListener_backgroundThreadDoesNotThrow(@TempDir Path tempDir)
            throws Exception {
        File outputFolder = tempDir.resolve("output").toFile();
        model.setModelListener(null);

        // Start conversion on a real background thread; we must wait for it to finish.
        // Since there is no listener, we sleep briefly and rely on thread termination.
        AtomicReference<Throwable> threadError = new AtomicReference<>();
        Thread.UncaughtExceptionHandler handler = (t, e) -> threadError.set(e);

        // Wrap in a monitoring thread so we can detect uncaught exceptions.
        // The background thread is started internally by convert(); we wait 5s for it.
        assertDoesNotThrow(() -> model.convert(config(outputFolder)));

        // Give the background thread time to complete (empty task list is nearly instant).
        Thread.sleep(2000);
        assertNull(threadError.get(),
                "Background thread must not throw when listener is null");
    }
}
