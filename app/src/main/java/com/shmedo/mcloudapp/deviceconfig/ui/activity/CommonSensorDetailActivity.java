package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dms.BleCommonSensorDetailFragment;

public class CommonSensorDetailActivity extends BaseConfigFragmentContainerActivity {
    public static void startActivity(Context context, DeviceInfo deviceInfo) {
        Intent intent = new Intent(context, CommonSensorDetailActivity.class);
        intent.putExtra(AppContants.Extras.DEVICE_INFO, deviceInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int connectWay) {
        Intent intent = new Intent(context, CommonSensorDetailActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("传感器配置");
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
//            fragment = NetDasSensorConfigFragment.newInstance(deviceInfo);

        } else if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
            fragment = BleCommonSensorDetailFragment.newInstance();
        }
        return fragment;
    }
}