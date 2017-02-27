package com.cysion.tdframework.ex_volley;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ByteArrayPool;
import com.android.volley.toolbox.HttpStack;
import com.cysion.tdframework.listener.TDownFileListener;
import com.cysion.tdframework.utils.TdFileUtils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.cookie.DateUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/6/13 0013.
 */
public class FileDownloadWork implements Network {

    protected static final boolean DEBUG = VolleyLog.DEBUG;

    private static int SLOW_REQUEST_THRESHOLD_MS = 3000;

    private static int DEFAULT_POOL_SIZE = 4096;


    protected final HttpStack mHttpStack;

    protected final ByteArrayPool mPool;

    private TDownFileListener mCallBack;
    private static final int DOWN_PERCENT = 1010;
    private static final int DOWN_FINISH = 1011;

    protected Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            long total = (long) msg.obj;
            switch (what) {
                case DOWN_PERCENT:
                    int percent = msg.arg1;
                    mCallBack.onProgress(percent, total);
                    break;
                case DOWN_FINISH:
                    mCallBack.onCompleted(total);
                    break;
            }
        }
    };

    private void setCallBack(TDownFileListener aCallBack) {
        if (aCallBack == null) {
            mCallBack = new FileDownloadRequest.DefaultFileCallBackT();
        }
        mCallBack = aCallBack;
    }

    /**
     * @param httpStack HTTP stack to be used
     */
    public FileDownloadWork(HttpStack httpStack) {
        // If a pool isn't passed in, then build a small default pool that will give us a lot of
        // benefit and not use too much memory.
        this(httpStack, new ByteArrayPool(DEFAULT_POOL_SIZE));
    }

    /**
     * @param httpStack HTTP stack to be used
     * @param pool      a buffer pool that improves GC performance in copy operations
     */
    public FileDownloadWork(HttpStack httpStack, ByteArrayPool pool) {
        mHttpStack = httpStack;
        mPool = pool;
    }

    @Override
    public NetworkResponse performRequest(Request<?> request) throws VolleyError {
        long requestStart = SystemClock.elapsedRealtime();
        setCallBack(((FileDownloadRequest) request).getCallBack());
        while (true) {
            HttpResponse httpResponse = null;
            byte[] responseContents = null;
            Map<String, String> responseHeaders = new HashMap<String, String>();
            try {
                // Gather headers.
                Map<String, String> headers = new HashMap<String, String>();
                addCacheHeaders(headers, request.getCacheEntry());
                httpResponse = mHttpStack.performRequest(request, headers);
                StatusLine statusLine = httpResponse.getStatusLine();
                int statusCode = statusLine.getStatusCode();

                responseHeaders = convertHeaders(httpResponse.getAllHeaders());
                // Handle cache validation.
                if (statusCode == HttpStatus.SC_NOT_MODIFIED) {
                    return new NetworkResponse(HttpStatus.SC_NOT_MODIFIED,
                            request.getCacheEntry() == null ? null : request.getCacheEntry().data,
                            responseHeaders, true);
                }

                // Some responses such as 204s do not have content.  We must check.
                if (httpResponse.getEntity() != null) {

                    responseContents = saveFile(httpResponse.getEntity(), request);
                } else {
                    // Add 0 byte response as a way of honestly representing a
                    // no-content request.
                    responseContents = new byte[0];
                }

                // if the request is slow, log it.
                long requestLifetime = SystemClock.elapsedRealtime() - requestStart;
                logSlowRequests(requestLifetime, request, responseContents, statusLine);

                if (statusCode < 200 || statusCode > 299) {
                    throw new IOException();
                }
                return new NetworkResponse(statusCode, responseContents, responseHeaders, false);
            } catch (SocketTimeoutException e) {
                attemptRetryOnException("socket", request, new TimeoutError());
            } catch (ConnectTimeoutException e) {
                attemptRetryOnException("connection", request, new TimeoutError());
            } catch (MalformedURLException e) {
                throw new RuntimeException("Bad URL " + request.getUrl(), e);
            } catch (IOException e) {
                int statusCode = 0;
                NetworkResponse networkResponse = null;
                if (httpResponse != null) {
                    statusCode = httpResponse.getStatusLine().getStatusCode();
                } else {
                    throw new NoConnectionError(e);
                }
                VolleyLog.e("Unexpected response code %d for %s", statusCode, request.getUrl());
                if (responseContents != null) {
                    networkResponse = new NetworkResponse(statusCode, responseContents,
                            responseHeaders, false);
                    if (statusCode == HttpStatus.SC_UNAUTHORIZED ||
                            statusCode == HttpStatus.SC_FORBIDDEN) {
                        attemptRetryOnException("auth",
                                request, new AuthFailureError(networkResponse));
                    } else {
                        // TODO: Only throw ServerError for 5xx status codes.
                        throw new ServerError(networkResponse);
                    }
                } else {
                    throw new NetworkError(networkResponse);
                }
            }
        }
    }

    /**
     * Logs requests that took over SLOW_REQUEST_THRESHOLD_MS to onCompleted.
     */
    private void logSlowRequests(long requestLifetime, Request<?> request,
                                 byte[] responseContents, StatusLine statusLine) {
        if (DEBUG || requestLifetime > SLOW_REQUEST_THRESHOLD_MS) {
            VolleyLog.d("HTTP response for request=<%s> [lifetime=%d], [size=%s], " +
                            "[rc=%d], [retryCount=%s]", request, requestLifetime,
                    responseContents != null ? responseContents.length : "null",
                    statusLine.getStatusCode(), request.getRetryPolicy().getCurrentRetryCount());
        }
    }

    /**
     * Attempts to prepare the request for a retry. If there are no more attempts remaining in the
     * request's retry policy, a timeout exception is thrown.
     *
     * @param request The request to use.
     */
    private static void attemptRetryOnException(String logPrefix, Request<?> request,
                                                VolleyError exception) throws VolleyError {
        RetryPolicy retryPolicy = request.getRetryPolicy();
        int oldTimeout = request.getTimeoutMs();

        try {
            retryPolicy.retry(exception);
        } catch (VolleyError e) {
            request.addMarker(
                    String.format("%s-timeout-giveup [timeout=%s]", logPrefix, oldTimeout));
            throw e;
        }
        request.addMarker(String.format("%s-retry [timeout=%s]", logPrefix, oldTimeout));
    }

    private void addCacheHeaders(Map<String, String> headers, Cache.Entry entry) {
        // If there's no cache entry, we're done.
        if (entry == null) {
            return;
        }

        if (entry.etag != null) {
            headers.put("If-None-Match", entry.etag);
        }

        if (entry.serverDate > 0) {
            Date refTime = new Date(entry.serverDate);
            headers.put("If-Modified-Since", DateUtils.formatDate(refTime));
        }
    }

    protected void logError(String what, String url, long start) {
        long now = SystemClock.elapsedRealtime();
        VolleyLog.v("HTTP ERROR(%s) %d ms to fetch %s", what, (now - start), url);
    }

    /**
     * Reads the contents of HttpEntity into a byte[] and save file.
     */
    private byte[] saveFile(HttpEntity entity, Request request) throws IOException, ServerError {
        File desFile = ((FileDownloadRequest) request).getDesFile();
        if (desFile == null) {
            desFile = TdFileUtils.getFile(request.getUrl());
        }
        BufferedOutputStream bytes = null;
        byte[] buffer = null;
        try {
            bytes = new BufferedOutputStream(new FileOutputStream(desFile));
            buffer = writeToFile(entity, bytes, buffer);
            return buffer;
        } finally {
            try {
                // Close the InputStream and release the resources by "consuming the content".
                entity.consumeContent();
            } catch (IOException e) {
                // This can happen if there was an exception above that left the entity in
                // an invalid state.
                VolleyLog.v("Error occured when calling consumingContent");
            }
            mPool.returnBuf(buffer);
            bytes.close();
        }
    }

    /**
     * 将网络返回结果写入文件，返回值暂时无意义
     *
     * @param entity
     * @param aBytes
     * @param aBuffer
     * @return
     * @throws IOException
     * @throws ServerError
     */
    private byte[] writeToFile(HttpEntity entity, BufferedOutputStream aBytes, byte[] aBuffer) throws IOException, ServerError {
        InputStream in = entity.getContent();
        long total = entity.getContentLength();
        if (in == null) {
            throw new ServerError();
        }
        aBuffer = mPool.getBuf(1024 * 2);
        int count;
        long progress = 0;
        int times = 0;//更新频率
        while ((count = in.read(aBuffer)) != -1) {
            aBytes.write(aBuffer, 0, count);
            progress = progress + count;
            if (times > 10) {
                times = 0;
                int percent = (int) (progress * 100 / total);
                if (total - progress < 1024) {
                    percent = 100;
                }
                sendState(total, percent, DOWN_PERCENT);
            }
            aBytes.flush();
            times++;
        }
        sendState(total, 0, DOWN_FINISH);
        return aBuffer;
    }

    /**
     * 根据文件下载进度发送消息
     *
     * @param aTotal
     * @param percent
     * @param code
     */
    private void sendState(long aTotal, int percent, int code) {
        Message msg = Message.obtain();
        msg.what = code;
        msg.obj = aTotal;
        switch (code) {
            case DOWN_PERCENT:
                msg.arg1 = percent;//进度百分比
                mHandler.sendMessage(msg);
                break;
            case DOWN_FINISH:
                mHandler.sendMessage(msg);
                break;
        }
    }

    /**
     * Converts Headers[] to Map<String, String>.
     */
    private static Map<String, String> convertHeaders(Header[] headers) {
        Map<String, String> result = new HashMap<String, String>();
        for (int i = 0; i < headers.length; i++) {
            result.put(headers[i].getName(), headers[i].getValue());
        }
        return result;
    }
}
