package com.shmedo.mcloudapp.deviceconfig.ui.activity.hac;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.hac.BleAdmeHacMeasuringDataProcedureFragment;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/12<br/>
 * 描述：     AC10 数据测量过程展示页面
 */
public class AdmeHacMeasuringDataProcedureActivity extends BaseConfigFragmentContainerActivity {
    protected static final String HOLE_DEPTH = "hole_depth";
    private String holeDepth;

    public static void startActivity(Context context, ActivityResultLauncher<Intent> launcher, int connectWay, String holeDepth) {
        Intent intent = new Intent(context, AdmeHacMeasuringDataProcedureActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(HOLE_DEPTH, holeDepth);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        launcher.launch(intent);
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

        if (intent.getExtras().containsKey(HOLE_DEPTH)) {
            holeDepth = intent.getStringExtra(HOLE_DEPTH);
        }
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {

        } else {
            fragment = BleAdmeHacMeasuringDataProcedureFragment.newInstance(holeDepth);
        }

        return fragment;
    }
}