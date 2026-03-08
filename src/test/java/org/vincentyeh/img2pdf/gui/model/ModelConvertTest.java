package org.vincentyeh.img2pdf.gui.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.vincentyeh.img2pdf.lib.image.ColorType;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageAlign;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageDirection;
import org.vincentyeh.img2pdf.lib.pdf.parameter.PageSize;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ModelConvertTest {

    private Model model;

    @BeforeEach
    void setUp() {
        model = new Model();
    }

    // ===== 輔助物件 =====

    private static class FakeModelListener implements ModelListener {
        final List<String> calls = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(0);
        String batchErrorTitle = null;

        @Override
        public void onBatchStart() {
            calls.add("onBatchStart");
        }

        @Override
        public void onBatchComplete() {
            calls.add("onBatchComplete");
            latch.countDown();
        }

        @Override
        public void onBatchProgressUpdate(int progress, int total) {
            calls.add("onBatchProgressUpdate(" + progress + "," + total + ")");
        }

        @Override
        public void onConversionProgressUpdate(int progress, int total) {
        }

        @Override
        public void onTaskComplete(Task task, int currentIndex, Exception error) {
            calls.add("onTaskComplete");
        }

        @Override
        public void onBatchError(String title, String message) {
            batchErrorTitle = title;
            calls.add("onBatchError");
        }

        @Override
        public void onTasksUpdate(List<Task> tasks) {}

        @Override
        public void onTaskDiskRemovalError(Task task, IOException error) {}
    }

    private static ConversionConfig defaultConfig(File outputFolder) {
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

    // ===== Group A：convert() 同步驗證失敗 =====

    // 輸出路徑是已存在的檔案 → onBatchError("Invalid Output Folder") 且背景執行緒不啟動
    @Test
    void convert_output_is_file_calls_onBatchError(@TempDir Path tempDir) throws IOException {
        File outputFile = tempDir.resolve("output.txt").toFile();
        assertTrue(outputFile.createNewFile());

        FakeModelListener listener = new FakeModelListener();
        model.setModelListener(listener);
        model.convert(defaultConfig(outputFile));

        assertEquals("Invalid Output Folder", listener.batchErrorTitle);
        assertFalse(listener.calls.contains("onBatchStart"));
    }

    // 輸出路徑的父層是檔案，mkdirs() 必然失敗 → onBatchError("Cannot Create Output Folder")
    @Test
    void convert_output_cannot_be_created_calls_onBatchError(@TempDir Path tempDir) throws IOException {
        File blocker = tempDir.resolve("blocker.txt").toFile();
        assertTrue(blocker.createNewFile());
        File outputFolder = new File(blocker, "subfolder");

        FakeModelListener listener = new FakeModelListener();
        model.setModelListener(listener);
        model.convert(defaultConfig(outputFolder));

        assertEquals("Cannot Create Output Folder", listener.batchErrorTitle);
        assertFalse(listener.calls.contains("onBatchStart"));
    }

    // listener 為 null 且輸出路徑是檔案 → 不應拋出 NullPointerException
    @Test
    void convert_null_listener_and_invalid_output_does_not_throw(@TempDir Path tempDir) throws IOException {
        File outputFile = tempDir.resolve("output.txt").toFile();
        assertTrue(outputFile.createNewFile());

        model.setModelListener(null);
        assertDoesNotThrow(() -> model.convert(defaultConfig(outputFile)));
    }

    // ===== Group B：convert() 背景執行緒生命週期 =====

    // 空任務清單 → onBatchStart、onBatchProgressUpdate(0,0)、onBatchComplete 依序觸發
    @Test
    void convert_empty_task_list_fires_full_lifecycle(@TempDir Path tempDir) throws InterruptedException {
        File outputFolder = tempDir.resolve("output").toFile();

        FakeModelListener listener = new FakeModelListener();
        listener.latch = new CountDownLatch(1);
        model.setModelListener(listener);
        model.convert(defaultConfig(outputFolder));

        boolean completed = listener.latch.await(10, TimeUnit.SECONDS);
        assertTrue(completed, "onBatchComplete was not called within timeout");
        assertTrue(listener.calls.contains("onBatchStart"));
        assertTrue(listener.calls.contains("onBatchProgressUpdate(0,0)"));
        assertTrue(listener.calls.contains("onBatchComplete"));
    }

    // ===== Group C：removeTasksFromDisk() 行為測試 =====

    // Empty source file list: task is removed from list without error callbacks.
    @Test
    void removeTasksFromDisk_empty_files_removes_task_without_disk_error(@TempDir Path tempDir) throws Exception {
        File dir = tempDir.resolve("album").toFile();
        dir.mkdirs(); // empty directory → task.files = []

        List<Task> captured = new ArrayList<>();
        boolean[] diskErrorCalled = {false};
        model.setModelListener(new ModelListener() {
            @Override public void onBatchStart() {}
            @Override public void onBatchComplete() {}
            @Override public void onBatchProgressUpdate(int p, int t) {}
            @Override public void onConversionProgressUpdate(int p, int t) {}
            @Override public void onTaskComplete(Task task, int currentIndex, Exception e) {}
            @Override public void onBatchError(String title, String msg) {}
            @Override public void onTasksUpdate(List<Task> tasks) { captured.clear(); captured.addAll(tasks); }
            @Override public void onTaskDiskRemovalError(Task task, IOException e) { diskErrorCalled[0] = true; }
        });
        model.importSources(new File[]{dir});
        assertEquals(1, captured.size());

        model.removeTasksFromDisk(Collections.singletonList(0));

        assertFalse(diskErrorCalled[0], "onTaskDiskRemovalError should not be called for empty files");
        assertTrue(captured.isEmpty(), "task should be removed");
    }

    // Non-existent source folder: fires onTaskDiskRemovalError and keeps the task in the list.
    @Test
    void removeTasksFromDisk_nonexistent_folder_fires_onTaskDiskRemovalError_and_keeps_task(
            @TempDir Path tempDir) throws Exception {
        File dir = tempDir.resolve("album").toFile();
        dir.mkdirs();
        File img = new File(dir, "1.jpg");
        img.createNewFile();

        List<Task> captured = new ArrayList<>();
        boolean[] diskErrorCalled = {false};
        model.setModelListener(new ModelListener() {
            @Override public void onBatchStart() {}
            @Override public void onBatchComplete() {}
            @Override public void onBatchProgressUpdate(int p, int t) {}
            @Override public void onConversionProgressUpdate(int p, int t) {}
            @Override public void onTaskComplete(Task task, int currentIndex, Exception e) {}
            @Override public void onBatchError(String title, String msg) {}
            @Override public void onTasksUpdate(List<Task> tasks) { captured.clear(); captured.addAll(tasks); }
            @Override public void onTaskDiskRemovalError(Task task, IOException e) { diskErrorCalled[0] = true; }
        });
        model.importSources(new File[]{dir});
        assertEquals(1, captured.size());

        // Delete image and directory so that Files.walk() fails with NoSuchFileException
        img.delete();
        dir.delete();

        model.removeTasksFromDisk(Collections.singletonList(0));

        assertTrue(diskErrorCalled[0], "onTaskDiskRemovalError should be called");
        assertEquals(1, captured.size(), "task should still be in list after IOException");
    }

    // Locked file on Windows: fires onTaskDiskRemovalError and keeps the task in the list.
    @Test
    @EnabledOnOs(OS.WINDOWS)
    void removeTasksFromDisk_when_file_is_locked_fires_onTaskDiskRemovalError_and_keeps_task(
            @TempDir Path tempDir) throws IOException {
        File dir = tempDir.resolve("locked_album").toFile();
        dir.mkdirs();
        File lockedFile = new File(dir, "locked.jpg");
        lockedFile.createNewFile();

        List<Task> captured = new ArrayList<>();
        boolean[] diskErrorCalled = {false};
        model.setModelListener(new ModelListener() {
            @Override public void onBatchStart() {}
            @Override public void onBatchComplete() {}
            @Override public void onBatchProgressUpdate(int p, int t) {}
            @Override public void onConversionProgressUpdate(int p, int t) {}
            @Override public void onTaskComplete(Task task, int currentIndex, Exception e) {}
            @Override public void onBatchError(String title, String msg) {}
            @Override public void onTasksUpdate(List<Task> tasks) { captured.clear(); captured.addAll(tasks); }
            @Override public void onTaskDiskRemovalError(Task task, IOException e) { diskErrorCalled[0] = true; }
        });
        model.importSources(new File[]{dir});
        assertEquals(1, captured.size());

        // Hold an exclusive file lock so that Files.delete() on Windows will fail
        try (RandomAccessFile raf = new RandomAccessFile(lockedFile, "rw");
             FileChannel channel = raf.getChannel();
             FileLock lock = channel.lock()) {
            model.removeTasksFromDisk(Collections.singletonList(0));
            assertTrue(diskErrorCalled[0], "onTaskDiskRemovalError should be called when file is locked");
            assertEquals(1, captured.size(), "task should still be in list after IOException");
        }
        // After lock is released, cleanup is handled by @TempDir
    }

    // ===== Group D: BUG-01 / BUG-05 resource-cleanup edge cases =====

    // Creates a minimal 1x1 JPEG image file in the given directory for use as a real image source.
    // PDFBox can successfully process this image, allowing factory.start() to return a real IDocument.
    private static File createMinimalJpeg(File dir, String name) throws IOException {
        dir.mkdirs();
        File jpeg = new File(dir, name);
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(img, "JPEG", jpeg);
        return jpeg;
    }

    // BUG-05: Verifies that after a successful conversion the output PDF file is not locked,
    // i.e. the FileOutputStream opened inside convert() is properly closed by try-with-resources.
    // On Windows an unclosed FileOutputStream holds an exclusive file lock; if the stream were
    // leaked the file could not be re-opened for writing.
    // This test also implicitly exercises the happy-path of BUG-01's try-finally (document.close()
    // is called after a successful save()).
    @Test
    @EnabledOnOs(OS.WINDOWS)
    void convert_after_successful_save_output_pdf_is_not_locked(@TempDir Path tempDir)
            throws Exception {
        File srcDir = tempDir.resolve("photo").toFile();
        createMinimalJpeg(srcDir, "photo.jpg");

        File outputFolder = tempDir.resolve("output").toFile();

        CountDownLatch latch = new CountDownLatch(1);
        FakeModelListener listener = new FakeModelListener();
        listener.latch = latch;
        model.setModelListener(listener);

        model.importSources(new File[]{srcDir});

        model.convert(defaultConfig(outputFolder));
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "onBatchComplete was not called within timeout");

        // BUG-05 regression check: re-open the output file for writing;
        // this would throw on Windows if the FileOutputStream was not closed.
        File outputPdf = new File(outputFolder, "photo.pdf");
        assertTrue(outputPdf.exists(), "Output PDF should have been created");
        assertDoesNotThrow(() -> {
            try (FileOutputStream reopen = new FileOutputStream(outputPdf)) {
                // successfully opened for writing: stream is not locked
            }
        }, "Output PDF must not be locked after successful conversion; FileOutputStream should be closed by try-with-resources");
    }

    // BUG-01 / BUG-05 variant: Verifies that when the output destination file is pre-created
    // and set read-only, causing FileOutputStream creation to throw IOException AFTER
    // factory.start() has already returned a real IDocument, the batch still completes
    // normally — onTaskComplete fires with an IOException error and onBatchComplete fires.
    // With a real PDFBox IDocument allocated but never saved, the try-finally { document.close() }
    // path is exercised; a missing close() would leak PDFBox resources while the observable
    // contract (batch lifecycle callbacks) still validates correct exception propagation.
    @Test
    void convert_when_output_file_is_read_only_task_reports_IOException_and_batch_completes(
            @TempDir Path tempDir) throws Exception {
        // Prepare a real JPEG so factory.start() can return a real IDocument
        File srcDir = tempDir.resolve("photo").toFile();
        createMinimalJpeg(srcDir, "photo.jpg");

        File outputFolder = tempDir.resolve("output").toFile();
        outputFolder.mkdirs();

        // Pre-create the output file and make it read-only so FileOutputStream cannot open it.
        // factory.start() will succeed (real IDocument allocated), but new FileOutputStream(dest)
        // will throw FileNotFoundException (subclass of IOException) — this is the exact scenario
        // that BUG-01's try-finally and BUG-05's try-with-resources protect against.
        File preCreatedOutput = new File(outputFolder, "photo.pdf");
        preCreatedOutput.createNewFile();
        assertTrue(preCreatedOutput.setReadOnly(), "Failed to make output file read-only");

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> capturedError = new AtomicReference<>();
        ModelListener listener = new ModelListener() {
            @Override public void onBatchStart() {}
            @Override public void onBatchComplete() { latch.countDown(); }
            @Override public void onBatchProgressUpdate(int progress, int total) {}
            @Override public void onConversionProgressUpdate(int progress, int total) {}
            @Override public void onTaskComplete(Task task, int currentIndex, Exception error) {
                capturedError.set(error);
            }
            @Override public void onBatchError(String title, String message) {}
            @Override public void onTasksUpdate(List<Task> tasks) {}
            @Override public void onTaskDiskRemovalError(Task task, IOException e) {}
        };
        model.setModelListener(listener);

        model.importSources(new File[]{srcDir});

        model.convert(defaultConfig(outputFolder));
        boolean completed = latch.await(30, TimeUnit.SECONDS);

        // Restore write permission so @TempDir cleanup can delete the file
        preCreatedOutput.setWritable(true);

        assertTrue(completed, "onBatchComplete was not called within timeout");
        assertNotNull(capturedError.get(),
                "onTaskComplete should receive an IOException when the output file is read-only");
        assertInstanceOf(IOException.class, capturedError.get(),
                "The error passed to onTaskComplete should be an IOException (FileNotFoundException)");
    }

    // BUG-01 / BUG-05: Two-task batch where the first task's output is read-only (IOException
    // at FileOutputStream creation AFTER factory.start() succeeds), and the second task has a
    // writable destination. Verifies that:
    // (a) the first task fires onTaskComplete with an IOException error
    // (b) the second task still runs and fires onTaskComplete with null error (success)
    // (c) onBatchComplete is called for the whole batch
    // This confirms the per-task catch block and the BUG-01 try-finally allow the batch to
    // continue even when FileOutputStream creation fails after IDocument allocation.
    @Test
    void convert_second_task_still_runs_when_first_task_output_is_read_only(
            @TempDir Path tempDir) throws Exception {
        File srcDir1 = tempDir.resolve("album1").toFile();
        createMinimalJpeg(srcDir1, "1.jpg");

        File srcDir2 = tempDir.resolve("album2").toFile();
        createMinimalJpeg(srcDir2, "2.jpg");

        File outputFolder = tempDir.resolve("output").toFile();
        outputFolder.mkdirs();

        // Make the first task's output file read-only to force FileOutputStream failure
        File readOnlyOutput = new File(outputFolder, "album1.pdf");
        readOnlyOutput.createNewFile();
        assertTrue(readOnlyOutput.setReadOnly(), "Failed to make output file read-only");

        CountDownLatch latch = new CountDownLatch(1);
        List<Exception> errors = new ArrayList<>();
        ModelListener listener = new ModelListener() {
            @Override public void onBatchStart() {}
            @Override public void onBatchComplete() { latch.countDown(); }
            @Override public void onBatchProgressUpdate(int progress, int total) {}
            @Override public void onConversionProgressUpdate(int progress, int total) {}
            @Override public void onTaskComplete(Task task, int currentIndex, Exception error) {
                errors.add(error); // null means success, non-null means failure
            }
            @Override public void onBatchError(String title, String message) {}
            @Override public void onTasksUpdate(List<Task> tasks) {}
            @Override public void onTaskDiskRemovalError(Task task, IOException e) {}
        };
        model.setModelListener(listener);

        // importSources creates tasks in NAME_ASC order: album1 first, album2 second
        model.importSources(new File[]{srcDir1, srcDir2});

        model.convert(defaultConfig(outputFolder));
        boolean completed = latch.await(30, TimeUnit.SECONDS);

        // Restore write permission so @TempDir cleanup can delete the file
        readOnlyOutput.setWritable(true);

        assertTrue(completed, "onBatchComplete was not called within timeout");
        assertEquals(2, errors.size(), "Both tasks should trigger onTaskComplete");
        assertNotNull(errors.get(0), "First task should fail with IOException (read-only output)");
        assertNull(errors.get(1), "Second task should succeed (null error)");
    }
}
