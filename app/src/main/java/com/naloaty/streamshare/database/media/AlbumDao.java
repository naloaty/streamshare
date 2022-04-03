package com.naloaty.streamshare.database.media;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;


/**
 * This class represents Data Access Object of network devices table in a StreamShare database.
 * It allows you to retrieve information from the database.
 * To understand how it works, you need to get acquainted with the Room library and Android Architecture Components.
 * @see Album
 */
@Dao
public interface AlbumDao {

    /**
     * Inserts information about album into the database.
     * @param album Information about album. Instance of {@link Album}
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Album album);

    /**
     * Updates information about album in the database.
     * @param album Information about album. Instance of {@link Album}
     */
    @Update
    void update(Album album);

    /**
     * Deletes information about album from the database.
     * @param album Information about album. Instance of {@link Album}
     */
    @Delete
    void delete(Album album);

    /**
     * Returns the number of records in the database.
     * @return Number of records.
     */
    @Query("SELECT COUNT(*) FROM albums_table")
    Integer getAlbumCount();

    /**
     * Returns all shared albums from database.
     * @return A list containing all shared albums and wrapped into LiveData object.
     */
    @Deprecated
    @Query("SELECT * FROM albums_table")
    LiveData<List<Album>> getAllAlbumsDep();

    /**
     * Returns all shared albums from database.
     * @return A list containing all shared albums.
     */
    @Query("SELECT * FROM albums_table")
    List<Album> getAllAlbumsList();

    /**
     * TODO: BUG! See in bug tracker.
     * Searches for the required shared album in the database. You can specify only one of two parameters.
     * @param name Album name.
     * @param path Absolute path of album directory.
     * @return Returns information about shared album, if found, as instance of {@link Album}.
     */
    @Query("SELECT * FROM albums_table WHERE name=:name OR path=:path")
    Album findAlbum(String name, String path);
}
