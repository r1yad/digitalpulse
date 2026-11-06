package com.example.ui.clipboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.database.entity.ClipboardEntity;
import com.example.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ClipboardAdapter extends RecyclerView.Adapter<ClipboardAdapter.ViewHolder> {
    public interface OnClipboardActionListener {
        void onCopy(ClipboardEntity item);
        void onTogglePin(ClipboardEntity item);
        void onDelete(ClipboardEntity item);
    }

    private final List<ClipboardEntity> items = new ArrayList<>();
    private final OnClipboardActionListener listener;

    public ClipboardAdapter(OnClipboardActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ClipboardEntity> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_clipboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClipboardEntity item = items.get(position);

        holder.tvCategory.setText(item.category.toUpperCase(Locale.getDefault()));
        holder.tvTime.setText(FormatUtils.getRelativeTimeSpan(item.timestamp));
        holder.tvContent.setText(item.content);

        boolean isPinned = ClipboardEntity.STATE_FAVORITE.equals(item.storageState) ||
                           ClipboardEntity.STATE_PERMANENT.equals(item.storageState);

        if (isPinned) {
            holder.btnPin.setImageResource(R.drawable.ic_bookmark_filled);
            holder.btnPin.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.accent_cyan));
        } else {
            holder.btnPin.setImageResource(R.drawable.ic_bookmark);
            holder.btnPin.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_muted));
        }

        holder.btnCopy.setOnClickListener(v -> {
            if (listener != null) listener.onCopy(item);
        });

        holder.btnPin.setOnClickListener(v -> {
            if (listener != null) listener.onTogglePin(item);
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
        final TextView tvCategory;
        final TextView tvTime;
        final TextView tvContent;
        final ImageView btnPin;
        final ImageView btnCopy;
        final ImageView btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_clip_category);
            tvTime = itemView.findViewById(R.id.tv_clip_time);
            tvContent = itemView.findViewById(R.id.tv_clip_content);
            btnPin = itemView.findViewById(R.id.btn_clip_pin);
            btnCopy = itemView.findViewById(R.id.btn_clip_copy);
            btnDelete = itemView.findViewById(R.id.btn_clip_delete);
        }
    }
}
