package com.cysion.tdframework.listener;

/**
 * Created by xianshang.liu on 2016/6/14.
 */
public interface TDownFileListener {

    /**
     * 获得下载进度
     *
     * @param percent 进度百分比
     * @param total   文件总大小，byte
     */
    void onProgress(int percent, long total);

    void onCompleted(long total);
}
