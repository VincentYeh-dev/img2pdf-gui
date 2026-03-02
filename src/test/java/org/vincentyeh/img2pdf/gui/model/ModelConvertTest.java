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

import java.io.File;
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
        public void onTaskComplete(Task task, Exception error) {
            calls.add("onTaskComplete");
        }

        @Override
        public void onBatchError(String title, String message) {
            batchErrorTitle = title;
            calls.add("onBatchError");
        }
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

    // ===== Group C：removeTaskFromDisk() 例外傳播 =====

    // files 為 null → 任務移除，不拋例外
    @Test
    void removeTaskFromDisk_null_files_removes_task_without_exception() throws IOException {
        Task task = new Task(new File("output.pdf"), null);
        model.setTask(Collections.singletonList(task));

        assertDoesNotThrow(() -> model.removeTaskFromDisk(task));
        assertTrue(model.getTasks().isEmpty());
    }

    // files 為空陣列 → 任務移除，不拋例外
    @Test
    void removeTaskFromDisk_empty_files_removes_task_without_exception() throws IOException {
        Task task = new Task(new File("output.pdf"), new File[0]);
        model.setTask(Collections.singletonList(task));

        assertDoesNotThrow(() -> model.removeTaskFromDisk(task));
        assertTrue(model.getTasks().isEmpty());
    }

    // 資料夾不存在 → 拋出 IOException，且任務仍留在清單中
    @Test
    void removeTaskFromDisk_nonexistent_folder_throws_and_keeps_task() {
        File nonexistentImage = new File("/nonexistent_path_xyz/img.jpg");
        Task task = new Task(new File("output.pdf"), new File[]{nonexistentImage});
        model.setTask(Collections.singletonList(task));

        assertThrows(IOException.class, () -> model.removeTaskFromDisk(task));
        assertFalse(model.getTasks().isEmpty(), "task should still be in list after IOException");
    }

    // 資料夾內有被鎖定的檔案（Windows 專用）→ 拋出 IOException，且任務仍留在清單中
    // On Windows, Files.delete() cannot delete a file that has an exclusive FileLock held
    // by another FileChannel in the same JVM process; this triggers UncheckedIOException
    // which removeTaskFromDisk() must re-throw as IOException.
    @Test
    @EnabledOnOs(OS.WINDOWS)
    void removeTaskFromDisk_when_file_is_locked_throws_IOException_and_keeps_task(
            @TempDir Path tempDir) throws IOException {
        File dir = tempDir.resolve("locked_album").toFile();
        dir.mkdirs();
        File lockedFile = new File(dir, "locked.jpg");
        lockedFile.createNewFile();

        Task task = new Task(new File("locked_album.pdf"), new File[]{lockedFile});
        model.setTask(Collections.singletonList(task));

        // Hold an exclusive file lock so that Files.delete() on Windows will fail
        try (RandomAccessFile raf = new RandomAccessFile(lockedFile, "rw");
             FileChannel channel = raf.getChannel();
             FileLock lock = channel.lock()) {

            assertThrows(IOException.class, () -> model.removeTaskFromDisk(task));
            assertFalse(model.getTasks().isEmpty(),
                    "task should still be in list after IOException caused by file lock");
        }
        // After lock is released, cleanup is handled by @TempDir
    }
}
