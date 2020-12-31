package com.shmedo.mcloudapp.deviceconfig.ui.activity.das;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.model.DevcieCurrentState;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.BleDasDeviceCurrentStateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.NetDasDeviceCurrentState;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/28/20 <br/>
 * 描述：    通过下发指令查看设备当前运行状态
 */
public class DasCurrentStateActivity extends BaseConfigFragmentContainerActivity {
    private static final String DEVICE_INFO = "device_info";
    private static final String DEVICE_CURRENT_STATE = "device_current_state";

    private ProjectDeviceInfo projectDeviceInfo;

    private DevcieCurrentState devcieCurrentState;

    public static void startActivity(Context context, int connectWay) {
        Intent intent = new Intent(context, DasCurrentStateActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, ProjectDeviceInfo projectDeviceInfo, DevcieCurrentState devcieCurrentState) {
        Intent intent = new Intent(context, DasCurrentStateActivity.class);
        intent.putExtra(DEVICE_INFO, projectDeviceInfo);
        intent.putExtra(DEVICE_CURRENT_STATE, devcieCurrentState);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("运行状态");
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;
        if (intent.getExtras().containsKey(DEVICE_INFO)) {
            projectDeviceInfo = intent.getParcelableExtra(DEVICE_INFO);
        }

        if (intent.getExtras().containsKey(DEVICE_CURRENT_STATE)) {
            devcieCurrentState = intent.getParcelableExtra(DEVICE_CURRENT_STATE);
        }
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            fragment = NetDasDeviceCurrentState.newInstance(projectDeviceInfo, devcieCurrentState);

        } else {
            fragment = BleDasDeviceCurrentStateFragment.newInstance();
        }

        return fragment;
    }
}
