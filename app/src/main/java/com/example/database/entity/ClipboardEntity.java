package com.example.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "clipboard_entries",
    indices = {
        @Index("timestamp"),
        @Index("storage_state"),
        @Index("category")
    }
)
public class ClipboardEntity {
    public static final String STATE_TEMPORARY = "temporary";
    public static final String STATE_FAVORITE = "favorite";
    public static final String STATE_PERMANENT = "permanent";

    public static final String CAT_TEXT = "text";
    public static final String CAT_LINK = "link";
    public static final String CAT_EMAIL = "email";
    public static final String CAT_CODE = "code";

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "storage_state")
    public String storageState; // temporary, favorite, permanent

    @ColumnInfo(name = "category")
    public String category; // text, link, email, code

    public ClipboardEntity(String content, long timestamp, String storageState, String category) {
        this.content = content != null ? content : "";
        this.timestamp = timestamp;
        this.storageState = storageState != null ? storageState : STATE_TEMPORARY;
        this.category = category != null ? category : CAT_TEXT;
    }
}
