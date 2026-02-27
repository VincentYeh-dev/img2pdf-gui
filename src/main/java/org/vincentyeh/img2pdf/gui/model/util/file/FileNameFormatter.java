package org.vincentyeh.img2pdf.gui.model.util.file;


import org.vincentyeh.img2pdf.gui.model.util.interfaces.NameFormatter;

import java.io.File;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link NameFormatter} that produces output PDF file names by substituting
 * placeholder tokens in a pattern string with values derived from a source {@link File}.
 * <p>
 * Supported tokens:
 * <ul>
 *   <li>{@code <NAME>} — the source file's base name (without extension)</li>
 *   <li>{@code <PARENT{n}>} — the n-th ancestor directory name (0 = direct parent)</li>
 *   <li>{@code <ROOT>} — the root component of an absolute path (e.g. {@code C:\})</li>
 *   <li>{@code <CY>/<CM>/<CD>/<CH>/<CN>/<CS>} — current year / month / day / hour / minute / second</li>
 *   <li>{@code <MY>/<MM>/<MD>/<MH>/<MN>/<MS>} — file last-modified year / month / day / hour / minute / second</li>
 * </ul>
 * See <a href="https://regex101.com/">regex101.com</a> for pattern debugging.
 *
 * @author VincentYeh
 */
public class FileNameFormatter extends NameFormatter<File> {

    /**
     * Creates a formatter using the given token pattern.
     *
     * @param pattern the filename template containing placeholder tokens
     */
    public FileNameFormatter(String pattern) {
        super(pattern);
    }

    /**
     * Formats the given file by resolving all tokens in the pattern string.
     *
     * @param data the source file whose metadata is used to resolve tokens
     * @return the formatted file name string with all tokens replaced
     * @throws FormatException if a {@code <PARENT{n}>} or {@code <ROOT>} token
     *                         cannot be resolved for the given file path
     */
    @Override
    public String format(File data) throws FormatException{
        try{
            HashMap<String, String> map = new HashMap<>();
            getFileMap(data, map);
            getCurrentTimeMap(map);
            getModifyTimeMap(data, map);
            verify(map);
            String buf=pattern;
            for (String key : map.keySet()) {
                buf = buf.replace(key, map.get(key));
            }

            return buf;
        }catch (IllegalArgumentException e){
            throw new FormatException(e);
        }
    }

    /**
     * Populates the token map with the current date/time values
     * ({@code <CY>}, {@code <CM>}, etc.).
     *
     * @param map the token-to-value map to populate
     */
    private void getCurrentTimeMap(HashMap<String, String> map) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        map.put(CurrentTime.year.getSymbol(), String.format("%d", cal.get(Calendar.YEAR)));
        map.put(CurrentTime.month.getSymbol(), String.format("%02d", cal.get(Calendar.MONTH) + 1));
        map.put(CurrentTime.day.getSymbol(), String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)));
        map.put(CurrentTime.hour.getSymbol(), String.format("%02d", cal.get(Calendar.HOUR)));
        map.put(CurrentTime.minute.getSymbol(), String.format("%02d", cal.get(Calendar.MINUTE)));
        map.put(CurrentTime.second.getSymbol(), String.format("%02d", cal.get(Calendar.SECOND)));
    }

    /**
     * Populates the token map with the file's last-modified date/time values
     * ({@code <MY>}, {@code <MM>}, etc.).
     *
     * @param file the file whose last-modified timestamp is used
     * @param map  the token-to-value map to populate
     */
    private void getModifyTimeMap(File file, HashMap<String, String> map) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(file.lastModified()));
        map.put(ModifyTime.year.getSymbol(), String.format("%d", cal.get(Calendar.YEAR)));
        map.put(ModifyTime.month.getSymbol(), String.format("%02d", cal.get(Calendar.MONTH) + 1));
        map.put(ModifyTime.day.getSymbol(), String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)));
        map.put(ModifyTime.hour.getSymbol(), String.format("%02d", cal.get(Calendar.HOUR)));
        map.put(ModifyTime.minute.getSymbol(), String.format("%02d", cal.get(Calendar.MINUTE)));
        map.put(ModifyTime.second.getSymbol(), String.format("%02d", cal.get(Calendar.SECOND)));
    }

    /**
     * Populates the token map with path-derived values: {@code <NAME>},
     * {@code <PARENT{n}>} for each ancestor level, and {@code <ROOT>}
     * for absolute paths.
     *
     * @param file the source file whose path components are used
     * @param map  the token-to-value map to populate
     * @throws IllegalArgumentException if {@code file} represents a root folder
     *                                  (i.e. has no name component)
     */
    private void getFileMap(File file, HashMap<String, String> map)
            throws IllegalArgumentException {
        Path p = file.toPath();
        int folderLevel=p.getNameCount();
        if(folderLevel<1)
            throw new IllegalArgumentException("root folder can not be selected: "+file.getAbsolutePath());

        map.put("<NAME>", p.getFileName().toString().split("\\.")[0]);

        for (int i = 1; i < folderLevel; i++) {
            map.put("<PARENT{" + (i - 1) + "}>", p.getName(folderLevel - 1 - i).getFileName().toString());
        }

        if (p.isAbsolute())
            map.put("<ROOT>", p.getRoot().toString());
    }


    /**
     * Verifies that every {@code <PARENT{n}>} and {@code <ROOT>} token present in
     * the pattern has a corresponding entry in the token map.
     *
     * @param map the populated token-to-value map to check against the pattern
     * @throws NotMappedPattern if any required token is absent from the map
     */
    private void verify(HashMap<String, String> map) throws NotMappedPattern {
        Matcher matcher = Pattern.compile("(<PARENT\\{[0-9]+}>|<ROOT>)").matcher(pattern);
        while (matcher.find()) {
            if (map.get(matcher.group(1)) == null)
                throw new NotMappedPattern(matcher.group(1));
        }

    }

    /**
     * Exception thrown when the pattern contains a token (e.g. {@code <PARENT{5}>})
     * that cannot be resolved for the given file path.
     */
    public static class NotMappedPattern extends IllegalArgumentException{
        private final String pattern;

        /**
         * Creates a {@code NotMappedPattern} exception for the given unresolvable token.
         *
         * @param pattern the token string that could not be mapped
         */
        public NotMappedPattern(String pattern) {
            super(pattern + " not mapped.");
            this.pattern = pattern;
        }

        /**
         * Returns the token string that could not be resolved.
         *
         * @return the unresolvable pattern token
         */
        public String getPattern() {
            return pattern;
        }
    }



    /**
     * Token symbols for the current date and time components.
     */
    private enum CurrentTime {
        year("<CY>"), month("<CM>"), day("<CD>"), hour("<CH>"), minute("<CN>"), second("<CS>");

        private final String symbol;

        CurrentTime(String symbol) {
            this.symbol = symbol;
        }

        /**
         * Returns the placeholder token string for this time component.
         *
         * @return the token symbol (e.g. {@code "<CY>"})
         */
        public String getSymbol() {
            return symbol;
        }
    }

    /**
     * Token symbols for the file's last-modified date and time components.
     */
    private enum ModifyTime {
        year("<MY>"), month("<MM>"), day("<MD>"), hour("<MH>"), minute("<MN>"), second("<MS>");

        private final String symbol;

        ModifyTime(String symbol) {
            this.symbol = symbol;
        }

        /**
         * Returns the placeholder token string for this time component.
         *
         * @return the token symbol (e.g. {@code "<MY>"})
         */
        public String getSymbol() {
            return symbol;
        }
    }

}
