package com.shmedo.mcloudapp.deviceconfig.ui.activity.adme;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeAdvancedSettingFragment;
/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/12/29<br/>
 * 描述：     ADME 设置页面
 */
public class AdmeAdvancedSettingActivity extends BaseConfigFragmentContainerActivity {

    public static void startActivity(Context context, int connectWay) {
        Intent intent = new Intent(context, AdmeAdvancedSettingActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("设置");
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {

        } else {
            fragment = BleAdmeAdvancedSettingFragment.newInstance();
        }

        return fragment;
    }
}