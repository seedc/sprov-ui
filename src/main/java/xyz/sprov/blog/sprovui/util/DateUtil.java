package xyz.sprov.blog.sprovui.util;

public class DateUtil {

    public static final long ONE_SECOND = 1;

    public static final long ONE_MINUTE = ONE_SECOND * 60;

    public static final long ONE_HOUR = ONE_MINUTE * 60;

    public static final long ONE_DAY = ONE_HOUR * 24;

    /**
     * 格式化秒
     */
    public static String formatSecond(long second) {
        if (second < ONE_MINUTE) {
            return second + " 秒";
        } else if (second < ONE_HOUR) {
            return second / ONE_MINUTE + " 分钟";
        } else if (second < ONE_DAY) {
            return second / ONE_HOUR + " 小时";
        } else {
            return second / ONE_DAY + " 天";
        }
    }

}
