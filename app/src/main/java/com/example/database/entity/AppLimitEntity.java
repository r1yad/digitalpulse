package com.example.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "app_limits",
    indices = {
        @Index(value = "package_name", unique = true)
    }
)
public class AppLimitEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "package_name")
    public String packageName;

    @ColumnInfo(name = "app_name")
    public String appName;

    @ColumnInfo(name = "daily_limit_minutes")
    public int dailyLimitMinutes;

    @ColumnInfo(name = "is_enabled")
    public boolean isEnabled;

    @ColumnInfo(name = "warning_75_sent")
    public boolean warning75Sent;

    @ColumnInfo(name = "warning_90_sent")
    public boolean warning90Sent;

    @ColumnInfo(name = "warning_100_sent")
    public boolean warning100Sent;

    public AppLimitEntity(String packageName, String appName, int dailyLimitMinutes, boolean isEnabled) {
        this.packageName = packageName != null ? packageName : "";
        this.appName = appName != null ? appName : "App";
        this.dailyLimitMinutes = dailyLimitMinutes;
        this.isEnabled = isEnabled;
        this.warning75Sent = false;
        this.warning90Sent = false;
        this.warning100Sent = false;
    }
}
