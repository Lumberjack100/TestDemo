package com.shmedo.mcloudapp.deviceconfig.ui.activity.e40;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40.NetE40EthernetFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40.TcpE40EthernetFragment;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/1/21 <br/>
 * 描述：     E40 有线网络接口参数配置页面
 */
public class E40EthernetActivity extends BaseConfigFragmentContainerActivity {

    public static void startActivity(Context context, DeviceInfo deviceInfo) {
        Intent intent = new Intent(context, E40EthernetActivity.class);
        intent.putExtra(AppContants.Extras.DEVICE_INFO, deviceInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int connectWay) {
        Intent intent = new Intent(context, E40EthernetActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("有线网络设置");
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            fragment = NetE40EthernetFragment.newInstance(deviceInfo);

        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            fragment = TcpE40EthernetFragment.newInstance();
        }

        return fragment;
    }
}
