package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeDataCenterHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20.BleM20DataCenterHomeFragment;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/12/29<br/>
 * 描述：    数据中心主页面
 */
public class DataCenterHomeActivity extends BaseConfigFragmentContainerActivity {
    private int configMethod = AppContants.DataCenterConfigMethod.BASIC_CONFIG;
    private int deviceType = AppContants.DeviceType.DAS;

    public static void startActivity(Context context, int deviceType, int connectWay, int configMethod) {
        Intent intent = new Intent(context, DataCenterHomeActivity.class);
        intent.putExtra(AppContants.Extras.DEVICE_TYPE, deviceType);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.DATA_CENTER_CONFIG_METHOD, configMethod);
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
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            switch (deviceType) {
                case AppContants.DeviceType.DAS:
                    break;

                case AppContants.DeviceType.ADME:
                    break;

                case AppContants.DeviceType.M20:
                    break;
            }
        }else if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
            switch (deviceType) {
                case AppContants.DeviceType.DAS:
                    break;

                case AppContants.DeviceType.ADME:
                    fragment =  BleAdmeDataCenterHomeFragment.newInstance(configMethod);
                    break;

                case AppContants.DeviceType.M20:
                    fragment =  BleM20DataCenterHomeFragment.newInstance(configMethod);
                    break;
            }
        }
        return fragment;
    }
}