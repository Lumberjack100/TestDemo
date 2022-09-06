package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeDataCenterHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.NetAdmeDataCenterHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.BleDasDataCenterHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.NetDasDataCenterHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40.NetE40DataCenterHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40.TcpE40DataCenterHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20.BleM20DataCenterHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20.NetM20DataCenterHomeFragment;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/12/29<br/>
 * 描述：    数据中心主页面
 */
public class DataCenterHomeActivity extends BaseConfigFragmentContainerActivity {
    private static final String LEVEL_INITIAL = "com.shmedo.mcloudapp.LEVEL_INITIAL";

    private ProductType productType = ProductType.DAS;
    private boolean isLevelInit = false;


    public static void startActivity(Context context, ProductType productType, DeviceInfo deviceInfo) {
        Intent intent = new Intent(context, DataCenterHomeActivity.class);
        intent.putExtra(AppContants.Extras.PRODUCT_TYPE, productType);
        intent.putExtra(AppContants.Extras.DEVICE_INFO, deviceInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, ProductType productType, DeviceInfo deviceInfo, boolean isLevelInit) {
        Intent intent = new Intent(context, DataCenterHomeActivity.class);
        intent.putExtra(AppContants.Extras.PRODUCT_TYPE, productType);
        intent.putExtra(AppContants.Extras.DEVICE_INFO, deviceInfo);
        intent.putExtra(LEVEL_INITIAL, isLevelInit);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, ProductType productType, int connectWay) {
        Intent intent = new Intent(context, DataCenterHomeActivity.class);
        intent.putExtra(AppContants.Extras.PRODUCT_TYPE, productType);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, ProductType productType, int connectWay, boolean isLevelInit) {
        Intent intent = new Intent(context, DataCenterHomeActivity.class);
        intent.putExtra(AppContants.Extras.PRODUCT_TYPE, productType);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(LEVEL_INITIAL, isLevelInit);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        mToolbarTitle.setText("数据中心");

        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(AppContants.Extras.PRODUCT_TYPE)) {
            productType = (ProductType) intent.getSerializableExtra(AppContants.Extras.PRODUCT_TYPE);
        }
        if (intent.getExtras().containsKey(LEVEL_INITIAL)) {
            isLevelInit = intent.getBooleanExtra(LEVEL_INITIAL, false);
        }
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            switch (productType) {
                case DAS:
                    fragment = NetDasDataCenterHomeFragment.newInstance(deviceInfo);
                    break;

                case ADME:
                    fragment = NetAdmeDataCenterHomeFragment.newInstance(deviceInfo);
                    break;

                case M20:
                    fragment = NetM20DataCenterHomeFragment.newInstance(isLevelInit, deviceInfo);
                    break;

                case E40:
                    fragment = NetE40DataCenterHomeFragment.newInstance(deviceInfo);
                    break;
            }
        } else if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
            switch (productType) {
                case DAS:
                    fragment = new BleDasDataCenterHomeFragment();
                    break;

                case ADME:
                    fragment = BleAdmeDataCenterHomeFragment.newInstance();
                    break;

                case M20:
                case LR200:
                    fragment = BleM20DataCenterHomeFragment.newInstance(isLevelInit);
                    break;
            }
        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            switch (productType) {
                case E40:
                    fragment = TcpE40DataCenterHomeFragment.newInstance();
                    break;
            }
        }
        return fragment;
    }
}