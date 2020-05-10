package com.naloaty.syncshare.media;

public class MediaObject {

    private String path;
    private String mime;
    private boolean isVideo = false;
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
