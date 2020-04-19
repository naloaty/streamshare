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

public class MyDevice extends BodyItem {

    private int iconResource;
    private String deviceName;
    private String ipAddress;

    public MyDevice(String deviceName, String ipAddress, int iconResource) {
        this.deviceName = deviceName;
        this.ipAddress = ipAddress;
        this.iconResource = iconResource;
    }

    public int getIconResource() {
        return iconResource;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.my_device, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void bindViewHolder(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof MyDevice.ViewHolder) {
            MyDevice.ViewHolder holder = (MyDevice.ViewHolder) viewHolder;

            holder.deviceIcon.setImageResource(getIconResource());
            holder.deviceName.setText(getDeviceName());
            holder.appVersion.setText(getIpAddress());
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView deviceIcon;
        TextView deviceName;
        TextView appVersion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            deviceIcon = itemView.findViewById(R.id.my_device_icon);
            deviceName = itemView.findViewById(R.id.my_device_name);
            appVersion = itemView.findViewById(R.id.my_device_current_address);
        }
    }
}