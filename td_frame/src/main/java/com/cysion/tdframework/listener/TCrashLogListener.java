package com.cysion.tdframework.listener;

import android.content.Context;

/**
 * Created by cysion.liu on 2016/6/17.
 *
 */
public interface TCrashLogListener {

    /**
     * 程序崩溃时，收集完设备信息并记录崩溃日志后，需要做的操作，此时在主线程；
     * @param fileName 错误日志文件的名称
     */
    void handleProcess(Context aContext, String fileName);

}
