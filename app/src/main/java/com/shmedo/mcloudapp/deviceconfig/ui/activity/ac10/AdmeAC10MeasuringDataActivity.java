package com.shmedo.mcloudapp.deviceconfig.ui.activity.ac10;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.ac10.BleAdmeAC10MeasuringDataFragment;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/12<br/>
 * 描述：     AC10 数据测量参数配置页面
 */
public class AdmeAC10MeasuringDataActivity extends BaseConfigFragmentContainerActivity {

    public static void startActivity(Context context, int connectWay) {
        Intent intent = new Intent(context, AdmeAC10MeasuringDataActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("数据测量");
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {

        } else {
            fragment = BleAdmeAC10MeasuringDataFragment.newInstance();
        }

        return fragment;
    }
}