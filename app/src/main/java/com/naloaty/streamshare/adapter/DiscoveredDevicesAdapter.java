package com.naloaty.streamshare.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.naloaty.streamshare.R;
import com.naloaty.streamshare.config.AppConfig;
import com.naloaty.streamshare.database.entity.NetworkDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class for RecyclerView that displays current devices on the network.
 * @see com.naloaty.streamshare.fragment.NearbyDiscoveryFragment
 */
public class DiscoveredDevicesAdapter extends RecyclerView.Adapter<DiscoveredDevicesAdapter.ViewHolder>{

    private List<NetworkDevice> mList = new ArrayList<>();
    private OnRVClickListener mClickListener;

    public DiscoveredDevicesAdapter(OnRVClickListener clickListener){
        mClickListener = clickListener;
    }

    /**
     * Updates the current list of devices
     * @param devicesList New or updated device list
     */
    public void setDevicesList(@NonNull List<NetworkDevice> devicesList) {
        /*
         * TODO: When adding item to RV it flashes during ViewMessage animation
         */
        if (devicesList.size() == 1 && mList.size() < 2){
            mList = devicesList;
            notifyDataSetChanged();
            return;
        }

        if (mList == null) {
            mList = devicesList;
            notifyItemRangeInserted(0, devicesList.size());
        }
        else
        {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mList.size();
                }

                @Override
                public int getNewListSize() {
                    return devicesList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mList.get(oldItemPosition).getId() ==
                            devicesList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    NetworkDevice newDevice = devicesList.get(newItemPosition);
                    NetworkDevice oldDevice = mList.get(oldItemPosition);

                    return newDevice.getId() == oldDevice.getId()
                            && TextUtils.equals(newDevice.getDeviceId(), oldDevice.getDeviceId())
                            && TextUtils.equals(newDevice.getAppVersion(), oldDevice.getAppVersion())
                            && TextUtils.equals(newDevice.getDeviceName(), oldDevice.getDeviceName())
                            && TextUtils.equals(newDevice.getIpAddress(), oldDevice.getIpAddress())
                            && TextUtils.equals(newDevice.getServiceName(), oldDevice.getServiceName());

                }
            });

            mList = devicesList;
            result.dispatchUpdatesTo(this);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.discovered_device, parent, false);

        return new ViewHolder(view, mClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        NetworkDevice networkDevice = mList.get(position);
        String[] appVersion = networkDevice.getAppVersion().split("::");

        int iconResource;

        switch (appVersion[1]) {
            case AppConfig.PLATFORM_MOBILE:
                iconResource = R.drawable.ic_phone_android_24dp;
                break;

            case AppConfig.PLATFORM_DESKTOP:
                iconResource = R.drawable.ic_desktop_windows_24dp;
                break;

            default:
                iconResource = R.drawable.ic_warning_24dp;
                break;
        }

        holder.deviceIcon.setImageResource(iconResource);
        holder.deviceName.setText(networkDevice.getDeviceName());
        holder.appVersion.setText(appVersion[0]);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView deviceIcon;
        TextView deviceName;
        TextView appVersion;
        RelativeLayout layout;

        OnRVClickListener clickListener;

        ViewHolder(@NonNull View itemView, OnRVClickListener clickListener) {
            super(itemView);

            deviceIcon = itemView.findViewById(R.id.discovered_device_icon);
            deviceName = itemView.findViewById(R.id.discovered_device_name);
            appVersion = itemView.findViewById(R.id.discovered_device_extra_information);
            layout = itemView.findViewById(R.id.discovered_device_layout);

            this.clickListener = clickListener;
            layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onClick(getAdapterPosition());
        }
    }


}
