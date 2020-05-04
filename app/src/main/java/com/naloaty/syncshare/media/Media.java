package com.naloaty.syncshare.media;

import android.database.Cursor;
import android.provider.MediaStore;

import java.io.Serializable;

public class Media implements Serializable {

    private String filename;
    private long dateTaken;;
    private String mimeType;
    private long size;
    private int orientation;
    private int mediaType;

    public Media(Cursor cursor) {
        String mediaId = cursor.getString(0);
        String path = cursor.getString(1);
        String extension = path.substring(path.lastIndexOf('.'));

        this.filename = mediaId + extension;
        this.dateTaken = cursor.getLong(2);
        this.mimeType = cursor.getString(3);
        this.size = cursor.getLong(4);
        this.orientation = cursor.getInt(5);
        this.mediaType = cursor.getInt(6);
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(long dateTaken) {
        this.dateTaken = dateTaken;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public static String[] getProjection() {
        return new String[] {
                MediaStore.Files.FileColumns._ID,       //media id
                MediaStore.Images.Media.DATA,           //path (with filename)
                MediaStore.Images.Media.DATE_TAKEN,     //date
                MediaStore.Images.Media.MIME_TYPE,      //MIME
                MediaStore.Images.Media.SIZE,           //Size
                MediaStore.Images.Media.ORIENTATION,    //Orientation
                MediaStore.Files.FileColumns.MEDIA_TYPE //Media type (image, video or audio)
        };
    }
}
