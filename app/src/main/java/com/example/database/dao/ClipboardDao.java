package com.example.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.database.entity.ClipboardEntity;

import java.util.List;

@Dao
public interface ClipboardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ClipboardEntity item);

    @Update
    void update(ClipboardEntity item);

    @Delete
    void delete(ClipboardEntity item);

    @Query("SELECT * FROM clipboard_entries ORDER BY timestamp DESC")
    LiveData<List<ClipboardEntity>> getAllEntries();

    @Query("SELECT * FROM clipboard_entries WHERE storage_state = :state ORDER BY timestamp DESC")
    LiveData<List<ClipboardEntity>> getEntriesByState(String state);

    @Query("SELECT * FROM clipboard_entries WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    LiveData<List<ClipboardEntity>> searchEntries(String query);

    @Query("SELECT COUNT(*) FROM clipboard_entries")
    LiveData<Integer> getCount();

    @Query("DELETE FROM clipboard_entries WHERE storage_state = 'temporary' AND timestamp < :cutoffTimestamp")
    int pruneExpiredTemporaryEntries(long cutoffTimestamp);

    @Query("DELETE FROM clipboard_entries")
    void deleteAll();
}
