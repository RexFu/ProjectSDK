package com.cysion.tdframework.base;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.cysion.tdframework.listener.TPageListener;
import com.cysion.tdframework.utils.TdSpUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2016/5/30 0030.
 */
public abstract class TBaseActivity extends AppCompatActivity implements TPageListener {

    protected TdSpUtils mSpUtils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//禁止所有Acitity横屏
        mSpUtils = TdSpUtils.getInstance(this);//获得sharePreference工具
        Td.getActyProxy().addActivity(this);//管理activity
        setContentView(getLayoutId());
        EventBus.getDefault().register(this);
        initView();
        setListener();
        getData();
    }

    protected abstract int getLayoutId();

    /**
     * 初始化布局及控件
     */
    protected abstract void initView();

    /**
     * 给控件设置事件监听
     */
    protected abstract void setListener();

    /**
     * 从网络获取数据
     */
    protected abstract void getData();

    @Override
    protected void onStart() {
        super.onStart();
        tracePage();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        Td.getActyProxy().removeActivity(this);
    }
}
