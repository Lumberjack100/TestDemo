package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.model.DevcieCurrentState;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeCurrentStateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.BleDasDeviceCurrentStateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.NetDasDeviceCurrentState;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/10/21 <br/>
 * 描述：   设备运行状态
 */
public class DeviceCurrentStateActivity extends BaseConfigFragmentContainerActivity {
    private static final String PRO_DEVICE_INFO = "com.shmedo.mcloudapp.PRO_DEVICE_INFO";
    private static final String DEVICE_CURRENT_STATE = "device_current_state";
    private int deviceType = AppContants.DeviceType.DAS;

    private ProjectDeviceInfo projectDeviceInfo;
    private DevcieCurrentState devcieCurrentState;

    public static void startActivity(Context context, int connectWay, int deviceType) {
        Intent intent = new Intent(context, DeviceCurrentStateActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.DEVICE_TYPE, deviceType);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, ProjectDeviceInfo projectDeviceInfo, DevcieCurrentState devcieCurrentState, int deviceType) {
        Intent intent = new Intent(context, DeviceCurrentStateActivity.class);
        intent.putExtra(PRO_DEVICE_INFO, projectDeviceInfo);
        intent.putExtra(DEVICE_CURRENT_STATE, devcieCurrentState);
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

        if (intent.getExtras().containsKey(PRO_DEVICE_INFO)) {
            projectDeviceInfo = intent.getParcelableExtra(PRO_DEVICE_INFO);
        }

        if (intent.getExtras().containsKey(DEVICE_CURRENT_STATE)) {
            devcieCurrentState = intent.getParcelableExtra(DEVICE_CURRENT_STATE);
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
                    fragment = NetDasDeviceCurrentState.newInstance(projectDeviceInfo, devcieCurrentState);
                    break;

                case AppContants.DeviceType.ADME:
                    break;
            }
        }else if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
            switch (deviceType) {
                case AppContants.DeviceType.DAS:
                    fragment = BleDasDeviceCurrentStateFragment.newInstance();
                    break;

                case AppContants.DeviceType.ADME:
                    fragment = BleAdmeCurrentStateFragment.newInstance();
                    break;
            }
        }

        return fragment;
    }

}