package com.cysion.tdframework.proxy;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cysion.tdframework.ex_okhttp.FileUpRequestBody;
import com.cysion.tdframework.ex_volley.FileDownloadRequest;
import com.cysion.tdframework.ex_volley.FileVolley;
import com.cysion.tdframework.listener.TDownFileListener;
import com.cysion.tdframework.listener.TUpFileListener;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

/**
 * Created by cysion.liu on 2016/6/17.
 * 文件下载和文件上传代理类，主要是文件的下载和上传职责
 * 下载文件时，结合{@link TDownFileListener}使用;
 * 上传文件时，结合{@link TUpFileListener}使用
 */
public class FileProxy {

    private static RequestQueue queue;
    private static volatile FileProxy instance = new FileProxy();
    private static final OkHttpClient client = new OkHttpClient.Builder()
            //设置超时，不设置可能会报异常
            .connectTimeout(1000, TimeUnit.MINUTES)
            .readTimeout(1000, TimeUnit.MINUTES)
            .writeTimeout(1000, TimeUnit.MINUTES)
            .build();

    private FileProxy() {

    }

    public static synchronized FileProxy getInstance() {
        return instance;
    }

    //单例模式创建请求队列，context应传进程的context
    public synchronized RequestQueue getQueue(Context context) {
        if (queue == null) {
            queue = FileVolley.newRequestQueue(context);
        }
        return queue;
    }

    //自动下载到某个文件夹下，并根据url命名下载文件
    public void downFile(String url, TDownFileListener listener) {
        downFile(url, null, listener);

    }

    /**
     * 下载文件
     *
     * @param url      资源网址
     * @param desfile  存储为文件
     * @param listener 下载结果回调
     */
    public void downFile(String url, File desfile, TDownFileListener listener) {
        FileDownloadRequest fileDownloadRequest = new FileDownloadRequest(Request.Method.GET,
                url, desfile, listener, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("flag--", "FileProxy--onErrorResponse--" + error.getMessage());
            }
        });
        queue.add(fileDownloadRequest);
    }

    //自定义下载
    public void downFile(FileDownloadRequest request) {
        queue.add(request);
    }


    //上传文件，定制较为自由
    public void upFile(String url, TUpFileListener aTUpFileListener, RequestBody aRequestBody) {
        //以下供参考的格式,addFormDataPart--post;addPart--head&file
//        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
//                .addFormDataPart("title", "zb1242")
//                .addFormDataPart("Filedata", file.getName(), RequestBody.create(null, file))
//                .addPart(Headers.of("Content-Disposition", "form-data; name=\"another\";
//                        filename =\"another.dex\""), RequestBody.create(MediaType.parse("application/octet-stream"),
//                        file)).build();
        final okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url).post(new FileUpRequestBody(aRequestBody, aTUpFileListener)).build();
        //开始请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
            }
        });
    }

    //上传文件
    public void upFile(String url, RequestBody aRequestBody, TUpFileListener aTUpFileListener, Callback callback) {
        final okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url).post(new FileUpRequestBody(aRequestBody, aTUpFileListener)).build();
        //开始请求
        if (callback == null) {
            throw new RuntimeException("Callback 回调对象不能为null");
        }
        client.newCall(request).enqueue(callback);
    }

}
