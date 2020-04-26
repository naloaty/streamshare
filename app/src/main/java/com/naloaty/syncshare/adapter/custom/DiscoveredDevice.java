package com.naloaty.syncshare.adapter.custom;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.adapter.base.BodyItem;

public class DiscoveredDevice extends BodyItem {

    private int iconResource;
    private String deviceName;
    private String appVersion;
    private String deviceId;

    public DiscoveredDevice(String deviceName, String appVersion, String deviceId, int iconResource) {
        this.deviceName = deviceName;
        this.appVersion = appVersion;
        this.deviceId = deviceId;
        this.iconResource = iconResource;
    }

    public String getDeviceId () {
        return deviceId;
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

        return new ViewHolder(view, getOnItemClickListener());
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView deviceIcon;
        TextView deviceName;
        TextView appVersion;
        RelativeLayout layout;

        OnItemClickListener onItemClickListener;

        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);

            deviceIcon = itemView.findViewById(R.id.discovered_device_icon);
            deviceName = itemView.findViewById(R.id.discovered_device_name);
            appVersion = itemView.findViewById(R.id.discovered_device_extra_information);
            layout = itemView.findViewById(R.id.discovered_device_layout);
            onItemClickListener = listener;

            layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d("DiscoveredDevice", "OnClick");
            if (onItemClickListener != null)
                onItemClickListener.onItemClick(DiscoveredDevice.this);
        }
    }
}