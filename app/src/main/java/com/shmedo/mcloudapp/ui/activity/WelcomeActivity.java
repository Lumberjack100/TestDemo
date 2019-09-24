package com.shmedo.mcloudapp.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.util.StartActivityUtil;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 文件名:   WelcomeActivity
 * 创建者:   dpc
 * 创建时间:  2019/1/8 09:17
 * 描述：    TODO
 */
public class WelcomeActivity extends BaseActivity {
    @Override protected int initContentView() {
        return R.layout.activity_welcome;
    }


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStates();
        initView();
    }

    private void initView() {
        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                StartActivityUtil.comeOnBaby(WelcomeActivity.this,LoginActivity.class);
                finish();
            }
        },2000);
    }
    /**
     * 沉浸式状态栏
     */
    private void initStates() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }
}
