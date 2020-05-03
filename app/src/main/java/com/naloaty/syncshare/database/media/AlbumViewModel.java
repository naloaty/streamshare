package com.naloaty.syncshare.database.media;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

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

    public void insert(Album album) {
        repository.insert(album);
    }

    public void update(Album album) {
        repository.update(album);
    }

    public void delete(Album album) {
        repository.delete(album);
    }

    public Album findDevice(String name, String path) {
        return repository.findAlbum(name, path);
    }

    public Integer getAlbumCount() {
        return repository.getAlbumCount();
    }

    public LiveData<List<Album>> getAllAlbums () {
        return allAlbums;
    }

    public List<Album> getAllAlbumsList () {
        return repository.getAllAlbumListDep();
    }

    public void publish(Album album) {
        repository.publish(album);
    }

}
