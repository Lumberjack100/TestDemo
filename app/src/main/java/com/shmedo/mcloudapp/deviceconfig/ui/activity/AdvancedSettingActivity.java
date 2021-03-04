package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeAdvancedSettingFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.BleDasAdvancedSettingFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.NetDasAdvancedSettingFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40.NetE40AdvancedSettingFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40.TcpE40AdvancedSettingFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20.BleM20AdvancedSettingFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20.NetM20AdvancedSettingFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20<br/>
 * 描述：     Das高级设置页面
 */
public class AdvancedSettingActivity extends BaseConfigFragmentContainerActivity {
    private static final String PRO_DEVICE_INFO = "com.shmedo.mcloudapp.PRO_DEVICE_INFO";

    private int deviceType = AppContants.DeviceType.DAS;

    private ProjectDeviceInfo projectDeviceInfo;


    public static void startActivity(Context context, ProjectDeviceInfo projectDeviceInfo, int deviceType) {
        Intent intent = new Intent(context, AdvancedSettingActivity.class);
        intent.putExtra(PRO_DEVICE_INFO, projectDeviceInfo);
        intent.putExtra(AppContants.Extras.DEVICE_TYPE, deviceType);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int connectWay, int deviceType) {
        Intent intent = new Intent(context, AdvancedSettingActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.DEVICE_TYPE, deviceType);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("设置");
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(PRO_DEVICE_INFO)) {
            projectDeviceInfo = intent.getParcelableExtra(PRO_DEVICE_INFO);
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
                    fragment = NetDasAdvancedSettingFragment.newInstance(projectDeviceInfo);
                    break;

                case AppContants.DeviceType.ADME:
                    break;

                case AppContants.DeviceType.M20:
                    fragment = NetM20AdvancedSettingFragment.newInstance(projectDeviceInfo);
                    break;

                case AppContants.DeviceType.E40:
                    fragment = NetE40AdvancedSettingFragment.newInstance(projectDeviceInfo);
                    break;
            }
        } else if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
            switch (deviceType) {
                case AppContants.DeviceType.DAS:
                    fragment = new BleDasAdvancedSettingFragment();
                    break;

                case AppContants.DeviceType.ADME:
                    fragment = BleAdmeAdvancedSettingFragment.newInstance();
                    break;

                case AppContants.DeviceType.M20:
                    fragment = BleM20AdvancedSettingFragment.newInstance();
                    break;
            }
        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            switch (deviceType) {
                case AppContants.DeviceType.E40:
                    fragment = TcpE40AdvancedSettingFragment.newInstance();
                    break;
            }
        }

        return fragment;
    }
}
