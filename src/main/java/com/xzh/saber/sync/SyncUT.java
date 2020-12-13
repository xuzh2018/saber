package com.xzh.saber.sync;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.impl.WorkManagerImpl;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by xzh on 2020/11/12.
 * 异步上传下载类
 */
@SuppressLint("RestrictedApi")
public class SyncUT {
    private static String[] REFLECT_ARRAY;
    private final Class<? extends SyncWorker> mWorkClass;

    public SyncUT(Class<? extends SyncWorker> mClass) {
        this.mWorkClass = mClass;
    }

    public static SyncUT instance(Class<? extends SyncWorker> mClass) {
        return new SyncUT(mClass);
    }

    public OneTimeWorkRequest downLoad(Context mContext, String url) {
        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(mWorkClass).setInputData(handleDownloadData(url));
        OneTimeWorkRequest build = builder.build();
        WorkManagerImpl.getInstance(mContext).enqueue(build);
        return build;
    }

    private Data handleDownloadData(String url) {
        Data.Builder requestBuilder = new Data.Builder();
        requestBuilder.put(SyncUploadBody.SYNC_TYPE, SyncUploadBody.SYNC_DOWNLOAD);
        requestBuilder.put(SyncUploadBody.SYNC_URL, url);
        return requestBuilder.build();
    }

    public OneTimeWorkRequest upLoad(Context mContext, String url, SyncUploadBody syncUploadBody) {
        REFLECT_ARRAY = new String[]{};
        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(mWorkClass).setInputData(handleSyncBody(syncUploadBody, url));
        OneTimeWorkRequest build = builder.build();
        WorkManagerImpl.getInstance(mContext).enqueue(build);
        return build;
    }

    private Data handleSyncBody(SyncUploadBody syncUploadBody, String url) {
        Data.Builder requestBuilder = new Data.Builder();
        requestBuilder.put(SyncUploadBody.SYNC_TYPE, SyncUploadBody.SYNC_UPLOAD);
        requestBuilder.put(SyncUploadBody.SYNC_URL, url);
        handleRequestBuilder(requestBuilder, syncUploadBody.getmRequestNormal(), SyncUploadBody.UPLOAD_TYPE_NORMAL_KEY, SyncUploadBody.UPLOAD_TYPE_NORMAL_VALUE);
        handleRequestBuilder(requestBuilder, syncUploadBody.getmRequestImg(), SyncUploadBody.UPLOAD_TYPE_IMG_KEY, SyncUploadBody.UPLOAD_TYPE_IMG_VALUE);
        handleRequestBuilder(requestBuilder, syncUploadBody.getmRequestVideo(), SyncUploadBody.UPLOAD_TYPE_VIDEO_KEY, SyncUploadBody.UPLOAD_TYPE_VIDEO_VALUE);
        return requestBuilder.build();
    }

    private void handleRequestBuilder(Data.Builder requestBuilder, ArrayList<Map<String, String>> maps, String uploadTypeKey, String uploadTypeValue) {
        if (maps != null && maps.size() > 0) {
            ArrayList<String> requestKeys = new ArrayList<>();
            ArrayList<String> requestValues = new ArrayList<>();
            for (int i = 0; i < maps.size(); i++) {
                Map<String, String> stringStringMap = maps.get(i);
                for (Map.Entry<String, String> entry : stringStringMap.entrySet()) {
                    requestKeys.add(entry.getKey());
                    requestValues.add(entry.getValue());
                }
            }
            requestBuilder.putStringArray(uploadTypeKey, requestKeys.toArray(REFLECT_ARRAY));
            requestBuilder.putStringArray(uploadTypeValue, requestValues.toArray(REFLECT_ARRAY));
        }
    }
}
