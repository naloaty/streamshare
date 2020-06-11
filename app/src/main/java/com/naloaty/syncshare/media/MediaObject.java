package com.naloaty.syncshare.media;

import android.content.Context;

/**
 * This class represents the lite version of {@link Media} and is the result of converting media-file name to its absolute path.
 * @see com.naloaty.syncshare.service.MediaServer
 * @see com.naloaty.syncshare.adapter.LocalAlbumsAdapter
 * @see MediaProvider#getMediaObjectById(Context, String) 
 */
public class MediaObject {

    /**
     * Absolute path of the media-file.
     */
    private String path;

    /**
     * MIME type of media-file.
     */
    private String mime;

    /**
     * True if this media object is a video file.
     */
    private boolean isVideo = false;

    /**
     * Orientation of media-file (photo and video only).
     */
    private int orientation;

    MediaObject(String path, String mime, boolean isVideo, int orientation)
    {
        this.path = path;
        this.mime = mime;
        this.isVideo = isVideo;
        this.orientation = orientation;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }
}
