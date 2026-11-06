package com.example.ui.network;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.database.AppDatabase;
import com.example.model.HourlyUsagePoint;
import com.example.model.NetworkAppUsageInfo;
import com.example.repository.NetworkStatsRepository;

import java.io.File;
import java.util.List;

public class NetworkViewModel extends AndroidViewModel {
    private final NetworkStatsRepository netRepo;

    private final MutableLiveData<long[]> networkTotalsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<NetworkAppUsageInfo>> appUsageLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<HourlyUsagePoint>> trendChartLiveData = new MutableLiveData<>();
    private final MutableLiveData<long[]> realtimeSpeedLiveData = new MutableLiveData<>(new long[]{0, 0});
    private final MutableLiveData<File> exportedCsvLiveData = new MutableLiveData<>();

    public NetworkViewModel(@NonNull Application application) {
        super(application);
        this.netRepo = new NetworkStatsRepository(application);
        refresh();
    }

    public LiveData<long[]> getNetworkTotals() { return networkTotalsLiveData; }
    public LiveData<List<NetworkAppUsageInfo>> getAppUsage() { return appUsageLiveData; }
    public LiveData<List<HourlyUsagePoint>> getTrendChart() { return trendChartLiveData; }
    public LiveData<long[]> getRealtimeSpeed() { return realtimeSpeedLiveData; }
    public LiveData<File> getExportedCsv() { return exportedCsvLiveData; }

    public void refresh() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long[] totals = netRepo.getTodayNetworkTotals();
            List<NetworkAppUsageInfo> apps = netRepo.getTodayPerAppUsage();
            List<HourlyUsagePoint> trend = netRepo.get7DayNetworkTrend();

            networkTotalsLiveData.postValue(totals);
            appUsageLiveData.postValue(apps);
            trendChartLiveData.postValue(trend);
        });
    }

    public void updateSpeed() {
        long[] speeds = netRepo.getRealtimeSpeed();
        realtimeSpeedLiveData.postValue(speeds);
    }

    public void exportCsv() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                File file = netRepo.exportToCsv();
                exportedCsvLiveData.postValue(file);
            } catch (Exception ignored) {}
        });
    }
}
