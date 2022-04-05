package com.naloaty.streamshare.adapter;

import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.naloaty.streamshare.R;
import com.naloaty.streamshare.config.AppConfig;
import com.naloaty.streamshare.database.entity.NetworkDevice;
import com.naloaty.streamshare.database.repository.NetworkDeviceRepository;
import com.naloaty.streamshare.database.entity.SSDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class for RecyclerView that displays a list of trusted devices.
 * @see com.naloaty.streamshare.fragment.MyDevicesFragment
 */
public class MyDevicesAdapter extends RecyclerView.Adapter<MyDevicesAdapter.ViewHolder>{

    private List<SSDevice> mList = new ArrayList<>();
    private OnRVClickListener mClickListener;

    public MyDevicesAdapter(OnRVClickListener clickListener){
        mClickListener = clickListener;
    }

    /**
     * Updates the current list of devices
     * @param devicesList New or updated device list
     */
    public void setDevicesList(@NonNull List<SSDevice> devicesList) {
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
                    SSDevice newDevice = devicesList.get(newItemPosition);
                    SSDevice oldDevice = mList.get(oldItemPosition);

                    return newDevice.getId() == oldDevice.getId()
                            && TextUtils.equals(newDevice.getDeviceId(), oldDevice.getDeviceId())
                            && TextUtils.equals(newDevice.getAppVersion(), oldDevice.getAppVersion())
                            && TextUtils.equals(newDevice.getNickname(), oldDevice.getNickname())
                            && TextUtils.equals(newDevice.getBrand(), oldDevice.getBrand())
                            && TextUtils.equals(newDevice.getModel(), oldDevice.getModel());

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
        View view = inflater.inflate(R.layout.my_device, parent, false);
        return new ViewHolder(view, mClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        SSDevice device = mList.get(position);

        NetworkDeviceRepository repository = new NetworkDeviceRepository(holder.deviceIcon.getContext());
        NetworkDevice networkDevice = repository.findDeviceDep(null, device.getDeviceId(), null);

        int imageResource;
        int colorResource;
        String generalInfo;
        String extraInfo;

        String platform = device.getAppVersion().split("::")[1];
        switch (platform) {

            case AppConfig.PLATFORM_MOBILE:
                imageResource = R.drawable.ic_phone_android_24dp;
                break;

            case AppConfig.PLATFORM_DESKTOP:
                imageResource = R.drawable.ic_desktop_windows_24dp;
                break;

            default:
                imageResource = R.drawable.ic_warning_24dp;
                break;
        }

        generalInfo = device.getNickname();

        if (networkDevice != null){
            colorResource = R.color.colorOnline;
            extraInfo = networkDevice.getIpAddress();
        }
        else
        {
            colorResource = R.color.colorOffline;
            extraInfo = holder.deviceIcon.getContext().getString(R.string.text_unknownAddress);
        }

        holder.deviceIcon.setImageResource(imageResource);
        holder.setImageTint(ContextCompat.getColor(holder.deviceIcon.getContext(), colorResource));
        holder.deviceName.setText(generalInfo);
        holder.currentAddress.setText(extraInfo);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView deviceIcon;
        TextView deviceName;
        TextView currentAddress;
        RelativeLayout layout;
        ImageView actionButton;

        OnRVClickListener clickListener;

        ViewHolder(@NonNull View itemView, OnRVClickListener clickListener) {
            super(itemView);

            this.deviceIcon = itemView.findViewById(R.id.online_device_icon);
            this.deviceName = itemView.findViewById(R.id.my_device_name);
            this.currentAddress = itemView.findViewById(R.id.my_device_current_address);
            this.layout = itemView.findViewById(R.id.online_device_layout);
            this.actionButton = itemView.findViewById(R.id.my_device_info_btn);

            this.clickListener = clickListener;

            this.layout.setOnClickListener(this);
            this.actionButton.setOnClickListener(this);
        }

        public void setImageTint(int tintColor) {
            ImageViewCompat.setImageTintList(deviceIcon, ColorStateList.valueOf(tintColor));
        }

        @Override
        public void onClick(View v) {
            clickListener.onClick(getAdapterPosition());
        }
    }
}
