package com.example.ui.usage;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.R;

public class SetLimitDialog extends Dialog {
    public interface OnLimitSavedListener {
        void onLimitSaved(String packageName, String appName, int minutes);
    }

    private final String packageName;
    private final String appName;
    private final OnLimitSavedListener listener;

    public SetLimitDialog(@NonNull Context context, String packageName, String appName, OnLimitSavedListener listener) {
        super(context);
        this.packageName = packageName;
        this.appName = appName;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_set_limit);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvTitle = findViewById(R.id.tv_limit_dialog_title);
        EditText etMinutes = findViewById(R.id.et_limit_minutes);
        TextView btnCancel = findViewById(R.id.btn_cancel_limit);
        TextView btnSave = findViewById(R.id.btn_save_limit);

        tvTitle.setText("Set Daily Limit: " + appName);

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String text = etMinutes.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(getContext(), "Please enter limit minutes", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int mins = Integer.parseInt(text);
                if (mins <= 0) {
                    Toast.makeText(getContext(), "Limit must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (listener != null) {
                    listener.onLimitSaved(packageName, appName, mins);
                }
                Toast.makeText(getContext(), "Limit set for " + appName + " (" + mins + "m)", Toast.LENGTH_SHORT).show();
                dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid number", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
