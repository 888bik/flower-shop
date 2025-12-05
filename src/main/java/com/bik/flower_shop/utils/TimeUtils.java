package com.bik.flower_shop.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author bik
 */
public class TimeUtils {

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
    public static String format(long epochSecond) {
        return DTF.format(Instant.ofEpochSecond(epochSecond));
    }
}
