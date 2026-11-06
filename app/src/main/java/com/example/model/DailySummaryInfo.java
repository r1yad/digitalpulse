package com.example.model;

import android.graphics.drawable.Drawable;

public class DailySummaryInfo {
    private final long screenTimeMillis;
    private final long cellularBytes;
    private final long wifiBytes;
    private final int notificationCount;
    private final int clipboardCount;
    private final int batteryLevel;
    private final float batteryTemperature;
    private final boolean isCharging;
    private final String topAppName;
    private final Drawable topAppIcon;
    private final long topAppDurationMillis;

    public DailySummaryInfo(
            long screenTimeMillis,
            long cellularBytes,
            long wifiBytes,
            int notificationCount,
            int clipboardCount,
            int batteryLevel,
            float batteryTemperature,
            boolean isCharging,
            String topAppName,
            Drawable topAppIcon,
            long topAppDurationMillis) {
        this.screenTimeMillis = screenTimeMillis;
        this.cellularBytes = cellularBytes;
        this.wifiBytes = wifiBytes;
        this.notificationCount = notificationCount;
        this.clipboardCount = clipboardCount;
        this.batteryLevel = batteryLevel;
        this.batteryTemperature = batteryTemperature;
        this.isCharging = isCharging;
        this.topAppName = topAppName;
        this.topAppIcon = topAppIcon;
        this.topAppDurationMillis = topAppDurationMillis;
    }

    public long getScreenTimeMillis() { return screenTimeMillis; }
    public long getCellularBytes() { return cellularBytes; }
    public long getWifiBytes() { return wifiBytes; }
    public int getNotificationCount() { return notificationCount; }
    public int getClipboardCount() { return clipboardCount; }
    public int getBatteryLevel() { return batteryLevel; }
    public float getBatteryTemperature() { return batteryTemperature; }
    public boolean isCharging() { return isCharging; }
    public String getTopAppName() { return topAppName; }
    public Drawable getTopAppIcon() { return topAppIcon; }
    public long getTopAppDurationMillis() { return topAppDurationMillis; }
}
