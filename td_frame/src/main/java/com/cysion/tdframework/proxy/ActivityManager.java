package com.cysion.tdframework.proxy;

import android.app.Activity;
import android.content.Context;

import java.util.Stack;


/**
 * 应用程序Activity堆栈管理类：用于Activity管理和应用程序退出
 *
 * @author cysion
 * @Date 2015-11-21
 */
public class ActivityManager {

    private static Stack<Activity> activityStack;
    private static ActivityManager instance = new ActivityManager();

    private ActivityManager() {
        activityStack = new Stack<>();
    }

    /**
     * 单一实例
     */
    public static ActivityManager getInstance() {
        return instance;
    }

    /**
     * 添加Activity到堆栈
     */
    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        activityStack.add(activity);
    }

    /**
     * 移除指定的Activity
     */
    public void removeActivity(Activity activity) {
        if (activity != null)
            activityStack.remove(activity);
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i)) {
                activityStack.get(i).finish();
            }
        }
        activityStack.clear();
    }

    //向外提供当前可见的acty,特殊情况使用，优先使用当前焦点的acty
    public Activity getActivity() {
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (activityStack.get(i) != null && activityStack.get(i).hasWindowFocus()) {
                return activityStack.get(i);
            }
        }
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (activityStack.get(i) != null && !activityStack.get(i).isFinishing()) {
                return activityStack.get(i);
            }
        }
        return null;
    }

    /**
     * 退出应用程序
     */
    public void AppExit(Context context) {
        try {
            finishAllActivity();
            System.exit(0);
        } catch (Exception e) {
        }
    }
}