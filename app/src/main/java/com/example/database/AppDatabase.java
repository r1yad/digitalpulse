package com.example.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.database.dao.AppLimitDao;
import com.example.database.dao.AutomationRuleDao;
import com.example.database.dao.BatteryLogDao;
import com.example.database.dao.ClipboardDao;
import com.example.database.dao.NotificationDao;
import com.example.database.entity.AppLimitEntity;
import com.example.database.entity.AutomationRuleEntity;
import com.example.database.entity.BatteryLogEntity;
import com.example.database.entity.ClipboardEntity;
import com.example.database.entity.NotificationEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
    entities = {
        NotificationEntity.class,
        ClipboardEntity.class,
        BatteryLogEntity.class,
        AppLimitEntity.class,
        AutomationRuleEntity.class
    },
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "digital_pulse_db";
    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public abstract NotificationDao notificationDao();
    public abstract ClipboardDao clipboardDao();
    public abstract BatteryLogDao batteryLogDao();
    public abstract AppLimitDao appLimitDao();
    public abstract AutomationRuleDao automationRuleDao();

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
