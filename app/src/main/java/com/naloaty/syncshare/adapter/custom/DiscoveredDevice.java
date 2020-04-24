package com.naloaty.syncshare.adapter.custom;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.naloaty.syncshare.R;
import com.naloaty.syncshare.adapter.CategoryAdapter;
import com.naloaty.syncshare.adapter.base.BodyItem;

public class DiscoveredDevice extends BodyItem {

    private int iconResource;
    private String deviceName;
    private String ipAddress;

    public DiscoveredDevice(String deviceName, String ipAddress, int iconResource) {
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
        View view = inflater.inflate(R.layout.discovered_device, parent, false);

        return new ViewHolder(view, getOnItemClickListener());
    }

    @Override
    public void bindViewHolder(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof DiscoveredDevice.ViewHolder) {
            DiscoveredDevice.ViewHolder holder = (DiscoveredDevice.ViewHolder) viewHolder;

            holder.deviceIcon.setImageResource(getIconResource());
            holder.deviceName.setText(getDeviceName());
            holder.ipAddress.setText(getIpAddress());
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView deviceIcon;
        TextView deviceName;
        TextView ipAddress;
        RelativeLayout layout;

        OnItemClickListener onItemClickListener;

        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);

            deviceIcon = itemView.findViewById(R.id.discovered_device_icon);
            deviceName = itemView.findViewById(R.id.discovered_device_name);
            ipAddress = itemView.findViewById(R.id.discovered_device_ip_address);
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