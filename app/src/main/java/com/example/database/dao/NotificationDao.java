package com.example.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.database.entity.NotificationEntity;

import java.util.List;

@Dao
public interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(NotificationEntity notification);

    @Update
    void update(NotificationEntity notification);

    @Delete
    void delete(NotificationEntity notification);

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    LiveData<List<NotificationEntity>> getAllNotifications();

    @Query("SELECT * FROM notifications WHERE is_bookmarked = 1 ORDER BY timestamp DESC")
    LiveData<List<NotificationEntity>> getBookmarkedNotifications();

    @Query("SELECT * FROM notifications WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR app_name LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    LiveData<List<NotificationEntity>> searchNotifications(String query);

    @Query("SELECT * FROM notifications WHERE package_name = :packageName ORDER BY timestamp DESC")
    LiveData<List<NotificationEntity>> getNotificationsByApp(String packageName);

    @Query("SELECT COUNT(*) FROM notifications WHERE timestamp >= :sinceTimestamp")
    LiveData<Integer> getNotificationCountSince(long sinceTimestamp);

    @Query("SELECT COUNT(*) FROM notifications WHERE timestamp >= :sinceTimestamp")
    int getNotificationCountSinceSync(long sinceTimestamp);

    @Query("SELECT DISTINCT package_name FROM notifications ORDER BY package_name ASC")
    LiveData<List<String>> getDistinctPackages();

    @Query("DELETE FROM notifications WHERE is_bookmarked = 0 AND timestamp < :cutoffTimestamp")
    int deleteOldNotifications(long cutoffTimestamp);

    @Query("DELETE FROM notifications")
    void deleteAll();
}
