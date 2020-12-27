package com.shmedo.mcloudapp.deviceconfig.ui.activity.adme;

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
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeDataCenterAdvancedConfigFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeDataCenterBasicConfigFragment;

import butterknife.BindView;

public class AdmeDataCenterConfigActivity extends BaseActivity {
    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    private int connectWay = AppContants.CommunicationWay.NET_PLATFORM_CONNECT;

    private int configMethod = AppContants.DataCenterConfigMethod.BASIC_CONFIG;

    private ServerNumber serverNumber;
    private String serverStatus;

    private Fragment fragment;

    public static void startActivity(Context context, int connectWay, int configMethod, ServerNumber serverNumber, String status) {
        Intent intent = new Intent(context, AdmeDataCenterConfigActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.DATA_CENTER_CONFIG_METHOD, configMethod);
        intent.putExtra(AppContants.Extras.DATA_SERVER_NUMBER, serverNumber);
        intent.putExtra(AppContants.Extras.DATA_SERVER_STATUS, status);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_adme_data_center_config;
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

        if (intent.getExtras().containsKey(AppContants.Extras.DATA_CENTER_CONFIG_METHOD)) {
            configMethod = intent.getIntExtra(AppContants.Extras.DATA_CENTER_CONFIG_METHOD, AppContants.DataCenterConfigMethod.BASIC_CONFIG);
        }

        if (intent.getExtras().containsKey(AppContants.Extras.DATA_SERVER_NUMBER)) {
            serverNumber = (ServerNumber) intent.getSerializableExtra(AppContants.Extras.DATA_SERVER_NUMBER);
            if (serverNumber == ServerNumber.NUMBER_ONE) {
                if (configMethod == AppContants.DataCenterConfigMethod.BASIC_CONFIG) {
                    mToolbarTitle.setText("中心1基础配置");
                } else {
                    mToolbarTitle.setText("中心1高级配置");
                }
            } else if (serverNumber == ServerNumber.NUMBER_TWO) {
                if (configMethod == AppContants.DataCenterConfigMethod.BASIC_CONFIG) {
                    mToolbarTitle.setText("中心2基础配置");
                } else {
                    mToolbarTitle.setText("中心2高级配置");
                }
            } else if (serverNumber == ServerNumber.NUMBER_THREE) {
                if (configMethod == AppContants.DataCenterConfigMethod.BASIC_CONFIG) {
                    mToolbarTitle.setText("中心3基础配置");
                } else {
                    mToolbarTitle.setText("中心3高级配置");
                }
            } else if (serverNumber == ServerNumber.NUMBER_FOUR) {
                if (configMethod == AppContants.DataCenterConfigMethod.BASIC_CONFIG) {
                    mToolbarTitle.setText("中心4基础配置");
                } else {
                    mToolbarTitle.setText("中心4高级配置");
                }
            }
        }

        if (intent.getExtras().containsKey(AppContants.Extras.DATA_SERVER_STATUS)) {
            serverStatus = intent.getStringExtra(AppContants.Extras.DATA_SERVER_STATUS);
        }
    }

    private void initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {

        } else {
            if (configMethod == AppContants.DataCenterConfigMethod.BASIC_CONFIG) {
                fragment = BleAdmeDataCenterBasicConfigFragment.newInstance(serverNumber, serverStatus);
            } else {
                fragment = BleAdmeDataCenterAdvancedConfigFragment.newInstance(serverNumber, serverStatus);
            }
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