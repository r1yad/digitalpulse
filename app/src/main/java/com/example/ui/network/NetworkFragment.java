package com.example.ui.network;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.service.LiveSpeedMonitorService;
import com.example.utils.FormatUtils;
import com.example.view.LiquidGlassChartView;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.io.File;

public class NetworkFragment extends Fragment {
    private NetworkViewModel viewModel;
    private NetworkAppAdapter adapter;

    private TextView tvSpeedDown;
    private TextView tvSpeedUp;
    private MaterialSwitch switchLiveService;
    private TextView tvCellularTotal;
    private TextView tvCellularSub;
    private TextView tvWifiTotal;
    private TextView tvWifiSub;
    private LiquidGlassChartView chartNetworkTrend;
    private RecyclerView rvNetworkApps;
    private ImageView btnExportCsv;
    private ImageView btnRefresh;

    private final Handler speedHandler = new Handler(Looper.getMainLooper());
    private final Runnable speedRunnable = new Runnable() {
        @Override
        public void run() {
            if (isResumed() && viewModel != null) {
                viewModel.updateSpeed();
                speedHandler.postDelayed(this, 1500);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_network, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(NetworkViewModel.class);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        observeViewModel();
    }

    private void initViews(View view) {
        tvSpeedDown = view.findViewById(R.id.tv_speed_down);
        tvSpeedUp = view.findViewById(R.id.tv_speed_up);
        switchLiveService = view.findViewById(R.id.switch_live_service);
        tvCellularTotal = view.findViewById(R.id.tv_cellular_total);
        tvCellularSub = view.findViewById(R.id.tv_cellular_sub);
        tvWifiTotal = view.findViewById(R.id.tv_wifi_total);
        tvWifiSub = view.findViewById(R.id.tv_wifi_sub);
        chartNetworkTrend = view.findViewById(R.id.chart_network_trend);
        chartNetworkTrend.setUnitLabel("MB");

        rvNetworkApps = view.findViewById(R.id.rv_network_apps);
        btnExportCsv = view.findViewById(R.id.btn_export_csv);
        btnRefresh = view.findViewById(R.id.btn_refresh_network);
    }

    private void setupRecyclerView() {
        adapter = new NetworkAppAdapter();
        rvNetworkApps.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNetworkApps.setAdapter(adapter);
    }

    private void setupListeners() {
        btnRefresh.setOnClickListener(v -> viewModel.refresh());

        btnExportCsv.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Exporting CSV...", Toast.LENGTH_SHORT).show();
            viewModel.exportCsv();
        });

        switchLiveService.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (getContext() == null) return;
            Intent intent = new Intent(getContext(), LiveSpeedMonitorService.class);
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    getContext().startForegroundService(intent);
                } else {
                    getContext().startService(intent);
                }
                Toast.makeText(getContext(), "Live speed monitor active in status bar", Toast.LENGTH_SHORT).show();
            } else {
                getContext().stopService(intent);
                Toast.makeText(getContext(), "Live speed monitor stopped", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getRealtimeSpeed().observe(getViewLifecycleOwner(), speeds -> {
            if (speeds != null && speeds.length >= 2) {
                tvSpeedDown.setText(FormatUtils.formatSpeed(speeds[0]));
                tvSpeedUp.setText(FormatUtils.formatSpeed(speeds[1]));
            }
        });

        viewModel.getNetworkTotals().observe(getViewLifecycleOwner(), totals -> {
            if (totals != null && totals.length >= 4) {
                long cellTotal = totals[0] + totals[1];
                tvCellularTotal.setText(FormatUtils.formatBytes(cellTotal));
                tvCellularSub.setText(String.format("▼ %s   ▲ %s",
                    FormatUtils.formatBytes(totals[0]),
                    FormatUtils.formatBytes(totals[1])
                ));

                long wifiTotal = totals[2] + totals[3];
                tvWifiTotal.setText(FormatUtils.formatBytes(wifiTotal));
                tvWifiSub.setText(String.format("▼ %s   ▲ %s",
                    FormatUtils.formatBytes(totals[2]),
                    FormatUtils.formatBytes(totals[3])
                ));
            }
        });

        viewModel.getAppUsage().observe(getViewLifecycleOwner(), apps -> {
            if (apps != null) adapter.submitList(apps);
        });

        viewModel.getTrendChart().observe(getViewLifecycleOwner(), trend -> {
            if (trend != null && !trend.isEmpty()) chartNetworkTrend.setData(trend);
        });

        viewModel.getExportedCsv().observe(getViewLifecycleOwner(), file -> {
            if (file != null && getContext() != null) {
                try {
                    Uri uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", file);
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/csv");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(shareIntent, "Share Network CSV Report"));
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Exported to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refresh();
        speedHandler.post(speedRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        speedHandler.removeCallbacks(speedRunnable);
    }
}
