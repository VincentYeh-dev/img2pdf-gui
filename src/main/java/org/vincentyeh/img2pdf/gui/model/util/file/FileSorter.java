package org.vincentyeh.img2pdf.gui.model.util.file;


import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link Comparator} for {@link File} objects that sorts by name, modification
 * date, or numeric filename value, in either ascending or descending order.
 */
public class FileSorter implements Comparator<File> {
    private final Sortby sortby;
    private final Sequence sequence;

    /**
     * Creates a sorter with the specified sort criterion and direction.
     *
     * @param sortby   the attribute to sort by (name, date, or numeric value)
     * @param sequence the sort direction (ascending or descending)
     * @throws IllegalArgumentException if either argument is {@code null}
     */
    public FileSorter(Sortby sortby, Sequence sequence) {
        if (sortby == null)
            throw new IllegalArgumentException("sortby==null");
        if (sequence == null)
            throw new IllegalArgumentException("sequence==null");

        this.sortby = sortby;
        this.sequence = sequence;
    }

    /**
     * Compares two files according to this sorter's criterion and direction.
     *
     * @param o1 the first file to compare
     * @param o2 the second file to compare
     * @return a negative integer, zero, or positive integer if {@code o1} is
     *         less than, equal to, or greater than {@code o2} under this ordering
     * @throws IllegalArgumentException if either argument is {@code null}
     */
    @Override
    public int compare(File o1, File o2) {
        if (o1 == null)
            throw new IllegalArgumentException("o1==null");
        if (o2 == null)
            throw new IllegalArgumentException("o2==null");
        switch (sortby) {
            case NAME:
                if (sequence == Sequence.INCREASE)
                    return o1.getName().compareTo(o2.getName());
                else if (sequence == Sequence.DECREASE)
                    return o2.getName().compareTo(o1.getName());
            case DATE:
                if (sequence == Sequence.INCREASE)
                    return (int) (o1.lastModified() - o2.lastModified());
                else if (sequence == Sequence.DECREASE)
                    return (int) (o2.lastModified() - o1.lastModified());
            case NUMERIC:
                if (sequence == Sequence.INCREASE)
                    return compareNumeric(o1.getName(), o2.getName());

                else if (sequence == Sequence.DECREASE)
                    return compareNumeric(o2.getName(), o1.getName());

            default:
                throw new RuntimeException("Multiple files need to be sorted by sort and sequence arguments.");
        }
    }

    /**
     * Compares two file-name strings using natural (numeric) ordering.
     * <p>
     * Non-numeric parts are compared lexicographically first; when the structural
     * template matches, embedded numeric sequences are compared numerically.
     * </p>
     *
     * @param ThisStr the name of the first file
     * @param OStr    the name of the second file
     * @return a negative integer, zero, or positive integer under numeric ordering
     */
    private int compareNumeric(String ThisStr, String OStr) {
        String noNumThis = ThisStr.replaceAll("[0-9]+", "*");
        String noNumO = OStr.replaceAll("[0-9]+", "*");

        if (noNumThis.equals(noNumO)) {
            int[] a = getNumber(ThisStr);
            int[] b = getNumber(OStr);
            for (int i = 0; i < a.length; i++) {
                int r = a[i] - b[i];
                if (r != 0) {
                    return r;
                }
            }
            return 0;
        } else {
            return ThisStr.compareTo(OStr);
        }
    }

    /**
     * Extracts all contiguous digit sequences from the given string as an int array.
     *
     * @param str the string to scan for numeric sequences
     * @return an array of integers found in the string, in order of occurrence
     */
    private int[] getNumber(String str) {
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(str);
        ArrayList<Integer> buf = new ArrayList<>();
        while (matcher.find()) {
            buf.add(Integer.valueOf(matcher.group()));
        }
        Integer[] result = new Integer[buf.size()];
        buf.toArray(result);
        int[] ints = new int[result.length];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = result[i];
        }
        return ints;
    }

    /**
     * Returns a string description in the form {@code "SORTBY-SEQUENCE"}.
     *
     * @return a human-readable representation of this sorter's configuration
     */
    @Override
    public String toString() {
        return String.format("%s-%s", sortby, sequence);
    }

    /**
     * Sort direction: ascending ({@link #INCREASE}) or descending ({@link #DECREASE}).
     */
    public enum Sequence {
        /** Sort from smallest to largest value. */
        INCREASE,
        /** Sort from largest to smallest value. */
        DECREASE
    }

    /**
     * The file attribute used as the sort key.
     */
    public enum Sortby {
        /** Sort by file name (lexicographic). */
        NAME,
        /** Sort by file last-modified timestamp. */
        DATE,
        /** Sort by embedded numeric sequences in the file name. */
        NUMERIC
    }

}