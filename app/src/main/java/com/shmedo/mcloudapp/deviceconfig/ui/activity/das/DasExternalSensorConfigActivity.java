package com.shmedo.mcloudapp.deviceconfig.ui.activity.das;

import android.content.Context;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;

import com.shmedo.configlibrary.iot.enums.IOTCollectorModel;
import com.shmedo.configlibrary.iot.enums.IOTSensorType;
import com.shmedo.configlibrary.iot.model.das.DasExternalSensorInfo;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.sensor.NetDasExternalDigitalSensorFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.sensor.NetDasExternalVibratingWireSensorFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.util.BlueResultParserUtil;

import java.util.ArrayList;
import java.util.Objects;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/7/16 <br/>
 * 描述：     TODO
 */
public class DasExternalSensorConfigActivity extends BaseConfigFragmentContainerActivity {
    private String collectorModel = "";//采集器类型
    private ArrayList<String> addressList = new ArrayList<>();
    private DasExternalSensorInfo externalSensorInfo;


    public static void startActivity(Context context, ActivityResultLauncher<Intent> launcher, ProjectDeviceInfo projectDeviceInfo, String collectorModel, ArrayList<String> addressList, DasExternalSensorInfo externalSensorInfo) {
        Intent intent = new Intent(context, DasExternalSensorConfigActivity.class);
        intent.putExtra(PRO_DEVICE_INFO, projectDeviceInfo);
        intent.putExtra(AppContants.Extras.COLLECTOR_MODE, collectorModel);
        intent.putStringArrayListExtra(AppContants.Extras.SENSOR_ADDRESS_LIST, addressList);
        intent.putExtra(AppContants.Extras.SENSOR_PARAM, externalSensorInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        launcher.launch(intent);
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(AppContants.Extras.COLLECTOR_MODE)) {
            collectorModel = intent.getStringExtra(AppContants.Extras.COLLECTOR_MODE);
        }
        if (intent.getExtras().containsKey(AppContants.Extras.SENSOR_ADDRESS_LIST)) {
            addressList = intent.getStringArrayListExtra(AppContants.Extras.SENSOR_ADDRESS_LIST);
        }
        if (intent.getExtras().containsKey(AppContants.Extras.SENSOR_PARAM)) {
            externalSensorInfo = (DasExternalSensorInfo) intent.getSerializableExtra(AppContants.Extras.SENSOR_PARAM);
        }
        String sensorName = BlueResultParserUtil.getSensorName(Objects.requireNonNull(IOTSensorType.value(externalSensorInfo.getType())));
        mToolbarTitle.setText(sensorName);
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            if (IOTCollectorModel.value(collectorModel) == IOTCollectorModel.VW08) {//振弦式传感器
                fragment = NetDasExternalVibratingWireSensorFragment.newInstance(addressList, externalSensorInfo);
            } else { //数字式传感器
                fragment = NetDasExternalDigitalSensorFragment.newInstance(addressList, externalSensorInfo);
            }
        } else if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {

        }
        return fragment;
    }
}