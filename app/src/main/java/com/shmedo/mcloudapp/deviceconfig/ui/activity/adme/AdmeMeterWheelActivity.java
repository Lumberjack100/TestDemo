package com.shmedo.mcloudapp.deviceconfig.ui.activity.adme;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeMeterWheelFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.NetAdmeMeterWheelFragment;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/12/27<br/>
 * 描述：     ADME 计米轮参数配置页面
 */
public class AdmeMeterWheelActivity extends BaseConfigFragmentContainerActivity {

    public static void startActivity(Context context, int connectWay, DeviceInfo deviceInfo) {
        Intent intent = new Intent(context, AdmeMeterWheelActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.DEVICE_INFO, deviceInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("计米轮参数配置");
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            fragment = NetAdmeMeterWheelFragment.newInstance(deviceInfo);

        } else {
            fragment = BleAdmeMeterWheelFragment.newInstance();
        }

        return fragment;
    }
}