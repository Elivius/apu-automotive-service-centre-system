package utils;

public class StringUtils {
    /**
     * Truncates a string to a maximum length, appending an ellipsis if necessary.
     */
    public static String truncate(String s, int max) {
        if (s == null || s.isEmpty()) return "";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}