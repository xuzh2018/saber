package com.xzh.saber.sync;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.blankj.utilcode.util.Utils;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by xzh on 2020/10/9.
 * 上传、下载服务
 */
public abstract class SyncWorker extends Worker {
    public final static String APP_ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Utils.getApp().getPackageName();
    public final static String DOWNLOAD_DIR = "/download/";
    public final static int DOWNLOAD_COMPLETE_PROCESS = 100;


    int mLastProcess = 0;
    private SyncIntercept syncIntercept;
    private String mSyncUrl;
    private boolean IS_DOWNLOAD;  //判断当前是上传还是下载
    private Data.Builder mSyncData = new Data.Builder();

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        int mSyncType = getInputData().getInt(SyncUploadBody.SYNC_TYPE, SyncUploadBody.SYNC_DOWNLOAD);
        IS_DOWNLOAD = mSyncType == SyncUploadBody.SYNC_DOWNLOAD;
        SaberSyncInterceptorListener saberSyncInterceptorListener = new SaberSyncInterceptorListener() {
            @Override
            public void onProcess(int process) {
                if (process >= mLastProcess && process < DOWNLOAD_COMPLETE_PROCESS) {
                    mLastProcess = process;
                    Data downLoadProcess = mSyncData.putInt(SyncUploadBody.SYNC_PROCESS, process).build();
                    setProgressAsync(downLoadProcess);
                }
            }

            @Override
            public void onComplete() {
                mLastProcess = DOWNLOAD_COMPLETE_PROCESS;
                Data downLoadProcess = mSyncData.putInt(SyncUploadBody.SYNC_PROCESS, DOWNLOAD_COMPLETE_PROCESS).build();
                setProgressAsync(downLoadProcess);
            }
        };
        mSyncUrl = getInputData().getString(SyncUploadBody.SYNC_URL);

        syncIntercept = new SyncIntercept(saberSyncInterceptorListener, mSyncUrl, mSyncType == SyncUploadBody.SYNC_UPLOAD);


    }

    @Override
    public void onStopped() {
        super.onStopped();
        if (syncIntercept != null) {
            syncIntercept.cancelDown();
        }

    }

    /**
     * 获取提供上传下载监听的拦截器
     */
    public SyncIntercept getSyncIntercept() {
        return syncIntercept;
    }

    public abstract SyncIntercept initIntercept();

    /**
     * 提供抽象下载方法，实现使用自己服务下载，返回下载结果
     */
    public abstract boolean syncDownLoadWork(String url, String lastFinish);

    /**
     * 提供抽象上传方法，实现使用自己服务上传，返回上传结果
     */
    public abstract boolean syncUpLoadWork(String url, MultipartBody multipartBody);

    @NonNull
    @Override
    public Result doWork() {
        boolean workResult;
        if (IS_DOWNLOAD) {
            RoomDownLoadDatabase build = RoomDownLoadDatabase.roomDownLoadDatabase();
            RoomDownLoad roomLastFinish = build.roomDownLoadDao().getLastFinish(mSyncUrl.substring(mSyncUrl.lastIndexOf("/") + 1));
            String lastFinish = "0";
            if (roomLastFinish != null) {
                lastFinish = roomLastFinish.lastFinish;
            }
            workResult = syncDownLoadWork(mSyncUrl, lastFinish);
        } else {
            String[] requestNormalKeys = getInputData().getStringArray(SyncUploadBody.UPLOAD_TYPE_NORMAL_KEY);
            String[] requestNormalValues = getInputData().getStringArray(SyncUploadBody.UPLOAD_TYPE_NORMAL_VALUE);
            String[] requestImgKeys = getInputData().getStringArray(SyncUploadBody.UPLOAD_TYPE_IMG_KEY);
            String[] requestImgValues = getInputData().getStringArray(SyncUploadBody.UPLOAD_TYPE_IMG_VALUE);
            String[] requestVideoKeys = getInputData().getStringArray(SyncUploadBody.UPLOAD_TYPE_VIDEO_KEY);
            String[] requestVideoValues = getInputData().getStringArray(SyncUploadBody.UPLOAD_TYPE_VIDEO_VALUE);
            MultipartBody multipartBody = analyseUpLoadBody(
                    requestNormalKeys,
                    requestNormalValues,
                    requestImgKeys,
                    requestImgValues,
                    requestVideoKeys,
                    requestVideoValues
            );
            workResult = syncUpLoadWork(mSyncUrl, multipartBody);
        }


        return workResult ? Result.success() : Result.failure();
    }

    /**
     * 获取上传请求体
     */
    private MultipartBody analyseUpLoadBody(String[] normalKeys, String[] normalValues, String[] requestImgKeys, String[] requestImgValues, String[] requestVideoKeys, String[] requestVideoValues) {
        MultipartBody.Builder multipartBody = new MultipartBody.Builder();
        handleRequestBody(normalKeys, normalValues, multipartBody, SyncUploadBody.UPLOAD_TYPE_NORMAL_KEY);
        handleRequestBody(requestImgKeys, requestImgValues, multipartBody, SyncUploadBody.UPLOAD_TYPE_IMG_KEY);
        handleRequestBody(requestVideoKeys, requestVideoValues, multipartBody, SyncUploadBody.UPLOAD_TYPE_VIDEO_KEY);
        return multipartBody.build();
    }

    /**
     * 分析上传数据
     */
    private void handleRequestBody(String[] keys, String[] values, MultipartBody.Builder multipartBody, String type) {
        if (keys != null && values != null && keys.length == values.length) {
            for (int i = 0; i < keys.length; i++) {
                switch (type) {
                    case SyncUploadBody.UPLOAD_TYPE_NORMAL_KEY:
                        multipartBody.addFormDataPart(
                                keys[i],
                                values[i]
                        );
                        break;
                    case SyncUploadBody.UPLOAD_TYPE_IMG_KEY:
                        File imgFile = new File(values[i]);
                        RequestBody imgBody = RequestBody.create(MediaType.parse("image/*"), imgFile);
                        multipartBody.addFormDataPart(
                                keys[i],
                                imgFile.getName(),
                                imgBody
                        );
                        break;
                    case SyncUploadBody.UPLOAD_TYPE_VIDEO_KEY:
                        File videoFile = new File(values[i]);
                        RequestBody videoBody = RequestBody.create(MediaType.parse("video/*"), videoFile);
                        multipartBody.addFormDataPart(
                                keys[i],
                                videoFile.getName(),
                                videoBody
                        );
                        break;
                }

            }
        }
    }
}
