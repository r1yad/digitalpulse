package com.example.ui.rules;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.database.entity.AutomationRuleEntity;
import com.example.repository.RuleRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class RulesBottomSheet extends BottomSheetDialogFragment {
    private RuleRepository ruleRepo;
    private RulesAdapter adapter;

    private ImageView btnCreate;
    private TextView tvEmpty;
    private RecyclerView rvRules;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_rules, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ruleRepo = new RuleRepository(requireContext());

        initViews(view);
        setupRecyclerView();
        setupListeners();
        observeRules();
    }

    private void initViews(View view) {
        btnCreate = view.findViewById(R.id.btn_create_rule);
        tvEmpty = view.findViewById(R.id.empty_rules_text);
        rvRules = view.findViewById(R.id.rv_rules);
    }

    private void setupRecyclerView() {
        adapter = new RulesAdapter(new RulesAdapter.OnRuleActionListener() {
            @Override
            public void onToggleEnable(AutomationRuleEntity rule, boolean isEnabled) {
                rule.isEnabled = isEnabled;
                ruleRepo.updateRule(rule);
            }

            @Override
            public void onDelete(AutomationRuleEntity rule) {
                ruleRepo.deleteRule(rule);
                Toast.makeText(getContext(), "Rule deleted", Toast.LENGTH_SHORT).show();
            }
        });
        rvRules.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRules.setAdapter(adapter);
    }

    private void setupListeners() {
        btnCreate.setOnClickListener(v -> {
            if (getContext() != null) {
                new CreateRuleDialog(getContext(), rule -> {
                    ruleRepo.saveRule(rule);
                }).show();
            }
        });
    }

    private void observeRules() {
        ruleRepo.getAllRules().observe(getViewLifecycleOwner(), rules -> {
            if (rules == null || rules.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                rvRules.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                rvRules.setVisibility(View.VISIBLE);
                adapter.submitList(rules);
            }
        });
    }
}
