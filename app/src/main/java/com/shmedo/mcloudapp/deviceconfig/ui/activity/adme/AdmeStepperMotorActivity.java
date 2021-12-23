package com.shmedo.mcloudapp.deviceconfig.ui.activity.adme;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeStepperMotorFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.NetAdmeStepperMotorFragment;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/12/27<br/>
 * 描述：     ADME 步进电机参数配置页面
 */
public class AdmeStepperMotorActivity extends BaseConfigFragmentContainerActivity {

    public static void startActivity(Context context, DeviceInfo deviceInfo) {
        Intent intent = new Intent(context, AdmeStepperMotorActivity.class);
        intent.putExtra(PRO_DEVICE_INFO, deviceInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    public static void startActivity(Context context, int connectWay) {
        Intent intent = new Intent(context, AdmeStepperMotorActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("步进电机参数配置");
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            fragment = NetAdmeStepperMotorFragment.newInstance(deviceInfo);

        } else {
            fragment = BleAdmeStepperMotorFragment.newInstance();
        }

        return fragment;
    }
}