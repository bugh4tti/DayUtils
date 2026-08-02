package com.dayutils.plugin.utils;

public final class TimeUtils {

    private TimeUtils() {
    }

    public static String formatSince(long millis) {
        long diff = System.currentTimeMillis() - millis;
        if (diff < 0) diff = 0;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + (days == 1 ? " día" : " días");
        if (hours > 0) return hours + (hours == 1 ? " hora" : " horas");
        if (minutes > 0) return minutes + (minutes == 1 ? " minuto" : " minutos");
        return seconds + (seconds == 1 ? " segundo" : " segundos");
    }
            }
