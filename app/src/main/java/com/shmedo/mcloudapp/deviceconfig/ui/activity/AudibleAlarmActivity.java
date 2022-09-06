package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.NetAudibleAlarmFragment;
/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/3/23 <br/>
 * 描述：     声光报警器
 */
public class AudibleAlarmActivity extends BaseConfigFragmentContainerActivity {

    public static void startActivity(Context context, DeviceInfo deviceInfo) {
        Intent intent = new Intent(context, AudibleAlarmActivity.class);
        intent.putExtra(AppContants.Extras.DEVICE_INFO, deviceInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("声光报警器");
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            fragment = NetAudibleAlarmFragment.newInstance(deviceInfo);

        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {

        }
        return fragment;
    }
}