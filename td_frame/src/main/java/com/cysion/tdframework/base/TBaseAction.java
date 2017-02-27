package com.cysion.tdframework.base;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.cysion.tdframework.listener.TActionListener;
import com.cysion.tdframework.listener.THttpListener;
import com.cysion.tdframework.proxy.CacheProxy;
import com.cysion.tdframework.utils.TdEncryptUtils;
import com.cysion.tdframework.utils.TdStateUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.cysion.tdframework.base.DataState.NO_CACHE;

/**
 * Created by cysion on 2016/8/11 0011.
 * 数据操作的父类，集中了数据网络请求，数据缓存等操作
 * 使用方式，例如child继承该类，则
 * new child(listener).params(pas).taskid(id).execute(code);
 * 注意，使用时，子类应根据数据情况重写父类的一些方法
 * 比如固定参数fixedParams()的方法，特殊情况处理网络返回的数据handleDataFromNet
 * 等
 */
public abstract class TBaseAction {

    protected static Context mContext;
    //请求网络的方法
    protected static final int Method_GET = 0;
    protected static final int Method_POST = 1;

    private static boolean DEBUG = false;
    private String url;//你懂得
    protected DataState mActionCode;//数据操作码
    protected int mReqMethod;//请求网络的方式
    private int taskId = -1;//请求行为的id
    protected Map<String, String> params;//请求网络的参数
    protected TActionListener listener;//结果回调
    private String key;

    protected static Map<String, String> mHeader;

    /**
     * 初始化数据类，在所有的数据请求前，全局调用一次即可
     * 建议放在application中
     *
     * @param aContext
     */
    public static void initAction(Context aContext) {
        mContext = aContext.getApplicationContext();
    }

    public TBaseAction(TActionListener aListener) {
        if (mContext != null) {
            listener = aListener;
            url = getUrl();
            mReqMethod = getHttpMethod();
            mActionCode = NO_CACHE;
        } else {
            try {
                throw new Exception("should invoke initAction firstly");
            } catch (Exception aE) {
                aE.printStackTrace();
            }
        }
    }

    /*给post请求设置参数 */
    public TBaseAction params(Map<String, String> aParams) {
        params = aParams;
        return this;
    }

    //设置调试模式
    public static void setDebug() {
        DEBUG = true;
    }

    /*给请求设置任务id */
    public TBaseAction taskId(int id) {
        taskId = id;
        return this;
    }

    /**
     * 执行方法，包括相关校验，各种数据请求方式的处理
     *
     * @param actionCode 数据操作码
     */
    public void execute(DataState actionCode) {
        mActionCode = actionCode;
        checkUrlAndId();
        checkActionCode();
        getKey();
        if (!TdStateUtils.isNetAvailable(mContext)) {
            whenNoNet();
            return;
        }
        if (mActionCode == DataState.CACHE_FIRST) {
            if (!isCacheValid(taskId)) {
                byHttp();
            }
        } else {
            byHttp();

        }
    }

    private void getKey() {
        params = getParams();
        String buffer = getAppearUrl(params);
        logv("flag--", "TBaseAction--getAppearUrl--218--" + buffer);
        try {
            key = TdEncryptUtils.MD5encrypt(buffer.toString(), "utf-8");
        } catch (Exception aE) {
            aE.printStackTrace();
        }
    }

    private void checkUrlAndId() {
        if (TextUtils.isEmpty(url) || taskId == -1) {
            try {
                throw new Exception("should give url a value or give taskId a value");
            } catch (Exception aE) {
                aE.printStackTrace();
            }
        }
    }

    private void checkActionCode() {
        DataState[] values = DataState.values();
        if (!Arrays.asList(values).contains(mActionCode)) {
            try {
                throw new Exception("invalid actionCode");
            } catch (Exception aE) {
                aE.printStackTrace();
            }
        }
    }


    private Map<String, String> getParams() {
        if (params != null) {
            return params;
        }
        Map<String, String> temp = fixedParams();
        return temp;
    }

    //被项目action基类实现
    protected Map<String, String> getHeader() {
        if (mHeader == null) {
            mHeader = new HashMap<>();
        }

        return mHeader;
    }


    //override by children when params are fix ;
    protected Map<String, String> fixedParams() {
        return new HashMap<>();
    }

    /**
     * 没有网络时，首先返回错误码，然后对应不同请求有不同方式
     * 对于默认缓存的，当然直接获得目标数据
     * 对于默认联网却也可得到缓存的，也直接返回目标数据
     */
    protected void whenNoNet() {
        listener.onFailure(Constant.NO_NET, taskId);
        switch (mActionCode) {
            case CACHE_FIRST:
            case NET_FIRST:
                isCacheValid(taskId);
                break;
            default:
                break;
        }
    }

    private boolean isCacheValid(int aTaskId) {
        String fromCache = readCache(aTaskId);
        if (!TextUtils.isEmpty(fromCache)) {
            return handleDataFromCache(fromCache, aTaskId);
        }
        return false;
    }

