package com.shmedo.mcloudapp.ui.activity.device.config;

import android.os.Bundle;
import android.support.annotation.Nullable;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device.config
 * 文件名:   SensorSettingActivity
 * 创建者:   dpc
 * 创建时间:  2019/2/15 11:06
 * 描述：    传感器参数配置
 */
public class SensorSettingActivity extends BaseActivity {
    @Override protected int initContentView() {
        return R.layout.activity_sensor_setting;
    }


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }


    private void initView() {

    }
}
