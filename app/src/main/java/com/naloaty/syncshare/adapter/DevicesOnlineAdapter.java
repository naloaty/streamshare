package com.naloaty.syncshare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.naloaty.syncshare.R;

import java.util.ArrayList;
import java.util.List;

public class DevicesOnlineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ListItem> mItems = new ArrayList<>();

    public void setItems(List<ListItem> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ListItem.TYPE_HEADER: {
                View itemView = inflater.inflate(R.layout.devices_online_item_header, parent, false);
                return new HeaderViewHolder(itemView);
            }
            case ListItem.TYPE_DEVICE: {
                View itemView = inflater.inflate(R.layout.devices_online_item_device, parent, false);
                return new DeviceViewHolder(itemView);
            }
            default:
                throw new IllegalStateException("Unsupported item type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);

        switch (viewType) {
            case ListItem.TYPE_HEADER: {
                HeaderItem header = (HeaderItem) mItems.get(position);
                HeaderViewHolder holder = (HeaderViewHolder) viewHolder;

                holder.headerCaption.setText(header.getCaptionResource());
                break;
            }
            case ListItem.TYPE_DEVICE: {
                DeviceItem device = (DeviceItem) mItems.get(position);
                DeviceViewHolder holder = (DeviceViewHolder) viewHolder;

                holder.deviceIcon.setImageResource(device.getIconResource());
                holder.deviceName.setText(device.getDeviceName());
                break;
            }
            default:
                throw new IllegalStateException("Unsupported item type");
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView headerCaption;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);

            headerCaption = itemView.findViewById(R.id.devices_online_header_caption);
        }
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {

        ImageView deviceIcon;
        TextView deviceName;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);

            deviceIcon = itemView.findViewById(R.id.devices_online_device_icon);
            deviceName = itemView.findViewById(R.id.devices_online_device_name);
        }
    }
}
