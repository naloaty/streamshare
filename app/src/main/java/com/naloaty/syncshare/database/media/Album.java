package com.naloaty.syncshare.database.media;

import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.naloaty.syncshare.media.MediaProvider;

/**
 * This class represents a table of shared albums in a StreamShare database.
 * It contains albums marked by the user as shared.
 * To understand how it works, you need to get acquainted with the Room library.
 * @see com.naloaty.syncshare.fragment.LocalAlbumsFragment
 */
@Entity(tableName = "albums_table")
public class Album {

    /**
     * Database row id
     */
    @PrimaryKey(autoGenerate = true)
    private int id;

    /**
     * Id of the shared album in the android media store database.
     */
    private long albumId;

    /**
     * Name of the shared album.
     */
    private String name;

    /**
     * Absolute path of shared album folder.
     */
    private String path;

    /**
     * Absolute path to the last taken media-file in the shared album folder.
     */
    private String lastItemFilename;

    /**
     * Count of media-files in shared album.
     */
    private int itemsCount;

    /**
     * True if the user has granted access to this album.
     */
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
        String lastItemExtension = lastItemPath.substring(lastItemPath.lastIndexOf('.'));

        this.lastItemFilename = lastItemId + lastItemExtension;
        this.accessAllowed = accessAllowed;
    }

    //id
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    //albumId
    public long getAlbumId() {
        return albumId;
    }
    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    //name
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    //path
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    //lastItemFilename
    public String getLastItemFilename() {
        return lastItemFilename;
    }
    public void setLastItemFilename(String lastItemFilename) {
        this.lastItemFilename = lastItemFilename;
    }

    //itemsCount
    public int getItemsCount() {
        return itemsCount;
    }
    public void setItemsCount(int itemsCount) {
        this.itemsCount = itemsCount;
    }

    //accessAllowed
    public boolean isAccessAllowed() {
        return accessAllowed;
    }
    public void setAccessAllowed(boolean accessAllowed) {
        this.accessAllowed = accessAllowed;
    }

    /**
     * Returns the projection in order to retrieve albums list from android media store database.
     * @return Projection for albums request.
     * @see MediaProvider
     */
    public static String[] getProjection() {
        return new String[]{
            MediaStore.Files.FileColumns.PARENT,           //albumId
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,   //name
            MediaStore.Images.Media.DATA,                  //path
            "count(*)",
            "max(" + MediaStore.Images.Media.DATE_MODIFIED + ")",
            MediaStore.Files.FileColumns._ID              //media id
        };
    }
}