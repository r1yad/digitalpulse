package com.example.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.database.AppDatabase;
import com.example.database.entity.BatteryLogEntity;
import com.example.model.AppUsageInfo;
import com.example.model.DailySummaryInfo;
import com.example.model.HourlyUsagePoint;
import com.example.repository.BatteryRepository;
import com.example.repository.NetworkStatsRepository;
import com.example.repository.UsageStatsRepository;

import java.util.List;

public class DashboardViewModel extends AndroidViewModel {
    private final UsageStatsRepository usageRepo;
    private final NetworkStatsRepository netRepo;
    private final BatteryRepository batteryRepo;

    private final MutableLiveData<DailySummaryInfo> summaryLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<HourlyUsagePoint>> chartPointsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        this.usageRepo = new UsageStatsRepository(application);
        this.netRepo = new NetworkStatsRepository(application);
        this.batteryRepo = new BatteryRepository(application);
        refresh();
    }

    public LiveData<DailySummaryInfo> getSummary() {
        return summaryLiveData;
    }

    public LiveData<List<HourlyUsagePoint>> getChartPoints() {
        return chartPointsLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void refresh() {
        isLoading.setValue(true);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Screen time & apps
                List<AppUsageInfo> usages = usageRepo.getTodayUsageStats();
                long totalScreenTime = 0;
                String topAppName = "No usage yet";
                android.graphics.drawable.Drawable topIcon = null;
                long topDuration = 0;

                if (!usages.isEmpty()) {
                    AppUsageInfo top = usages.get(0);
                    topAppName = top.getAppName();
                    topIcon = top.getAppIcon();
                    topDuration = top.getTotalTimeInForegroundMillis();

                    for (AppUsageInfo u : usages) {
                        totalScreenTime += u.getTotalTimeInForegroundMillis();
                    }
                }

                // Network
                long[] netTotals = netRepo.getTodayNetworkTotals();
                long cellular = netTotals[0] + netTotals[1];
                long wifi = netTotals[2] + netTotals[3];

                // Battery
                BatteryLogEntity battery = batteryRepo.getCurrentBatteryState();

                // Notification count
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                int notifCount = AppDatabase.getInstance(getApplication())
                    .notificationDao()
                    .getNotificationCountSinceSync(cal.getTimeInMillis());

                DailySummaryInfo summary = new DailySummaryInfo(
                    totalScreenTime,
                    cellular,
                    wifi,
                    notifCount,
                    0,
                    battery.level,
                    battery.temperature,
                    battery.isCharging,
                    topAppName,
                    topIcon,
                    topDuration
                );

                List<HourlyUsagePoint> points = usageRepo.getTodayHourlyUsagePoints();

                summaryLiveData.postValue(summary);
                chartPointsLiveData.postValue(points);
            } catch (Exception ignored) {
            } finally {
                isLoading.postValue(false);
            }
        });
    }
}
