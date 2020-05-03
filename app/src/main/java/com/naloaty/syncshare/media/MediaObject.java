package com.naloaty.syncshare.media;

public class MediaObject {

    private String path;
    private String mime;
    private boolean isVideo = false;

    MediaObject(String path, String mime, boolean isVideo)
    {
        this.path = path;
        this.mime = mime;
        this.isVideo = isVideo;
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
}
