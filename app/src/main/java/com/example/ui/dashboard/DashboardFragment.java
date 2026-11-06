package com.example.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.R;
import com.example.model.DailySummaryInfo;
import com.example.ui.battery.BatteryBottomSheet;
import com.example.ui.clipboard.ClipboardBottomSheet;
import com.example.ui.rules.RulesBottomSheet;
import com.example.ui.settings.SettingsBottomSheet;
import com.example.utils.FormatUtils;
import com.example.utils.PermissionUtils;
import com.example.view.LiquidGlassChartView;
import com.example.view.LiquidGlassRingView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DashboardFragment extends Fragment {
    private DashboardViewModel viewModel;

    private TextView tvGreeting;
    private TextView tvCurrentDate;
    private View bannerPermissions;
    private View btnGrantPermission;
    private ImageView btnSettings;

    private LiquidGlassRingView ringActivityMeter;
    private TextView tvRingScreentime;
    private TextView tvRingData;
    private TextView tvRingBattery;

    private TextView tvDashScreentime;
    private TextView tvDashNetwork;
    private TextView tvDashBattery;
    private TextView tvDashBatterySub;

    private LiquidGlassChartView chartDailyTrend;
    private ImageView ivTopAppIcon;
    private TextView tvTopAppName;
    private TextView tvTopAppTime;

    private View btnDockClipboard;
    private View btnDockBattery;
    private View btnDockRules;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        initViews(view);
        setupGreetingAndDate();
        setupListeners();
        observeViewModel();
    }

    private void initViews(View view) {
        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvCurrentDate = view.findViewById(R.id.tv_current_date);
        bannerPermissions = view.findViewById(R.id.banner_permissions);
        btnGrantPermission = view.findViewById(R.id.btn_grant_permission);
        btnSettings = view.findViewById(R.id.btn_settings);

        ringActivityMeter = view.findViewById(R.id.ring_activity_meter);
        tvRingScreentime = view.findViewById(R.id.tv_ring_screentime);
        tvRingData = view.findViewById(R.id.tv_ring_data);
        tvRingBattery = view.findViewById(R.id.tv_ring_battery);

        tvDashScreentime = view.findViewById(R.id.tv_dash_screentime);
        tvDashNetwork = view.findViewById(R.id.tv_dash_network);
        tvDashBattery = view.findViewById(R.id.tv_dash_battery);
        tvDashBatterySub = view.findViewById(R.id.tv_dash_battery_sub);

        chartDailyTrend = view.findViewById(R.id.chart_daily_trend);
        chartDailyTrend.setUnitLabel("min");

        ivTopAppIcon = view.findViewById(R.id.iv_top_app_icon);
        tvTopAppName = view.findViewById(R.id.tv_top_app_name);
        tvTopAppTime = view.findViewById(R.id.tv_top_app_time);

        btnDockClipboard = view.findViewById(R.id.btn_dock_clipboard);
        btnDockBattery = view.findViewById(R.id.btn_dock_battery);
        btnDockRules = view.findViewById(R.id.btn_dock_rules);
    }

    private void setupGreetingAndDate() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) {
            tvGreeting.setText("Good morning");
        } else if (hour < 18) {
            tvGreeting.setText("Good afternoon");
        } else {
            tvGreeting.setText("Good evening");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM dd", Locale.getDefault());
        tvCurrentDate.setText(sdf.format(new Date()));
    }

    private void setupListeners() {
        btnGrantPermission.setOnClickListener(v -> {
            if (getContext() != null) {
                PermissionUtils.requestUsageAccess(getContext());
            }
        });

        btnSettings.setOnClickListener(v -> {
            new SettingsBottomSheet().show(getParentFragmentManager(), "Settings");
        });

        btnDockClipboard.setOnClickListener(v -> {
            new ClipboardBottomSheet().show(getParentFragmentManager(), "Clipboard");
        });

        btnDockBattery.setOnClickListener(v -> {
            new BatteryBottomSheet().show(getParentFragmentManager(), "Battery");
        });

        btnDockRules.setOnClickListener(v -> {
            new RulesBottomSheet().show(getParentFragmentManager(), "Rules");
        });
    }

    private void observeViewModel() {
        viewModel.getSummary().observe(getViewLifecycleOwner(), this::updateDashboardUi);
        viewModel.getChartPoints().observe(getViewLifecycleOwner(), points -> {
            if (points != null && !points.isEmpty()) {
                chartDailyTrend.setData(points);
            }
        });
    }

    private void updateDashboardUi(DailySummaryInfo summary) {
        if (summary == null) return;

        // Permissions banner visibility
        if (getContext() != null) {
            boolean hasUsage = PermissionUtils.hasUsageAccess(getContext());
            bannerPermissions.setVisibility(hasUsage ? View.GONE : View.VISIBLE);
        }

        // Screen time
        String screenTimeStr = FormatUtils.formatDuration(summary.getScreenTimeMillis());
        tvDashScreentime.setText(screenTimeStr);
        tvRingScreentime.setText(screenTimeStr);

        // Network Data
        long totalData = summary.getCellularBytes() + summary.getWifiBytes();
        String netStr = FormatUtils.formatBytes(totalData);
        tvDashNetwork.setText(netStr);
        tvRingData.setText(netStr);

        // Battery
        String battLevelStr = summary.getBatteryLevel() + "%";
        tvDashBattery.setText(battLevelStr);
        tvRingBattery.setText(battLevelStr);
        tvDashBatterySub.setText(String.format(Locale.getDefault(), "%.1f°C", summary.getBatteryTemperature()));

        // Rings normalized progress
        // Screen time: 4 hours = 100%
        float screenNorm = summary.getScreenTimeMillis() / (4 * 3600 * 1000f);
        // Data: 2 GB = 100%
        float dataNorm = totalData / (2L * 1024 * 1024 * 1024f);
        // Battery: level / 100
        float battNorm = summary.getBatteryLevel() / 100f;
        ringActivityMeter.setProgresses(screenNorm, dataNorm, battNorm);

        // Top App
        tvTopAppName.setText(summary.getTopAppName());
        tvTopAppTime.setText(FormatUtils.formatDuration(summary.getTopAppDurationMillis()));
        if (summary.getTopAppIcon() != null) {
            ivTopAppIcon.setImageDrawable(summary.getTopAppIcon());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refresh();
    }
}
