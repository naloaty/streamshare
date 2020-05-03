package com.naloaty.syncshare.database.media;

import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.naloaty.syncshare.media.MediaProvider;

@Entity(tableName = "albums_table")
public class Album {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private long albumId;
    private String name;
    private String path;
    private String lastItemFilename;
    private int itemsCount;
    private boolean accessAllowed;

    public Album(long albumId, String name, String path, boolean accessAllowed) {
        this.albumId = albumId;
        this.name = name;
        this.path = path;
        this.accessAllowed = accessAllowed;
    }

    public Album(Cursor cursor, boolean accessAllowed) {

        /* See column index in method getProjection() */
        this.albumId = cursor.getLong(0);
        this.name = cursor.getString(1);
        this.path = MediaProvider.getBucketPathByImagePath(cursor.getString(2));
        this.itemsCount = cursor.getInt(3);

        String lastItemPath = cursor.getString(2);
        String lastItemId = cursor.getString(5);
        String lastItemExtension = lastItemPath.substring(lastItemPath.lastIndexOf('.') + 1);

        this.lastItemFilename = lastItemId + "." + lastItemExtension;
        this.accessAllowed = accessAllowed;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public long getAlbumId() {
        return albumId;
    }
    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    public String getLastItemFilename() {
        return lastItemFilename;
    }
    public void setLastItemFilename(String lastItemFilename) {
        this.lastItemFilename = lastItemFilename;
    }

    public int getItemsCount() {
        return itemsCount;
    }

    public void setItemsCount(int itemsCount) {
        this.itemsCount = itemsCount;
    }

    public boolean isAccessAllowed() {
        return accessAllowed;
    }
    public void setAccessAllowed(boolean accessAllowed) {
        this.accessAllowed = accessAllowed;
    }

    public static String[] getProjection() {
        return new String[]
        {
            MediaStore.Files.FileColumns.PARENT,           //albumId
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,   //name
            MediaStore.Images.Media.DATA,                  //path
            "count(*)",
            "max(" + MediaStore.Images.Media.DATE_MODIFIED + ")",
            MediaStore.Files.FileColumns._ID              //media id
        };
    }
}