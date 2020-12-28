package com.shmedo.mcloudapp.deviceconfig.ui.activity.adme;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.shmedo.configlibrary.iot.enums.ServerNumber;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeDataCenterAdvancedConfigFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeDataCenterBasicConfigFragment;

public class AdmeDataCenterConfigActivity extends BaseConfigFragmentContainerActivity {
    private int configMethod = AppContants.DataCenterConfigMethod.BASIC_CONFIG;

    private ServerNumber serverNumber;
    private String serverStatus;


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
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;

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

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {

        } else {
            if (configMethod == AppContants.DataCenterConfigMethod.BASIC_CONFIG) {
                fragment = BleAdmeDataCenterBasicConfigFragment.newInstance(serverNumber, serverStatus);
            } else {
                fragment = BleAdmeDataCenterAdvancedConfigFragment.newInstance(serverNumber, serverStatus);
            }
        }

        return fragment;
    }
}