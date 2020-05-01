package com.naloaty.syncshare.adapter.custom;

import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.adapter.base.BodyItem;

public class OnlineDevice extends BodyItem {

    private int iconResource;
    private String deviceNickname;
    private String extraData;
    private String deviceId;

    public OnlineDevice(String deviceId, String deviceNickname, String extraData, int iconResource) {
        this.deviceNickname = deviceNickname;
        this.extraData = extraData;
        this.iconResource = iconResource;
        this.deviceId = deviceId;
    }


    public int getIconResource() {
        return iconResource;
    }

    public String getDeviceNickname() {
        return deviceNickname;
    }

    public String getExtraData() {
        return extraData;
    }

    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.online_device, parent, false);

        return new OnlineDevice.ViewHolder(view, getOnItemClickListener(), this);
    }

    @Override
    public void bindViewHolder(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof OnlineDevice.ViewHolder) {
            OnlineDevice.ViewHolder holder = (OnlineDevice.ViewHolder) viewHolder;

            holder.deviceIcon.setImageResource(getIconResource());
            holder.deviceNickname.setText(getDeviceNickname());
            holder.extraData.setText(getExtraData());
            holder.onItemClickListener = getOnItemClickListener();
            holder.onlineDevice = this;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView deviceIcon;
        TextView deviceNickname;
        TextView extraData;
        LinearLayout layout;
        //ImageView actionButton;

        OnItemClickListener onItemClickListener;
        OnlineDevice onlineDevice;

        public ViewHolder(@NonNull View itemView, OnItemClickListener listener, OnlineDevice device) {
            super(itemView);

            this.deviceIcon = itemView.findViewById(R.id.online_device_icon);
            this.deviceNickname = itemView.findViewById(R.id.online_device_nickname);
            this.extraData = itemView.findViewById(R.id.online_device_extra_data);
            this.layout = itemView.findViewById(R.id.online_device_layout);
            //this.actionButton = itemView.findViewById(R.id.online_device_btn);
            this.onItemClickListener = listener;
            this.onlineDevice = device;

            this.layout.setOnClickListener(this);
            //this.actionButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d("OnlineDevice", "OnClick");

            if (onItemClickListener != null)
                onItemClickListener.onItemClick(onlineDevice);
        }
    }
}
