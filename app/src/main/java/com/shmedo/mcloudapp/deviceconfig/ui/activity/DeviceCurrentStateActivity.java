package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeCurrentStateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.NetAdmeCurrentStateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.bhy.NetBhyCurrentStateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.BleDasCurrentStateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.NetDasCurrentStateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40.NetE40CurrentStateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40.TcpE40CurrentStateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.lr200.BleLR200CurrentStateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20.BleM20CurrentStateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20.NetM20CurrentStateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.rn20.BleRN20CurrentStateFragment;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/10/21 <br/>
 * 描述：   设备运行状态
 */
public class DeviceCurrentStateActivity extends BaseConfigFragmentContainerActivity {
    private ProductType productType = ProductType.DAS;

    public static void startActivity(Context context, int connectWay, ProductType productType) {
        Intent intent = new Intent(context, DeviceCurrentStateActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.PRODUCT_TYPE, productType);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, DeviceInfo deviceInfo, ProductType productType) {
        Intent intent = new Intent(context, DeviceCurrentStateActivity.class);
        intent.putExtra(PRO_DEVICE_INFO, deviceInfo);
        intent.putExtra(AppContants.Extras.PRODUCT_TYPE, productType);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("运行状态");
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
                    fragment = NetDasCurrentStateFragment.newInstance(deviceInfo);
                    break;

                case ADME:
                    fragment = NetAdmeCurrentStateFragment.newInstance(deviceInfo);
                    break;

                case M20:
                    fragment = NetM20CurrentStateFragment.newInstance(deviceInfo);
                    break;

                case E40:
                    fragment = NetE40CurrentStateFragment.newInstance(deviceInfo);
                    break;

                case BHY:
                    fragment = NetBhyCurrentStateFragment.newInstance(deviceInfo);
                    break;
            }
        } else if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
            switch (productType) {
                case DAS:
                    fragment = BleDasCurrentStateFragment.newInstance();
                    break;

                case ADME:
                    fragment = BleAdmeCurrentStateFragment.newInstance();
                    break;

                case M20:
                    fragment = BleM20CurrentStateFragment.newInstance();
                    break;

                case RN20:
                    fragment = BleRN20CurrentStateFragment.newInstance();
                    break;

                case LR200:
                    fragment = BleLR200CurrentStateFragment.newInstance();
                    break;
            }
        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            switch (productType) {
                case E40:
                    fragment = TcpE40CurrentStateFragment.newInstance();
                    break;
            }
        }
        return fragment;
    }
}