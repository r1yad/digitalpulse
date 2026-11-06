package com.example.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatUtils {
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private static final SimpleDateFormat FULL_FORMAT = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());

    public static String formatDuration(long millis) {
        if (millis <= 0) return "0m";
        long totalSeconds = millis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return minutes + "m";
    }

    public static String formatDurationShort(long millis) {
        long minutes = (millis / (1000 * 60));
        long hours = minutes / 60;
        long remainingMins = minutes % 60;
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%dh %02dm", hours, remainingMins);
        }
        return minutes + " min";
    }

    public static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    public static String formatSpeed(long bytesPerSec) {
        return formatBytes(bytesPerSec) + "/s";
    }

    public static String formatTime(long timestamp) {
        return TIME_FORMAT.format(new Date(timestamp));
    }

    public static String formatDate(long timestamp) {
        return DATE_FORMAT.format(new Date(timestamp));
    }

    public static String formatDateTime(long timestamp) {
        return FULL_FORMAT.format(new Date(timestamp));
    }

    public static String getRelativeTimeSpan(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        if (diff < 60000) return "Just now";
        if (diff < 3600000) return (diff / 60000) + "m ago";
        if (diff < 86400000) return (diff / 3600000) + "h ago";
        return DATE_FORMAT.format(new Date(timestamp));
    }
}
