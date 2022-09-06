package com.shmedo.mcloudapp.deviceconfig.ui.activity.adme;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeMeasuringHoleDepthFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.hac.BleAdmeHacMeasuringHoleDepthFragment;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/6/21 <br/>
 * 描述：     ADME 测量孔深
 */
public class AdmeMeasuringHoleDepthActivity extends BaseConfigFragmentContainerActivity {
    private ProductType productType = ProductType.ADME;

    public static void startActivity(Context context, int connectWay, ProductType productType, DeviceInfo deviceInfo) {
        Intent intent = new Intent(context, AdmeMeasuringHoleDepthActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.PRODUCT_TYPE, productType);
        intent.putExtra(AppContants.Extras.DEVICE_INFO, deviceInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("孔深测量");
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

        } else {
            fragment = (productType == ProductType.ADME) ? BleAdmeMeasuringHoleDepthFragment.newInstance() : BleAdmeHacMeasuringHoleDepthFragment.newInstance();
        }

        return fragment;
    }
}