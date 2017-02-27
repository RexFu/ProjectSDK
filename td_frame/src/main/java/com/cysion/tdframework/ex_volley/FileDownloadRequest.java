package com.cysion.tdframework.ex_volley;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.cysion.tdframework.listener.TDownFileListener;

import java.io.File;

/**
 * Created by xianshang.liu on 2016/6/14.
 */
public class FileDownloadRequest extends Request<String> {


    private TDownFileListener mCallBack;
    private File mDesFile;

    public FileDownloadRequest(int method, String url, Response.ErrorListener listener) {
        super(method, url, listener);
    }

    public File getDesFile() {
        return mDesFile;
    }

    public FileDownloadRequest(int method, String url, File desFile, TDownFileListener aCallBack, Response.ErrorListener listener) {
        super(method, url, listener);
        mCallBack = aCallBack;
        mDesFile = desFile;
        setShouldCache(false);

    }

    public TDownFileListener getCallBack() {
        return mCallBack;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        return Response.success("download", HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(String response) {
    }

   static class DefaultFileCallBackT implements TDownFileListener {


        @Override
        public void onProgress(int percent, long total) {

        }

        @Override
        public void onCompleted(long total) {

        }
    }
}