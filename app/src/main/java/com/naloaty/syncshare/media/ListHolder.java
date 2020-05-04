package com.naloaty.syncshare.media;

import com.naloaty.syncshare.database.device.NetworkDevice;

import java.io.Serializable;
import java.util.List;

public class ListHolder implements Serializable {

    private List<Media> mediaList;
    private NetworkDevice networkDevice;

    public ListHolder(List<Media> mediaList, NetworkDevice networkDevice) {
        this.mediaList = mediaList;
        this.networkDevice = networkDevice;
    }

    public List<Media> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<Media> mediaList) {
        this.mediaList = mediaList;
    }

    public NetworkDevice getNetworkDevice() {
        return networkDevice;
    }

    public void setNetworkDevice(NetworkDevice networkDevice) {
        this.networkDevice = networkDevice;
    }
}
