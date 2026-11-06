package com.example.repository;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Patterns;

import androidx.lifecycle.LiveData;

import com.example.database.AppDatabase;
import com.example.database.dao.ClipboardDao;
import com.example.database.entity.ClipboardEntity;

import java.util.List;
import java.util.regex.Pattern;

public class ClipboardRepository {
    private final ClipboardDao clipboardDao;
    private final ClipboardManager clipboardManager;
    private static final Pattern CODE_PATTERN = Pattern.compile("(\\{.*\\}|class\\s|void\\s|function\\s|import\\s|<.*>|def\\s|var\\s|const\\s)", Pattern.DOTALL);

    public ClipboardRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.clipboardDao = db.clipboardDao();
        this.clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    public LiveData<List<ClipboardEntity>> getAllEntries() {
        return clipboardDao.getAllEntries();
    }

    public LiveData<List<ClipboardEntity>> getEntriesByState(String state) {
        return clipboardDao.getEntriesByState(state);
    }

    public LiveData<List<ClipboardEntity>> searchEntries(String query) {
        return clipboardDao.searchEntries(query);
    }

    public LiveData<Integer> getCount() {
        return clipboardDao.getCount();
    }

    public void saveClipboardText(String text, String state) {
        if (text == null || text.trim().isEmpty()) return;
        String category = detectCategory(text);
        ClipboardEntity entity = new ClipboardEntity(text.trim(), System.currentTimeMillis(), state, category);
        AppDatabase.databaseWriteExecutor.execute(() -> clipboardDao.insert(entity));
    }

    public void update(ClipboardEntity entity) {
        AppDatabase.databaseWriteExecutor.execute(() -> clipboardDao.update(entity));
    }

    public void delete(ClipboardEntity entity) {
        AppDatabase.databaseWriteExecutor.execute(() -> clipboardDao.delete(entity));
    }

    public void pruneExpiredTemporaryEntries() {
        long cutoff = System.currentTimeMillis() - (24 * 3600 * 1000L); // 24 hours
        AppDatabase.databaseWriteExecutor.execute(() -> clipboardDao.pruneExpiredTemporaryEntries(cutoff));
    }

    public void copyToSystemClipboard(String text) {
        if (clipboardManager != null && text != null) {
            ClipData clip = ClipData.newPlainText("DigitalPulse", text);
            clipboardManager.setPrimaryClip(clip);
        }
    }

    public String getCurrentSystemClipboardText() {
        if (clipboardManager != null && clipboardManager.hasPrimaryClip()) {
            ClipData clip = clipboardManager.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                CharSequence text = clip.getItemAt(0).getText();
                if (text != null) return text.toString();
            }
        }
        return null;
    }

    private String detectCategory(String text) {
        if (Patterns.WEB_URL.matcher(text).matches() || text.startsWith("http://") || text.startsWith("https://")) {
            return ClipboardEntity.CAT_LINK;
        }
        if (Patterns.EMAIL_ADDRESS.matcher(text).matches()) {
            return ClipboardEntity.CAT_EMAIL;
        }
        if (CODE_PATTERN.matcher(text).find()) {
            return ClipboardEntity.CAT_CODE;
        }
        return ClipboardEntity.CAT_TEXT;
    }
}
