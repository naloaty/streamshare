package com.naloaty.streamshare.media;

import com.naloaty.streamshare.database.entity.NetworkDevice;

import java.io.Serializable;
import java.util.List;

/**
 * This class used as a parcel from {@link com.naloaty.streamshare.activity.RemoteViewActivity} to {@link com.naloaty.streamshare.activity.ImageViewActivity}.
 * Contains a list of media-files and network information about the device from which the list was received.
 * @see NetworkDevice
 */
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
