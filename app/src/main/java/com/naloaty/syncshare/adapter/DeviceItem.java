package com.naloaty.syncshare.adapter;

public class DeviceItem extends ListItem {

    private int iconResource;
    private String deviceName;

    public DeviceItem(String deviceName, int iconResource) {
        this.deviceName = deviceName;
        this.iconResource = iconResource;
    }

    public int getIconResource() {
        return iconResource;
    }

    public String getDeviceName() {
        return deviceName;
    }

    @Override
    public int getType() {
        return TYPE_DEVICE;
    }

}