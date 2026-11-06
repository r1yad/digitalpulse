package com.example.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "battery_logs",
    indices = {
        @Index("timestamp")
    }
)
public class BatteryLogEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "level")
    public int level; // 0 - 100

    @ColumnInfo(name = "temperature")
    public float temperature; // Celsius

    @ColumnInfo(name = "voltage")
    public int voltage; // mV

    @ColumnInfo(name = "is_charging")
    public boolean isCharging;

    @ColumnInfo(name = "charge_plug")
    public String chargePlug; // USB, AC, Wireless, None

    public BatteryLogEntity(long timestamp, int level, float temperature, int voltage, boolean isCharging, String chargePlug) {
        this.timestamp = timestamp;
        this.level = level;
        this.temperature = temperature;
        this.voltage = voltage;
        this.isCharging = isCharging;
        this.chargePlug = chargePlug != null ? chargePlug : "None";
    }
}
