package com.xzh.saber.sync;

import androidx.lifecycle.MutableLiveData;

import org.jetbrains.annotations.NotNull;

/**
 * Created by xzh on 2020/10/27.
 */
public class SaberSyncInterceptorListener implements SyncInterceptorListener {

    private MutableLiveData<SyncSource> progressBeanLiveData = new MutableLiveData<>();
    private Long totalLength = 0L;

    @Override
    public void start(long l) {
        totalLength = l;
    }

    @Override
    public void onProcess(int process) {
        SyncSource value = progressBeanLiveData.getValue();
        if (value == null) {
            value = new SyncSource(process, totalLength);
        } else {
            value.setTotalDuration(totalLength);
            value.setCurrentProgress(process);
        }
        progressBeanLiveData.postValue(value);
    }

    @Override
    public void onComplete() {

    }

    @Override
    public void setSyncCurrentSource(@NotNull MutableLiveData<SyncSource> syncSource) {
        progressBeanLiveData = syncSource;
    }

    @NotNull
    @Override
    public MutableLiveData<SyncSource> getSyncCurrentSource() {
        return progressBeanLiveData;
    }
}
