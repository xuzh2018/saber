package com.xzh.saber.sync;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Created by xzh on 2020/11/9.
 */
@Entity(tableName = "room_download", indices = {@Index(value = {"download_url"}, unique = true)} )
public class RoomDownLoad {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "download_url")
    public String downLoadUrl;

    @ColumnInfo(name = "last_finish")
    public String lastFinish;
}
