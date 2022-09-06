package com.shmedo.mcloudapp.deviceconfig.ui.activity.e40;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40.NetE40SocketDataDebugFragment;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间: 7/4/21 <br/>
 * 描述：     E50 设备数据通过 Socket 实时输出调试(型批测试需要)
 */
public class E40SocketDataDebugActivity extends BaseConfigFragmentContainerActivity {

    public static void startActivity(Context context, DeviceInfo deviceInfo) {
        Intent intent = new Intent(context, E40SocketDataDebugActivity.class);
        intent.putExtra(AppContants.Extras.DEVICE_INFO, deviceInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("数据调试");
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            fragment = NetE40SocketDataDebugFragment.newInstance(deviceInfo);

        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
        }

        return fragment;
    }
}