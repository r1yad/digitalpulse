package com.example.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.database.AppDatabase;
import com.example.database.dao.AppLimitDao;
import com.example.database.dao.AutomationRuleDao;
import com.example.database.entity.AppLimitEntity;
import com.example.database.entity.AutomationRuleEntity;
import com.example.model.AppUsageInfo;
import com.example.utils.NotificationHelper;

import java.util.List;

public class RuleRepository {
    private final Context context;
    private final AppLimitDao appLimitDao;
    private final AutomationRuleDao automationRuleDao;
    private final UsageStatsRepository usageStatsRepository;

    public RuleRepository(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(context);
        this.appLimitDao = db.appLimitDao();
        this.automationRuleDao = db.automationRuleDao();
        this.usageStatsRepository = new UsageStatsRepository(context);
    }

    public LiveData<List<AppLimitEntity>> getAllLimits() {
        return appLimitDao.getAllLimits();
    }

    public void saveLimit(AppLimitEntity limit) {
        AppDatabase.databaseWriteExecutor.execute(() -> appLimitDao.insert(limit));
    }

    public void updateLimit(AppLimitEntity limit) {
        AppDatabase.databaseWriteExecutor.execute(() -> appLimitDao.update(limit));
    }

    public void deleteLimit(AppLimitEntity limit) {
        AppDatabase.databaseWriteExecutor.execute(() -> appLimitDao.delete(limit));
    }

    public LiveData<List<AutomationRuleEntity>> getAllRules() {
        return automationRuleDao.getAllRules();
    }

    public LiveData<Integer> getActiveRuleCount() {
        return automationRuleDao.getActiveRuleCount();
    }

    public void saveRule(AutomationRuleEntity rule) {
        AppDatabase.databaseWriteExecutor.execute(() -> automationRuleDao.insert(rule));
    }

    public void updateRule(AutomationRuleEntity rule) {
        AppDatabase.databaseWriteExecutor.execute(() -> automationRuleDao.update(rule));
    }

    public void deleteRule(AutomationRuleEntity rule) {
        AppDatabase.databaseWriteExecutor.execute(() -> automationRuleDao.delete(rule));
    }

    public void evaluateSmartLimits() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<AppLimitEntity> limits = appLimitDao.getEnabledLimitsSync();
            if (limits == null || limits.isEmpty()) return;

            List<AppUsageInfo> usages = usageStatsRepository.getTodayUsageStats();
            for (AppLimitEntity limit : limits) {
                for (AppUsageInfo usage : usages) {
                    if (usage.getPackageName().equals(limit.packageName)) {
                        long usedMinutes = usage.getTotalTimeInForegroundMillis() / 60000;
                        int targetMins = limit.dailyLimitMinutes;
                        if (targetMins <= 0) break;

                        float pct = (usedMinutes / (float) targetMins) * 100f;

                        // Check 100% limit
                        if (pct >= 100f && !limit.warning100Sent) {
                            NotificationHelper.postAppLimitWarning(context, limit.appName, 100, targetMins);
                            limit.warning100Sent = true;
                            appLimitDao.update(limit);
                        } else if (pct >= 90f && !limit.warning90Sent) {
                            NotificationHelper.postAppLimitWarning(context, limit.appName, 90, targetMins);
                            limit.warning90Sent = true;
                            appLimitDao.update(limit);
                        } else if (pct >= 75f && !limit.warning75Sent) {
                            NotificationHelper.postAppLimitWarning(context, limit.appName, 75, targetMins);
                            limit.warning75Sent = true;
                            appLimitDao.update(limit);
                        }
                        break;
                    }
                }
            }
        });
    }

    public void evaluateAutomationRules(long todayScreenTimeMillis, long todayCellularBytes, float batteryTemp, int todayNotifCount) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<AutomationRuleEntity> rules = automationRuleDao.getEnabledRulesSync();
            if (rules == null || rules.isEmpty()) return;

            long now = System.currentTimeMillis();
            for (AutomationRuleEntity rule : rules) {
                // Rate-limit notifications to once per 2 hours
                if (now - rule.lastTriggeredTimestamp < 7200000L) continue;

                boolean triggered = false;
                switch (rule.ruleType) {
                    case AutomationRuleEntity.TYPE_SCREEN_TIME:
                        long screenMins = todayScreenTimeMillis / 60000;
                        if (screenMins >= rule.thresholdValue) triggered = true;
                        break;
                    case AutomationRuleEntity.TYPE_MOBILE_DATA:
                        double dataMB = todayCellularBytes / (1024.0 * 1024.0);
                        if (dataMB >= rule.thresholdValue) triggered = true;
                        break;
                    case AutomationRuleEntity.TYPE_BATTERY_TEMP:
                        if (batteryTemp >= rule.thresholdValue) triggered = true;
                        break;
                    case AutomationRuleEntity.TYPE_NOTIF_SPIKE:
                        if (todayNotifCount >= rule.thresholdValue) triggered = true;
                        break;
                }

                if (triggered) {
                    NotificationHelper.postAutomationRuleAlert(context, rule.name, rule.message);
                    rule.lastTriggeredTimestamp = now;
                    automationRuleDao.update(rule);
                }
            }
        });
    }
}
