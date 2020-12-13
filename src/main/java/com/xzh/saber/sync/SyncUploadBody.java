package com.xzh.saber.sync;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by xzh on 2020/10/30.
 */
public class SyncUploadBody {
    public static final int SYNC_NORMAL = 0;
    public static final int SYNC_DOWNLOAD = 1;
    public static final int SYNC_UPLOAD = 2;
    public static final String SYNC_TYPE = "sync_type";
    public static final String SYNC_URL = "sync_url"; //异步上传下载所需链接
    public static final String SYNC_PROCESS = "sync_process";
    public static final String UPLOAD_TYPE_NORMAL_KEY  = "upload_type_normal_key";
    public static final String UPLOAD_TYPE_NORMAL_VALUE = "upload_type_normal_value";
    public static final String UPLOAD_TYPE_IMG_KEY  = "upload_type_img_key";
    public static final String UPLOAD_TYPE_IMG_VALUE = "upload_type_img_value";
    public static final String UPLOAD_TYPE_VIDEO_KEY  = "upload_type_video_key";
    public static final String UPLOAD_TYPE_VIDEO_VALUE = "upload_type_video_value";
    ArrayList<Map<String, String>> mRequestNormal;
    ArrayList<Map<String, String>> mRequestImg;
    ArrayList<Map<String, String>> mRequestVideo;

    public SyncUploadBody(ArrayList<Map<String, String>> mRequestNormal) {
        this.mRequestNormal = mRequestNormal;
    }

    public ArrayList<Map<String, String>> getmRequestNormal() {
        return mRequestNormal;
    }

    public void setmRequestNormal(ArrayList<Map<String, String>> mRequestNormal) {
        this.mRequestNormal = mRequestNormal;
    }

    public ArrayList<Map<String, String>> getmRequestImg() {
        return mRequestImg;
    }

    public void setmRequestImg(ArrayList<Map<String, String>> mRequestImg) {
        this.mRequestImg = mRequestImg;
    }

    public ArrayList<Map<String, String>> getmRequestVideo() {
        return mRequestVideo;
    }

    public void setmRequestVideo(ArrayList<Map<String, String>> mRequestVideo) {
        this.mRequestVideo = mRequestVideo;
    }
}
