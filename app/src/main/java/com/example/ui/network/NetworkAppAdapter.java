package com.example.ui.network;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.model.NetworkAppUsageInfo;
import com.example.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class NetworkAppAdapter extends RecyclerView.Adapter<NetworkAppAdapter.ViewHolder> {
    private final List<NetworkAppUsageInfo> items = new ArrayList<>();

    public void submitList(List<NetworkAppUsageInfo> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_network_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NetworkAppUsageInfo item = items.get(position);
        holder.tvName.setText(item.getAppName());
        holder.ivIcon.setImageDrawable(item.getAppIcon());

        String details = String.format("▼ %s   ▲ %s",
            FormatUtils.formatBytes(item.getRxBytes()),
            FormatUtils.formatBytes(item.getTxBytes())
        );
        holder.tvDetails.setText(details);
        holder.tvTotal.setText(FormatUtils.formatBytes(item.getTotalBytes()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvName;
        final TextView tvDetails;
        final TextView tvTotal;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_net_app_icon);
            tvName = itemView.findViewById(R.id.tv_net_app_name);
            tvDetails = itemView.findViewById(R.id.tv_net_app_details);
            tvTotal = itemView.findViewById(R.id.tv_net_app_total);
        }
    }
}
