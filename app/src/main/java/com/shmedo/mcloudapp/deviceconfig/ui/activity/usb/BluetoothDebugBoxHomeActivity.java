package com.shmedo.mcloudapp.deviceconfig.ui.activity.usb;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.BluetoothDebugBoxHomeFragment;

public class BluetoothDebugBoxHomeActivity extends BaseConfigFragmentContainerActivity {
    private int deviceId, portNum, baudRate;

    public static void startActivity(Context context, int deviceId, int port, int baudRate) {
        Intent intent = new Intent(context, BluetoothDebugBoxHomeActivity.class);
        intent.putExtra(AppContants.Extras.DEVICE_ID, deviceId);
        intent.putExtra(AppContants.Extras.USB_PORT_NUM, port);
        intent.putExtra(AppContants.Extras.USB_BAUD_RATE, baudRate);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("设备配置");
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;

        deviceId = intent.getIntExtra(AppContants.Extras.DEVICE_ID, -1);
        portNum = intent.getIntExtra(AppContants.Extras.USB_PORT_NUM, 0);
        baudRate = intent.getIntExtra(AppContants.Extras.USB_BAUD_RATE, 115200);
    }

    @Override
    protected Fragment initFragment() {
        fragment = BluetoothDebugBoxHomeFragment.newInstance(deviceId, portNum, baudRate);
        return fragment;
    }
}