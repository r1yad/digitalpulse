package com.example.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.database.entity.AutomationRuleEntity;

import java.util.List;

@Dao
public interface AutomationRuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AutomationRuleEntity rule);

    @Update
    void update(AutomationRuleEntity rule);

    @Delete
    void delete(AutomationRuleEntity rule);

    @Query("SELECT * FROM automation_rules ORDER BY id DESC")
    LiveData<List<AutomationRuleEntity>> getAllRules();

    @Query("SELECT * FROM automation_rules WHERE is_enabled = 1")
    List<AutomationRuleEntity> getEnabledRulesSync();

    @Query("SELECT COUNT(*) FROM automation_rules WHERE is_enabled = 1")
    LiveData<Integer> getActiveRuleCount();
}
