package com.shmedo.mcloudapp.deviceconfig.ui.activity.vms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.configlibrary.iot.model.vms.VmsTerminalInfo;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms.NetVmsTerminalExternalSensorHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms.TcpVmsTerminalExternalSensorHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.model.ProjectDeviceInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20<br/>
 * 描述：     Vms终端扩展传感器主页面
 */
public class VmsTerminalExternalSensorHomeActivity extends BaseConfigFragmentContainerActivity {
    private static final String TERMINAL_INFO = "terminal_info";

    private VmsTerminalInfo vmsTerminalInfo;

    public static void startActivity(Context context, ProjectDeviceInfo projectDeviceInfo, VmsTerminalInfo vmsTerminalInfo) {
        Intent intent = new Intent(context, VmsTerminalExternalSensorHomeActivity.class);
        intent.putExtra(PRO_DEVICE_INFO, projectDeviceInfo);
        intent.putExtra(TERMINAL_INFO, vmsTerminalInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int connectWay, VmsTerminalInfo vmsTerminalInfo) {
        Intent intent = new Intent(context, VmsTerminalExternalSensorHomeActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(TERMINAL_INFO, vmsTerminalInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("扩展传感器配置");
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(TERMINAL_INFO)) {
            vmsTerminalInfo = intent.getParcelableExtra(TERMINAL_INFO);
        }
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            fragment = NetVmsTerminalExternalSensorHomeFragment.newInstance(projectDeviceInfo, vmsTerminalInfo);

        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            fragment = TcpVmsTerminalExternalSensorHomeFragment.newInstance(vmsTerminalInfo);
        }
        return fragment;
    }
}