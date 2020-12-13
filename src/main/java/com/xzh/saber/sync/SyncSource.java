package com.xzh.saber.sync;

/**
 * Created by xzh on 2020/10/27.
 * 下载进度监听参数实体类
 */

public class SyncSource {
    public SyncSource(int currentProgress, Long totalDuration) {
        this.currentProgress = currentProgress;
        this.totalDuration = totalDuration;
    }
    private int currentProgress;
    private Long totalDuration;

    public int getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
    }

    public Long getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(Long totalDuration) {
        this.totalDuration = totalDuration;
    }
}
