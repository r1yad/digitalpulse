package com.example.ui.usage;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.utils.FormatUtils;
import com.example.view.LiquidGlassChartView;

public class UsageFragment extends Fragment {
    private UsageViewModel viewModel;
    private UsageAppAdapter adapter;

    private TextView tvHeroTime;
    private TextView tvHeroLaunches;
    private TextView tvHeroAppsCount;
    private LiquidGlassChartView chartHourly;
    private RecyclerView rvUsageApps;
    private ImageView btnRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_usage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UsageViewModel.class);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        observeViewModel();
    }

    private void initViews(View view) {
        tvHeroTime = view.findViewById(R.id.tv_usage_hero_time);
        tvHeroLaunches = view.findViewById(R.id.tv_usage_hero_launches);
        tvHeroAppsCount = view.findViewById(R.id.tv_usage_hero_apps_count);
        chartHourly = view.findViewById(R.id.chart_usage_hourly);
        chartHourly.setUnitLabel("min");

        rvUsageApps = view.findViewById(R.id.rv_usage_apps);
        btnRefresh = view.findViewById(R.id.btn_refresh_usage);
    }

    private void setupRecyclerView() {
        adapter = new UsageAppAdapter(app -> {
            if (getContext() != null) {
                new SetLimitDialog(getContext(), app.getPackageName(), app.getAppName(), (pkg, name, minutes) -> {
                    viewModel.setAppLimit(pkg, name, minutes);
                }).show();
            }
        });
        rvUsageApps.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUsageApps.setAdapter(adapter);
    }

    private void setupListeners() {
        btnRefresh.setOnClickListener(v -> viewModel.refresh());
    }

    private void observeViewModel() {
        viewModel.getTotalScreenTime().observe(getViewLifecycleOwner(), totalMillis -> {
            tvHeroTime.setText(FormatUtils.formatDuration(totalMillis));
        });

        viewModel.getTotalLaunches().observe(getViewLifecycleOwner(), launches -> {
            tvHeroLaunches.setText(launches + " Total Launches");
        });

        viewModel.getAppUsageList().observe(getViewLifecycleOwner(), apps -> {
            if (apps != null) {
                adapter.submitList(apps);
                tvHeroAppsCount.setText(apps.size() + " Apps Tracked");
            }
        });

        viewModel.getHourlyChart().observe(getViewLifecycleOwner(), points -> {
            if (points != null && !points.isEmpty()) {
                chartHourly.setData(points);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refresh();
    }
}
