package com.shmedo.mcloudapp.deviceconfig.ui.activity.das;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;

import com.shmedo.configlibrary.iot.model.das.DasExternalSensorInfo;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.sensor.NetDasExternalDigitalSensorFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

import java.util.ArrayList;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/21 <br/>
 * 描述：     TODO
 */
public class DasExternalSensorConfigActivity extends BaseConfigFragmentContainerActivity {
    private ArrayList<String> addressList = new ArrayList<>();
    private DasExternalSensorInfo externalSensorInfo;


    public static void startActivity(Context context, ActivityResultLauncher<Intent> launcher, ProjectDeviceInfo projectDeviceInfo, ArrayList<String> addressList, DasExternalSensorInfo externalSensorInfo) {
        Intent intent = new Intent(context, DasExternalSensorConfigActivity.class);
        intent.putExtra(PRO_DEVICE_INFO, projectDeviceInfo);
        intent.putStringArrayListExtra(AppContants.Extras.SENSOR_ADDRESS_LIST, addressList);
        intent.putExtra(AppContants.Extras.SENSOR_PARAM, externalSensorInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        launcher.launch(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("板卡解算");
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(AppContants.Extras.SENSOR_ADDRESS_LIST)) {
            addressList = intent.getStringArrayListExtra(AppContants.Extras.SENSOR_ADDRESS_LIST);
        }
        if (intent.getExtras().containsKey(AppContants.Extras.SENSOR_PARAM)) {
            externalSensorInfo = (DasExternalSensorInfo) intent.getSerializableExtra(AppContants.Extras.SENSOR_PARAM);
        }
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            fragment = NetDasExternalDigitalSensorFragment.newInstance(projectDeviceInfo, addressList, externalSensorInfo);

        } else if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {

        }
        return fragment;
    }
}
