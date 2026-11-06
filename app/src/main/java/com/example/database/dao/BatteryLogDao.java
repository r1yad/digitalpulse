package com.example.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.database.entity.BatteryLogEntity;

import java.util.List;

@Dao
public interface BatteryLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(BatteryLogEntity log);

    @Query("SELECT * FROM battery_logs ORDER BY timestamp DESC LIMIT 1")
    LiveData<BatteryLogEntity> getLatestLog();

    @Query("SELECT * FROM battery_logs ORDER BY timestamp DESC LIMIT 1")
    BatteryLogEntity getLatestLogSync();

    @Query("SELECT * FROM battery_logs WHERE timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    LiveData<List<BatteryLogEntity>> getLogsSince(long sinceTimestamp);

    @Query("SELECT * FROM battery_logs WHERE timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    List<BatteryLogEntity> getLogsSinceSync(long sinceTimestamp);

    @Query("DELETE FROM battery_logs WHERE timestamp < :cutoffTimestamp")
    int pruneOldLogs(long cutoffTimestamp);
}
