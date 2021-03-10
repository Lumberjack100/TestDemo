package com.shmedo.mcloudapp.deviceconfig.ui.activity.vms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.shmedo.configlibrary.iot.model.vms.VmsTerminalInfo;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms.TcpVmsTerminalParamSettingFragment;

import butterknife.BindView;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/12/2 <br/>
 * 描述：     Vms 终端参数配置
 */
public class VmsTerminalParamSettingActivity extends BaseActivity {
    private static final String DEVICE_INFO = "device_info";

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    private int connectWay = AppContants.CommunicationWay.NET_PLATFORM_CONNECT;

    private Fragment fragment;

    private VmsTerminalInfo vmsTerminalInfo;


    public static void startActivity(Context context, int connectWay, VmsTerminalInfo vmsTerminalInfo) {
        Intent intent = new Intent(context, VmsTerminalParamSettingActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(DEVICE_INFO, vmsTerminalInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.vms_terminal_param_setting_activity;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("终端配置");
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

        if (intent.getExtras().containsKey(DEVICE_INFO)) {
            vmsTerminalInfo = intent.getParcelableExtra(DEVICE_INFO);
        }
    }

    private void initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {

        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            fragment = TcpVmsTerminalParamSettingFragment.newInstance(vmsTerminalInfo);
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