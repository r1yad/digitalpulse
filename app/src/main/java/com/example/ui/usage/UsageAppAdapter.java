package com.example.ui.usage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.model.AppUsageInfo;
import com.example.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class UsageAppAdapter extends RecyclerView.Adapter<UsageAppAdapter.ViewHolder> {
    public interface OnLimitClickListener {
        void onSetLimit(AppUsageInfo app);
    }

    private final List<AppUsageInfo> items = new ArrayList<>();
    private final OnLimitClickListener limitClickListener;

    public UsageAppAdapter(OnLimitClickListener listener) {
        this.limitClickListener = listener;
    }

    public void submitList(List<AppUsageInfo> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usage_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppUsageInfo item = items.get(position);
        holder.tvName.setText(item.getAppName());
        holder.ivIcon.setImageDrawable(item.getAppIcon());

        String sessions = String.format("%d launches • Avg %s",
            item.getLaunchCount(),
            FormatUtils.formatDuration(item.getAverageSessionMillis())
        );
        holder.tvSessions.setText(sessions);
        holder.tvTime.setText(FormatUtils.formatDuration(item.getTotalTimeInForegroundMillis()));

        holder.btnSetLimit.setOnClickListener(v -> {
            if (limitClickListener != null) {
                limitClickListener.onSetLimit(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvName;
        final TextView tvSessions;
        final TextView tvTime;
        final TextView btnSetLimit;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_usage_app_icon);
            tvName = itemView.findViewById(R.id.tv_usage_app_name);
            tvSessions = itemView.findViewById(R.id.tv_usage_app_sessions);
            tvTime = itemView.findViewById(R.id.tv_usage_app_time);
            btnSetLimit = itemView.findViewById(R.id.btn_set_limit);
        }
    }
}
