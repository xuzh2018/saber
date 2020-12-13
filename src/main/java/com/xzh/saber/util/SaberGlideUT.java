package com.xzh.saber.util;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;

/**
 * Created by xzh on 2020/9/30.
 */
public class SaberGlideUT {
    /**
    *recycleView 滑动时不加载图片
    */
    public static void smoothScrollForRecycle(RecyclerView rc, final RequestManager mGlideRequest) {
        if (rc != null) {
            rc.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    switch (newState) {
                        case SCROLL_STATE_IDLE:
                            //滑动停止
                            try {
                                mGlideRequest.resumeRequests();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case SCROLL_STATE_DRAGGING:
                            //正在滚动
                            try {
                                mGlideRequest.pauseRequests();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;

                    }
                }
            });
        }
    }
}
