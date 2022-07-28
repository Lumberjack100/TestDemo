package com.shmedo.mcloudapp.deviceconfig.ui.activity.hac;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.configlibrary.iot.model.hac.HacMotionState;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.hac.BleAdmeHacMeasuringDataProcedureFragment;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/12<br/>
 * 描述：     AC10 数据测量过程展示页面
 */
public class AdmeHacMeasuringDataProcedureActivity extends BaseConfigFragmentContainerActivity {
    protected static final String MOTION_STATE = "motion_state";
    private HacMotionState motionState;

    public static void startActivity(Context context, int connectWay, HacMotionState motionState) {
        Intent intent = new Intent(context, AdmeHacMeasuringDataProcedureActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(MOTION_STATE, motionState);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("数据测量");
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(MOTION_STATE)) {
            motionState =  intent.getParcelableExtra(MOTION_STATE);
        }
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {

        } else {
            fragment = BleAdmeHacMeasuringDataProcedureFragment.newInstance(motionState);
        }

        return fragment;
    }
}