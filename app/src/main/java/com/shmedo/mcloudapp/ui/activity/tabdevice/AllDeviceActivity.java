package com.shmedo.mcloudapp.ui.activity.tabdevice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.ui.fragment.AdvanceSetFragment;
import com.shmedo.mcloudapp.ui.fragment.DeviceDetailsFragment;
import com.shmedo.mcloudapp.ui.fragment.DeviceFragment;
import com.shmedo.mcloudapp.ui.fragment.QueryDataFragment;
import com.shmedo.mcloudapp.ui.fragment.RunStatusFragment;
import com.shmedo.mcloudapp.ui.fragment.SetGuideFragment;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.tabdevice
 * 文件名:   AllDeviceActivity
 * 创建者:   dpc
 * 创建时间:  2019/3/12 15:00
 * 描述：    tab-设备
 */
public class AllDeviceActivity extends BaseActivity {

    private DeviceFragment deviceFragment;

    @Override protected int initContentView() {
        return R.layout.activity_all_device;
    }


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }


    private void initData() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        deviceFragment = new DeviceFragment();
        transaction.replace(R.id.frame_content,deviceFragment);
        transaction.commit();
    }
}
