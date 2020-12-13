package com.xzh.saber.sync;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * Created by xzh on 2020/11/9.
 */
@Dao
public interface RoomDownLoadDao {
    @Query("SELECT * FROM room_download WHERE download_url = :downloadUrl")
    RoomDownLoad getLastFinish(String downloadUrl);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void updateLastFinish(RoomDownLoad downLoad);
}
