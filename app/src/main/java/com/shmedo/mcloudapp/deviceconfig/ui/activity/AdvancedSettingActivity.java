package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeAdvancedSettingFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.NetAdmeAdvancedSettingFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.BleDasAdvancedSettingFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.NetDasAdvancedSettingFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40.NetE40AdvancedSettingFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40.TcpE40AdvancedSettingFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.lr200.BleLR200AdvancedSettingFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20.BleM20AdvancedSettingFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20.NetM20AdvancedSettingFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.rn20.BleRN20AdvancedSettingFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms.NetVmsAdvancedSettingsFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms.TcpVmsAdvancedSettingsFragment;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20<br/>
 * 描述：     设备设置页面
 */
public class AdvancedSettingActivity extends BaseConfigFragmentContainerActivity {
    private ProductType productType = ProductType.UnKnown;


    public static void startActivity(Context context, DeviceInfo deviceInfo, ProductType productType) {
        Intent intent = new Intent(context, AdvancedSettingActivity.class);
        intent.putExtra(AppContants.Extras.DEVICE_INFO, deviceInfo);
        intent.putExtra(AppContants.Extras.PRODUCT_TYPE, productType);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int connectWay, ProductType productType) {
        Intent intent = new Intent(context, AdvancedSettingActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.PRODUCT_TYPE, productType);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("设置");
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(AppContants.Extras.PRODUCT_TYPE)) {
            productType = (ProductType) intent.getSerializableExtra(AppContants.Extras.PRODUCT_TYPE);
        }
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            switch (productType) {
                case DAS:
                    fragment = NetDasAdvancedSettingFragment.newInstance(deviceInfo);
                    break;

                case ADME:
                    fragment = NetAdmeAdvancedSettingFragment.newInstance(deviceInfo);
                    break;

                case M20:
                    fragment = NetM20AdvancedSettingFragment.newInstance(deviceInfo);
                    break;

                case E40:
                    fragment = NetE40AdvancedSettingFragment.newInstance(deviceInfo);
                    break;

                case VMS:
                    fragment = NetVmsAdvancedSettingsFragment.newInstance(deviceInfo);
                    break;
            }
        } else if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
            switch (productType) {
                case DAS:
                    fragment = new BleDasAdvancedSettingFragment();
                    break;

                case ADME:
                    fragment = BleAdmeAdvancedSettingFragment.newInstance();
                    break;

                case M20:
                    fragment = BleM20AdvancedSettingFragment.newInstance();
                    break;

                case RN20:
                    fragment = BleRN20AdvancedSettingFragment.newInstance();
                    break;

                case LR200:
                    fragment = BleLR200AdvancedSettingFragment.newInstance();
            }
        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            switch (productType) {
                case E40:
                    fragment = TcpE40AdvancedSettingFragment.newInstance();
                    break;

                case VMS:
                    fragment = TcpVmsAdvancedSettingsFragment.newInstance();
                    break;
            }
        }

        return fragment;
    }
}