    /**
     * 联网请求数据，返回的结果为json字符串
     * 只定义的Get和Post方法，可重写定义特殊参数的请求
     * 请求完成后将taskid设为0，之后注意taskid的处理，防止多次请求
     * 的taskid一致，造成数据混乱
     */
    protected void byHttp() {
        if (mReqMethod == Method_GET) {
            if (params.size() > 0) {
                url = getAppearUrl(params);
            }
            Td.getHttpProxy().getHttpDataWithHeader(url, callBack, getHeader(),
                    taskId);
        } else {
            Td.getHttpProxy().postHttpDataWithHeader(url, callBack, params, getHeader(), taskId);
        }
        taskId = -1;
        url = getUrl();
        params = null;
    }

    private String getAppearUrl(Map<String, String> aParams) {
        String temp = url + "?";
        Set<String> strings = aParams.keySet();
        for (String aKey : strings) {
            temp = temp + aKey + "=" + aParams.get(aKey) + "&";
        }
        temp = temp.substring(0, temp.length() - 1);

        return temp;
    }

    private void logv(String tag, String lable) {
        if (DEBUG) {
            Log.v(tag, lable);
        }
    }

    private String readCache(int aTaskId) {
        if (mActionCode == NO_CACHE) {
            return null;
        }
        CacheProxy cacheObj = CacheProxy.get(mContext);
        String fromCache = cacheObj.getAsString(key);
        return fromCache;

    }

    private void writeToCache(String shouldCache) {
        if (mActionCode == NO_CACHE) {
            return;
        }
        if (TextUtils.isEmpty(shouldCache)) {
            return;
        }
        CacheProxy cacheObj = CacheProxy.get(mContext);
        switch (mActionCode) {
            case CACHE_FIRST:
            case NET_FIRST:
                cacheObj.remove(key);
                cacheObj.put(key, shouldCache);
                break;
            case HEAD_REFRESH:
                addHeadCache(shouldCache);
                break;
            case LOAD_MORE:
                loadMoreCache(shouldCache);
                break;
        }
    }

    /*网络回调的默认接收者*/
    protected THttpListener callBack = new THttpListener() {
        @Override
        public void onSuccess(Object obj, int aTaskId) {
            String result = (String) obj;
            //验证得到的数据是否包含目标数据，
            //是，则处理获得目标数据，返回给指令者目标数据，并将目标数据写入缓存
            //否，若非GLOABL_REFRESH，则返回失败码，若GLOABL_REFRESH，读缓存

            if (handleDataFromNet(result, aTaskId)) {
                writeToCache(result);
            } else {
                if (mActionCode == DataState.NET_FIRST && isCacheValid(aTaskId)) {
                } else {
                    listener.onFailure(Constant.NO_TARGET_DATA, aTaskId);
                }
            }
        }

        @Override
        public void onFailure(Object obj, int taskId) {
            listener.onFailure(Constant.UNKNOWN_ERROR, taskId);
        }
    };

    protected abstract String getUrl();

    protected abstract int getHttpMethod();

    /**
     * 判断网络返回的数据是否有目标数据， 对应于不同taskid数据有不同的处理
     * 当有目标数据时，通过listener.onSuccess回调目标结果指令者，返回true;
     * 没有目标数据时，返回false;
     * 如有必要，此方法需要重写
     *
     * @param aResult 来自网络或是缓存的json字符串，需要根据其判断是否有目标数据
     * @param aTaskId 不同任务的id
     * @return 有目标数据，则返回true
     */
    protected abstract boolean getTargetDataFromJson(String aResult, int aTaskId);

    /**
     * 判断网络返回的数据是否有目标数据， 对应于不同taskid数据有不同的处理
     * 当有目标数据时，通过listener.onSuccess回调目标结果指令者，返回true;
     * 没有目标数据时，返回false;
     * 如有必要，此方法需要重写
     *
     * @param aResult 网络返回的数据
     * @param aTaskId 任务id
     * @return
     */
    protected boolean handleDataFromNet(String aResult, int aTaskId) {
        return getTargetDataFromJson(aResult, aTaskId);
    }


    /**
     * 将缓存中的数据处理成目标数据返回给 指令输入者，应该根据操作码mActionCode的不同处理不同的逻辑
     * 处理完成后应该调用 listener.onSuccess() 返回数据
     * 如有必要，需要重写该方法，
     * 此时aResult一定含有目标数据
     *
     * @param aResult 来自于缓存中的数据
     * @param aTaskId 任务id
     */
    protected boolean handleDataFromCache(String aResult, int aTaskId) {
        return getTargetDataFromJson(aResult, aTaskId);
    }

    /**
     * 在成功获得目标数据时，加载更多需要进行的缓存写入操作
     * 对于只缓存最新数据来说，此处不缓存
     * 对于需要缓存所有已加载数据来说，需根据数据格式重写
     * 注意传入的某些参数要在默认加载时保持统一，比如页数
     *
     * @param aShouldCache 目标数据对应的json字符串
     */
    protected void loadMoreCache(String aShouldCache) {
    }

    /**
     * 在成功获得目标数据时，上拉刷新需要进行的缓存写入操作
     * 对于只缓存最新数据来说，此处置换式写入即可
     * 对于需要缓存所见数据来说，需根据数据格式重写
     *
     * @param aShouldCache 目标数据对应的json字符串
     */
    protected void addHeadCache(String aShouldCache) {
        CacheProxy cacheObj = CacheProxy.get(mContext);
        cacheObj.remove(key);
        cacheObj.put(key, aShouldCache);
    }
}
