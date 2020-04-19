package com.naloaty.syncshare.adapter.custom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.adapter.base.BodyItem;
import com.naloaty.syncshare.adapter.base.ListItem;

public class DiscoveredDevice extends BodyItem {

    private int iconResource;
    private String deviceName;
    private String appVersion;

    public DiscoveredDevice(String deviceName, String appVersion, int iconResource) {
        this.deviceName = deviceName;
        this.appVersion = appVersion;
        this.iconResource = iconResource;
    }

    public int getIconResource() {
        return iconResource;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.discovered_device, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void bindViewHolder(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof DiscoveredDevice.ViewHolder) {
            DiscoveredDevice.ViewHolder holder = (DiscoveredDevice.ViewHolder) viewHolder;

            holder.deviceIcon.setImageResource(getIconResource());
            holder.deviceName.setText(getDeviceName());
            holder.appVersion.setText(getAppVersion());
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView deviceIcon;
        TextView deviceName;
        TextView appVersion;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            deviceIcon = itemView.findViewById(R.id.discovered_device_icon);
            deviceName = itemView.findViewById(R.id.discovered_device_name);
            appVersion = itemView.findViewById(R.id.discovered_device_app_version);
            cardView = itemView.findViewById(R.id.discovered_device_card_view);
        }
    }
}