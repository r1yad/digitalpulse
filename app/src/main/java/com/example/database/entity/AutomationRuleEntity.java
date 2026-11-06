package com.example.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "automation_rules")
public class AutomationRuleEntity {
    public static final String TYPE_SCREEN_TIME = "screen_time";
    public static final String TYPE_MOBILE_DATA = "mobile_data";
    public static final String TYPE_BATTERY_TEMP = "battery_temp";
    public static final String TYPE_NOTIF_SPIKE = "notif_spike";

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "rule_type")
    public String ruleType; // screen_time, mobile_data, battery_temp, notif_spike

    @ColumnInfo(name = "target_package")
    public String targetPackage; // optional, e.g. "com.google.android.youtube" or empty for all

    @ColumnInfo(name = "threshold_value")
    public double thresholdValue; // e.g. minutes, MB, Celsius, count

    @ColumnInfo(name = "message")
    public String message;

    @ColumnInfo(name = "is_enabled")
    public boolean isEnabled;

    @ColumnInfo(name = "last_triggered_timestamp")
    public long lastTriggeredTimestamp;

    public AutomationRuleEntity(String name, String ruleType, String targetPackage, double thresholdValue, String message, boolean isEnabled) {
        this.name = name != null ? name : "Rule";
        this.ruleType = ruleType != null ? ruleType : TYPE_SCREEN_TIME;
        this.targetPackage = targetPackage != null ? targetPackage : "";
        this.thresholdValue = thresholdValue;
        this.message = message != null ? message : "Triggered rule threshold";
        this.isEnabled = isEnabled;
        this.lastTriggeredTimestamp = 0;
    }
}
