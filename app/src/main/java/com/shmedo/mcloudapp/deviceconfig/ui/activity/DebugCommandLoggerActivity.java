package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.bluetooth_debug_box.InclinometerDebugBoxLoggerFragment;

public class DebugCommandLoggerActivity extends BaseActivity {

    private Fragment fragment;

    private Intent intent;

    private int connectWay = AppContants.CommunicationWay.NET_PLATFORM_CONNECT;

    private int deviceType = AppContants.DeviceType.DAS;

    public static void startActivity(Context context, int connectWay) {
        startActivity(context, connectWay, -1);
    }

    public static void startActivity(Context context, int connectWay, int deviceType) {
        Intent intent = new Intent(context, DebugCommandLoggerActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.DEVICE_TYPE, deviceType);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_debug_command_log_print;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        getSupportActionBar().setTitle("指令调试");
        parseIntent();
        replaceFragment(initFragment());
    }

    private void parseIntent() {
        intent = getIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(AppContants.Extras.COMMUNICATION_WAY)) {
            connectWay = intent.getIntExtra(AppContants.Extras.COMMUNICATION_WAY, AppContants.CommunicationWay.NET_PLATFORM_CONNECT);
        }

        if (intent.getExtras().containsKey(AppContants.Extras.DEVICE_TYPE)) {
            deviceType = intent.getIntExtra(AppContants.Extras.DEVICE_TYPE, AppContants.DeviceType.DAS);
        }
    }

    private Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
//            switch (deviceType) {
//                case AppContants.DeviceType.DAS:
//                    fragment = new BleDasCustomCommandLogPrintFragment();
//                    break;
//
//                case AppContants.DeviceType.ADME:
//                case AppContants.DeviceType.RN20:
//                    fragment = USRBleIotCustomCommandLogPrintFragment.newInstance();
//                    break;
//
//                case AppContants.DeviceType.M20:
//                    fragment = BleM20CustomCommandLogPrintFragment.newInstance();
//                    break;
//            }
        } else if (connectWay == AppContants.CommunicationWay.USB_SERIAL) {
            switch (deviceType) {
                case AppContants.DeviceType.INCLINOMETER_DEBUG_BOX:
                    fragment = new InclinometerDebugBoxLoggerFragment();
                    break;
            }
        }
        return fragment;
    }

    private void replaceFragment(Fragment fragment) {
        if (fragment == null)
            return;

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container_view, fragment);
        transaction.commit();
    }
}