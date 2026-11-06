package com.example.repository;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import androidx.lifecycle.LiveData;

import com.example.database.AppDatabase;
import com.example.database.dao.BatteryLogDao;
import com.example.database.entity.BatteryLogEntity;
import com.example.model.HourlyUsagePoint;

import java.util.ArrayList;
import java.util.List;

public class BatteryRepository {
    private final Context context;
    private final BatteryLogDao batteryLogDao;

    public BatteryRepository(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(context);
        this.batteryLogDao = db.batteryLogDao();
    }

    public BatteryLogEntity getCurrentBatteryState() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        if (batteryStatus == null) {
            return new BatteryLogEntity(System.currentTimeMillis(), 100, 25.0f, 4000, false, "None");
        }

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int batteryPct = (level >= 0 && scale > 0) ? (int) ((level / (float) scale) * 100) : 50;

        int temp = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
        float tempCelsius = temp / 10.0f;

        int voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                             status == BatteryManager.BATTERY_STATUS_FULL;

        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        String plugType = "Battery";
        if (chargePlug == BatteryManager.BATTERY_PLUGGED_USB) plugType = "USB";
        else if (chargePlug == BatteryManager.BATTERY_PLUGGED_AC) plugType = "AC Charger";
        else if (chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS) plugType = "Wireless";

        return new BatteryLogEntity(System.currentTimeMillis(), batteryPct, tempCelsius, voltage, isCharging, plugType);
    }

    public void logCurrentState() {
        BatteryLogEntity state = getCurrentBatteryState();
        AppDatabase.databaseWriteExecutor.execute(() -> batteryLogDao.insert(state));
    }

    public LiveData<BatteryLogEntity> getLatestLog() {
        return batteryLogDao.getLatestLog();
    }

    public LiveData<List<BatteryLogEntity>> getTodayBatteryHistory() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return batteryLogDao.getLogsSince(cal.getTimeInMillis());
    }

    public List<HourlyUsagePoint> getBatteryCurvePoints() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);

        List<BatteryLogEntity> logs = batteryLogDao.getLogsSinceSync(cal.getTimeInMillis());
        List<HourlyUsagePoint> points = new ArrayList<>();

        if (logs == null || logs.isEmpty()) {
            BatteryLogEntity current = getCurrentBatteryState();
            points.add(new HourlyUsagePoint(0, current.level, "Now"));
            return points;
        }

        for (int i = 0; i < logs.size(); i++) {
            BatteryLogEntity log = logs.get(i);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            String timeStr = sdf.format(new java.util.Date(log.timestamp));
            points.add(new HourlyUsagePoint(i, log.level, timeStr));
        }

        return points;
    }
}
