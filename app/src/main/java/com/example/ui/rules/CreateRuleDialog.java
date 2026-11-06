package com.example.ui.rules;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.R;
import com.example.database.entity.AutomationRuleEntity;

public class CreateRuleDialog extends Dialog {
    public interface OnRuleCreatedListener {
        void onRuleCreated(AutomationRuleEntity rule);
    }

    private final OnRuleCreatedListener listener;

    public CreateRuleDialog(@NonNull Context context, OnRuleCreatedListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_create_rule);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etName = findViewById(R.id.et_rule_name);
        RadioGroup rgType = findViewById(R.id.rg_rule_type);
        EditText etThreshold = findViewById(R.id.et_rule_threshold);
        TextView btnCancel = findViewById(R.id.btn_cancel_rule);
        TextView btnSave = findViewById(R.id.btn_save_rule);

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String threshStr = etThreshold.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a rule name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (threshStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a threshold value", Toast.LENGTH_SHORT).show();
                return;
            }

            double threshold;
            try {
                threshold = Double.parseDouble(threshStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid threshold number", Toast.LENGTH_SHORT).show();
                return;
            }

            int checkedId = rgType.getCheckedRadioButtonId();
            String ruleType = AutomationRuleEntity.TYPE_SCREEN_TIME;
            String message = name + " alert threshold triggered!";

            if (checkedId == R.id.rb_mobile_data) {
                ruleType = AutomationRuleEntity.TYPE_MOBILE_DATA;
                message = "Mobile data exceeded " + threshStr + " MB!";
            } else if (checkedId == R.id.rb_battery_temp) {
                ruleType = AutomationRuleEntity.TYPE_BATTERY_TEMP;
                message = "High battery temperature detected (" + threshStr + " °C)!";
            } else {
                message = "Screen time exceeded " + threshStr + " minutes today!";
            }

            AutomationRuleEntity rule = new AutomationRuleEntity(name, ruleType, "", threshold, message, true);
            if (listener != null) {
                listener.onRuleCreated(rule);
            }
            Toast.makeText(getContext(), "Rule created: " + name, Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }
}
