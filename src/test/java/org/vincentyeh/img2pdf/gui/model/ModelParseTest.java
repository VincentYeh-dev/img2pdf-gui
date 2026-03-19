package org.vincentyeh.img2pdf.gui.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModelParseTest {

    @Test
    void returns_one_task_per_directory(@TempDir Path tempDir) throws Exception {
        File dir1 = tempDir.resolve("album1").toFile();
        File dir2 = tempDir.resolve("album2").toFile();
        dir1.mkdirs();
        dir2.mkdirs();
        new File(dir1, "1.jpg").createNewFile();
        new File(dir2, "2.jpg").createNewFile();

        List<Task> tasks = Model.parseSourceFiles(new File[]{dir1, dir2});

        assertEquals(2, tasks.size());
    }

    @Test
    void destination_name_is_based_on_directory_name(@TempDir Path tempDir) throws Exception {
        File dir = tempDir.resolve("myfolder").toFile();
        dir.mkdirs();
        new File(dir, "1.jpg").createNewFile();

        List<Task> tasks = Model.parseSourceFiles(new File[]{dir});

        assertEquals(1, tasks.size());
        assertEquals("myfolder.pdf", tasks.get(0).destination.getName());
    }

    @Test
    void filters_out_non_image_files(@TempDir Path tempDir) throws Exception {
        File dir = tempDir.resolve("mixed").toFile();
        dir.mkdirs();
        new File(dir, "photo.jpg").createNewFile();
        new File(dir, "readme.txt").createNewFile();
        new File(dir, "data.pdf").createNewFile();

        List<Task> tasks = Model.parseSourceFiles(new File[]{dir});

        assertEquals(1, tasks.size());
        assertEquals(1, tasks.get(0).files.length);
        assertEquals("photo.jpg", tasks.get(0).files[0].getName());
    }

    @Test
    void numeric_sort_is_applied_to_files(@TempDir Path tempDir) throws Exception {
        File dir = tempDir.resolve("sorted").toFile();
        dir.mkdirs();
        new File(dir, "img10.jpg").createNewFile();
        new File(dir, "img2.jpg").createNewFile();
        new File(dir, "img1.jpg").createNewFile();

        List<Task> tasks = Model.parseSourceFiles(new File[]{dir});

        File[] files = tasks.get(0).files;
        assertEquals(3, files.length);
        assertEquals("img1.jpg", files[0].getName());
        assertEquals("img2.jpg", files[1].getName());
        assertEquals("img10.jpg", files[2].getName());
    }

    @Test
    void empty_directory_still_produces_task(@TempDir Path tempDir) {
        File dir = tempDir.resolve("empty").toFile();
        dir.mkdirs();

        List<Task> tasks = Model.parseSourceFiles(new File[]{dir});

        assertEquals(1, tasks.size());
        assertEquals(0, tasks.get(0).files.length);
    }

    @Test
    void null_directories_throws_illegal_argument() {
        assertThrows(IllegalArgumentException.class,
                () -> Model.parseSourceFiles(null));
    }
}
