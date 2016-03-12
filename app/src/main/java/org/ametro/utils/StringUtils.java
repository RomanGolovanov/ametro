package org.ametro.utils;

import java.text.Collator;

public class StringUtils {

    private static final Collator COLLATOR = Collator.getInstance();

    private static final String[] SI_UNITS = {"k","M","G","T","P","E"};
    private static final String[] COMPUTING_UNITS = {"Ki","Mi","Gi","Ti","Pi","Ei"};

    static{
        COLLATOR.setStrength(Collator.PRIMARY);
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? SI_UNITS : COMPUTING_UNITS)[exp-1];
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static boolean startsWithoutDiacritics(String text, String prefix){
        final int textLength = text.length();
        final int prefixLength = prefix.length();
        if(textLength<prefixLength){
            return false;
        }
        String textPrefix = text.substring(0, prefixLength);
        return COLLATOR.compare(textPrefix, prefix) == 0;
    }

    public static boolean isNullOrEmpty(String value) {
        return value == null || "".equals(value.trim());
    }


    public static String humanReadableTime(int totalSeconds) {
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 60 / 60;

        if(hours>0){
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }

        return String.format("%02d:%02d", minutes, seconds);
    }
}
