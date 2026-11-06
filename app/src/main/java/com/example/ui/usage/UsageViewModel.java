package com.example.ui.usage;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.database.AppDatabase;
import com.example.database.entity.AppLimitEntity;
import com.example.model.AppUsageInfo;
import com.example.model.HourlyUsagePoint;
import com.example.repository.RuleRepository;
import com.example.repository.UsageStatsRepository;

import java.util.List;

public class UsageViewModel extends AndroidViewModel {
    private final UsageStatsRepository usageRepo;
    private final RuleRepository ruleRepo;

    private final MutableLiveData<List<AppUsageInfo>> appUsageListLiveData = new MutableLiveData<>();
    private final MutableLiveData<Long> totalScreenTimeLiveData = new MutableLiveData<>(0L);
    private final MutableLiveData<Integer> totalLaunchesLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<List<HourlyUsagePoint>> hourlyChartLiveData = new MutableLiveData<>();

    public UsageViewModel(@NonNull Application application) {
        super(application);
        this.usageRepo = new UsageStatsRepository(application);
        this.ruleRepo = new RuleRepository(application);
        refresh();
    }

    public LiveData<List<AppUsageInfo>> getAppUsageList() { return appUsageListLiveData; }
    public LiveData<Long> getTotalScreenTime() { return totalScreenTimeLiveData; }
    public LiveData<Integer> getTotalLaunches() { return totalLaunchesLiveData; }
    public LiveData<List<HourlyUsagePoint>> getHourlyChart() { return hourlyChartLiveData; }

    public void refresh() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<AppUsageInfo> apps = usageRepo.getTodayUsageStats();
            long totalTime = 0;
            int totalLaunches = 0;

            for (AppUsageInfo app : apps) {
                totalTime += app.getTotalTimeInForegroundMillis();
                totalLaunches += app.getLaunchCount();
            }

            List<HourlyUsagePoint> points = usageRepo.getTodayHourlyUsagePoints();

            appUsageListLiveData.postValue(apps);
            totalScreenTimeLiveData.postValue(totalTime);
            totalLaunchesLiveData.postValue(totalLaunches);
            hourlyChartLiveData.postValue(points);
        });
    }

    public void setAppLimit(String packageName, String appName, int minutes) {
        AppLimitEntity limit = new AppLimitEntity(packageName, appName, minutes, true);
        ruleRepo.saveLimit(limit);
    }
}
