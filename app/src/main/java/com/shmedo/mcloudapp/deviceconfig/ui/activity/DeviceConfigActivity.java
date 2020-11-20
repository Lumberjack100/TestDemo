package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.BleConfigDeviceFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.NetConfigDeviceFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

import butterknife.BindView;
import butterknife.OnClick;
/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20<br/>
 * 描述：     Das设备配置页面
 */
public class DeviceConfigActivity extends BaseActivity {
    private static final String DEVICE_INFO = "device_info";

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.right_icon)
    ImageView mIvRightIcon;

    private int connectWay = AppContants.CommunicationWay.NET_PLATFORM_CONNECT;

    private Fragment fragment;

    private ProjectDeviceInfo projectDeviceInfo;

    private String bleInfo;


    public static void startActivity(Context context, ProjectDeviceInfo projectDeviceInfo) {
        Intent intent = new Intent(context, DeviceConfigActivity.class);
        intent.putExtra(DEVICE_INFO, projectDeviceInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int connectWay, String deviceInfo) {
        Intent intent = new Intent(context, DeviceConfigActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.CUR_BLE_DEVICE_INFO, deviceInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_device_config;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("设备配置");
        mIvRightIcon.setVisibility(View.VISIBLE);
        mIvRightIcon.setImageResource(R.drawable.ic_query_device_data);
        parseIntent();
        initFragment();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(AppContants.Extras.COMMUNICATION_WAY)) {
            connectWay = intent.getIntExtra(AppContants.Extras.COMMUNICATION_WAY, AppContants.CommunicationWay.NET_PLATFORM_CONNECT);
        }

        if (intent.getExtras().containsKey(DEVICE_INFO)) {
            projectDeviceInfo = intent.getParcelableExtra(DEVICE_INFO);
        }

        if (intent.getExtras().containsKey(AppContants.Extras.CUR_BLE_DEVICE_INFO)) {
            bleInfo = intent.getStringExtra(AppContants.Extras.CUR_BLE_DEVICE_INFO);
        }
    }

    private void initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            fragment = NetConfigDeviceFragment.newInstance(projectDeviceInfo);

        } else {
            fragment = BleConfigDeviceFragment.newInstance(bleInfo);
        }
        replaceFragment(fragment);
    }

    @OnClick({R.id.right_icon})
    public void onClick(View v) {
        if (v.getId() == R.id.right_icon) {
            String sn;
            if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
                sn = projectDeviceInfo.getToken();
            } else {
                sn = MCloudApp.getCurDeviceToken();
            }
            QueryDeviceDataActivity.startActivity(DeviceConfigActivity.this, sn);
        }
    }


    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commitAllowingStateLoss();
    }

    public void switchToNetConfigPage(ProjectDeviceInfo projectDeviceInfo) {
        fragment = NetConfigDeviceFragment.newInstance(projectDeviceInfo);
        replaceFragment(fragment);
    }

}
