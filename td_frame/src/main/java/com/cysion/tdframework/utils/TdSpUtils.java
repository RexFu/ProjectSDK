package com.cysion.tdframework.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by cysion.liu on 2016/6/20.
 * SharedPreference工具类，主要是更换sp文件，并进行存储和读取操作
 */
public class TdSpUtils {

    private Context context;

    public static final String NAME = "td_cache";

    private static SharedPreferences preferences;

    private static SharedPreferences.Editor edit;

    private static volatile TdSpUtils preferenceUtils;

    public static TdSpUtils getInstance(Context context) {
        if((preferenceUtils == null) && (context != null)) {
            synchronized(TdSpUtils.class) {
                if(preferenceUtils == null) {
                    preferenceUtils = new TdSpUtils(context);
                }
            }
        }
        return preferenceUtils;
    }

    /**
     * 打开一个新的sharedPreference文件
     * @param newName 新的sp文件的名称
     */
    public void setSpName(String newName){
        if(context!=null){
            preferences = context.getSharedPreferences(newName, 0);
            edit = preferences.edit();
        }
    }

    private TdSpUtils(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(NAME, 0);
        this.edit = this.preferences.edit();
    }

    public SharedPreferences getPreferences() {
        return this.preferences;
    }

    // 向当前sp文件里存入boolean值
    public void setValue(String key, boolean value) {
        edit.putBoolean(key, value);
        edit.commit();
    }

    public void setValue(int resKey, boolean value) {
        setValue(this.context.getString(resKey), value);
    }

    // 向当前sp文件里存入float值
    public void setValue(String key, float value) {
        edit.putFloat(key, value);
        edit.commit();
    }

    public void setValue(int resKey, float value) {
        setValue(this.context.getString(resKey), value);
    }

    // 向当前sp文件里存入Integer值
    public void setValue(String key, int value) {
        edit.putInt(key, value);
        edit.commit();
    }

    public void setValue(int resKey, int value) {
        setValue(this.context.getString(resKey), value);
    }

    // 向当前sp文件里存入long值
    public void setValue(String key, long value) {
        edit.putLong(key, value);
        edit.commit();
    }

    public void setValue(int resKey, long value) {
        setValue(this.context.getString(resKey), value);
    }

    // 向当前sp文件里存入String值
    public void setValue(String key, String value) {
        edit.putString(key, value);
        edit.commit();
    }

    public void setValue(int resKey, String value) {
        setValue(this.context.getString(resKey), value);
    }

    // Get

    // 从当前sp文件中取boolean值
    public boolean getValue(String key, boolean defaultValue) {
        try {
            return this.preferences.getBoolean(key, defaultValue);
        } catch(Exception e) {
            return defaultValue;
        }

    }

    public boolean getValue(int resKey, boolean defaultValue) {
        try {
            return getValue(this.context.getString(resKey), defaultValue);
        } catch(Exception e) {
            return defaultValue;
        }

    }

    // 从当前sp文件中取Float值
    public float getValue(String key, float defaultValue) {
        try {
            return this.preferences.getFloat(key, defaultValue);
        } catch(Exception e) {
            return defaultValue;
        }
    }

    public float getValue(int resKey, float defaultValue) {
        try {
            return getValue(this.context.getString(resKey), defaultValue);
        } catch(Exception e) {
            return defaultValue;
        }
    }

    // 从当前sp文件中取Integer值
    public int getValue(String key, int defaultValue) {
        try {
            return this.preferences.getInt(key, defaultValue);
        } catch(Exception e) {
            return defaultValue;
        }
    }

    public int getValue(int resKey, int defaultValue) {
        try {
            return getValue(this.context.getString(resKey), defaultValue);
        } catch(Exception e) {
            return defaultValue;
        }
    }

    //从当前sp文件中取long值
    public long getValue(String key, long defaultValue) {
        try {
            return this.preferences.getLong(key, defaultValue);
        } catch(Exception e) {
            return defaultValue;
        }
    }

    public long getValue(int resKey, long defaultValue) {
        try {
            return getValue(this.context.getString(resKey), defaultValue);
        } catch(Exception e) {
            return defaultValue;
        }
    }

    // 从当前sp文件中取String值
    public String getValue(String key, String defaultValue) {
        try {
            return this.preferences.getString(key, defaultValue);
        } catch(Exception e) {
            return defaultValue;
        }
    }

    public String getValue(int resKey, String defaultValue) {
        try {
            return getValue(this.context.getString(resKey), defaultValue);
        } catch(Exception e) {
            return defaultValue;
        }
    }

    //删除某个键值对
    public void remove(String key) {
        edit.remove(key);
        edit.commit();
    }

    //清空当前名称的sp文件
    public void clear() {
        edit.clear();
        edit.commit();
    }
}
