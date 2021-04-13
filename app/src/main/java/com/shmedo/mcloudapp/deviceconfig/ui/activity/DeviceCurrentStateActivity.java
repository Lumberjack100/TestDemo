package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeCurrentStateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.BleDasCurrentStateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.NetDasCurrentStateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40.TcpE40CurrentStateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20.BleM20CurrentStateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20.NetM20CurrentStateFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/10/21 <br/>
 * 描述：   设备运行状态
 */
public class DeviceCurrentStateActivity extends BaseConfigFragmentContainerActivity {
    private int deviceType = AppContants.DeviceType.DAS;

    public static void startActivity(Context context, int connectWay, int deviceType) {
        Intent intent = new Intent(context, DeviceCurrentStateActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.DEVICE_TYPE, deviceType);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, ProjectDeviceInfo projectDeviceInfo, int deviceType) {
        Intent intent = new Intent(context, DeviceCurrentStateActivity.class);
        intent.putExtra(PRO_DEVICE_INFO, projectDeviceInfo);
        intent.putExtra(AppContants.Extras.DEVICE_TYPE, deviceType);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("运行状态");
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(AppContants.Extras.DEVICE_TYPE)) {
            deviceType = intent.getIntExtra(AppContants.Extras.DEVICE_TYPE, AppContants.DeviceType.DAS);
        }
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            switch (deviceType) {
                case AppContants.DeviceType.DAS:
                    fragment = NetDasCurrentStateFragment.newInstance(projectDeviceInfo);
                    break;

                case AppContants.DeviceType.ADME:
                    break;

                case AppContants.DeviceType.M20:
                case AppContants.DeviceType.E40:
                    fragment = NetM20CurrentStateFragment.newInstance(projectDeviceInfo);
                    break;
            }
        }else if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
            switch (deviceType) {
                case AppContants.DeviceType.DAS:
                    fragment = BleDasCurrentStateFragment.newInstance();
                    break;

                case AppContants.DeviceType.ADME:
                    fragment = BleAdmeCurrentStateFragment.newInstance();
                    break;

                case AppContants.DeviceType.M20:
                    fragment = BleM20CurrentStateFragment.newInstance();
                    break;
            }
        }else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            switch (deviceType) {
                case AppContants.DeviceType.E40:
                    fragment = TcpE40CurrentStateFragment.newInstance();
                    break;
            }
        }

        return fragment;
    }

}