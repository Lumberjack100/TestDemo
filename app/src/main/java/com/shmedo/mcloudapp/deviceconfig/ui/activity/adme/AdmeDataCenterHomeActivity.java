package com.shmedo.mcloudapp.deviceconfig.ui.activity.adme;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeDataCenterHomeFragment;

/**
 * ADME 设备数据中心配置主页面
 */
public class AdmeDataCenterHomeActivity extends BaseConfigFragmentContainerActivity {
    private int configMethod = AppContants.DataCenterConfigMethod.BASIC_CONFIG;

    public static void startActivity(Context context, int connectWay, int configMethod) {
        Intent intent = new Intent(context, AdmeDataCenterHomeActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.DATA_CENTER_CONFIG_METHOD, configMethod);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(AppContants.Extras.DATA_CENTER_CONFIG_METHOD)) {
            configMethod = intent.getIntExtra(AppContants.Extras.DATA_CENTER_CONFIG_METHOD, AppContants.DataCenterConfigMethod.BASIC_CONFIG);
            if (configMethod == AppContants.DataCenterConfigMethod.BASIC_CONFIG) {
                mToolbarTitle.setText("中心基础配置");
            } else {
                mToolbarTitle.setText("中心高级配置");
            }
        }
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {

        } else {
            fragment =  BleAdmeDataCenterHomeFragment.newInstance(configMethod);
        }

        return fragment;
    }
}