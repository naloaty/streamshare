package com.naloaty.streamshare.database.media;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

/**
 * This class represents extra abstract layer above {@link AlbumRepository}.
 * It connects activity with repository and allows you to retrieve information from the database.
 * To understand how it works, you need to get acquainted with Android Architecture Components.
 * @see Album
 * @see AlbumDao
 * @see AlbumRepository
 */
public class AlbumViewModel extends AndroidViewModel {

    private AlbumRepository repository;
    private LiveData<List<Album>> allAlbums;
    private Context mContext;

    public AlbumViewModel(@NonNull Application application) {
        super(application);
        repository = new AlbumRepository(application);
        allAlbums = repository.getAllAlbums();
        mContext = application;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * Inserts information about album into the database.
     * @param album Information about album. Instance of {@link Album}.
     */
    public void insert(Album album) {
        repository.insert(album);
    }

    /**
     * Updates information about album in the database.
     * @param album Information about album. Instance of {@link Album}.
     */
    public void update(Album album) {
        repository.update(album);
    }

    /**
     * Deletes information about album from the database.
     * @param album Information about album. Instance of {@link Album}
     */
    public void delete(Album album) {
        repository.delete(album);
    }

    /**
     * TODO: BUG! See in bug tracker.
     * Searches for the required shared album in the database. You can specify only one of two parameters.
     * @param name Album name.
     * @param path Absolute path of album directory.
     * @return Returns information about shared album, if found, as instance of {@link Album}.
     */
    public Album findAlbum(String name, String path) {
        return repository.findAlbum(name, path);
    }

    /**
     * Returns the number of records in the database.
     * @return Number of records.
     */
    public Integer getAlbumCount() {
        return repository.getAlbumCount();
    }

    /**
     * Returns all shared albums from database.
     * @return A list containing all shared albums and wrapped into LiveData object.
     */
    public LiveData<List<Album>> getAllAlbums () {
        return allAlbums;
    }

    /**
     * Returns all shared albums from database.
     * NOTE: Should be used only outside of UI thread.
     * @return A list containing all shared albums.
     */
    public List<Album> getAllAlbumsList () {
        return repository.getAllAlbumsList();
    }

    /**
     * Decides whether to insert or update information about album.
     * @param album Information about album. Instance of {@link Album}.
     */
    public void publish(Album album) {
        repository.publish(album);
    }

}
