package com.naloaty.syncshare.database.media;

import android.database.Observable;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.naloaty.syncshare.database.device.SSDevice;

import java.util.List;

@Dao
public interface AlbumDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Album album);

    @Delete
    void delete(Album album);

    @Update
    void update(Album album);

    @Query("SELECT COUNT(*) FROM albums_table")
    Integer getAlbumCount();

    @Deprecated
    @Query("SELECT * FROM albums_table")
    LiveData<List<Album>> getAllAlbumsDep();

    @Query("SELECT * FROM albums_table")
    List<Album> getAllAlbumsList();

    @Query("SELECT * FROM albums_table WHERE name=:name OR path=:path")
    Album findAlbum(String name, String path);
}
