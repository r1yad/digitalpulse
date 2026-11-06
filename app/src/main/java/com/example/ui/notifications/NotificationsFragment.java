package com.example.ui.notifications;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.database.entity.NotificationEntity;
import com.example.utils.PermissionUtils;

public class NotificationsFragment extends Fragment {
    private NotificationsViewModel viewModel;
    private NotificationAdapter adapter;

    private View bannerPermission;
    private View btnGrantPermission;
    private EditText etSearch;
    private ImageView btnClearSearch;
    private TextView tabAll;
    private TextView tabBookmarked;
    private View emptyView;
    private RecyclerView rvNotifications;
    private ImageView btnClearAll;

    private boolean isBookmarkedTab = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        observeViewModel();
    }

    private void initViews(View view) {
        bannerPermission = view.findViewById(R.id.banner_notif_permission);
        btnGrantPermission = view.findViewById(R.id.btn_grant_notif_permission);
        etSearch = view.findViewById(R.id.et_search_notifications);
        btnClearSearch = view.findViewById(R.id.btn_clear_search);
        tabAll = view.findViewById(R.id.tab_notif_all);
        tabBookmarked = view.findViewById(R.id.tab_notif_bookmarked);
        emptyView = view.findViewById(R.id.empty_notifs_view);
        rvNotifications = view.findViewById(R.id.rv_notifications);
        btnClearAll = view.findViewById(R.id.btn_clear_all_notifs);
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(new NotificationAdapter.OnNotificationActionListener() {
            @Override
            public void onToggleBookmark(NotificationEntity notification) {
                viewModel.toggleBookmark(notification);
            }

            @Override
            public void onDelete(NotificationEntity notification) {
                viewModel.delete(notification);
                Toast.makeText(getContext(), "Notification removed", Toast.LENGTH_SHORT).show();
            }
        });
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotifications.setAdapter(adapter);
    }

    private void setupListeners() {
        btnGrantPermission.setOnClickListener(v -> {
            if (getContext() != null) {
                PermissionUtils.requestNotificationListenerAccess(getContext());
            }
        });

        btnClearAll.setOnClickListener(v -> {
            viewModel.clearAll();
            Toast.makeText(getContext(), "Archive cleared", Toast.LENGTH_SHORT).show();
        });

        tabAll.setOnClickListener(v -> selectTab(false));
        tabBookmarked.setOnClickListener(v -> selectTab(true));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String q = s.toString();
                btnClearSearch.setVisibility(q.isEmpty() ? View.GONE : View.VISIBLE);
                viewModel.setSearchQuery(q);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> etSearch.setText(""));
    }

    private void selectTab(boolean bookmarked) {
        isBookmarkedTab = bookmarked;
        viewModel.setOnlyBookmarked(bookmarked);

        if (getContext() == null) return;
        if (bookmarked) {
            tabBookmarked.setBackgroundResource(R.drawable.bg_glass_pill_active);
            tabBookmarked.setTextColor(ContextCompat.getColor(getContext(), R.color.accent_cyan));

            tabAll.setBackgroundResource(R.drawable.bg_glass_pill);
            tabAll.setTextColor(ContextCompat.getColor(getContext(), R.color.text_muted));
        } else {
            tabAll.setBackgroundResource(R.drawable.bg_glass_pill_active);
            tabAll.setTextColor(ContextCompat.getColor(getContext(), R.color.accent_cyan));

            tabBookmarked.setBackgroundResource(R.drawable.bg_glass_pill);
            tabBookmarked.setTextColor(ContextCompat.getColor(getContext(), R.color.text_muted));
        }
    }

    private void observeViewModel() {
        viewModel.getNotifications().observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                rvNotifications.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                rvNotifications.setVisibility(View.VISIBLE);
                adapter.submitList(list);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null) {
            boolean hasListener = PermissionUtils.hasNotificationListenerAccess(getContext());
            bannerPermission.setVisibility(hasListener ? View.GONE : View.VISIBLE);
        }
    }
}
