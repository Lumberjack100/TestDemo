package com.shmedo.mcloudapp.deviceconfig.ui.activity.e40;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40.NetE40GpsWorkParamFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40.TcpE40GpsWorkParamFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  6/18/21 <br/>
 * 描述：     E40 GPS 工作参数配置页面
 */
public class E40GpsWorkParamActivity extends BaseConfigFragmentContainerActivity {

    public static void startActivity(Context context, ProjectDeviceInfo projectDeviceInfo) {
        Intent intent = new Intent(context, E40GpsWorkParamActivity.class);
        intent.putExtra(PRO_DEVICE_INFO, projectDeviceInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int connectWay) {
        Intent intent = new Intent(context, E40GpsWorkParamActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("GPS 工作参数设置");
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            fragment = NetE40GpsWorkParamFragment.newInstance(projectDeviceInfo);

        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            fragment = TcpE40GpsWorkParamFragment.newInstance();
        }

        return fragment;
    }
}