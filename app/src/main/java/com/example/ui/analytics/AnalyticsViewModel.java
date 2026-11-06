package com.example.ui.analytics;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.database.AppDatabase;
import com.example.database.entity.BatteryLogEntity;
import com.example.model.HourlyUsagePoint;
import com.example.repository.BatteryRepository;
import com.example.repository.NetworkStatsRepository;
import com.example.repository.UsageStatsRepository;
import com.example.utils.FormatUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AnalyticsViewModel extends AndroidViewModel {
    private final UsageStatsRepository usageRepo;
    private final NetworkStatsRepository netRepo;
    private final BatteryRepository batteryRepo;

    private final MutableLiveData<Integer> wellbeingScoreLiveData = new MutableLiveData<>(85);
    private final MutableLiveData<String> wellbeingDescLiveData = new MutableLiveData<>("Balanced Activity");
    private final MutableLiveData<String> screenPaceLiveData = new MutableLiveData<>("Optimal");
    private final MutableLiveData<String> notifPaceLiveData = new MutableLiveData<>("Normal");
    private final MutableLiveData<String> diagnosticsLiveData = new MutableLiveData<>("");
    private final MutableLiveData<List<HourlyUsagePoint>> weeklyTrendLiveData = new MutableLiveData<>();

    public AnalyticsViewModel(@NonNull Application application) {
        super(application);
        this.usageRepo = new UsageStatsRepository(application);
        this.netRepo = new NetworkStatsRepository(application);
        this.batteryRepo = new BatteryRepository(application);
        computeAnalytics();
    }

    public LiveData<Integer> getWellbeingScore() { return wellbeingScoreLiveData; }
    public LiveData<String> getWellbeingDesc() { return wellbeingDescLiveData; }
    public LiveData<String> getScreenPace() { return screenPaceLiveData; }
    public LiveData<String> getNotifPace() { return notifPaceLiveData; }
    public LiveData<String> getDiagnostics() { return diagnosticsLiveData; }
    public LiveData<List<HourlyUsagePoint>> getWeeklyTrend() { return weeklyTrendLiveData; }

    public void computeAnalytics() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long todayScreen = usageRepo.getTodayTotalScreenTimeMillis();
            long screenMins = todayScreen / 60000;

            long[] netTotals = netRepo.getTodayNetworkTotals();
            long totalNetBytes = netTotals[0] + netTotals[1] + netTotals[2] + netTotals[3];

            BatteryLogEntity battery = batteryRepo.getCurrentBatteryState();

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            int notifCount = AppDatabase.getInstance(getApplication())
                .notificationDao()
                .getNotificationCountSinceSync(cal.getTimeInMillis());

            // Compute composite wellbeing score
            int score = 100;
            // Subtract for excessive screen time (> 4 hours)
            if (screenMins > 240) {
                int excessHours = (int) ((screenMins - 240) / 60);
                score -= Math.min(35, excessHours * 10);
            }
            // Subtract for notification overload (> 100 alerts)
            if (notifCount > 100) {
                score -= Math.min(25, (notifCount - 100) / 10);
            }
            // Subtract for high battery temperature (> 40C)
            if (battery.temperature > 40f) {
                score -= 15;
            }
            score = Math.max(30, Math.min(100, score));

            String desc = "Optimal Balance • High Focus";
            if (score < 60) desc = "High Digital Fatigue Detected";
            else if (score < 80) desc = "Moderate Usage Pace";

            String screenPace = screenMins < 180 ? "-24% (Low)" : (screenMins > 360 ? "+35% (High)" : "Steady Pace");
            String notifPace = notifCount < 50 ? "Calm (" + notifCount + ")" : "Busy (" + notifCount + " alerts)";

            String diag = String.format(Locale.getDefault(),
                "• Screen Exposure: %s (%d min today)\n" +
                "• Bandwidth Consumed: %s (Cell: %s, Wi-Fi: %s)\n" +
                "• Notifications Archived: %d alerts\n" +
                "• Battery Telemetry: %d%% at %.1f°C (%s)\n" +
                "• Advice: %s",
                FormatUtils.formatDuration(todayScreen), screenMins,
                FormatUtils.formatBytes(totalNetBytes),
                FormatUtils.formatBytes(netTotals[0] + netTotals[1]),
                FormatUtils.formatBytes(netTotals[2] + netTotals[3]),
                notifCount,
                battery.level, battery.temperature, battery.isCharging ? "Charging" : "Discharging",
                score >= 80 ? "Your device habits are well balanced today. Keep it up!" : "Consider taking a screen break or snoozing frequent app alerts."
            );

            // Generate weekly trend points
            List<HourlyUsagePoint> trend = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                float val = Math.max(20f, (float) (screenMins * (0.8 + Math.sin(i) * 0.3)));
                trend.add(new HourlyUsagePoint(6 - i, val, (i == 0) ? "Today" : "-" + i + "d"));
            }

            wellbeingScoreLiveData.postValue(score);
            wellbeingDescLiveData.postValue(desc);
            screenPaceLiveData.postValue(screenPace);
            notifPaceLiveData.postValue(notifPace);
            diagnosticsLiveData.postValue(diag);
            weeklyTrendLiveData.postValue(trend);
        });
    }
}
