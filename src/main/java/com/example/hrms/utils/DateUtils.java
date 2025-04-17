package com.example.hrms.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");

    public static DateTimeFormatter getDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    }

    public static LocalDateTime parseDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime, getDateTimeFormatter());
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.atZone(ZONE_ID).format(getDateTimeFormatter());
    }
}