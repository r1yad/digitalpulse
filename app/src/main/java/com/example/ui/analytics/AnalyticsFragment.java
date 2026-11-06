package com.example.ui.analytics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.R;
import com.example.view.LiquidGlassChartView;

public class AnalyticsFragment extends Fragment {
    private AnalyticsViewModel viewModel;

    private TextView tvScore;
    private TextView tvScoreDesc;
    private TextView tvScreenPace;
    private TextView tvNotifPace;
    private TextView tvDiagSummary;
    private LiquidGlassChartView chartWeeklyTrend;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analytics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AnalyticsViewModel.class);

        initViews(view);
        observeViewModel();
    }

    private void initViews(View view) {
        tvScore = view.findViewById(R.id.tv_wellbeing_score);
        tvScoreDesc = view.findViewById(R.id.tv_wellbeing_desc);
        tvScreenPace = view.findViewById(R.id.tv_analytics_screen_pace);
        tvNotifPace = view.findViewById(R.id.tv_analytics_notif_pace);
        tvDiagSummary = view.findViewById(R.id.tv_diag_summary);
        chartWeeklyTrend = view.findViewById(R.id.chart_analytics_trend);
        chartWeeklyTrend.setUnitLabel("activity");
    }

    private void observeViewModel() {
        viewModel.getWellbeingScore().observe(getViewLifecycleOwner(), score -> {
            tvScore.setText(score + " / 100");
        });

        viewModel.getWellbeingDesc().observe(getViewLifecycleOwner(), desc -> {
            tvScoreDesc.setText(desc);
        });

        viewModel.getScreenPace().observe(getViewLifecycleOwner(), pace -> {
            tvScreenPace.setText(pace);
        });

        viewModel.getNotifPace().observe(getViewLifecycleOwner(), pace -> {
            tvNotifPace.setText(pace);
        });

        viewModel.getDiagnostics().observe(getViewLifecycleOwner(), diag -> {
            tvDiagSummary.setText(diag);
        });

        viewModel.getWeeklyTrend().observe(getViewLifecycleOwner(), trend -> {
            if (trend != null && !trend.isEmpty()) {
                chartWeeklyTrend.setData(trend);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.computeAnalytics();
    }
}
