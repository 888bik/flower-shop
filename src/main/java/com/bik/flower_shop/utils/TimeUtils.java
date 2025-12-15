package com.bik.flower_shop.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author bik
 */
public class TimeUtils {


    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZONE);

    private static final DateTimeFormatter DATETIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZONE);

    /**
     * 默认日期时间格式 yyyy-MM-dd HH:mm:ss
     */
    private static final DateTimeFormatter DTF =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    private TimeUtils() {
        // 工具类禁止实例化
    }

    /**
     * 时间戳(秒) 转 yyyy-MM-dd HH:mm:ss
     */
    public static String formatDate(Long epochSecond) {
        if (epochSecond == null) {
            return null;
        }
        return DTF.format(Instant.ofEpochSecond(epochSecond));
    }

    public static String formatDate(Integer epochSecond) {
        if (epochSecond == null) {
            return null;
        }
        return DTF.format(Instant.ofEpochSecond(epochSecond));
    }

    public static String formatDateTime(Integer epochSecond) {
        if (epochSecond == null) {
            return null;
        }
        return DATETIME.format(Instant.ofEpochSecond(epochSecond));
    }
}
