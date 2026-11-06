package com.example.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.example.R;
import com.example.utils.PermissionUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SettingsBottomSheet extends BottomSheetDialogFragment {
    private static final String PREF_NAME = "digital_pulse_prefs";
    private static final String KEY_THEME = "key_theme_mode";

    private RadioGroup rgTheme;
    private RadioButton rbDark;
    private RadioButton rbLight;
    private RadioButton rbSystem;
    private TextView tvStatusUsage;
    private TextView tvStatusNotif;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupThemePreference();
        updatePermissionStatuses();
    }

    private void initViews(View view) {
        rgTheme = view.findViewById(R.id.rg_theme);
        rbDark = view.findViewById(R.id.rb_theme_dark);
        rbLight = view.findViewById(R.id.rb_theme_light);
        rbSystem = view.findViewById(R.id.rb_theme_system);
        tvStatusUsage = view.findViewById(R.id.tv_status_usage);
        tvStatusNotif = view.findViewById(R.id.tv_status_notif);
    }

    private void setupThemePreference() {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int themeMode = prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_YES);

        if (themeMode == AppCompatDelegate.MODE_NIGHT_NO) {
            rbLight.setChecked(true);
        } else if (themeMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            rbSystem.setChecked(true);
        } else {
            rbDark.setChecked(true);
        }

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int newMode = AppCompatDelegate.MODE_NIGHT_YES;
            if (checkedId == R.id.rb_theme_light) {
                newMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.rb_theme_system) {
                newMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }
            prefs.edit().putInt(KEY_THEME, newMode).apply();
            AppCompatDelegate.setDefaultNightMode(newMode);
        });
    }

    private void updatePermissionStatuses() {
        if (getContext() == null) return;

        boolean hasUsage = PermissionUtils.hasUsageAccess(getContext());
        if (hasUsage) {
            tvStatusUsage.setText(R.string.permission_granted);
            tvStatusUsage.setTextColor(ContextCompat.getColor(getContext(), R.color.accent_emerald));
            tvStatusUsage.setOnClickListener(null);
        } else {
            tvStatusUsage.setText(R.string.permission_not_granted);
            tvStatusUsage.setTextColor(ContextCompat.getColor(getContext(), R.color.accent_amber));
            tvStatusUsage.setOnClickListener(v -> PermissionUtils.requestUsageAccess(getContext()));
        }

        boolean hasNotif = PermissionUtils.hasNotificationListenerAccess(getContext());
        if (hasNotif) {
            tvStatusNotif.setText(R.string.permission_granted);
            tvStatusNotif.setTextColor(ContextCompat.getColor(getContext(), R.color.accent_emerald));
            tvStatusNotif.setOnClickListener(null);
        } else {
            tvStatusNotif.setText(R.string.permission_not_granted);
            tvStatusNotif.setTextColor(ContextCompat.getColor(getContext(), R.color.accent_amber));
            tvStatusNotif.setOnClickListener(v -> PermissionUtils.requestNotificationListenerAccess(getContext()));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePermissionStatuses();
    }
}
