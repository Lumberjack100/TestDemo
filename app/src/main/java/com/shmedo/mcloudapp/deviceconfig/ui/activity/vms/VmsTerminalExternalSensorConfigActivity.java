package com.shmedo.mcloudapp.deviceconfig.ui.activity.vms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;

import com.shmedo.configlibrary.iot.model.vms.VmsTerminalSensorInfo;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms.NetVmsTerminalExternalSensorParamFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms.TcpVmsTerminalExternalSensorParamFragment;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/23 <br/>
 * 描述：     Vms终端外接振弦式传感器参数配置
 */
public class VmsTerminalExternalSensorConfigActivity extends BaseConfigFragmentContainerActivity {
    private VmsTerminalSensorInfo sensorInfo;

    public static void startActivity(Context context, ActivityResultLauncher<Intent> launcher, DeviceInfo deviceInfo, VmsTerminalSensorInfo sensorInfo) {
        Intent intent = new Intent(context, VmsTerminalExternalSensorConfigActivity.class);
        intent.putExtra(PRO_DEVICE_INFO, deviceInfo);
        intent.putExtra(AppContants.Extras.SENSOR_PARAM, sensorInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        launcher.launch(intent);
    }

    public static void startActivity(Context context, ActivityResultLauncher<Intent> launcher, int connectWay, VmsTerminalSensorInfo sensorInfo) {
        Intent intent = new Intent(context, VmsTerminalExternalSensorConfigActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.SENSOR_PARAM, sensorInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        launcher.launch(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("传感器配置");
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(AppContants.Extras.SENSOR_PARAM)) {
            sensorInfo = intent.getParcelableExtra(AppContants.Extras.SENSOR_PARAM);
        }
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            fragment = NetVmsTerminalExternalSensorParamFragment.newInstance(deviceInfo, sensorInfo);
        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            fragment = TcpVmsTerminalExternalSensorParamFragment.newInstance(sensorInfo);
        }
        return fragment;
    }
}