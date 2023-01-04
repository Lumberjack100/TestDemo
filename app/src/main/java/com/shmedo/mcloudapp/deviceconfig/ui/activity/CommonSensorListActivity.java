package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.shmedo.configlibrary.iot.enums.ChannelNumber;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dms.BleCommonSensorListFragment;

public class CommonSensorListActivity extends BaseConfigFragmentContainerActivity {
    private ChannelNumber channelNumber;

    public static void startActivity(Context context, DeviceInfo deviceInfo, ChannelNumber channelNumber ) {
        Intent intent = new Intent(context, CommonSensorListActivity.class);
        intent.putExtra(AppContants.Extras.DEVICE_INFO, deviceInfo);
        intent.putExtra(AppContants.Extras.SENSOR_CHANNEL_NUMBER, channelNumber);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int connectWay, ChannelNumber channelNumber) {
        Intent intent = new Intent(context, CommonSensorListActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.SENSOR_CHANNEL_NUMBER, channelNumber);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;
        
        if (intent.getExtras().containsKey(AppContants.Extras.SENSOR_CHANNEL_NUMBER)) {
            channelNumber = (ChannelNumber) intent.getSerializableExtra(AppContants.Extras.SENSOR_CHANNEL_NUMBER);
            if (channelNumber == ChannelNumber.NUMBER_ONE) {
                mToolbarTitle.setText("通道1传感器");
            } else if (channelNumber == ChannelNumber.NUMBER_TWO) {
                mToolbarTitle.setText("通道2传感器");
            } else if (channelNumber == ChannelNumber.NUMBER_THREE) {
                mToolbarTitle.setText("通道3传感器");
            } else if (channelNumber == ChannelNumber.NUMBER_FOUR) {
                mToolbarTitle.setText("通道4传感器");
            }
        }
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {

        } else if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
            fragment = BleCommonSensorListFragment.newInstance();
        }
        return fragment;
    }
}