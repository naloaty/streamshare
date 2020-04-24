package com.naloaty.syncshare.database;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {NetworkDevice.class, SSDevice.class}, version = 1)
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

}
