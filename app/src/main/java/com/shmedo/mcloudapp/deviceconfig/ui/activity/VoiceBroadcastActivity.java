package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.iflytek.cloud.SpeechUtility;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.NetVoiceBroadcastFragment;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/3/2 <br/>
 * 描述：     语音播报
 */
public class VoiceBroadcastActivity extends BaseConfigFragmentContainerActivity {

    public static void startActivity(Context context, DeviceInfo deviceInfo) {
        Intent intent = new Intent(context, VoiceBroadcastActivity.class);
        intent.putExtra(PRO_DEVICE_INFO, deviceInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("语音播报");
        SpeechUtility.createUtility(this, "appid=2ad1229c");
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            fragment = NetVoiceBroadcastFragment.newInstance(deviceInfo);

        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {

        }
        return fragment;
    }
}