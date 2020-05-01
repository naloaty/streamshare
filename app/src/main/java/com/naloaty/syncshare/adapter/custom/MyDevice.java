package com.naloaty.syncshare.adapter.custom;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.adapter.base.BodyItem;

public class MyDevice extends BodyItem {

    private int iconResource;
    private String deviceName;
    private String ipAddress;
    private int imageTint = 0;
    private String deviceId;

    public MyDevice(String deviceId, String deviceName, String ipAddress, int iconResource) {
        this.deviceName = deviceName;
        this.ipAddress = ipAddress;
        this.iconResource = iconResource;
        this.deviceId = deviceId;
    }

    public void setImageTint(Context context, int colorResource) {
        imageTint = ContextCompat.getColor(context, colorResource);
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

    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.my_device, parent, false);

        return new ViewHolder(view, getOnItemClickListener(), this);
    }

    @Override
    public void bindViewHolder(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof MyDevice.ViewHolder) {
            MyDevice.ViewHolder holder = (MyDevice.ViewHolder) viewHolder;

            holder.deviceIcon.setImageResource(getIconResource());
            holder.deviceName.setText(getDeviceName());
            holder.appVersion.setText(getIpAddress());
            holder.onItemClickListener = getOnItemClickListener();
            holder.myDevice = this;

            if (imageTint != 0)
                holder.setImageTint(imageTint);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView deviceIcon;
        TextView deviceName;
        TextView appVersion;
        RelativeLayout layout;
        ImageView actionButton;

        OnItemClickListener onItemClickListener;
        MyDevice myDevice;

        public ViewHolder(@NonNull View itemView, OnItemClickListener listener, MyDevice device) {
            super(itemView);

            this.deviceIcon = itemView.findViewById(R.id.online_device_icon);
            this.deviceName = itemView.findViewById(R.id.my_device_name);
            this.appVersion = itemView.findViewById(R.id.my_device_current_address);
            this.layout = itemView.findViewById(R.id.online_device_layout);
            this.actionButton = itemView.findViewById(R.id.my_device_info_btn);
            this.onItemClickListener = listener;
            this.myDevice = device;

            this.layout.setOnClickListener(this);
            this.actionButton.setOnClickListener(this);
        }

        public void setImageTint(int tintColor) {
            ImageViewCompat.setImageTintList(deviceIcon, ColorStateList.valueOf(tintColor));
        }
        @Override
        public void onClick(View v) {
            Log.d("MyDevice", "OnClick");
            if (onItemClickListener != null)
                onItemClickListener.onItemClick(myDevice);
        }
    }
}