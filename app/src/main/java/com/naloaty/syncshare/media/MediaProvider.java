package com.naloaty.syncshare.media;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.naloaty.syncshare.database.media.Album;
import com.naloaty.syncshare.database.media.AlbumRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public class MediaProvider {

    private static final String TAG = "MediaProvider";

    public static List<Album> getAlbums(Context context) throws Exception{

        Uri      uri        = MediaStore.Files.getContentUri("external");
        String[] projection = Album.getProjection();
        String   sort       = "max(" + MediaStore.Images.Media.DATE_MODIFIED + ")";
        boolean  ascending  = false; //По возрастанию

        String selection =
                String.format("%s=? or %s=?) group by (%s",
                        MediaStore.Files.FileColumns.MEDIA_TYPE,
                        MediaStore.Files.FileColumns.MEDIA_TYPE,
                        MediaStore.Files.FileColumns.PARENT);

        Object[] args =
        {
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE,
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
        };

        String[] argsStr = new String[args.length];

        for (int i = 0; i < args.length; i++)
            argsStr[i] = String.valueOf(args[i]);


        ContentResolver contentResolver = context.getContentResolver();

        Cursor cursor = contentResolver.query(uri, projection, selection, argsStr, sort);
        ArrayList<Album> albumsList = new ArrayList<>();

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext())
                albumsList.add(new Album(cursor, false));
        }

        cursor.close();
        return albumsList;

    }

    public static List<Album> getSharedAlbums(Context context) throws Exception{

        AlbumRepository shared = new AlbumRepository(context);

        List<Album> dbAlbums = shared.getAllAlbumsList();
        List<Album> actualAlbums = getAlbums(context);

        /* Here we check if album exists and access to it is granted */

        List<Album> sharedAlbums = new ArrayList<>();

        for (Album dbAlbum: dbAlbums) {
            if (!dbAlbum.isAccessAllowed())
                continue;

            boolean isFound = false;

            for (Album actualAlbum: actualAlbums) {
                boolean exists = dbAlbum.getAlbumId() == actualAlbum.getAlbumId() &&
                                 TextUtils.equals(dbAlbum.getPath(), actualAlbum.getPath());

                if (exists)
                {
                    isFound = true;
                    sharedAlbums.add(actualAlbum);
                    break;
                }
            }

            /* clear ghost albums */
            if (!isFound)
                shared.delete(dbAlbum);
        }

        return sharedAlbums;

    }

    public static void clearGhostAlbums(Context context) throws Exception {
        AlbumRepository shared = new AlbumRepository(context);

        List<Album> dbAlbums = shared.getAllAlbumsList();
        List<Album> actualAlbums = getAlbums(context);


        List<Album> sharedAlbums = new ArrayList<>();

        for (Album dbAlbum: dbAlbums) {
            for (Album actualAlbum: actualAlbums) {
                boolean exists = dbAlbum.getAlbumId() == actualAlbum.getAlbumId() &&
                        TextUtils.equals(dbAlbum.getPath(), actualAlbum.getPath());

                if (!exists)
                    shared.delete(dbAlbum);
            }
        }
    }

    public static String getBucketPathByImagePath(String path) {
        String b[] = path.split("/");
        String c = "";
        for (int x = 0; x < b.length - 1; x++) c += b[x] + "/";
        c = c.substring(0, c.length() - 1);
        return c;
    }

    public static List<Media> getMediaFromMediaStore(Context context, String albumId) {

        Uri      uri        = MediaStore.Files.getContentUri("external");
        String[] projection = Media.getProjection();
        String   sort       = MediaStore.MediaColumns.DATE_MODIFIED;
        boolean  ascending  = false; //По возрастанию

        String selection =
                String.format("%s=? or %s=?) and (%s=?",
                        MediaStore.Files.FileColumns.MEDIA_TYPE,
                        MediaStore.Files.FileColumns.MEDIA_TYPE,
                        MediaStore.Files.FileColumns.PARENT);

        Object[] args =
                {
                        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE,
                        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
                        albumId
                };

        String[] argsStr = new String[args.length];

        for (int i = 0; i < args.length; i++)
            argsStr[i] = String.valueOf(args[i]);


        ContentResolver contentResolver = context.getContentResolver();

        Cursor cursor = contentResolver.query(uri, projection, selection, argsStr, sort);
        List<Media> mediaList = new ArrayList<>();

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext())
                mediaList.add(new Media(cursor));
        }

        cursor.close();

        return mediaList;
    }

    /*
     * filename has following structure:
     * 12345.jpg
     *  ^
     * media (row) id
     */
    public static MediaObject getMediaObjectById(Context context, String filename) throws Exception{

        // Remove extension
        int dot = filename.lastIndexOf('.');
        if (dot >= 0)
            filename = filename.substring(0, dot);

        Uri uri = MediaStore.Files.getContentUri("external");

        if(uri != null)
        {
            String[] projection = new String[]{ MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.MIME_TYPE, MediaStore.Files.FileColumns.MEDIA_TYPE};
            String selection = MediaStore.Files.FileColumns._ID + "=?";
            String[] args = { filename };

            Cursor cursor = context.getContentResolver().query(uri, projection, selection, args, null);

            String path = null;
            String mime = null;
            int media_type = 0;

            if(cursor.moveToFirst())
            {
                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));
                mime = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE));
                media_type = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE));
            }

            cursor.close();

            if(path != null)
                return new MediaObject(path, mime, media_type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO);
        }

        throw new Exception("Not found");
    }
}
