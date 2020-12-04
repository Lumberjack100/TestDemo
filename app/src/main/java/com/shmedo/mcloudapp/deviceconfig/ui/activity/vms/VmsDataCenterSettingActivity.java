package com.shmedo.mcloudapp.deviceconfig.ui.activity.vms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.shmedo.configlibrary.iot.enums.ServerNumber;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms.TcpVmsDataCenterSettingFragment;

import butterknife.BindView;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20 <br/>
 * 描述：     Vms 网关数据中心参数配置
 */
public class VmsDataCenterSettingActivity extends BaseActivity {
    private static final String DATA_SERVER_NUMBER = "data_server_number";
    private static final String DATA_SERVER_STATUS = "data_server_status";


    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    private int connectWay = AppContants.CommunicationWay.NET_PLATFORM_CONNECT;

    private ServerNumber serverNumber;
    private String serverStatus;


    private Fragment fragment;


    public static void startActivity(Context context, int connectWay, ServerNumber serverNumber, String status) {
        Intent intent = new Intent(context, VmsDataCenterSettingActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(DATA_SERVER_NUMBER, serverNumber);
        intent.putExtra(DATA_SERVER_STATUS, status);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.vms_data_center_setting_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
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

        if (intent.getExtras().containsKey(DATA_SERVER_NUMBER)) {
            serverNumber = (ServerNumber) intent.getSerializableExtra(DATA_SERVER_NUMBER);
            if (serverNumber == ServerNumber.NUMBER_ONE) {
                mToolbarTitle.setText("数据中心1");
            } else if (serverNumber == ServerNumber.NUMBER_TWO) {
                mToolbarTitle.setText("数据中心2");
            } else if (serverNumber == ServerNumber.NUMBER_THREE) {
                mToolbarTitle.setText("数据中心3");
            } else if (serverNumber == ServerNumber.NUMBER_FOUR) {
                mToolbarTitle.setText("数据中心4");
            }
        }
        if (intent.getExtras().containsKey(DATA_SERVER_STATUS)) {
            serverStatus = intent.getStringExtra(DATA_SERVER_STATUS);
        }
    }

    private void initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {

        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            fragment = TcpVmsDataCenterSettingFragment.newInstance(serverNumber, serverStatus);
        }

        replaceFragment(fragment);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commitAllowingStateLoss();
    }
}