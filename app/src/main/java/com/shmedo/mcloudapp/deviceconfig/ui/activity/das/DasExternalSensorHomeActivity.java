package com.shmedo.mcloudapp.deviceconfig.ui.activity.das;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.shmedo.configlibrary.ble.enums.CollectorModel;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.sensor.NetDasExternalSensorHomeFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20<br/>
 * 描述：     Das 扩展传感器配置主页面
 */
public class DasExternalSensorHomeActivity extends BaseConfigFragmentContainerActivity {
    private String collectorModel = "";//采集器类型

    public static void startActivity(Context context, ProjectDeviceInfo projectDeviceInfo) {
        Intent intent = new Intent(context, DasExternalSensorHomeActivity.class);
        intent.putExtra(PRO_DEVICE_INFO, projectDeviceInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int connectWay, String collectorModel) {
        Intent intent = new Intent(context, DasExternalSensorHomeActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.COLLECTOR_MODE, collectorModel);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(AppContants.Extras.COLLECTOR_MODE)) {
            collectorModel = intent.getStringExtra(AppContants.Extras.COLLECTOR_MODE);
            if (CollectorModel.value(collectorModel) == CollectorModel.VW08) {//振弦式传感器
                mToolbarTitle.setText("振弦式传感器");
            } else {
                mToolbarTitle.setText("数字式传感器");
            }
        }
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            fragment = NetDasExternalSensorHomeFragment.newInstance(projectDeviceInfo);

        } else if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {

        }
        return fragment;
    }
}
