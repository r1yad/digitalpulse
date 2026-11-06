package com.example.ui.rules;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.database.entity.AutomationRuleEntity;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RulesAdapter extends RecyclerView.Adapter<RulesAdapter.ViewHolder> {
    public interface OnRuleActionListener {
        void onToggleEnable(AutomationRuleEntity rule, boolean isEnabled);
        void onDelete(AutomationRuleEntity rule);
    }

    private final List<AutomationRuleEntity> items = new ArrayList<>();
    private final OnRuleActionListener listener;

    public RulesAdapter(OnRuleActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<AutomationRuleEntity> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AutomationRuleEntity item = items.get(position);

        holder.tvName.setText(item.name);

        String unit = "mins";
        if (AutomationRuleEntity.TYPE_MOBILE_DATA.equals(item.ruleType)) unit = "MB";
        else if (AutomationRuleEntity.TYPE_BATTERY_TEMP.equals(item.ruleType)) unit = "°C";

        String desc = String.format(Locale.getDefault(), "Trigger when %s reaches %.0f %s",
            item.ruleType.replace('_', ' '), item.thresholdValue, unit);
        holder.tvDesc.setText(desc);

        holder.switchEnabled.setOnCheckedChangeListener(null);
        holder.switchEnabled.setChecked(item.isEnabled);
        holder.switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) listener.onToggleEnable(item, isChecked);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvDesc;
        final MaterialSwitch switchEnabled;
        final ImageView btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_rule_name);
            tvDesc = itemView.findViewById(R.id.tv_rule_desc);
            switchEnabled = itemView.findViewById(R.id.switch_rule_enabled);
            btnDelete = itemView.findViewById(R.id.btn_delete_rule);
        }
    }
}
