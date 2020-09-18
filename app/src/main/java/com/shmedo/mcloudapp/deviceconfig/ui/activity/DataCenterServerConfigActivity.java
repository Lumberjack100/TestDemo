package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.shmedo.configlibrary.ble.enums.ServerNumber;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.BleDataCenterServerConfigFragment;

import butterknife.BindView;

public class DataCenterServerConfigActivity extends BaseActivity {
    private static final String SERVER_NUMBER = "server_number";

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    private int connectWay = AppContants.CommunicationWay.NET_CONNECT;

    private ServerNumber serverNumber;

    private Fragment fragment;


    public static void startActivity(Context context, int connectWay, ServerNumber serverNumber) {
        Intent intent = new Intent(context, DataCenterActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(SERVER_NUMBER, serverNumber);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_data_center_server_config;
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
            connectWay = intent.getIntExtra(AppContants.Extras.COMMUNICATION_WAY, AppContants.CommunicationWay.NET_CONNECT);
        }

        if (intent.getExtras().containsKey(SERVER_NUMBER)) {
            serverNumber = (ServerNumber) intent.getSerializableExtra(SERVER_NUMBER);
            if (serverNumber == ServerNumber.NUMBER_ONE) {
                mToolbarTitle.setText("数据中心1");
            } else if (serverNumber == ServerNumber.NUMBER_TWO) {
                mToolbarTitle.setText("数据中心2");
            } else if (serverNumber == ServerNumber.NUMBER_THREE) {
                mToolbarTitle.setText("数据中心3");
            }
        }
    }

    private void initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_CONNECT) {

        } else {
            fragment = BleDataCenterServerConfigFragment.newInstance(serverNumber);
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
