package com.naloaty.syncshare.database.media;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.naloaty.syncshare.database.SSDatabase;
import com.naloaty.syncshare.database.device.SSDevice;
import com.naloaty.syncshare.database.device.SSDeviceDao;
import com.naloaty.syncshare.database.device.SSDeviceRepository;

import java.util.List;

public class AlbumRepository {

    private static final String TAG = "AlbumRepo";

    /*
     * TODO: AsyncTask get() method DOES NOT asynchronous
     * it is block UI, so it should be replaced by callback (or make it using Rx style)
     */

    private AlbumDao albumDao;
    private LiveData<List<Album>> allAlbums;

    public AlbumRepository(Context context) {
        SSDatabase database = SSDatabase.getInstance(context);

        //Room auto generates these abstract methods
        albumDao = database.albumDao();
        allAlbums = albumDao.getAllAlbums();
    }

    public void insert(Album album) {
        new InsertAlbumAT(albumDao).execute(album);
    }

    public void update(Album album) {
        new UpdateAlbumAT(albumDao).execute(album);
    }

    public void publish(Album album) {
        Album foundedAlbum = findAlbum(album.getName(), album.getPath());

        if (foundedAlbum != null) {
            album.setId(foundedAlbum.getId());
            update(album);
        }
        else
            insert(album);
    }


    public void delete(Album album) {
        new DeleteAlbumAT(albumDao).execute(album);
    }

    public LiveData<List<Album>> getAllAlbums () {
        return allAlbums;
    }

    public int getAlbumCount() {
        try
        {
            return new GetAlbumCountAT(albumDao).execute().get();
        }
        catch (Exception e) {
            Log.d(TAG, "getAlbumCount() exception: " + e.getMessage());
            return new Integer(0);
        }

    }

    public Album findAlbum(String name, String path) {
        try{
            return new FindAlbumAT(albumDao).execute(name, path).get();
        }
        catch (Exception e) {
            Log.d(TAG, "findAlbum() exception: " + e.getMessage());
            return null;
        }
    }

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

    /*
     * Used only outside of UI Thread
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
