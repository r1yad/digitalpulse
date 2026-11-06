package com.example.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.database.entity.AppLimitEntity;

import java.util.List;

@Dao
public interface AppLimitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AppLimitEntity limit);

    @Update
    void update(AppLimitEntity limit);

    @Delete
    void delete(AppLimitEntity limit);

    @Query("SELECT * FROM app_limits ORDER BY app_name ASC")
    LiveData<List<AppLimitEntity>> getAllLimits();

    @Query("SELECT * FROM app_limits WHERE is_enabled = 1")
    List<AppLimitEntity> getEnabledLimitsSync();

    @Query("SELECT * FROM app_limits WHERE package_name = :packageName LIMIT 1")
    AppLimitEntity getLimitForPackageSync(String packageName);

    @Query("UPDATE app_limits SET warning_75_sent = 0, warning_90_sent = 0, warning_100_sent = 0")
    void resetDailyWarningFlags();
}
