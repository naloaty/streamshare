package com.naloaty.streamshare.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.naloaty.streamshare.database.device.NetworkDevice;
import com.naloaty.streamshare.database.device.NetworkDeviceDao;
import com.naloaty.streamshare.database.device.SSDevice;
import com.naloaty.streamshare.database.device.SSDeviceDao;
import com.naloaty.streamshare.database.media.Album;
import com.naloaty.streamshare.database.media.AlbumDao;

/**
 * This class represents StreamShare database.
 * To understand how it works, you need to get acquainted with the Room library.
 */
@Database(entities = {NetworkDevice.class, SSDevice.class, Album.class}, version = 1)
public abstract class SSDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "StreamShare_DB";

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

    /**
     * Network devices table. Contains network information about current devices on the network.
     */
    public abstract NetworkDeviceDao NetworkDeviceDao();

    /**
     * Trusted devices table. Contains general information about devices marked by the user as trusted.
     */
    public abstract SSDeviceDao ssDeviceDao();

    /**
     * Shared albums table. Contains albums marked by the user as shared.
     */
    public abstract AlbumDao albumDao();

}
