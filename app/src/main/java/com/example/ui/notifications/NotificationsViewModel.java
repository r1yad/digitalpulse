package com.example.ui.notifications;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.database.entity.NotificationEntity;
import com.example.repository.NotificationRepository;

import java.util.List;

public class NotificationsViewModel extends AndroidViewModel {
    private final NotificationRepository repo;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> onlyBookmarked = new MutableLiveData<>(false);

    public NotificationsViewModel(@NonNull Application application) {
        super(application);
        this.repo = new NotificationRepository(application);
    }

    public LiveData<List<NotificationEntity>> getNotifications() {
        return Transformations.switchMap(onlyBookmarked, bookmarked -> {
            if (bookmarked) {
                return repo.getBookmarkedNotifications();
            } else {
                return Transformations.switchMap(searchQuery, query -> {
                    if (query == null || query.trim().isEmpty()) {
                        return repo.getAllNotifications();
                    } else {
                        return repo.searchNotifications(query.trim());
                    }
                });
            }
        });
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void setOnlyBookmarked(boolean bookmarked) {
        onlyBookmarked.setValue(bookmarked);
    }

    public void toggleBookmark(NotificationEntity notification) {
        notification.isBookmarked = !notification.isBookmarked;
        repo.update(notification);
    }

    public void delete(NotificationEntity notification) {
        repo.delete(notification);
    }

    public void clearAll() {
        repo.deleteAll();
    }
}
