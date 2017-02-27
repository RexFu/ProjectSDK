package com.cysion.tdframework.listener;

/**
 * Created by cysion on 2016/8/11 0011.
 */
public interface TActionListener {

    /**
     * 成功获得结果返回，注意，此结果代表本次请求最希望的数据，即目标数据
     * @param obj
     * @param taskId
     */
    void onSuccess(Object obj, int taskId);

    /**
     * 结果获得失败。可以为缓存带有的结果码
     * @param obj
     * @param taskId
     */
    void onFailure(Object obj, int taskId);
}
