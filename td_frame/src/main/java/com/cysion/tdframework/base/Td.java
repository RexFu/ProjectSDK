package com.cysion.tdframework.base;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.cysion.tdframework.proxy.ActivityManager;
import com.cysion.tdframework.proxy.CacheProxy;
import com.cysion.tdframework.proxy.FileProxy;
import com.cysion.tdframework.proxy.HttpProxy;
import com.cysion.tdframework.proxy.ImgProxy;
import com.cysion.tdframework.proxy.ParserProxy;
import com.cysion.tdframework.utils.TdFileUtils;
import com.cysion.tdframework.utils.TdSpUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Created by Administrator on 2016/5/30 0030.
 * 功能分发者，short of TaskDispatcher
 * 通过相应的方法，可以获得对应功能的中介，进一步使用
 * 相应第三方功能的功能
 */
public class Td {
    /**
     * Td is short of this;
     */
    private static final String TASK_DISPATCHER = "TASK_DISPATCHER";
    private static final String TASK_DISPATCHER_NEWDAY = "TASK_DISPATCHER_NEWDAY";

    private static Map<String, String> header;

    private String recordDate;


    /**
     * Init.
     *
     * @param aContext the a context
     */
    public static void init(Context aContext) {
        Context tempContext = aContext.getApplicationContext();
        getHttpProxy().getQueue(tempContext);
        getFileProxy().getQueue(tempContext);
        TBaseAction.initAction(tempContext);
        //若是新的一天，则清空缓存
        if (newDayOrNot(tempContext)) {
//            clearAll(tempContext);
            //默认周三清空一次图片缓存
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            if (cal.get(Calendar.DAY_OF_WEEK)==Calendar.WEDNESDAY) {
                getImgProxy().clearDiskCache(tempContext);
            }
            getCacheProxy(tempContext).clear();

        }
    }

    /**
     * 获得图片相关的代理者
     *
     * @return 图片加载代理对象
     */
    public static ImgProxy getImgProxy() {
        return ImgProxy.getInstance();
    }

    /**
     * 获得网络相关的代理者
     *
     * @return 网络加载代理对象
     */
    public static HttpProxy getHttpProxy() {
        return HttpProxy.getInstance();
    }

    /**
     * 获得上传和下载文件相关的代理者
     *
     * @return 上传和下载代理对象
     */
    public static FileProxy getFileProxy() {
        return FileProxy.getInstance();
    }

    /**
     * 获得缓存相关的代理者
     *
     * @return 缓存代理对象
     */
    public static CacheProxy getCacheProxy(Context aContext) {
        return CacheProxy.get(aContext);
    }

    /**
     * 获得解析json字符串相关的代理者
     *
     * @return json解析者代理对象
     */
    public static ParserProxy getParseProxy() {
        return ParserProxy.getInstance();
    }

    /**
     * 获得activity管理者
     *
     * @return activity管理者代理对象
     */
    public static ActivityManager getActyProxy() {
        return ActivityManager.getInstance();
    }


    //判断是否新的一天，首先读取缓存，缓存不等于今天，是新的一天；
    //否则，不是新的一天
    private static boolean newDayOrNot(Context aContext) {
        String asString = TdSpUtils.getInstance(aContext).getValue(TASK_DISPATCHER_NEWDAY, "");
        if (!getToday().equals(asString)) {
            TdSpUtils.getInstance(aContext).setValue(TASK_DISPATCHER_NEWDAY, getToday());
            return true;
        }
        return false;
    }

    private static String getToday() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        return sdf.format(new Date());
    }

    public static void clearAll(Context aContext) {
        getCacheProxy(aContext).clear();
        getImgProxy().clearCache(aContext);
        getImgProxy().clearDiskCache(aContext);

    }

    public static String getCacheSize(Context aContext) {
        long cacheSize = 0L;
        File jsonFile = new File(aContext.getCacheDir(), "CacheProxy");
        File imgFile = Glide.getPhotoCacheDir(aContext);
        cacheSize += TdFileUtils.getFileSize(jsonFile);
        cacheSize += TdFileUtils.getFileSize(imgFile);
        String fileSize = TdFileUtils.FormatFileSize(cacheSize);
        return fileSize;

    }

}
