package com.example.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "notifications",
    indices = {
        @Index("package_name"),
        @Index("timestamp"),
        @Index("is_bookmarked")
    }
)
public class NotificationEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "package_name")
    public String packageName;

    @ColumnInfo(name = "app_name")
    public String appName;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "is_bookmarked")
    public boolean isBookmarked;

    @ColumnInfo(name = "is_read")
    public boolean isRead;

    public NotificationEntity(String packageName, String appName, String title, String content, long timestamp) {
        this.packageName = packageName != null ? packageName : "";
        this.appName = appName != null ? appName : "Unknown App";
        this.title = title != null ? title : "";
        this.content = content != null ? content : "";
        this.timestamp = timestamp;
        this.isBookmarked = false;
        this.isRead = false;
    }
}
