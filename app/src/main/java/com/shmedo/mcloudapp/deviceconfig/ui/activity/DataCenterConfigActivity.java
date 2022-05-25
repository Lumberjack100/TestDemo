package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;

import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.configlibrary.iot.enums.ServerNumber;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.UniversalUSRBleDataCenterAdvancedConfigFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.BleDasDataCenterServerConfigFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.UniversalNetDataCenterAdvancedConfigFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.tcpcommon.UniversalTcpDataCenterAdvancedConfigFragment;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/12/29<br/>
 * 描述：    数据中心配置页面
 */
public class DataCenterConfigActivity extends BaseConfigFragmentContainerActivity {
    private ProductType productType = ProductType.DAS;

    private ServerNumber serverNumber;
    private String serverStatus;

    public static void startActivity(Context context, ActivityResultLauncher<Intent> launcher, ProductType productType, DeviceInfo deviceInfo, ServerNumber serverNumber, String status) {
        Intent intent = new Intent(context, DataCenterConfigActivity.class);
        intent.putExtra(AppContants.Extras.PRODUCT_TYPE, productType);
        intent.putExtra(PRO_DEVICE_INFO, deviceInfo);
        intent.putExtra(AppContants.Extras.DATA_SERVER_NUMBER, serverNumber);
        intent.putExtra(AppContants.Extras.DATA_SERVER_STATUS, status);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        launcher.launch(intent);
    }

    public static void startActivity(Context context, ActivityResultLauncher<Intent> launcher, ProductType productType, int connectWay, ServerNumber serverNumber, String status) {
        Intent intent = new Intent(context, DataCenterConfigActivity.class);
        intent.putExtra(AppContants.Extras.PRODUCT_TYPE, productType);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.DATA_SERVER_NUMBER, serverNumber);
        intent.putExtra(AppContants.Extras.DATA_SERVER_STATUS, status);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        launcher.launch(intent);
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(AppContants.Extras.PRODUCT_TYPE)) {
            productType = (ProductType) intent.getSerializableExtra(AppContants.Extras.PRODUCT_TYPE);
        }
        if (intent.getExtras().containsKey(AppContants.Extras.DATA_SERVER_NUMBER)) {
            serverNumber = (ServerNumber) intent.getSerializableExtra(AppContants.Extras.DATA_SERVER_NUMBER);
            if (serverNumber == ServerNumber.NUMBER_ONE) {
                mToolbarTitle.setText("数据中心1");
            } else if (serverNumber == ServerNumber.NUMBER_TWO) {
                mToolbarTitle.setText("数据中心2");
            } else if (serverNumber == ServerNumber.NUMBER_THREE) {
                mToolbarTitle.setText("数据中心3");
            } else if (serverNumber == ServerNumber.NUMBER_FOUR) {
                mToolbarTitle.setText("数据中心4");
            }
        }
        if (intent.getExtras().containsKey(AppContants.Extras.DATA_SERVER_STATUS)) {
            serverStatus = intent.getStringExtra(AppContants.Extras.DATA_SERVER_STATUS);
        }
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            switch (productType) {
                case DAS:
                case ADME:
                case M20:
                case E40:
                case VMS:
                    fragment = UniversalNetDataCenterAdvancedConfigFragment.newInstance(productType, serverNumber, serverStatus, deviceInfo);
                    break;
            }
        } else if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
            switch (productType) {
                case DAS:
                    fragment = BleDasDataCenterServerConfigFragment.newInstance(serverNumber);
                    break;

                case ADME:
                case M20:
                    fragment = UniversalUSRBleDataCenterAdvancedConfigFragment.newInstance(productType, serverNumber, serverStatus);
                    break;
            }
        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            switch (productType) {
                case VMS:
                case E40:
                    fragment = UniversalTcpDataCenterAdvancedConfigFragment.newInstance(productType, serverNumber, serverStatus);
                    break;
            }
        }
        return fragment;
    }
}