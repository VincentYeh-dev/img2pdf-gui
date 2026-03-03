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

    // Verifies that NUMERIC INCREASE uses the first differing digit group when multiple groups exist.
    // "a2b1.jpg" has first group=2; "a1b9.jpg" has first group=1; so a2b1 > a1b9.
    @Test
    void numeric_increase_multiple_groups_first_group_dominates() {
        FileSorter sorter = new FileSorter(FileSorter.Sortby.NUMERIC, FileSorter.Sequence.INCREASE);
        List<File> files = Arrays.asList(f("a2b1.jpg"), f("a1b9.jpg"));
        files.sort(sorter);
        assertEquals("a1b9.jpg", files.get(0).getName());
        assertEquals("a2b1.jpg", files.get(1).getName());
    }

    // Verifies that NUMERIC INCREASE resolves the second digit group when the first group is equal.
    // "a1b3.jpg" second group=3 > "a1b2.jpg" second group=2, so a1b2 < a1b3.
    @Test
    void numeric_increase_multiple_groups_second_group_used_when_first_equal() {
        FileSorter sorter = new FileSorter(FileSorter.Sortby.NUMERIC, FileSorter.Sequence.INCREASE);
        List<File> files = Arrays.asList(f("a1b3.jpg"), f("a1b2.jpg"));
        files.sort(sorter);
        assertEquals("a1b2.jpg", files.get(0).getName());
        assertEquals("a1b3.jpg", files.get(1).getName());
    }

    // Verifies that NUMERIC DECREASE reverses the multi-group ordering correctly.
    @Test
    void numeric_decrease_multiple_groups_reverses_order() {
        FileSorter sorter = new FileSorter(FileSorter.Sortby.NUMERIC, FileSorter.Sequence.DECREASE);
        List<File> files = Arrays.asList(f("a1b2.jpg"), f("a1b3.jpg"), f("a2b1.jpg"));
        files.sort(sorter);
        // a2b1 first group=2 > a1b3 first group=1 >= a1b2 first group=1;
        // a1b3 second group=3 > a1b2 second group=2
        assertEquals("a2b1.jpg", files.get(0).getName());
        assertEquals("a1b3.jpg", files.get(1).getName());
        assertEquals("a1b2.jpg", files.get(2).getName());
    }

    // Verifies that pure numeric filenames (with same extension) are sorted by numeric value, not
    // lexicographic order. "9.jpg" < "10.jpg" numerically, whereas lexicographically "9" > "10".
    @Test
    void numeric_increase_pure_numeric_filename_sorts_by_value_not_lexicographic() {
        FileSorter sorter = new FileSorter(FileSorter.Sortby.NUMERIC, FileSorter.Sequence.INCREASE);
        List<File> files = Arrays.asList(f("10.jpg"), f("9.jpg"), f("1.jpg"));
        files.sort(sorter);
        assertEquals("1.jpg", files.get(0).getName());
        assertEquals("9.jpg", files.get(1).getName());
        assertEquals("10.jpg", files.get(2).getName());
    }

    // Verifies that zero-padded and non-padded filenames representing the same value compare as equal.
    // "001.jpg" and "1.jpg" both parse to numeric value 1.
    @Test
    void numeric_increase_zero_padded_equals_non_padded() {
        FileSorter sorter = new FileSorter(FileSorter.Sortby.NUMERIC, FileSorter.Sequence.INCREASE);
        assertEquals(0, sorter.compare(f("001.jpg"), f("1.jpg")));
    }

    // Verifies that zero-padded filenames with different values are sorted by numeric value.
    // "001.jpg"=1 < "010.jpg"=10 < "100.jpg"=100.
    @Test
    void numeric_increase_zero_padded_sequence_sorts_correctly() {
        FileSorter sorter = new FileSorter(FileSorter.Sortby.NUMERIC, FileSorter.Sequence.INCREASE);
        List<File> files = Arrays.asList(f("100.jpg"), f("001.jpg"), f("010.jpg"));
        files.sort(sorter);
        assertEquals("001.jpg", files.get(0).getName());
        assertEquals("010.jpg", files.get(1).getName());
        assertEquals("100.jpg", files.get(2).getName());
    }

    // Verifies that pure-text filenames with no digits fall back to lexicographic comparison
    // under NUMERIC mode, since their skeletons (all non-digit chars) differ.
    @Test
    void numeric_increase_no_digits_falls_back_to_string_compare() {
        FileSorter sorter = new FileSorter(FileSorter.Sortby.NUMERIC, FileSorter.Sequence.INCREASE);
        // "abc.jpg" < "def.jpg" lexicographically; neither has any digit groups
        assertTrue(sorter.compare(f("abc.jpg"), f("def.jpg")) < 0);
    }

    // Verifies that two identical no-digit filenames compare as zero under NUMERIC mode.
    @Test
    void numeric_increase_identical_no_digit_filenames_compare_as_zero() {
        FileSorter sorter = new FileSorter(FileSorter.Sortby.NUMERIC, FileSorter.Sequence.INCREASE);
        assertEquals(0, sorter.compare(f("abc.jpg"), f("abc.jpg")));
    }

    // Verifies that compare() throws IllegalArgumentException when the first argument is null.
    @Test
    void compare_throws_on_null_first_argument() {
        FileSorter sorter = new FileSorter(FileSorter.Sortby.NUMERIC, FileSorter.Sequence.INCREASE);
        assertThrows(IllegalArgumentException.class, () -> sorter.compare(null, f("a.jpg")));
    }

    // Verifies that compare() throws IllegalArgumentException when the second argument is null.
    @Test
    void compare_throws_on_null_second_argument() {
        FileSorter sorter = new FileSorter(FileSorter.Sortby.NUMERIC, FileSorter.Sequence.INCREASE);
        assertThrows(IllegalArgumentException.class, () -> sorter.compare(f("a.jpg"), null));
    }

    // Verifies that a single-element list remains unchanged after sort under NUMERIC INCREASE.
    @Test
    void numeric_increase_single_element_list_unchanged() {
        FileSorter sorter = new FileSorter(FileSorter.Sortby.NUMERIC, FileSorter.Sequence.INCREASE);
        List<File> files = Arrays.asList(f("img5.jpg"));
        files.sort(sorter);
        assertEquals("img5.jpg", files.get(0).getName());
    }

    // Verifies that NUMERIC DECREASE correctly orders 9 > 1 > ... (reverse of numeric ascending).
    @Test
    void numeric_decrease_pure_numeric_filename_sorts_by_value_descending() {
        FileSorter sorter = new FileSorter(FileSorter.Sortby.NUMERIC, FileSorter.Sequence.DECREASE);
        List<File> files = Arrays.asList(f("1.jpg"), f("10.jpg"), f("9.jpg"));
        files.sort(sorter);
        assertEquals("10.jpg", files.get(0).getName());
        assertEquals("9.jpg", files.get(1).getName());
        assertEquals("1.jpg", files.get(2).getName());
    }

    // Verifies that NAME INCREASE sort on a single-element list leaves it unchanged.
    @Test
    void name_increase_single_element_list_unchanged() {
        FileSorter sorter = new FileSorter(FileSorter.Sortby.NAME, FileSorter.Sequence.INCREASE);
        List<File> files = Arrays.asList(f("only.jpg"));
        files.sort(sorter);
        assertEquals("only.jpg", files.get(0).getName());
    }
}
