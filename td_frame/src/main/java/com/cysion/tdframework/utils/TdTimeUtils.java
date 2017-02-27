package com.cysion.tdframework.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by cysion.liu on 2016/6/21.
 * 时间工具类，目前功能包括：
 * 1--获得指定格式的当前日期
 * 2--将时间戳转化为指定格式的日期
 * 3--将时间戳转化为文字显示，例如 3小时前
 */
public class TdTimeUtils {

    /**
     * 获得当前日期时间格式
     * @param timeFormat 日期格式，例如：“yyyy-MM-dd:HH”
     * @return 指定格式的日期
     */
    public static String getCurDate(String timeFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timeFormat, Locale.getDefault());
        Date date = new Date();
        return simpleDateFormat.format(date);
    }

    /**
     * 获得指定时间戳日期时间格式
     * @param originTime  初始格式的时间
     * @param timeFormat  目标时间格式，例如“yyyy-MM-dd:HH”
     * @return 指定格式的日期
     */
    public static String getFormatDate(String originTime,String timeFormat) {
        long mill = 0;
        try {
            mill = Long.parseLong(originTime);
        } catch(Exception e) {
            e.printStackTrace();
            return "-1";
        }
        Date date = new Date(mill * 1000L);
        String strs = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            strs = sdf.format(date);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return strs;
    }

    /**
     * 将时间戳转化为文字显示【 xxx前】
     * @param time 单位 秒
     * @return
     */
    public static String getDescriptTime(long time) {
        long currentTime = System.currentTimeMillis() / 1000;
        String string = "";
        long distance = 0;
        distance = currentTime - time;
        if(distance < 1) {// avoid 0 seconds
            string = "刚刚";
        } else if(distance < 60) {
            string = distance + "秒前";
        } else if(distance < 3600) {// 60 * 60
            string = distance / 60 + "分钟前";
        } else if(distance < 86400) {// 60 * 60 * 24
            string = distance / 3600 + "小时前";
        } else if(distance < 604800) {// 60 * 60 * 24 * 7
            string = distance / 86400 + "天前";
        } else if(distance < 2419200) {// 60 * 60 * 24 * 7 * 4
            string = distance / 604800 + "周前";
        } else {
            string = getFormatDate(String.valueOf(time),"yyyy-MM-dd");
        }
        return string;
    }
}
