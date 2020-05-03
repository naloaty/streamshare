package com.naloaty.syncshare.database;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.naloaty.syncshare.database.device.NetworkDevice;
import com.naloaty.syncshare.database.device.NetworkDeviceDao;
import com.naloaty.syncshare.database.device.SSDevice;
import com.naloaty.syncshare.database.device.SSDeviceDao;
import com.naloaty.syncshare.database.media.Album;
import com.naloaty.syncshare.database.media.AlbumDao;

@Database(entities = {NetworkDevice.class, SSDevice.class, Album.class}, version = 1)
public abstract class SSDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "syncshare_db";

    private static SSDatabase instance;

    public static synchronized SSDatabase getInstance(Context context){
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    SSDatabase.class, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return instance;
    }

    public abstract NetworkDeviceDao NetworkDeviceDao();

    public abstract SSDeviceDao ssDeviceDao();

    public abstract AlbumDao albumDao();

}
