package com.example.repository;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.Environment;

import androidx.core.content.ContextCompat;

import com.example.R;
import com.example.model.HourlyUsagePoint;
import com.example.model.NetworkAppUsageInfo;
import com.example.utils.FormatUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkStatsRepository {
    private final Context context;
    private final NetworkStatsManager networkStatsManager;
    private final PackageManager packageManager;

    private static long lastRxBytes = -1;
    private static long lastTxBytes = -1;
    private static long lastSpeedCheckTime = -1;

    public NetworkStatsRepository(Context context) {
        this.context = context.getApplicationContext();
        this.networkStatsManager = (NetworkStatsManager) this.context.getSystemService(Context.NETWORK_STATS_SERVICE);
        this.packageManager = this.context.getPackageManager();
    }

    public long[] getTodayNetworkTotals() {
        // [0] = Cellular Rx, [1] = Cellular Tx, [2] = Wifi Rx, [3] = Wifi Tx
        long[] totals = new long[4];
        if (networkStatsManager == null) return totals;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();
        long end = System.currentTimeMillis();

        try {
            // Cellular
            NetworkStats.Bucket cellBucket = networkStatsManager.querySummaryForDevice(
                ConnectivityManager.TYPE_MOBILE,
                null,
                start,
                end
            );
            if (cellBucket != null) {
                totals[0] = cellBucket.getRxBytes();
                totals[1] = cellBucket.getTxBytes();
            }
        } catch (Exception ignored) {}

        try {
            // Wi-Fi
            NetworkStats.Bucket wifiBucket = networkStatsManager.querySummaryForDevice(
                ConnectivityManager.TYPE_WIFI,
                null,
                start,
                end
            );
            if (wifiBucket != null) {
                totals[2] = wifiBucket.getRxBytes();
                totals[3] = wifiBucket.getTxBytes();
            }
        } catch (Exception ignored) {}

        // Fallback to TrafficStats if NetworkStatsManager returns 0
        if (totals[0] == 0 && totals[1] == 0 && totals[2] == 0 && totals[3] == 0) {
            long totalMobileRx = TrafficStats.getMobileRxBytes();
            long totalMobileTx = TrafficStats.getMobileTxBytes();
            long totalRx = TrafficStats.getTotalRxBytes();
            long totalTx = TrafficStats.getTotalTxBytes();

            totals[0] = Math.max(0, totalMobileRx);
            totals[1] = Math.max(0, totalMobileTx);
            totals[2] = Math.max(0, totalRx - totalMobileRx);
            totals[3] = Math.max(0, totalTx - totalMobileTx);
        }

        return totals;
    }

    public List<NetworkAppUsageInfo> getTodayPerAppUsage() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();
        long end = System.currentTimeMillis();

        return getPerAppUsageInRange(start, end);
    }

    public List<NetworkAppUsageInfo> getPerAppUsageInRange(long start, long end) {
        List<NetworkAppUsageInfo> list = new ArrayList<>();
        if (networkStatsManager == null) return list;

        Map<Integer, long[]> uidBytes = new HashMap<>();

        try {
            // Cellular per UID
            NetworkStats cellStats = networkStatsManager.querySummary(ConnectivityManager.TYPE_MOBILE, null, start, end);
            aggregateStats(cellStats, uidBytes);
        } catch (Exception ignored) {}

        try {
            // Wi-Fi per UID
            NetworkStats wifiStats = networkStatsManager.querySummary(ConnectivityManager.TYPE_WIFI, null, start, end);
            aggregateStats(wifiStats, uidBytes);
        } catch (Exception ignored) {}

        for (Map.Entry<Integer, long[]> entry : uidBytes.entrySet()) {
            int uid = entry.getKey();
            long rx = entry.getValue()[0];
            long tx = entry.getValue()[1];
            if (rx + tx < 1024) continue; // Ignore under 1 KB

            String[] packages = packageManager.getPackagesForUid(uid);
            String pkg = (packages != null && packages.length > 0) ? packages[0] : "UID " + uid;
            String label = getAppLabel(pkg);
            Drawable icon = getAppIcon(pkg);

            list.add(new NetworkAppUsageInfo(pkg, label, icon, rx, tx));
        }

        Collections.sort(list, (a, b) -> Long.compare(b.getTotalBytes(), a.getTotalBytes()));
        return list;
    }

    private void aggregateStats(NetworkStats stats, Map<Integer, long[]> uidBytes) {
        if (stats == null) return;
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        while (stats.hasNextBucket()) {
            stats.getNextBucket(bucket);
            int uid = bucket.getUid();
            if (uid < 10000) continue; // Ignore kernel / root system UIDs

            long[] cur = uidBytes.get(uid);
            if (cur == null) {
                cur = new long[2];
                uidBytes.put(uid, cur);
            }
            cur[0] += bucket.getRxBytes();
            cur[1] += bucket.getTxBytes();
        }
        stats.close();
    }

    public long[] getRealtimeSpeed() {
        // [0] = Download bytes/sec, [1] = Upload bytes/sec
        long now = System.currentTimeMillis();
        long currentRx = TrafficStats.getTotalRxBytes();
        long currentTx = TrafficStats.getTotalTxBytes();

        long[] speeds = new long[2];

        if (lastRxBytes >= 0 && lastSpeedCheckTime > 0) {
            long timeDeltaSec = (now - lastSpeedCheckTime) / 1000;
            if (timeDeltaSec > 0) {
                long rxDelta = currentRx - lastRxBytes;
                long txDelta = currentTx - lastTxBytes;
                speeds[0] = Math.max(0, rxDelta / timeDeltaSec);
                speeds[1] = Math.max(0, txDelta / timeDeltaSec);
            }
        }

        lastRxBytes = currentRx;
        lastTxBytes = currentTx;
        lastSpeedCheckTime = now;

        return speeds;
    }

    public List<HourlyUsagePoint> get7DayNetworkTrend() {
        List<HourlyUsagePoint> points = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        for (int i = 6; i >= 0; i--) {
            Calendar dayCal = (Calendar) cal.clone();
            dayCal.add(Calendar.DAY_OF_YEAR, -i);
            dayCal.set(Calendar.HOUR_OF_DAY, 0);
            dayCal.set(Calendar.MINUTE, 0);
            dayCal.set(Calendar.SECOND, 0);
            dayCal.set(Calendar.MILLISECOND, 0);

            long start = dayCal.getTimeInMillis();
            dayCal.set(Calendar.HOUR_OF_DAY, 23);
            dayCal.set(Calendar.MINUTE, 59);
            dayCal.set(Calendar.SECOND, 59);
            long end = dayCal.getTimeInMillis();

            long totalBytes = 0;
            if (networkStatsManager != null) {
                try {
                    NetworkStats.Bucket bCell = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, start, end);
                    if (bCell != null) totalBytes += bCell.getRxBytes() + bCell.getTxBytes();
                    NetworkStats.Bucket bWifi = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, null, start, end);
                    if (bWifi != null) totalBytes += bWifi.getRxBytes() + bWifi.getTxBytes();
                } catch (Exception ignored) {}
            }

            float mb = totalBytes / (1024f * 1024f);
            String dayLabel = (i == 0) ? "Today" : String.format(java.util.Locale.getDefault(), "-%dd", i);
            points.add(new HourlyUsagePoint(6 - i, mb, dayLabel));
        }

        return points;
    }

    public File exportToCsv() throws Exception {
        List<NetworkAppUsageInfo> apps = getTodayPerAppUsage();
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (dir == null) dir = context.getFilesDir();

        File file = new File(dir, "digitalpulse_network_export_" + System.currentTimeMillis() + ".csv");
        PrintWriter pw = new PrintWriter(new FileWriter(file));
        pw.println("Package,Application,Download_Bytes,Upload_Bytes,Total_Bytes,Formatted_Total");

        for (NetworkAppUsageInfo app : apps) {
            pw.printf("\"%s\",\"%s\",%d,%d,%d,\"%s\"%n",
                app.getPackageName(),
                app.getAppName().replace("\"", "\"\""),
                app.getRxBytes(),
                app.getTxBytes(),
                app.getTotalBytes(),
                FormatUtils.formatBytes(app.getTotalBytes())
            );
        }
        pw.flush();
        pw.close();
        return file;
    }

    private String getAppLabel(String pkg) {
        try {
            ApplicationInfo ai = packageManager.getApplicationInfo(pkg, 0);
            CharSequence label = packageManager.getApplicationLabel(ai);
            return label != null ? label.toString() : pkg;
        } catch (Exception e) {
            return pkg;
        }
    }

    private Drawable getAppIcon(String pkg) {
        try {
            return packageManager.getApplicationIcon(pkg);
        } catch (Exception e) {
            return ContextCompat.getDrawable(context, R.drawable.ic_nav_network);
        }
    }
}
