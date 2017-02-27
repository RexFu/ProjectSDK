package com.cysion.tdframework.proxy;


import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.cysion.tdframework.base.Td;
import com.cysion.tdframework.ex_volley.HeaderProvider;
import com.cysion.tdframework.ex_volley.StringRequest;
import com.cysion.tdframework.listener.THttpListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Cysion on 2016/6/16 .
 * 频繁网络交互的代理者，目前基于volley
 * 要结合结合{@link THttpListener}使用
 * get和post方法，还包括加入header的情况
 */
public class
HttpProxy {

    private static RequestQueue queue;
    private static HttpProxy instance = new HttpProxy();
    private HeaderProvider mHeaderProvider = new HeaderProvider() {
        @Override
        public Map<String, String> getHeader() {
            return new HashMap   <>();
        }
    };

    private HttpProxy() {

    }

    public static synchronized HttpProxy getInstance() {

        return instance;
    }

    //单例模式创建请求队列，context应传进程的context
    public static synchronized RequestQueue getQueue(Context context) {
        if (queue == null) {
            queue = Volley.newRequestQueue(context);
        }
        return queue;
    }

    //get网络请求返回String
    public void getHttpData(String url, final THttpListener callBack, final int taskId) {
        getHttpData(url, callBack, null, taskId);
    }

    /**
     * get请求,返回网络请求结果，可直接获得对应类型
     *
     * @param url         网络地址
     * @param callBack    结果回调
     * @param taskId      任务ID同时也是该任务的TAG
     * @param targetClass 需要得到的对象
     */
    public void getHttpData(String url, final THttpListener callBack, final Class<?> targetClass,
                            final int taskId) {
        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {
                if (targetClass != null) {
                    Object obj = getEntity(response, targetClass);
                    callBack.onSuccess(obj, taskId);
                } else {
                    callBack.onSuccess(response, taskId);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callBack.onFailure(error, taskId);
            }
        }
        );
        stringRequest.setTag(taskId);
        queue.add(stringRequest);
    }

    //post网络获得String
    public void postHttpData(String url, final THttpListener callBack,
                             final Map<String, String> paraMap, final int taskId) {
        postHttpData(url, callBack, null, paraMap, taskId);
    }

    /**
     * post网络获得结果
     *
     * @param url         请求地址
     * @param callBack    请求回调
     * @param paraMap     参数集合
     * @param taskId      任务id
     * @param targetClass 需要得到的对象
     */
    public void postHttpData(String url, final THttpListener callBack, final Class<?> targetClass,
                             final Map<String, String> paraMap, final int taskId) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response
                .Listener<String>() {
            @Override
            public void onResponse(final String response) {
                if (targetClass != null) {
                    Object obj = getEntity(response, targetClass);
                    callBack.onSuccess(obj, taskId);
                } else {
                    callBack.onSuccess(response, taskId);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callBack.onFailure(error, taskId);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return paraMap;
            }
        };
        stringRequest.setTag(taskId);
        queue.add(stringRequest);
    }

    //特定请求头的get返回字符串结果
    public void getHttpDataWithHeader(String url, final THttpListener callBack, final Map<String,
            String> headers,
                                      final int taskId) {
        getHttpDataWithHeader(url, callBack, headers, null, taskId);

    }

    public void setHeader(HeaderProvider aProvider) {
        mHeaderProvider = aProvider;

    }

    //特定请求头的get返回字符串结果
    public void getHttpDataWithHeader(String url, THttpListener callBack,
                                      Class<?> targetClass, int taskId) {
        getHttpDataWithHeader(url, callBack, mHeaderProvider.getHeader(), targetClass, taskId);
    }


    /**
     * 带特定请求头的get请求,
     *
     * @param url
     * @param callBack
     * @param headers
     * @param taskId
     * @param targetClass 需要得到的对象
     */
    public void getHttpDataWithHeader(String url, final THttpListener callBack, final Map<String,
            String> headers,
                                      final Class<?> targetClass, final int taskId) {
        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {
                if (targetClass != null) {
                    Object obj = getEntity(response, targetClass);
                    callBack.onSuccess(obj, taskId);
                } else {
                    callBack.onSuccess(response, taskId);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callBack.onFailure(error, taskId);
            }
        }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };
        stringRequest.setTag(taskId);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

    //特定请求头的post返回字符串结果
    public void postHttpDataWithHeader(String url, final THttpListener callBack,
                                       final Map<String, String> paraMap, final Map<String,
            String> headers, final int taskId) {
        postHttpDataWithHeader(url, callBack, null, paraMap, headers, taskId);
    }
    //特定请求头的post返回字符串结果
    public void postHttpDataWithHeader(String url, THttpListener callBack,
                                       Map<String, String> paraMap, Class<?> targetClass ,int taskId) {
        postHttpDataWithHeader(url, callBack, targetClass, paraMap, mHeaderProvider.getHeader(), taskId);
    }

    /**
     * 带特定请求头的post请求
     *
     * @param url
     * @param callBack
     * @param targetClass
     * @param paraMap
     * @param headers
     * @param taskId
     */
    public void postHttpDataWithHeader(String url, final THttpListener callBack, final Class<?>
            targetClass,
                                       final Map<String, String> paraMap, final Map<String,
            String> headers, final int taskId) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response
                .Listener<String>() {
            @Override
            public void onResponse(final String response) {

                if (targetClass != null) {
                    Object obj = getEntity(response, targetClass);
                    callBack.onSuccess(obj, taskId);
                } else {
                    callBack.onSuccess(response, taskId);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callBack.onFailure(error, taskId);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return paraMap;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        stringRequest.setTag(taskId);
        queue.add(stringRequest);
    }

    private Object getEntity(String result, Class<?> aClass) {
        if (aClass == null) {
            return result;
        }
        return Td.getParseProxy().parse(result, aClass);
    }

    //取消特定tag的请求
    public void cancel(Object tag) {
        queue.cancelAll(tag);
    }

    public void cancelAll(Object[] tags) {
        for (int i = 0; i < tags.length; i++) {
            cancel(tags[i]);
        }
    }

    //清除http的缓存
    public void clearHttpCache() {
        queue.getCache().clear();
    }
}
