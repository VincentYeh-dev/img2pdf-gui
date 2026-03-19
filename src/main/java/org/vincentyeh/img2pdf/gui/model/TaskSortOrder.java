package org.vincentyeh.img2pdf.gui.model;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Defines the available sort orders for the task list displayed in the UI.
 * <p>
 * Each constant carries a human-readable label (with Unicode arrows) and can
 * produce a {@link Comparator} for sorting {@link Task} objects accordingly.
 * </p>
 */
public enum TaskSortOrder {

    /** Sort tasks by destination file name, A → Z. */
    NAME_ASC("Name A\u2192Z"),

    /** Sort tasks by destination file name, Z → A. */
    NAME_DESC("Name Z\u2192A"),

    /** Sort tasks by image count, descending (most images first). */
    COUNT_DESC("Count \u2193"),

    /** Sort tasks by image count, ascending (fewest images first). */
    COUNT_ASC("Count \u2191"),

    /** Sort tasks by source folder modification date, newest first. */
    DATE_DESC("Date \u2193"),

    /** Sort tasks by source folder modification date, oldest first. */
    DATE_ASC("Date \u2191");

    private final String label;

    /**
     * Constructs a sort-order constant with the given display label.
     *
     * @param label the human-readable label shown in the sort combo-box
     */
    TaskSortOrder(String label) {
        this.label = label;
    }

    /**
     * Returns the human-readable label for this sort order (used by combo-boxes).
     *
     * @return the display label
     */
    @Override
    public String toString() {
        return label;
    }

    /**
     * Returns a {@link Comparator} that sorts {@link Task} objects according to
     * this sort order.
     *
     * @return a comparator implementing the sort strategy for this constant
     */
    public Comparator<Task> getComparator() {
        switch (this) {
            case NAME_ASC: {
                Collator c = Collator.getInstance(Locale.getDefault());
                return (a, b) -> c.compare(a.destination.getName(), b.destination.getName());
            }
            case NAME_DESC: {
                Collator c = Collator.getInstance(Locale.getDefault());
                return (a, b) -> c.compare(b.destination.getName(), a.destination.getName());
            }
            case COUNT_ASC:
                return Comparator.comparingInt(t -> t.files.length);
            case COUNT_DESC:
                return (a, b) -> Integer.compare(b.files.length, a.files.length);
            case DATE_ASC:
                return Comparator.comparingLong(t ->
                        (t.files != null && t.files.length > 0 && t.files[0].getParentFile() != null)
                        ? t.files[0].getParentFile().lastModified() : 0L);
            case DATE_DESC:
                return (a, b) -> {
                    long aDate = (a.files != null && a.files.length > 0 && a.files[0].getParentFile() != null)
                            ? a.files[0].getParentFile().lastModified() : 0L;
                    long bDate = (b.files != null && b.files.length > 0 && b.files[0].getParentFile() != null)
                            ? b.files[0].getParentFile().lastModified() : 0L;
                    return Long.compare(bDate, aDate);
                };
            default:
                return (a, b) -> 0;
        }
    }
}
