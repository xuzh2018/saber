package com.xzh.saber.sync;

import androidx.lifecycle.MutableLiveData;

/**
 * Created by xzh on 2020/10/27.
 */
interface SyncInterceptorListener {
    void start(long l);

    void onProcess(int process);

    void onComplete();

    void setSyncCurrentSource(MutableLiveData<SyncSource> syncSource);

    MutableLiveData<SyncSource> getSyncCurrentSource();
}
