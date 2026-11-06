package com.example.ui.battery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.R;
import com.example.database.entity.BatteryLogEntity;
import com.example.model.HourlyUsagePoint;
import com.example.repository.BatteryRepository;
import com.example.view.LiquidGlassChartView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;
import java.util.Locale;

public class BatteryBottomSheet extends BottomSheetDialogFragment {
    private BatteryRepository batteryRepo;

    private TextView tvStatusTitle;
    private TextView tvPct;
    private TextView tvSource;
    private TextView tvTempVal;
    private TextView tvVoltageVal;
    private LiquidGlassChartView chartBatteryTrend;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_battery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        batteryRepo = new BatteryRepository(requireContext());

        initViews(view);
        loadBatteryData();
    }

    private void initViews(View view) {
        tvStatusTitle = view.findViewById(R.id.tv_battery_status_title);
        tvPct = view.findViewById(R.id.tv_battery_pct);
        tvSource = view.findViewById(R.id.tv_battery_source);
        tvTempVal = view.findViewById(R.id.tv_battery_temp_val);
        tvVoltageVal = view.findViewById(R.id.tv_battery_voltage_val);
        chartBatteryTrend = view.findViewById(R.id.chart_battery_trend);
        chartBatteryTrend.setUnitLabel("%");
    }

    private void loadBatteryData() {
        BatteryLogEntity state = batteryRepo.getCurrentBatteryState();

        tvPct.setText(state.level + "%");
        tvStatusTitle.setText(state.isCharging ? "Battery Charging" : "Battery Discharging");
        tvSource.setText(state.isCharging ? "Connected to " + state.chargePlug : "On Battery Power");
        tvTempVal.setText(String.format(Locale.getDefault(), "%.1f °C", state.temperature));
        tvVoltageVal.setText(String.format(Locale.getDefault(), "%.2f V", state.voltage / 1000f));

        com.example.database.AppDatabase.databaseWriteExecutor.execute(() -> {
            List<HourlyUsagePoint> points = batteryRepo.getBatteryCurvePoints();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (points != null && !points.isEmpty()) {
                        chartBatteryTrend.setData(points);
                    }
                });
            }
        });
    }
}
