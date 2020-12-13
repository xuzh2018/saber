package com.xzh.saber.sync;

import android.os.Environment;
import android.util.Log;

import com.blankj.utilcode.util.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import javax.inject.Inject;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ForwardingSink;
import okio.ForwardingSource;
import okio.Okio;
import okio.Sink;
import okio.Source;


/**
 * Created by xzh on 2020/10/27.
 * 异步上传下载拦截器
 */
public class SyncIntercept implements Interceptor {
    public final static String APP_ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Utils.getApp().getPackageName();
    public final static String DOWNLOAD_DIR = "/download/";
    private String syncUrl;
    private String mSyncFileName;
    private boolean isUpLoad;
    SyncInterceptorListener syncListener;
    private volatile boolean mIsDownLoadCancel = false;
    public SyncIntercept(SyncInterceptorListener syncListener, String mSyncUrl, boolean UpLoad) {
        this.syncListener = syncListener;
        this.syncUrl = mSyncUrl;
        this.isUpLoad = UpLoad;
        mSyncFileName = syncUrl.substring(syncUrl.lastIndexOf("/") + 1);
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (isUpLoad) {
            Request build = request
                    .newBuilder()
                    .method(request.method(), new SyncRequestBody(request.body(), syncListener))
                    .build();
            return chain.proceed(build);
        } else {
            Response response = chain.proceed(request);
            return response
                    .newBuilder()
                    .body(new SyncResponseBody(response.body(), syncListener))
                    .build();
        }
    }

    private RandomAccessFile initDownLoadFile() throws IOException {
        RandomAccessFile downLoadRandomFile = null;
        String downloadDir = APP_ROOT_PATH + DOWNLOAD_DIR;
        File dir = new File(downloadDir);
        boolean mkdirs = true;
        if (!dir.exists()) {
            mkdirs = dir.mkdirs();

        }
        if (mkdirs) {
            File downLoadFile = new File(downloadDir, mSyncFileName);
            downLoadRandomFile = new RandomAccessFile(downLoadFile, "rwd");
        }
        return downLoadRandomFile;
    }

    public void cancelDown() {
        this.mIsDownLoadCancel = true;
    }


    /**
     * 上传
     */
    private static class SyncRequestBody extends RequestBody {

        private RequestBody body;
        private SyncInterceptorListener syncListener;
        private BufferedSink mSink;

        public SyncRequestBody(RequestBody body, SyncInterceptorListener syncListener) {
            this.body = body;
            this.syncListener = syncListener;
        }

        @Override
        public long contentLength() throws IOException {
            return body.contentLength();
        }

        @Override
        public MediaType contentType() {
            return body.contentType();
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            if (mSink == null) {
                mSink = Okio.buffer(sink(sink));
            }
            body.writeTo(mSink);
            mSink.flush();
        }

        private Sink sink(Sink sink) {
            return new ForwardingSink(sink) {
                long bytesWritten = 0L;
                long contentLength = 0L;
                long mLastTime = 0L;
                boolean isComplete = false;

                @Override
                public void write(Buffer source, long byteCount) throws IOException {
                    super.write(source, byteCount);
                    if (contentLength == 0) {
                        contentLength = contentLength();
                    }
                    bytesWritten += byteCount;
                    if (syncListener != null) {
                        if (System.currentTimeMillis() - mLastTime > 200) {
                            mLastTime = System.currentTimeMillis();
                            float v = (float) bytesWritten / contentLength * 100;
                            if (v == 100) {
                                if (!isComplete) {
                                    isComplete = true;
                                    syncListener.onComplete();
                                }
                            } else {
                                syncListener.onProcess((int) v);
                            }
                            Log.i("work", "interceptor...upload." + v);
                        }

                    }
                }
            };
        }
    }

    /**
     * 下载
     */
    private class SyncResponseBody extends ResponseBody {

        private ResponseBody body;
        private SyncInterceptorListener syncListener;
        private BufferedSource mBufferedSource;


        public SyncResponseBody(ResponseBody body, SyncInterceptorListener syncListener) {
            this.body = body;
            this.syncListener = syncListener;
        }


        @Override
        public MediaType contentType() {
            return body.contentType();
        }

        @Override
        public long contentLength() {
            return body.contentLength();
        }


        private SyncSource attach(BufferedSource source) throws IOException {
            RandomAccessFile downloadFile = initDownLoadFile();
            RoomDownLoadDatabase saber_db = RoomDownLoadDatabase.roomDownLoadDatabase();
            RoomDownLoad lastFinish1 = saber_db.roomDownLoadDao().getLastFinish(mSyncFileName);
            long bodyContent = body.contentLength();
            long range = 0;
            if (lastFinish1 != null && (downloadFile.length() == bodyContent)) {
                String lastFinish = lastFinish1.lastFinish;
                range = Long.parseLong(lastFinish);
            }
            if (range == 0) {
                downloadFile.setLength(bodyContent);
            }
            downloadFile.seek(range);
            return new SyncSource(source, downloadFile, range, syncListener);
        }

        @Override
        public BufferedSource source() {
            if (mBufferedSource == null) {
                try {
                    mBufferedSource = Okio.buffer(attach(body.source()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return mBufferedSource;
        }
    }

    private class SyncSource extends ForwardingSource {
        RandomAccessFile downloadFile;
        SyncInterceptorListener syncInterceptorListener;
        private long totalBytesRead;
        private long mLastTime = 0L;

        public SyncSource(Source delegate, RandomAccessFile downloadFile, long range, SyncInterceptorListener syncListener) {
            super(delegate);
            this.downloadFile = downloadFile;
            this.syncInterceptorListener = syncListener;
            this.totalBytesRead = range;
            Log.i("workLogin", "work" + "已下载位置" + totalBytesRead);
        }


        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
            long read = super.read(sink, byteCount);
            InputStream input = sink.inputStream();
            if (mIsDownLoadCancel) {
                updateDownLoadDB(totalBytesRead);
                input.close();
                downloadFile.close();
                close();
                return -1;
            }
            byte[] buffer = new byte[1024 * 4];
            int len;
            while ((len = input.read(buffer)) != -1) {
                downloadFile.write(buffer, 0, len);
            }
            if (read != -1L) {
                totalBytesRead += read;
                float v = (float) totalBytesRead / downloadFile.length() * 100;
                if (System.currentTimeMillis() - mLastTime > 200) {
                    mLastTime = System.currentTimeMillis();
                    syncInterceptorListener.onProcess((int) v);
                }
                Log.i("workinterceptor", "interceptor...." + v + "......");
            } else {
                Log.i("workinterceptor", "interceptor...." + 100);
                syncInterceptorListener.onComplete();
                updateDownLoadDB(0L);
                input.close();
                downloadFile.close();
            }
            return read;
        }

        private void updateDownLoadDB(long last) {
            Log.i("workLogin", "work" + "取消下载");
            RoomDownLoadDatabase saber_db = RoomDownLoadDatabase.roomDownLoadDatabase();
            RoomDownLoad roomDownLoad = new RoomDownLoad();
            roomDownLoad.downLoadUrl = mSyncFileName;
            roomDownLoad.lastFinish = String.valueOf(last);
            Log.i("workLogin", "work" + "下载结束保存位置" + last);
            saber_db.roomDownLoadDao().updateLastFinish(roomDownLoad);
        }
    }
}
