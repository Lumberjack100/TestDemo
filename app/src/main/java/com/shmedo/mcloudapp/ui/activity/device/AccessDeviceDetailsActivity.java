package com.shmedo.mcloudapp.ui.activity.device;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;

import butterknife.BindView;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device
 * 文件名:   AccessDeviceDetailsActivity
 * 创建者:   dpc
 * 创建时间:  2019/1/28 17:07
 * 描述：    获取设备详情
 */
public class AccessDeviceDetailsActivity extends BaseActivity {

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;


    @Override
    protected int initContentView() {
        return R.layout.activity_access_device_details;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        initView();
    }


    private void initView() {
        mToolbarTitle.setText("设备详情");
    }
}
