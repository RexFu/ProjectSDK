package com.cysion.tdframework.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.cysion.tdframework.base.Td;

/**
 * Created by cysion on 2016/5/30 0030.
 * 以下工具类主要用于：
 * （1）检查是否有网络、
 * （2）检查是否有WIFI、
 * （3）检查是否有移动网络
 * （4）检查内存卡是否挂载
 *
 */
public class TdStateUtils {
    /**
     * 检查是否有网络
     *
     * @param context the context
     * @return the boolean
     */
    public static boolean isNetAvailable(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            return info.isAvailable();
        }
        return false;
    }

    /**
     * 检查是否是WIFI
     *
     * @param context the context
     * @return the boolean
     */
    public static boolean isWifi(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI)
                return true;
        }
        return false;
    }

    /**
     * 检查是否是移动网络
     *
     * @param context the context
     * @return the boolean
     */
    public static boolean isMobile(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE)
                return true;
        }
        return false;
    }

    private static NetworkInfo getNetworkInfo(Context context) {
        if (context==null) {
            Td.getActyProxy().AppExit(context);
        }
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * 检查SD卡是否存在
     *
     * @return the boolean
     */
    public static boolean checkSdCard() {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }
}
