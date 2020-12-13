package com.xzh.saber.net;

import android.util.Log;

import androidx.lifecycle.LiveData;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by xzh on 2020/8/30.
 * retrofit 转 LiveData
 */
public class SaberLiveDataCallAdapter<T> implements CallAdapter<T, LiveData<T>> {
    private final Type responseType;

    public SaberLiveDataCallAdapter(Type bodyType) {
        this.responseType = bodyType;
        Log.i("xuzh","我走到这里了"+responseType.toString());
    }

    @Override
    public Type responseType() {
        return responseType;
    }

    @Override
    public LiveData<T> adapt(final Call<T> call) {
        return new LiveData<T>() {
            AtomicBoolean started = new AtomicBoolean(false);

            @Override
            protected void onActive() {
                super.onActive();
                Log.i("xuzh","我走到这里了hahaha3");
                if (started.compareAndSet(false, true)) {
                    Log.i("xuzh","我走到这里了hahaha4"+call.toString());
                    call.enqueue(new Callback<T>() {
                        @Override
                        public void onResponse(Call<T> call, Response<T> response) {
                            Log.i("xuzh","我走到这里了hahaha"+response.message());
                            postValue(response.body());
                        }

                        @Override
                        public void onFailure(Call<T> call, Throwable t) {
                            Log.i("xuzh","我走到这里了hahaha"+t.toString());
                            postValue((T) t.getMessage());
                        }
                    });
                }
            }
        };
    }
}
