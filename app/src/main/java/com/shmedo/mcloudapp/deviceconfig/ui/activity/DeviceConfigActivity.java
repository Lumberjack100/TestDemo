package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.model.DiscoveredBluetoothDevice;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.NetAdmeHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.bhy.NetBhyHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.BleDasHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.NetDasHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40.NetE40HomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40.TcpE40HomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.hac.BleAdmeHacHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.lr200.BleLR200HomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20.BleM20HomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20.NetM20HomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.NetDeviceHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.rn20.BleRN20HomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms.NetVmsHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms.TcpVmsHomeFragment;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20<br/>
 * 描述：     Das设备配置页面
 */
public class DeviceConfigActivity extends BaseConfigFragmentContainerActivity {
    private static final String BLE_DEVICE = "com.shmedo.mcloudapp.BLE_DEVICE";

    private ProductType productType = ProductType.UnKnown;

    private DiscoveredBluetoothDevice device;

    /**
     * 4G 通讯方式
     *
     * @param context
     * @param deviceInfo
     */
    public static void startActivity(Context context, DeviceInfo deviceInfo) {
        ProductType type = ProductType.valueByPrefix(deviceInfo.getProductToken().toUpperCase());
        if (type == ProductType.UnKnown) {
            ToastUtils.show("暂不支持此设备类型!");
            return;
        }
        Intent intent = new Intent(context, DeviceConfigActivity.class);
        intent.putExtra(PRO_DEVICE_INFO, deviceInfo);
        intent.putExtra(AppContants.Extras.PRODUCT_TYPE, type);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    /**
     * Ble 通讯方式
     *
     * @param context
     * @param device
     */
    public static void startActivity(Context context, DiscoveredBluetoothDevice device, String productToken) {
        ProductType type = TextUtils.isEmpty(productToken) ? ProductType.valueBySuffix(device.getDevice().getName()) : ProductType.valueByPrefix(productToken);
        if (type == ProductType.UnKnown) {
            ToastUtils.show("暂不支持此设备类型!");
            return;
        }
        Intent intent = new Intent(context, DeviceConfigActivity.class);
        intent.putExtra(BLE_DEVICE, device);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, AppContants.CommunicationWay.BLE_CONNECT);
        intent.putExtra(AppContants.Extras.PRODUCT_TYPE, type);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    /**
     * Wi-Fi 通讯方式
     *
     * @param context
     * @param productType
     */
    public static void startActivity(Context context, ProductType productType) {
        Intent intent = new Intent(context, DeviceConfigActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, AppContants.CommunicationWay.TCP_CONNECT);
        intent.putExtra(AppContants.Extras.PRODUCT_TYPE, productType);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;

        if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
            device = intent.getParcelableExtra(BLE_DEVICE);
        }
        if (intent.getExtras().containsKey(AppContants.Extras.PRODUCT_TYPE)) {
            productType = (ProductType) intent.getSerializableExtra(AppContants.Extras.PRODUCT_TYPE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("设备配置");

        if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            if (productType == ProductType.VMS) {
                mIvAction.setVisibility(View.VISIBLE);
                mIvAction.setImageResource(R.drawable.ic_vms_advanced_settings);
            } else {
                mIvAction.setVisibility(View.GONE);
            }
        } else {
            mIvAction.setVisibility(View.VISIBLE);
            if (productType == ProductType.VMS) {
                mIvAction.setImageResource(R.drawable.ic_vms_advanced_settings);
            } else {
                mIvAction.setImageResource(R.drawable.ic_query_device_data);
            }
        }
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            switch (productType) {
                case DAS:
                    fragment = NetDasHomeFragment.newInstance(deviceInfo);
                    break;

                case ADME:
                    fragment = NetAdmeHomeFragment.newInstance(deviceInfo);
                    break;

                case M20:
                    fragment = NetM20HomeFragment.newInstance(deviceInfo);
                    break;

                case E40:
                    fragment = NetE40HomeFragment.newInstance(deviceInfo);
                    break;

                case VMS:
                    fragment = NetVmsHomeFragment.newInstance(deviceInfo);
                    break;

                case BHY:
                    fragment = NetBhyHomeFragment.newInstance(deviceInfo);
                    break;

                default:
                    fragment = NetDeviceHomeFragment.newInstance(deviceInfo);
                    break;
            }
        } else if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
            switch (productType) {
                case DAS:
                case BHY:
                    fragment = BleDasHomeFragment.newInstance(device);
                    break;

                case ADME:
                    fragment = BleAdmeHomeFragment.newInstance(device);
                    break;

                case HAC:
                    fragment = BleAdmeHacHomeFragment.newInstance(device);
                    break;

                case M20:
                    fragment = BleM20HomeFragment.newInstance(device);
                    break;

                case RN20:
                    fragment = BleRN20HomeFragment.newInstance(device);
                    break;

                case LR200:
                    fragment = BleLR200HomeFragment.newInstance(device);
                    break;
            }
        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            switch (productType) {
                case E40:
                    fragment = TcpE40HomeFragment.newInstance();
                    break;

                case VMS:
                    fragment = TcpVmsHomeFragment.newInstance();
                    break;
            }
        }
        return fragment;
    }

    @Override
    protected void onIconActionClick() {
        if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            if (productType == ProductType.VMS) {
                AdvancedSettingActivity.startActivity(this, AppContants.CommunicationWay.TCP_CONNECT, ProductType.VMS);
            }
        } else if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            if (productType == ProductType.VMS) {
                AdvancedSettingActivity.startActivity(this, deviceInfo, ProductType.VMS);
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
        if (getSupportFragmentManager().getFragments().size() > 0) {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            for (Fragment mFragment : fragments) {
                mFragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
}
