package com.example.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.database.AppDatabase;
import com.example.database.dao.NotificationDao;
import com.example.database.entity.NotificationEntity;

import java.util.List;

public class NotificationRepository {
    private final NotificationDao notificationDao;

    public NotificationRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.notificationDao = db.notificationDao();
    }

    public LiveData<List<NotificationEntity>> getAllNotifications() {
        return notificationDao.getAllNotifications();
    }

    public LiveData<List<NotificationEntity>> getBookmarkedNotifications() {
        return notificationDao.getBookmarkedNotifications();
    }

    public LiveData<List<NotificationEntity>> searchNotifications(String query) {
        return notificationDao.searchNotifications(query);
    }

    public LiveData<List<NotificationEntity>> getNotificationsByApp(String packageName) {
        return notificationDao.getNotificationsByApp(packageName);
    }

    public LiveData<Integer> getTodayNotificationCount() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return notificationDao.getNotificationCountSince(cal.getTimeInMillis());
    }

    public LiveData<List<String>> getDistinctPackages() {
        return notificationDao.getDistinctPackages();
    }

    public void insert(NotificationEntity entity) {
        AppDatabase.databaseWriteExecutor.execute(() -> notificationDao.insert(entity));
    }

    public void update(NotificationEntity entity) {
        AppDatabase.databaseWriteExecutor.execute(() -> notificationDao.update(entity));
    }

    public void delete(NotificationEntity entity) {
        AppDatabase.databaseWriteExecutor.execute(() -> notificationDao.delete(entity));
    }

    public void pruneOldNotifications(int days) {
        long cutoff = System.currentTimeMillis() - (days * 86400000L);
        AppDatabase.databaseWriteExecutor.execute(() -> notificationDao.deleteOldNotifications(cutoff));
    }

    public void deleteAll() {
        AppDatabase.databaseWriteExecutor.execute(notificationDao::deleteAll);
    }
}
