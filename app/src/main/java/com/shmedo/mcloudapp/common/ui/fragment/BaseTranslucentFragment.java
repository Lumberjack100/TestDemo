package com.shmedo.mcloudapp.common.ui.fragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.gyf.immersionbar.ImmersionBar;
import com.shmedo.mcloudapp.R;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/8/14 <br/>
 * 描述：     TODO
 */
public abstract class BaseTranslucentFragment extends BaseFragment {
    protected Toolbar toolbar;
    protected View statusBarView;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        statusBarView = view.findViewById(R.id.status_bar_view);
        toolbar = view.findViewById(R.id.toolbar);
        fitsLayoutOverlap();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //旋转屏幕为什么要重新设置布局与状态栏重叠呢？因为旋转屏幕有可能使状态栏高度不一样，如果你是使用的静态方法修复的，所以要重新调用修复
        fitsLayoutOverlap();
    }


    protected void fitsLayoutOverlap() {
        if (statusBarView != null) {
            ImmersionBar.setStatusBarView(this, statusBarView);
        } else if (toolbar != null) {
            ImmersionBar.setTitleBar(this, toolbar);
        }
    }
}
