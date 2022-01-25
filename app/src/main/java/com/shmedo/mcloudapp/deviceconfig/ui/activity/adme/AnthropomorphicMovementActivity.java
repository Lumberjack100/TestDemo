package com.shmedo.mcloudapp.deviceconfig.ui.activity.adme;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeVoltageConfigFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.NetAdmeVoltageConfigFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

public class AnthropomorphicMovementActivity extends BaseConfigFragmentContainerActivity {

    public static void startActivity(Context context, ProjectDeviceInfo projectDeviceInfo) {
        Intent intent = new Intent(context, AnthropomorphicMovementActivity.class);
        intent.putExtra(PRO_DEVICE_INFO, projectDeviceInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    public static void startActivity(Context context, int connectWay) {
        Intent intent = new Intent(context, AnthropomorphicMovementActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("拟人运动");
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            fragment = NetAdmeVoltageConfigFragment.newInstance(projectDeviceInfo);

        } else {
            fragment = BleAdmeVoltageConfigFragment.newInstance();
        }
        return fragment;
    }
}