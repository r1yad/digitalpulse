package com.example.model;

import android.graphics.drawable.Drawable;

public class AppUsageInfo {
    private final String packageName;
    private final String appName;
    private final Drawable appIcon;
    private long totalTimeInForegroundMillis;
    private int launchCount;
    private long lastTimeUsedMillis;

    public AppUsageInfo(String packageName, String appName, Drawable appIcon, long totalTimeInForegroundMillis, int launchCount, long lastTimeUsedMillis) {
        this.packageName = packageName;
        this.appName = appName;
        this.appIcon = appIcon;
        this.totalTimeInForegroundMillis = totalTimeInForegroundMillis;
        this.launchCount = launchCount;
        this.lastTimeUsedMillis = lastTimeUsedMillis;
    }

    public String getPackageName() { return packageName; }
    public String getAppName() { return appName; }
    public Drawable getAppIcon() { return appIcon; }
    public long getTotalTimeInForegroundMillis() { return totalTimeInForegroundMillis; }
    public int getLaunchCount() { return launchCount; }
    public long getLastTimeUsedMillis() { return lastTimeUsedMillis; }

    public void addForegroundTime(long deltaMillis) {
        this.totalTimeInForegroundMillis += deltaMillis;
    }

    public void incrementLaunchCount() {
        this.launchCount++;
    }

    public long getAverageSessionMillis() {
        if (launchCount <= 0) return totalTimeInForegroundMillis;
        return totalTimeInForegroundMillis / launchCount;
    }
}
