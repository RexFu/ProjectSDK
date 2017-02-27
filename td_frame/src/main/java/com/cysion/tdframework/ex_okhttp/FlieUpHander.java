package com.cysion.tdframework.ex_okhttp;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.cysion.tdframework.listener.TUpFileListener;

import java.lang.ref.WeakReference;


/**
 * User:lizhangqu(513163535@qq.com)
 * Date:2015-10-02
 * Time: 15:25
 */
public abstract class FlieUpHander extends Handler {
    public static final int UPDATE = 0x01;
    public static final int START = 0x02;
    public static final int FINISH = 0x03;
    //弱引用
    private final WeakReference<TUpFileListener> mUIProgressListenerWeakReference;

    public FlieUpHander(TUpFileListener aTUpFileListener) {
        super(Looper.getMainLooper());
        mUIProgressListenerWeakReference = new WeakReference<TUpFileListener>(aTUpFileListener);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case UPDATE: {
                TUpFileListener uiProgessListener = mUIProgressListenerWeakReference.get();
                if (uiProgessListener != null) {
                    //获得进度实体类
                    ProgressModel progressModel = (ProgressModel) msg.obj;
                    //回调抽象方法
                    progress(uiProgessListener, progressModel.getCurrentBytes(), progressModel
                            .getContentLength(), progressModel.isDone());
                }
                break;
            }
            case START: {
                TUpFileListener TUpFileListener = mUIProgressListenerWeakReference.get();
                if (TUpFileListener != null) {
                    //获得进度实体类
                    ProgressModel progressModel = (ProgressModel) msg.obj;
                    //回调抽象方法
                    start(TUpFileListener, progressModel.getCurrentBytes(), progressModel
                            .getContentLength(), progressModel.isDone());

                }
                break;
            }
            case FINISH: {
                TUpFileListener TUpFileListener = mUIProgressListenerWeakReference.get();
                if (TUpFileListener != null) {
                    //获得进度实体类
                    ProgressModel progressModel = (ProgressModel) msg.obj;
                    //回调抽象方法
                    finish(TUpFileListener, progressModel.getCurrentBytes(), progressModel
                            .getContentLength(), progressModel.isDone());
                }
                break;
            }
            default:
                super.handleMessage(msg);
                break;
        }
    }

    public abstract void start(TUpFileListener aTUpFileListener, long currentBytes, long
            contentLength, boolean done);

    public abstract void progress(TUpFileListener aTUpFileListener, long currentBytes, long
            contentLength, boolean done);

    public abstract void finish(TUpFileListener aTUpFileListener, long currentBytes, long
            contentLength, boolean done);
}
