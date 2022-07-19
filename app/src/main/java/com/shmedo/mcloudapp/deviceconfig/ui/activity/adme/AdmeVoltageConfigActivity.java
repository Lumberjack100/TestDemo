package com.shmedo.mcloudapp.deviceconfig.ui.activity.adme;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeVoltageConfigFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.NetAdmeVoltageConfigFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.hac.BleAdmeHacInclinometerFragment;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/12/13 <br/>
 * 描述：      ADME电压配置页面
 */
public class AdmeVoltageConfigActivity extends BaseConfigFragmentContainerActivity {
    private ProductType productType = ProductType.ADME;

    public static void startActivity(Context context, int connectWay, ProductType productType, DeviceInfo deviceInfo) {
        Intent intent = new Intent(context, AdmeVoltageConfigActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.PRODUCT_TYPE, productType);
        intent.putExtra(PRO_DEVICE_INFO, deviceInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("电压配置");
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
            fragment = NetAdmeVoltageConfigFragment.newInstance(deviceInfo);

        } else {
            fragment = (productType == ProductType.ADME) ? BleAdmeVoltageConfigFragment.newInstance() : BleAdmeHacInclinometerFragment.newInstance();
        }
        return fragment;
    }
}