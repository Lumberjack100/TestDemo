package com.shmedo.mcloudapp.deviceconfig.ui.activity.adme;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeLowEnergyModeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.NetAdmeLowEnergyModeFragment;
import com.shmedo.mcloudapp.deviceconfig.model.ProjectDeviceInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/10/14 <br/>
 * 描述：      ADME 继电器低功耗使能页面
 */
public class AdmeLowEnergyModeActivity extends BaseConfigFragmentContainerActivity {

    public static void startActivity(Context context, ProjectDeviceInfo projectDeviceInfo) {
        Intent intent = new Intent(context, AdmeLowEnergyModeActivity.class);
        intent.putExtra(PRO_DEVICE_INFO, projectDeviceInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    public static void startActivity(Context context, int connectWay) {
        Intent intent = new Intent(context, AdmeLowEnergyModeActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("低功耗使能");
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            fragment = NetAdmeLowEnergyModeFragment.newInstance(projectDeviceInfo);

        } else {
            fragment = BleAdmeLowEnergyModeFragment.newInstance();
        }

        return fragment;
    }
}