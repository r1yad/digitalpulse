package com.example.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.database.entity.NotificationEntity;
import com.example.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    public interface OnNotificationActionListener {
        void onToggleBookmark(NotificationEntity notification);
        void onDelete(NotificationEntity notification);
    }

    private final List<NotificationEntity> items = new ArrayList<>();
    private final OnNotificationActionListener listener;

    public NotificationAdapter(OnNotificationActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<NotificationEntity> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationEntity item = items.get(position);

        holder.tvAppName.setText(item.appName);
        holder.tvTime.setText("• " + FormatUtils.getRelativeTimeSpan(item.timestamp));
        holder.tvTitle.setText(item.title);
        holder.tvContent.setText(item.content);

        if (item.isBookmarked) {
            holder.btnBookmark.setImageResource(R.drawable.ic_bookmark_filled);
            holder.btnBookmark.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.accent_cyan));
        } else {
            holder.btnBookmark.setImageResource(R.drawable.ic_bookmark);
            holder.btnBookmark.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_muted));
        }

        holder.btnBookmark.setOnClickListener(v -> {
            if (listener != null) listener.onToggleBookmark(item);
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
        final ImageView ivAppIcon;
        final TextView tvAppName;
        final TextView tvTime;
        final TextView tvTitle;
        final TextView tvContent;
        final ImageView btnBookmark;
        final ImageView btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAppIcon = itemView.findViewById(R.id.iv_notif_app_icon);
            tvAppName = itemView.findViewById(R.id.tv_notif_app_name);
            tvTime = itemView.findViewById(R.id.tv_notif_time);
            tvTitle = itemView.findViewById(R.id.tv_notif_title);
            tvContent = itemView.findViewById(R.id.tv_notif_content);
            btnBookmark = itemView.findViewById(R.id.btn_notif_bookmark);
            btnDelete = itemView.findViewById(R.id.btn_notif_delete);
        }
    }
}
