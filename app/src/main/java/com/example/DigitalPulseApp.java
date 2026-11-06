package com.example;

import android.app.Application;

import com.example.database.AppDatabase;
import com.example.repository.BatteryRepository;
import com.example.utils.NotificationHelper;
import com.example.worker.PeriodicTelemetryWorker;

public class DigitalPulseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize notification channels
        NotificationHelper.initChannels(this);

        // Pre-warm database
        AppDatabase.getInstance(this);

        // Log initial battery status
        AppDatabase.databaseWriteExecutor.execute(() -> {
            new BatteryRepository(this).logCurrentState();
        });

        // Enqueue periodic telemetry worker
        PeriodicTelemetryWorker.enqueue(this);
    }
}
