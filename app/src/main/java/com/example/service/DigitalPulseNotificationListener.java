package com.example.service;

import android.app.Notification;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.example.database.entity.NotificationEntity;
import com.example.repository.NotificationRepository;

public class DigitalPulseNotificationListener extends NotificationListenerService {
    private NotificationRepository notificationRepository;
    private PackageManager packageManager;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationRepository = new NotificationRepository(this);
        packageManager = getPackageManager();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null || sbn.getNotification() == null) return;
        String packageName = sbn.getPackageName();
        if (packageName == null || packageName.equals(getPackageName())) return;

        Notification notification = sbn.getNotification();
        Bundle extras = notification.extras;
        if (extras == null) return;

        CharSequence titleSeq = extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence textSeq = extras.getCharSequence(Notification.EXTRA_TEXT);
        if (textSeq == null) {
            textSeq = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
        }

        String title = titleSeq != null ? titleSeq.toString().trim() : "";
        String content = textSeq != null ? textSeq.toString().trim() : "";

        if (title.isEmpty() && content.isEmpty()) return;

        String appName = getAppLabel(packageName);
        long timestamp = sbn.getPostTime();
        if (timestamp <= 0) timestamp = System.currentTimeMillis();

        NotificationEntity entity = new NotificationEntity(packageName, appName, title, content, timestamp);
        notificationRepository.insert(entity);
    }

    private String getAppLabel(String packageName) {
        try {
            ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 0);
            CharSequence label = packageManager.getApplicationLabel(ai);
            return label != null ? label.toString() : packageName;
        } catch (Exception e) {
            return packageName;
        }
    }
}
