package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DiscoveredBluetoothDevice;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.NetAdmeHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.BleDasHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.NetDasHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40.NetE40HomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40.TcpE40HomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20.BleM20HomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20.NetM20HomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.rn20.BleRN20HomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms.NetVmsHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms.TcpVmsHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20<br/>
 * 描述：     Das设备配置页面
 */
public class DeviceConfigActivity extends BaseConfigFragmentContainerActivity {
    public static final String EXTRA_DEVICE = "com.shmedo.mcloudapp.EXTRA_DEVICE";

    private int deviceType = AppContants.DeviceType.DAS;

    private DiscoveredBluetoothDevice device;


    public static void startActivity(Context context, DeviceInfo deviceInfo) {
        int deviceType = AppContants.DeviceType.UnKnown;
        String sn = deviceInfo.getProductToken().toUpperCase();
        if (sn.contains("DAS")) {
            deviceType = AppContants.DeviceType.DAS;
        } else if (sn.contains("ADME")) {
            deviceType = AppContants.DeviceType.ADME;
        } else if (sn.contains("M20")) {
            deviceType = AppContants.DeviceType.M20;
        } else if (sn.contains("E40") || sn.contains("E60")) {
            deviceType = AppContants.DeviceType.E40;
        } else if (sn.contains("VMS") || sn.contains("GW300")) {
            deviceType = AppContants.DeviceType.VMS;
        }
        if (deviceType == AppContants.DeviceType.UnKnown) {
            ToastUtils.show("暂不支持此设备类型!");
            return;
        }
        Intent intent = new Intent(context, DeviceConfigActivity.class);
        intent.putExtra(EXTRA_DEVICE, deviceInfo);
        intent.putExtra(AppContants.Extras.DEVICE_TYPE, deviceType);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int connectWay, DiscoveredBluetoothDevice device) {
        int deviceType = AppContants.DeviceType.UnKnown;
        String deviceName = device.getDevice().getName();
        if (deviceName.endsWith("L")) {
            deviceType = AppContants.DeviceType.DAS;
        } else if (deviceName.endsWith("T")) {
            if (deviceName.startsWith("M20"))
                deviceType = AppContants.DeviceType.M20;
            else
                deviceType = AppContants.DeviceType.ADME;
        } else if (deviceName.endsWith("V")) {
            deviceType = AppContants.DeviceType.M20;
        } else if (deviceName.endsWith("Y")) {
            deviceType = AppContants.DeviceType.RN20;
        }
        if (deviceType == AppContants.DeviceType.UnKnown) {
            ToastUtils.show("暂不支持此设备类型");
            return;
        }
        Intent intent = new Intent(context, DeviceConfigActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(EXTRA_DEVICE, device);
        intent.putExtra(AppContants.Extras.DEVICE_TYPE, deviceType);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("设备配置");
        if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            if (deviceType == AppContants.DeviceType.VMS) {
                mIvAction.setVisibility(View.VISIBLE);
                mIvAction.setImageResource(R.drawable.ic_vms_advanced_settings);
            } else {
                mIvAction.setVisibility(View.GONE);
            }
        } else {
            mIvAction.setVisibility(View.VISIBLE);
            if (deviceType == AppContants.DeviceType.VMS) {
                mIvAction.setImageResource(R.drawable.ic_vms_advanced_settings);
            } else {
                mIvAction.setImageResource(R.drawable.ic_query_device_data);
            }
        }
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;

        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            deviceInfo = intent.getParcelableExtra(EXTRA_DEVICE);
        } else if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
            device = intent.getParcelableExtra(EXTRA_DEVICE);
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
                    fragment = NetDasHomeFragment.newInstance(deviceInfo, deviceType);
                    break;

                case AppContants.DeviceType.ADME:
                    fragment = NetAdmeHomeFragment.newInstance(deviceInfo);
                    break;

                case AppContants.DeviceType.M20:
                    fragment = NetM20HomeFragment.newInstance(deviceInfo, deviceType);
                    break;

                case AppContants.DeviceType.E40:
                    fragment = NetE40HomeFragment.newInstance(deviceInfo, deviceType);
                    break;

                case AppContants.DeviceType.VMS:
                    fragment = NetVmsHomeFragment.newInstance(deviceInfo);
                    break;

                default:
                    break;
            }
        } else if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
            switch (deviceType) {
                case AppContants.DeviceType.DAS:
                    fragment = BleDasHomeFragment.newInstance(device);
                    break;

                case AppContants.DeviceType.ADME:
                    fragment = BleAdmeHomeFragment.newInstance(device);
                    break;

                case AppContants.DeviceType.M20:
                    fragment = BleM20HomeFragment.newInstance(device);
                    break;

                case AppContants.DeviceType.RN20:
                    fragment = BleRN20HomeFragment.newInstance(device);
                    break;
            }
        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            switch (deviceType) {
                case AppContants.DeviceType.E40:
                    fragment = TcpE40HomeFragment.newInstance();
                    break;

                case AppContants.DeviceType.VMS:
                    fragment = TcpVmsHomeFragment.newInstance();
                    break;
            }
        }
        return fragment;
    }

    @Override
    protected void onIconActionClick() {
        if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            if (deviceType == AppContants.DeviceType.VMS) {
                AdvancedSettingActivity.startActivity(this, AppContants.CommunicationWay.TCP_CONNECT, AppContants.DeviceType.VMS);
            }
        } else if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            if (deviceType == AppContants.DeviceType.VMS) {
                AdvancedSettingActivity.startActivity(this, deviceInfo, AppContants.DeviceType.VMS);
            } else {
                QueryDeviceDataActivity.startActivity(DeviceConfigActivity.this, deviceInfo.getDeviceToken());
            }
        } else if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
            QueryDeviceDataActivity.startActivity(DeviceConfigActivity.this, MCloudApp.getCurDeviceToken());
        }
    }

    /**
     * 解决Fragment中的onActivityResult()方法无响应问题。
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * 1.使用getSupportFragmentManager().getFragments()获取到当前Activity中添加的Fragment集合
         * 2.遍历Fragment集合，手动调用在当前Activity中的Fragment中的onActivityResult()方法。
         */
        if (getSupportFragmentManager().getFragments() != null && getSupportFragmentManager().getFragments().size() > 0) {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            for (Fragment mFragment : fragments) {
                mFragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
}
