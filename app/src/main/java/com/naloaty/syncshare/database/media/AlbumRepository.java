package com.naloaty.syncshare.database.media;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.naloaty.syncshare.database.SSDatabase;

import java.util.List;

/**
 * This class represents extra abstract layer above {@link AlbumDao} of {@link Album} in a StreamShare database.
 * It allows you to retrieve information from the database.
 * @see Album
 * @see AlbumDao
 */
public class AlbumRepository {

    private static final String TAG = "AlbumRepo";

    /*
     * TODO: AsyncTask get() method DOES NOT asynchronous (replace by ReactiveX object)
     */

    private AlbumDao albumDao;
    private LiveData<List<Album>> allAlbums;

    public AlbumRepository(Context context) {
        SSDatabase database = SSDatabase.getInstance(context);

        //Room auto generates these abstract methods
        albumDao = database.albumDao();
        allAlbums = albumDao.getAllAlbumsDep();
    }

    /**
     * Inserts information about album into the database.
     * @param album Information about album. Instance of {@link Album}.
     */
    public void insert(Album album) {
        new InsertAlbumAT(albumDao).execute(album);
    }

    /**
     * Updates information about album in the database.
     * @param album Information about album. Instance of {@link Album}.
     */
    public void update(Album album) {
        new UpdateAlbumAT(albumDao).execute(album);
    }

    /**
     * Decides whether to insert or update information about album.
     * @param album Information about album. Instance of {@link Album}.
     */
    public void publish(Album album) {
        Album foundedAlbum = findAlbum(album.getName(), album.getPath());

        if (foundedAlbum != null) {
            album.setId(foundedAlbum.getId());
            update(album);
        }
        else
            insert(album);
    }

    /**
     * Deletes information about album from the database.
     * @param album Information about album. Instance of {@link Album}
     */
    public void delete(Album album) {
        new DeleteAlbumAT(albumDao).execute(album);
    }

    /**
     * Returns all shared albums from database.
     * @return A list containing all shared albums and wrapped into LiveData object.
     */
    public LiveData<List<Album>> getAllAlbums () {
        return allAlbums;
    }

    /**
     * Returns the number of records in the database.
     * @return Number of records.
     */
    public int getAlbumCount() {
        try {
            return new GetAlbumCountAT(albumDao).execute().get();
        }
        catch (Exception e) {
            Log.d(TAG, "getAlbumCount() exception: " + e.getMessage());
            return new Integer(0);
        }

    }

    /**
     * TODO: BUG! See in bug tracker.
     * Searches for the required shared album in the database. You can specify only one of two parameters.
     * @param name Album name.
     * @param path Absolute path of album directory.
     * @return Returns information about shared album, if found, as instance of {@link Album}.
     */
    public Album findAlbum(String name, String path) {
        try{
            return new FindAlbumAT(albumDao).execute(name, path).get();
        }
        catch (Exception e) {
            Log.d(TAG, "findAlbum() exception: " + e.getMessage());
            return null;
        }
    }

    /**
     * Returns all shared albums from database.
     * @return A list containing all shared albums.
     */
    @Deprecated
    public List<Album> getAllAlbumListDep() {
        try{
            return new GetAllAlbumsListAT(albumDao).execute().get();
        }
        catch (Exception e) {
            Log.d(TAG, "getAllAlbumListDep() exception: " + e.getMessage());
            return null;
        }
    }

    /**
     * Returns all shared albums from database.
     * NOTE: Should be used only outside of UI thread.
     * @return A list containing all shared albums.
     */
    public List<Album> getAllAlbumsList() {
        return albumDao.getAllAlbumsList();
    }

    public static class InsertAlbumAT extends AsyncTask<Album, Void, Void> {

        private AlbumDao albumDao;

        public InsertAlbumAT(AlbumDao albumDao) {
            this.albumDao = albumDao;
        }

        @Override
        protected Void doInBackground(Album... albums) {
            albumDao.insert(albums[0]);
            return null;
        }
    }

    public static class UpdateAlbumAT extends AsyncTask<Album, Void, Void> {

        private AlbumDao albumDao;

        public UpdateAlbumAT(AlbumDao albumDao) {
            this.albumDao = albumDao;
        }

        @Override
        protected Void doInBackground(Album... albums) {
            albumDao.update(albums[0]);
            return null;
        }
    }

    public static class DeleteAlbumAT extends AsyncTask<Album, Void, Void> {

        private AlbumDao albumDao;

        public DeleteAlbumAT(AlbumDao albumDao) {
            this.albumDao = albumDao;
        }

        @Override
        protected Void doInBackground(Album... albums) {
            albumDao.delete(albums[0]);
            return null;
        }
    }

    public static class FindAlbumAT extends AsyncTask<String, Void, Album> {

        private AlbumDao albumDao;

        public FindAlbumAT(AlbumDao albumDao) {
            this.albumDao = albumDao;
        }

        @Override
        protected Album doInBackground(String... strings) {
            return albumDao.findAlbum(strings[0], strings[1]);
        }
    }

    public static class GetAlbumCountAT extends AsyncTask<Void, Void, Integer> {

        private AlbumDao albumDao;

        public GetAlbumCountAT(AlbumDao albumDao) {
            this.albumDao = albumDao;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            return albumDao.getAlbumCount();
        }
    }

    public static class GetAllAlbumsListAT extends AsyncTask<Void, Void, List<Album>> {
        private AlbumDao albumDao;

        public GetAllAlbumsListAT(AlbumDao albumDao) {
            this.albumDao = albumDao;
        }

        @Override
        protected List<Album> doInBackground(Void... voids) {
            return albumDao.getAllAlbumsList();
        }
    }
}
