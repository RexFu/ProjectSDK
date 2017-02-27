package com.cysion.tdframework.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cysion.tdframework.listener.TPageListener;
import com.cysion.tdframework.utils.TdSpUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by cysion.liu on 2016/6/21.
 * fragment的基类，主要功能包括：
 * 1--获得sharedpreference工具
 * 2--获得全局activity
 * 3--getLayoutId方法设置布局文件
 * 4--tracePage方法记录page
 */
public abstract class TdBaseFragment extends Fragment implements TPageListener {

    protected Activity mActivity;
    protected View mRootView;
    protected TdSpUtils mSpUtils;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mActivity = getActivity();
        //获得sharePreference工具
        mSpUtils = TdSpUtils.getInstance(mActivity);
        EventBus.getDefault().register(this);
        mRootView = inflater.inflate(getLayoutId(), container, false);
        findViews();
        return mRootView;
    }

    //给fragment设置布局文件
    protected abstract int getLayoutId();

    //找到fragment布局文件中的view
    protected abstract void findViews();

    @Override
    public void onStart() {
        super.onStart();
        tracePage();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
