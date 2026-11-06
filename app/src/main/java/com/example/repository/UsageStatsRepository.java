package com.example.repository;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.example.R;
import com.example.model.AppUsageInfo;
import com.example.model.HourlyUsagePoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsageStatsRepository {
    private final Context context;
    private final UsageStatsManager usageStatsManager;
    private final PackageManager packageManager;

    public UsageStatsRepository(Context context) {
        this.context = context.getApplicationContext();
        this.usageStatsManager = (UsageStatsManager) this.context.getSystemService(Context.USAGE_STATS_SERVICE);
        this.packageManager = this.context.getPackageManager();
    }

    public List<AppUsageInfo> getTodayUsageStats() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();
        long end = System.currentTimeMillis();

        return getUsageStatsInRange(start, end);
    }

    public List<AppUsageInfo> getUsageStatsInRange(long startTime, long endTime) {
        if (usageStatsManager == null) return Collections.emptyList();

        Map<String, AppUsageInfo> appMap = new HashMap<>();

        // Query aggregated stats first
        List<UsageStats> statsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        );

        if (statsList != null) {
            for (UsageStats stats : statsList) {
                if (stats.getTotalTimeInForeground() <= 1000) continue; // Ignore under 1s
                String pkg = stats.getPackageName();
                if (isSystemApp(pkg)) continue;

                AppUsageInfo info = getOrCreateAppInfo(appMap, pkg);
                info.addForegroundTime(stats.getTotalTimeInForeground());
            }
        }

        // Query events for launch counts and accurate session intervals
        UsageEvents events = usageStatsManager.queryEvents(startTime, endTime);
        if (events != null) {
            UsageEvents.Event event = new UsageEvents.Event();
            while (events.hasNextEvent()) {
                events.getNextEvent(event);
                String pkg = event.getPackageName();
                if (pkg == null || isSystemApp(pkg)) continue;

                if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                    AppUsageInfo info = getOrCreateAppInfo(appMap, pkg);
                    info.incrementLaunchCount();
                }
            }
        }

        List<AppUsageInfo> result = new ArrayList<>(appMap.values());
        Collections.sort(result, (a, b) -> Long.compare(b.getTotalTimeInForegroundMillis(), a.getTotalTimeInForegroundMillis()));
        return result;
    }

    public long getTodayTotalScreenTimeMillis() {
        List<AppUsageInfo> apps = getTodayUsageStats();
        long total = 0;
        for (AppUsageInfo info : apps) {
            total += info.getTotalTimeInForegroundMillis();
        }
        return total;
    }

    public List<HourlyUsagePoint> getTodayHourlyUsagePoints() {
        List<HourlyUsagePoint> points = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long dayStart = cal.getTimeInMillis();

        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        // Compute hourly buckets from usage events
        float[] hourlyMinutes = new float[24];
        if (usageStatsManager != null) {
            UsageEvents events = usageStatsManager.queryEvents(dayStart, System.currentTimeMillis());
            if (events != null) {
                UsageEvents.Event event = new UsageEvents.Event();
                long lastResumedTime = 0;
                String currentPkg = null;

                while (events.hasNextEvent()) {
                    events.getNextEvent(event);
                    if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                        lastResumedTime = event.getTimeStamp();
                        currentPkg = event.getPackageName();
                    } else if (event.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED && lastResumedTime > 0) {
                        if (currentPkg != null && !isSystemApp(currentPkg)) {
                            long duration = event.getTimeStamp() - lastResumedTime;
                            if (duration > 0 && duration < 3600000 * 2) {
                                Calendar eventCal = Calendar.getInstance();
                                eventCal.setTimeInMillis(lastResumedTime);
                                int h = eventCal.get(Calendar.HOUR_OF_DAY);
                                if (h >= 0 && h < 24) {
                                    hourlyMinutes[h] += (duration / 60000f);
                                }
                            }
                        }
                        lastResumedTime = 0;
                    }
                }
            }
        }

        for (int h = 0; h <= Math.min(23, currentHour); h++) {
            String label = String.format("%02d:00", h);
            points.add(new HourlyUsagePoint(h, Math.min(60f, hourlyMinutes[h]), label));
        }

        if (points.isEmpty()) {
            points.add(new HourlyUsagePoint(0, 0, "00:00"));
        }
        return points;
    }

    private AppUsageInfo getOrCreateAppInfo(Map<String, AppUsageInfo> map, String pkg) {
        AppUsageInfo info = map.get(pkg);
        if (info == null) {
            String label = getAppLabel(pkg);
            Drawable icon = getAppIcon(pkg);
            info = new AppUsageInfo(pkg, label, icon, 0, 0, 0);
            map.put(pkg, info);
        }
        return info;
    }

    private boolean isSystemApp(String pkg) {
        if (pkg.equals(context.getPackageName())) return false;
        if (pkg.startsWith("com.android.systemui") || pkg.equals("android")) return true;
        try {
            ApplicationInfo ai = packageManager.getApplicationInfo(pkg, 0);
            return (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0 && (ai.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public String getAppLabel(String pkg) {
        try {
            ApplicationInfo ai = packageManager.getApplicationInfo(pkg, 0);
            CharSequence label = packageManager.getApplicationLabel(ai);
            return label != null ? label.toString() : pkg;
        } catch (Exception e) {
            return pkg;
        }
    }

    public Drawable getAppIcon(String pkg) {
        try {
            return packageManager.getApplicationIcon(pkg);
        } catch (Exception e) {
            return ContextCompat.getDrawable(context, R.drawable.ic_nav_usage);
        }
    }
}
