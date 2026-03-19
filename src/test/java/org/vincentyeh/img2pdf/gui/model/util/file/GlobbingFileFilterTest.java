package org.vincentyeh.img2pdf.gui.model.util.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class GlobbingFileFilterTest {

    @Test
    void accepts_matching_extension() {
        GlobbingFileFilter filter = new GlobbingFileFilter("*.jpg");
        assertTrue(filter.accept(new File("photo.jpg")));
    }

    @Test
    void rejects_non_matching_extension() {
        GlobbingFileFilter filter = new GlobbingFileFilter("*.jpg");
        assertFalse(filter.accept(new File("photo.png")));
    }

    @Test
    void accepts_all_extensions_in_brace_pattern() {
        GlobbingFileFilter filter = new GlobbingFileFilter("*.{jpg,png,bmp}");
        assertTrue(filter.accept(new File("a.jpg")));
        assertTrue(filter.accept(new File("a.png")));
        assertTrue(filter.accept(new File("a.bmp")));
        assertFalse(filter.accept(new File("a.gif")));
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void windows_glob_is_case_insensitive() {
        GlobbingFileFilter filter = new GlobbingFileFilter("*.jpg");
        assertTrue(filter.accept(new File("photo.JPG")));
        assertTrue(filter.accept(new File("photo.Jpg")));
    }

    @Test
    void throws_on_null_pattern() {
        assertThrows(IllegalArgumentException.class, () -> new GlobbingFileFilter(null));
    }

    @Test
    void accepts_full_image_glob_used_by_model() {
        GlobbingFileFilter filter = new GlobbingFileFilter(
                "*.{JPG,jpg,JPEG,jpeg,PNG,png,BMP,bmp,webp,WEBP}");
        assertTrue(filter.accept(new File("a.jpg")));
        assertTrue(filter.accept(new File("a.JPG")));
        assertTrue(filter.accept(new File("a.jpeg")));
        assertTrue(filter.accept(new File("a.png")));
        assertTrue(filter.accept(new File("a.PNG")));
        assertTrue(filter.accept(new File("a.bmp")));
        assertTrue(filter.accept(new File("a.BMP")));
        assertTrue(filter.accept(new File("a.webp")));
        assertTrue(filter.accept(new File("a.WEBP")));
        assertFalse(filter.accept(new File("a.gif")));
        assertFalse(filter.accept(new File("a.pdf")));
        assertFalse(filter.accept(new File("a.txt")));
    }

    @Test
    void rejects_file_with_no_extension() {
        GlobbingFileFilter filter = new GlobbingFileFilter("*.jpg");
        assertFalse(filter.accept(new File("photo")));
    }
}
