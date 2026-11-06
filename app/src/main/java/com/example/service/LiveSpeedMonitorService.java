package com.example.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.MainActivity;
import com.example.R;
import com.example.repository.NetworkStatsRepository;
import com.example.utils.FormatUtils;
import com.example.utils.NotificationHelper;

public class LiveSpeedMonitorService extends Service {
    private static final int NOTIFICATION_ID = 9101;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private NetworkStatsRepository networkStatsRepository;
    private boolean isRunning = false;

    private final Runnable speedUpdater = new Runnable() {
        @Override
        public void run() {
            if (!isRunning) return;
            long[] speeds = networkStatsRepository.getRealtimeSpeed();
            long rx = speeds[0];
            long tx = speeds[1];

            updateNotification(rx, tx);
            handler.postDelayed(this, 1500);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        networkStatsRepository = new NetworkStatsRepository(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            isRunning = true;
            NotificationHelper.initChannels(this);
            Notification notification = buildSpeedNotification(0, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            } else {
                startForeground(NOTIFICATION_ID, notification);
            }
            handler.post(speedUpdater);
        }
        return START_STICKY;
    }

    private void updateNotification(long rxBytesPerSec, long txBytesPerSec) {
        Notification notification = buildSpeedNotification(rxBytesPerSec, txBytesPerSec);
        android.app.NotificationManager manager = getSystemService(android.app.NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }
    }

    private Notification buildSpeedNotification(long rxBytes, long txBytes) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String speedText = String.format("▼ %s  ▲ %s", FormatUtils.formatSpeed(rxBytes), FormatUtils.formatSpeed(txBytes));

        return new NotificationCompat.Builder(this, NotificationHelper.CHANNEL_SPEED)
            .setSmallIcon(R.drawable.ic_speed)
            .setContentTitle("DigitalPulse Network Speed")
            .setContentText(speedText)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        handler.removeCallbacks(speedUpdater);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
