package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.BleScannerListFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.USBDeviceListFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.WiFiDeviceListFragment;

public class DeviceListActivity extends BaseConfigFragmentContainerActivity {

    public static void startActivity(Context context, int connectWay) {
        Intent intent = new Intent(context, DeviceListActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
            mToolbarTitle.setText("蓝牙设备");
        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            mToolbarTitle.setText("WIFI 设备");
        } else if (connectWay == AppContants.CommunicationWay.USB_SERIAL) {
            mToolbarTitle.setText("USB 设备");
        }
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
            fragment = BleScannerListFragment.newInstance();
        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            fragment = WiFiDeviceListFragment.newInstance();
        } else if (connectWay == AppContants.CommunicationWay.USB_SERIAL) {
            fragment = USBDeviceListFragment.newInstance();
        }
        return fragment;
    }
}