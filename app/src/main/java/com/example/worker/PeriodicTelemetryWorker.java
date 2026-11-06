package com.example.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.database.AppDatabase;
import com.example.database.entity.BatteryLogEntity;
import com.example.repository.BatteryRepository;
import com.example.repository.ClipboardRepository;
import com.example.repository.NetworkStatsRepository;
import com.example.repository.RuleRepository;
import com.example.repository.UsageStatsRepository;

import java.util.concurrent.TimeUnit;

public class PeriodicTelemetryWorker extends Worker {
    private static final String WORK_NAME = "DigitalPulseTelemetryWork";

    public PeriodicTelemetryWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        try {
            // 1. Log Battery
            BatteryRepository batteryRepo = new BatteryRepository(context);
            batteryRepo.logCurrentState();
            BatteryLogEntity batteryState = batteryRepo.getCurrentBatteryState();

            // 2. Prune expired temporary clipboard items (>24 hours)
            ClipboardRepository clipboardRepo = new ClipboardRepository(context);
            clipboardRepo.pruneExpiredTemporaryEntries();

            // 3. Evaluate Smart Usage Limits
            RuleRepository ruleRepo = new RuleRepository(context);
            ruleRepo.evaluateSmartLimits();

            // 4. Evaluate Automation Rules
            UsageStatsRepository usageRepo = new UsageStatsRepository(context);
            long screenTime = usageRepo.getTodayTotalScreenTimeMillis();

            NetworkStatsRepository netRepo = new NetworkStatsRepository(context);
            long[] netTotals = netRepo.getTodayNetworkTotals();
            long cellularBytes = netTotals[0] + netTotals[1];

            AppDatabase db = AppDatabase.getInstance(context);
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            cal.set(java.util.Calendar.MINUTE, 0);
            int notifCount = db.notificationDao().getNotificationCountSinceSync(cal.getTimeInMillis());

            ruleRepo.evaluateAutomationRules(screenTime, cellularBytes, batteryState.temperature, notifCount);

            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }

    public static void enqueue(Context context) {
        Constraints constraints = new Constraints.Builder()
            .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
            PeriodicTelemetryWorker.class,
            15, TimeUnit.MINUTES
        )
        .setConstraints(constraints)
        .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        );
    }
}
