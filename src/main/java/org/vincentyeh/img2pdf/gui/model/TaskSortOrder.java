package org.vincentyeh.img2pdf.gui.model;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public enum TaskSortOrder {
    NAME_ASC("Name A\u2192Z"),
    NAME_DESC("Name Z\u2192A"),
    COUNT_DESC("Count \u2193"),
    COUNT_ASC("Count \u2191"),
    DATE_DESC("Date \u2193"),
    DATE_ASC("Date \u2191");

    private final String label;

    TaskSortOrder(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

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
