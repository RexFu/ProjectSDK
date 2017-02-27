package com.cysion.tdframework.utils;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

/**
 * Created by cysion.liu on 2016/6/20.
 * 调试工具类
 * 1--纯吐司测试
 * 2--纯log测试
 * 3--自定义位置的普通吐司
 * 4--自定义视图吐司
 */
public class TdDebugUtils {

    public static boolean debug = true;

    /**
     * 纯测试吐司，可以通过修改debug的值来改变展示与否
     * @param aContext
     * @param text
     */
    public static void debugToast(Context aContext,String text){
        if(debug){
            Toast.makeText(aContext, text, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 纯测试log，可以通过修改debug的值来改变展示与否
     * @param tag
     * @param text
     */
    public static void debugLog(String tag,String text){
        if(debug){
            Log.e(tag,text);
        }
    }

    /**
     * 在屏幕某个位置吐司--正常文本，默认底部
     * @param aContext
     * @param text
     */
    public static void toast(Context aContext,String text,int gravity){
        if(gravity!=Gravity.BOTTOM&&gravity!=Gravity.CENTER&&gravity!=Gravity.TOP){
            gravity = Gravity.BOTTOM;
        }
        Toast toast = Toast.makeText(aContext,text,Toast.LENGTH_SHORT);
        toast.setGravity(gravity,0,140);
        toast.show();
    }

    /**
     * 在屏幕某个位置自定义视图的吐司，自定义视图，默认底部
     * @param aContext
     * @param customView
     */
    public static void toastView(Context aContext,View customView,int gravity){
        if(gravity!=Gravity.BOTTOM&&gravity!=Gravity.CENTER&&gravity!=Gravity.TOP){
            gravity = Gravity.BOTTOM;
        }
        Toast toast = new Toast(aContext);
        toast.setGravity(gravity,0,140);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(customView);
        toast.show();
    }
}
