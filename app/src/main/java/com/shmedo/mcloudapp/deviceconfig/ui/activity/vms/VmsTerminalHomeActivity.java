package com.shmedo.mcloudapp.deviceconfig.ui.activity.vms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.shmedo.configlibrary.iot.model.vms.VmsTerminalInfo;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.QueryDeviceDataActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms.NetVmsTerminalHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms.TcpVmsTerminalHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/19 <br/>
 * 描述：     Vms 网关挂载的终端设备主页面
 */
public class VmsTerminalHomeActivity extends BaseConfigFragmentContainerActivity {
    private static final String TERMINAL_INFO = "terminal_info";

    private VmsTerminalInfo vmsTerminalInfo;


    public static void startActivity(Context context, DeviceInfo deviceInfo, VmsTerminalInfo vmsTerminalInfo) {
        Intent intent = new Intent(context, VmsTerminalHomeActivity.class);
        intent.putExtra(PRO_DEVICE_INFO, deviceInfo);
        intent.putExtra(TERMINAL_INFO, vmsTerminalInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int connectWay, VmsTerminalInfo vmsTerminalInfo) {
        Intent intent = new Intent(context, VmsTerminalHomeActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(TERMINAL_INFO, vmsTerminalInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("设备配置");
        if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            mIvAction.setVisibility(View.GONE);
        } else {
            mIvAction.setVisibility(View.GONE);
            mIvAction.setImageResource(R.drawable.ic_query_device_data);
        }
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
            fragment = NetVmsTerminalHomeFragment.newInstance(deviceInfo, vmsTerminalInfo);

        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            fragment = TcpVmsTerminalHomeFragment.newInstance(vmsTerminalInfo);
        }
        return fragment;
    }

    @Override
    protected void onIconActionClick() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            QueryDeviceDataActivity.startActivity(VmsTerminalHomeActivity.this, vmsTerminalInfo.getSn());
        }
    }
}