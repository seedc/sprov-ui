package xyz.sprov.blog.sprovui.util;

public class FileUtil {

    public static final long ONE_KB = 1024;
    private static final long ONE_MB = ONE_KB * 1024;
    private static final long ONE_GB = ONE_MB * 1024;
    private static final long ONE_TB = ONE_GB * 1024;

    public static String formatSize(long size) {
        if (size < ONE_KB) {
            return size + " B";
        } else if (size < ONE_MB) {
            return String.format("%.2f", 1.0 * size / ONE_KB) + " KB";
        } else if (size < ONE_GB) {
            return String.format("%.2f", 1.0 * size / ONE_MB) + " MB";
        } else if (size < ONE_TB) {
            return String.format("%.2f", 1.0 * size / ONE_GB) + " GB";
        } else {
            return String.format("%.2f", 1.0 * size / ONE_TB) + " TB";
        }
    }

}
