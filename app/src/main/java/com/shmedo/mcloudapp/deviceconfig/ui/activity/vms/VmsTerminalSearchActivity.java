package com.shmedo.mcloudapp.deviceconfig.ui.activity.vms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms.NetVmsTerminalSearchFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms.TcpVmsTerminalSearchFragment;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/19 <br/>
 * 描述：     Vms网关挂载的终端设备搜索页面
 */
public class VmsTerminalSearchActivity extends BaseActivity {
    private static final String PRO_DEVICE_INFO = "com.shmedo.mcloudapp.PRO_DEVICE_INFO";

    private int connectWay = AppContants.CommunicationWay.NET_PLATFORM_CONNECT;

    private Fragment fragment;

    private DeviceInfo deviceInfo;


    public static void startActivity(Context context, DeviceInfo deviceInfo) {
        Intent intent = new Intent(context, VmsTerminalSearchActivity.class);
        intent.putExtra(PRO_DEVICE_INFO, deviceInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int connectWay) {
        Intent intent = new Intent(context, VmsTerminalSearchActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_vms_terminal_search;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseIntent();
        replaceFragment(initFragment());
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(AppContants.Extras.COMMUNICATION_WAY)) {
            connectWay = intent.getIntExtra(AppContants.Extras.COMMUNICATION_WAY, AppContants.CommunicationWay.NET_PLATFORM_CONNECT);
        }

        if (intent.getExtras().containsKey(PRO_DEVICE_INFO)) {
            deviceInfo = intent.getParcelableExtra(PRO_DEVICE_INFO);
        }
    }

    private Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            fragment = NetVmsTerminalSearchFragment.newInstance(deviceInfo);
        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            fragment = TcpVmsTerminalSearchFragment.newInstance();
        }
        return fragment;
    }

    private void replaceFragment(Fragment fragment) {
        if (fragment == null)
            return;

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container_view, fragment);
        transaction.commitAllowingStateLoss();
    }
}