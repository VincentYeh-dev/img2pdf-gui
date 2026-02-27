package org.vincentyeh.img2pdf.gui.model.util.file;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileSorterTest {

    private static File f(String name) {
        return new File(name);
    }

    @Test
    void name_increase_sorts_alphabetically() {
        FileSorter sorter = new FileSorter(FileSorter.Sortby.NAME, FileSorter.Sequence.INCREASE);
        List<File> files = Arrays.asList(f("c.jpg"), f("a.jpg"), f("b.jpg"));
        files.sort(sorter);
        assertEquals("a.jpg", files.get(0).getName());
        assertEquals("b.jpg", files.get(1).getName());
        assertEquals("c.jpg", files.get(2).getName());
    }

    @Test
    void name_decrease_sorts_reverse_alphabetically() {
        FileSorter sorter = new FileSorter(FileSorter.Sortby.NAME, FileSorter.Sequence.DECREASE);
        List<File> files = Arrays.asList(f("a.jpg"), f("c.jpg"), f("b.jpg"));
        files.sort(sorter);
        assertEquals("c.jpg", files.get(0).getName());
        assertEquals("b.jpg", files.get(1).getName());
        assertEquals("a.jpg", files.get(2).getName());
    }

    @Test
    void numeric_increase_treats_embedded_numbers_correctly() {
        FileSorter sorter = new FileSorter(FileSorter.Sortby.NUMERIC, FileSorter.Sequence.INCREASE);
        List<File> files = Arrays.asList(f("img10.jpg"), f("img2.jpg"), f("img1.jpg"));
        files.sort(sorter);
        assertEquals("img1.jpg", files.get(0).getName());
        assertEquals("img2.jpg", files.get(1).getName());
        assertEquals("img10.jpg", files.get(2).getName());
    }

    @Test
    void numeric_decrease_treats_embedded_numbers_correctly() {
        FileSorter sorter = new FileSorter(FileSorter.Sortby.NUMERIC, FileSorter.Sequence.DECREASE);
        List<File> files = Arrays.asList(f("img1.jpg"), f("img10.jpg"), f("img2.jpg"));
        files.sort(sorter);
        assertEquals("img10.jpg", files.get(0).getName());
        assertEquals("img2.jpg", files.get(1).getName());
        assertEquals("img1.jpg", files.get(2).getName());
    }

    @Test
    void numeric_equal_files_compare_as_zero() {
        FileSorter sorter = new FileSorter(FileSorter.Sortby.NUMERIC, FileSorter.Sequence.INCREASE);
        assertEquals(0, sorter.compare(f("img1.jpg"), f("img1.jpg")));
    }

    @Test
    void throws_on_null_sortby() {
        assertThrows(IllegalArgumentException.class,
                () -> new FileSorter(null, FileSorter.Sequence.INCREASE));
    }

    @Test
    void throws_on_null_sequence() {
        assertThrows(IllegalArgumentException.class,
                () -> new FileSorter(FileSorter.Sortby.NAME, null));
    }

    @Test
    void numeric_falls_back_to_string_compare_when_patterns_differ() {
        FileSorter sorter = new FileSorter(FileSorter.Sortby.NUMERIC, FileSorter.Sequence.INCREASE);
        // "a1.jpg" vs "b1.jpg" — different string skeletons, fallback to String.compareTo
        assertTrue(sorter.compare(f("a1.jpg"), f("b1.jpg")) < 0);
    }
}
