package org.vincentyeh.img2pdf.gui.model.util.file;

import org.junit.jupiter.api.Test;
import org.vincentyeh.img2pdf.gui.model.util.interfaces.NameFormatter;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class FileNameFormatterTest {

    @Test
    void name_pattern_returns_filename_without_extension() throws Exception {
        FileNameFormatter formatter = new FileNameFormatter("<NAME>");
        assertEquals("photo", formatter.format(new File("photo.jpg")));
    }

    @Test
    void name_pattern_with_pdf_suffix() throws Exception {
        FileNameFormatter formatter = new FileNameFormatter("<NAME>.pdf");
        assertEquals("photo.pdf", formatter.format(new File("parent/photo.jpg")));
    }

    @Test
    void parent0_returns_immediate_parent_directory() throws Exception {
        FileNameFormatter formatter = new FileNameFormatter("<PARENT{0}>");
        assertEquals("parent", formatter.format(new File("parent/photo.jpg")));
    }

    @Test
    void parent1_returns_grandparent_directory() throws Exception {
        FileNameFormatter formatter = new FileNameFormatter("<PARENT{1}>");
        assertEquals("grandparent", formatter.format(new File("grandparent/parent/photo.jpg")));
    }

    @Test
    void unmapped_parent_throws_format_exception_wrapping_not_mapped_pattern() {
        FileNameFormatter formatter = new FileNameFormatter("<PARENT{5}>");
        // NotMappedPattern extends IllegalArgumentException, which format() wraps in FormatException
        NameFormatter.FormatException ex = assertThrows(
                NameFormatter.FormatException.class,
                () -> formatter.format(new File("photo.jpg")));
        assertInstanceOf(FileNameFormatter.NotMappedPattern.class, ex.getCause());
        assertEquals("<PARENT{5}>",
                ((FileNameFormatter.NotMappedPattern) ex.getCause()).getPattern());
    }

    @Test
    void null_pattern_throws_illegal_argument() {
        assertThrows(IllegalArgumentException.class, () -> new FileNameFormatter(null));
    }

    @Test
    void combined_pattern_replaces_all_markers() throws Exception {
        FileNameFormatter formatter = new FileNameFormatter("<PARENT{0}>_<NAME>.pdf");
        assertEquals("folder_photo.pdf", formatter.format(new File("folder/photo.jpg")));
    }
}
