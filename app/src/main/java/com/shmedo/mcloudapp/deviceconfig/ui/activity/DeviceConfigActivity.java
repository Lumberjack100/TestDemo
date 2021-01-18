package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DiscoveredBluetoothDevice;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.NetConfigDeviceFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.BleDasHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20.BleM20HomeFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20<br/>
 * 描述：     Das设备配置页面
 */
public class DeviceConfigActivity extends BaseConfigFragmentContainerActivity {
    public static final String EXTRA_DEVICE = "com.shmedo.mcloudapp.EXTRA_DEVICE";

    private int deviceType = AppContants.DeviceType.DAS;

    private ProjectDeviceInfo projectDeviceInfo;

    private DiscoveredBluetoothDevice device;


    public static void startActivity(Context context, ProjectDeviceInfo projectDeviceInfo, int deviceType) {
        Intent intent = new Intent(context, DeviceConfigActivity.class);
        intent.putExtra(EXTRA_DEVICE, projectDeviceInfo);
        intent.putExtra(AppContants.Extras.DEVICE_TYPE, deviceType);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int connectWay, DiscoveredBluetoothDevice device, int deviceType) {
        Intent intent = new Intent(context, DeviceConfigActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(EXTRA_DEVICE, device);
        intent.putExtra(AppContants.Extras.DEVICE_TYPE, deviceType);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("设备配置");
        mIvAction.setVisibility(View.VISIBLE);
        mIvAction.setImageResource(R.drawable.ic_query_device_data);
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;

        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            projectDeviceInfo = intent.getParcelableExtra(EXTRA_DEVICE);
        } else if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
            device = intent.getParcelableExtra(EXTRA_DEVICE);
        }

        if (intent.getExtras().containsKey(AppContants.Extras.DEVICE_TYPE)) {
            deviceType = intent.getIntExtra(AppContants.Extras.DEVICE_TYPE, AppContants.DeviceType.DAS);
        }
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            switch (deviceType) {
                case AppContants.DeviceType.DAS:
                    fragment = NetConfigDeviceFragment.newInstance(projectDeviceInfo);
                    break;

                case AppContants.DeviceType.ADME:
                    break;
            }
        } else if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
            switch (deviceType) {
                case AppContants.DeviceType.DAS:
                    fragment = BleDasHomeFragment.newInstance(device);
                    break;

                case AppContants.DeviceType.ADME:
                    fragment = BleAdmeHomeFragment.newInstance(device);
                    break;

                case AppContants.DeviceType.M20:
                    fragment = BleM20HomeFragment.newInstance(device);
                    break;
            }
        }

        return fragment;
    }

    @OnClick({R.id.iv_action})
    public void onClick(View v) {
        if (v.getId() == R.id.iv_action) {
            String sn;
            if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
                sn = projectDeviceInfo.getToken();
            } else {
                sn = MCloudApp.getCurDeviceToken();
            }
            QueryDeviceDataActivity.startActivity(DeviceConfigActivity.this, sn);
        }
    }

}
