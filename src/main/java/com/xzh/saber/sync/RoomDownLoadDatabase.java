package com.xzh.saber.sync;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.blankj.utilcode.util.Utils;

/**
 * Created by xzh on 2020/11/9.
 */
@Database(entities = {RoomDownLoad.class}, version = 1)
public abstract class RoomDownLoadDatabase extends RoomDatabase {
    public abstract RoomDownLoadDao roomDownLoadDao();

    public RoomDownLoadDatabase() {
    }

    private volatile static RoomDownLoadDatabase userDatabase;

    synchronized public static RoomDownLoadDatabase roomDownLoadDatabase() {
        if (null == userDatabase) {
            userDatabase = Room.databaseBuilder(Utils.getApp().getApplicationContext(), RoomDownLoadDatabase.class, "saber_db").build();
        }
        return userDatabase;
    }

}
