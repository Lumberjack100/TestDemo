package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeDataCenterHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.NetAdmeDataCenterHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.NetDasDataCenterHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40.NetE40DataCenterHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40.TcpE40DataCenterHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20.BleM20DataCenterHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20.NetM20DataCenterHomeFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/12/29<br/>
 * 描述：    数据中心主页面
 */
public class DataCenterHomeActivity extends BaseConfigFragmentContainerActivity {
    private static final String LEVEL_INITIAL = "com.shmedo.mcloudapp.LEVEL_INITIAL";

    private int configMethod = AppContants.DataCenterConfigMethod.BASIC_CONFIG;
    private int deviceType = AppContants.DeviceType.DAS;
    private boolean isLevelInit = false;


    public static void startActivity(Context context, int deviceType, ProjectDeviceInfo projectDeviceInfo, int configMethod) {
        Intent intent = new Intent(context, DataCenterHomeActivity.class);
        intent.putExtra(AppContants.Extras.DEVICE_TYPE, deviceType);
        intent.putExtra(PRO_DEVICE_INFO, projectDeviceInfo);
        intent.putExtra(AppContants.Extras.DATA_CENTER_CONFIG_METHOD, configMethod);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int deviceType, int connectWay, int configMethod) {
        Intent intent = new Intent(context, DataCenterHomeActivity.class);
        intent.putExtra(AppContants.Extras.DEVICE_TYPE, deviceType);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.DATA_CENTER_CONFIG_METHOD, configMethod);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int deviceType, ProjectDeviceInfo projectDeviceInfo, int configMethod, boolean isLevelInit) {
        Intent intent = new Intent(context, DataCenterHomeActivity.class);
        intent.putExtra(AppContants.Extras.DEVICE_TYPE, deviceType);
        intent.putExtra(PRO_DEVICE_INFO, projectDeviceInfo);
        intent.putExtra(AppContants.Extras.DATA_CENTER_CONFIG_METHOD, configMethod);
        intent.putExtra(LEVEL_INITIAL, isLevelInit);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int deviceType, int connectWay, int configMethod, boolean isLevelInit) {
        Intent intent = new Intent(context, DataCenterHomeActivity.class);
        intent.putExtra(AppContants.Extras.DEVICE_TYPE, deviceType);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.DATA_CENTER_CONFIG_METHOD, configMethod);
        intent.putExtra(LEVEL_INITIAL, isLevelInit);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(AppContants.Extras.DEVICE_TYPE)) {
            deviceType = intent.getIntExtra(AppContants.Extras.DEVICE_TYPE, AppContants.DeviceType.DAS);
        }

        if (intent.getExtras().containsKey(AppContants.Extras.DATA_CENTER_CONFIG_METHOD)) {
            configMethod = intent.getIntExtra(AppContants.Extras.DATA_CENTER_CONFIG_METHOD, AppContants.DataCenterConfigMethod.BASIC_CONFIG);
            if (configMethod == AppContants.DataCenterConfigMethod.BASIC_CONFIG) {
                mToolbarTitle.setText("中心基础配置");
            } else {
                mToolbarTitle.setText("中心高级配置");
            }
        }

        if (intent.getExtras().containsKey(LEVEL_INITIAL)) {
            isLevelInit = intent.getBooleanExtra(LEVEL_INITIAL, false);
        }
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            switch (deviceType) {
                case AppContants.DeviceType.DAS:
                    fragment = NetDasDataCenterHomeFragment.newInstance(projectDeviceInfo);
                    break;

                case AppContants.DeviceType.ADME:
                    fragment = NetAdmeDataCenterHomeFragment.newInstance(configMethod, projectDeviceInfo);
                    break;

                case AppContants.DeviceType.M20:
                    fragment = NetM20DataCenterHomeFragment.newInstance(configMethod, isLevelInit, projectDeviceInfo);
                    break;

                case AppContants.DeviceType.E40:
                    fragment = NetE40DataCenterHomeFragment.newInstance(configMethod, projectDeviceInfo);
                    break;
            }
        } else if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
            switch (deviceType) {
                case AppContants.DeviceType.DAS:
                    break;

                case AppContants.DeviceType.ADME:
                    fragment = BleAdmeDataCenterHomeFragment.newInstance(configMethod);
                    break;

                case AppContants.DeviceType.M20:
                    fragment = BleM20DataCenterHomeFragment.newInstance(configMethod, isLevelInit);
                    break;
            }
        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            switch (deviceType) {
                case AppContants.DeviceType.E40:
                    fragment = TcpE40DataCenterHomeFragment.newInstance(configMethod);
                    break;
            }
        }
        return fragment;
    }
}