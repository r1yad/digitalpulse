package com.example.model;

import android.graphics.drawable.Drawable;

public class NetworkAppUsageInfo {
    private final String packageName;
    private final String appName;
    private final Drawable appIcon;
    private final long rxBytes; // Download
    private final long txBytes; // Upload
    private final long totalBytes;

    public NetworkAppUsageInfo(String packageName, String appName, Drawable appIcon, long rxBytes, long txBytes) {
        this.packageName = packageName;
        this.appName = appName;
        this.appIcon = appIcon;
        this.rxBytes = rxBytes;
        this.txBytes = txBytes;
        this.totalBytes = rxBytes + txBytes;
    }

    public String getPackageName() { return packageName; }
    public String getAppName() { return appName; }
    public Drawable getAppIcon() { return appIcon; }
    public long getRxBytes() { return rxBytes; }
    public long getTxBytes() { return txBytes; }
    public long getTotalBytes() { return totalBytes; }
}
