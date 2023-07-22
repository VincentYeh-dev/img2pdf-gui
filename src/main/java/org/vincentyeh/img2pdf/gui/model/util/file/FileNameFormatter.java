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
 * https://regex101.com/
 *
 * @author VincentYeh
 */
public class FileNameFormatter extends NameFormatter<File> {

    public FileNameFormatter(String pattern) {
        super(pattern);
    }

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


    private void verify(HashMap<String, String> map) throws NotMappedPattern {
        Matcher matcher = Pattern.compile("(<PARENT\\{[0-9]+}>|<ROOT>)").matcher(pattern);
        while (matcher.find()) {
            if (map.get(matcher.group(1)) == null)
                throw new NotMappedPattern(matcher.group(1));
        }

    }

    public static class NotMappedPattern extends IllegalArgumentException{
        private final String pattern;

        public NotMappedPattern(String pattern) {
            super(pattern + " not mapped.");
            this.pattern = pattern;
        }

        public String getPattern() {
            return pattern;
        }
    }



    private enum CurrentTime {
        year("<CY>"), month("<CM>"), day("<CD>"), hour("<CH>"), minute("<CN>"), second("<CS>");

        private final String symbol;

        CurrentTime(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }
    }

    private enum ModifyTime {
        year("<MY>"), month("<MM>"), day("<MD>"), hour("<MH>"), minute("<MN>"), second("<MS>");

        private final String symbol;

        ModifyTime(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }
    }

}
