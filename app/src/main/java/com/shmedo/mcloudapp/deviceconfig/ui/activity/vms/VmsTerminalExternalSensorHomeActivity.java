package com.shmedo.mcloudapp.deviceconfig.ui.activity.vms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms.TcpVmsTerminalExternalSensorHomeFragment;

import butterknife.BindView;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20<br/>
 * 描述：     Vms终端扩展传感器主页面
 */
public class VmsTerminalExternalSensorHomeActivity extends BaseActivity {
    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    private int connectWay = AppContants.CommunicationWay.NET_PLATFORM_CONNECT;

    private Fragment fragment;

    private String sn;

    public static void startActivity(Context context, int connectWay, String sn) {
        Intent intent = new Intent(context, VmsTerminalExternalSensorHomeActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.CUR_DEVICE_SN, sn);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.vms_terminal_sensor_home_activity;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("扩展传感器配置");
        parseIntent();
        initFragment();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(AppContants.Extras.COMMUNICATION_WAY)) {
            connectWay = intent.getIntExtra(AppContants.Extras.COMMUNICATION_WAY, AppContants.CommunicationWay.NET_PLATFORM_CONNECT);
        }

        if (intent.getExtras().containsKey(AppContants.Extras.CUR_DEVICE_SN)) {
            sn = intent.getStringExtra(AppContants.Extras.CUR_DEVICE_SN);
        }
    }

    private void initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {

        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            fragment = TcpVmsTerminalExternalSensorHomeFragment.newInstance(sn);
        }

        replaceFragment(fragment);
    }

    private void replaceFragment(Fragment fragment) {
        if (fragment == null)
            return;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commitAllowingStateLoss();
    }
}