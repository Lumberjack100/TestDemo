package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;

import com.shmedo.configlibrary.iot.enums.ServerNumber;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeDataCenterAdvancedConfigFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeDataCenterBasicConfigFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20.BleM20DataCenterAdvancedConfigFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20.BleM20DataCenterBasicConfigFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.UniversalNetDataCenterAdvancedConfigFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.UniversalNetDataCenterBasicConfigFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.tcpcommon.UniversalTcpDataCenterAdvancedConfigFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.tcpcommon.UniversalTcpDataCenterBasicConfigFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/12/29<br/>
 * 描述：    数据中心配置页面
 */
public class DataCenterConfigActivity extends BaseConfigFragmentContainerActivity {
    private int configMethod = AppContants.DataCenterConfigMethod.BASIC_CONFIG;
    private int deviceType = AppContants.DeviceType.DAS;

    private ServerNumber serverNumber;
    private String serverStatus;

    public static void startActivity(Context context, ActivityResultLauncher<Intent> launcher, int deviceType, ProjectDeviceInfo projectDeviceInfo, int configMethod, ServerNumber serverNumber, String status) {
        Intent intent = new Intent(context, DataCenterConfigActivity.class);
        intent.putExtra(AppContants.Extras.DEVICE_TYPE, deviceType);
        intent.putExtra(PRO_DEVICE_INFO, projectDeviceInfo);
        intent.putExtra(AppContants.Extras.DATA_CENTER_CONFIG_METHOD, configMethod);
        intent.putExtra(AppContants.Extras.DATA_SERVER_NUMBER, serverNumber);
        intent.putExtra(AppContants.Extras.DATA_SERVER_STATUS, status);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        launcher.launch(intent);
    }

    public static void startActivity(Context context, ActivityResultLauncher<Intent> launcher, int deviceType, int connectWay, int configMethod, ServerNumber serverNumber, String status) {
        Intent intent = new Intent(context, DataCenterConfigActivity.class);
        intent.putExtra(AppContants.Extras.DEVICE_TYPE, deviceType);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.DATA_CENTER_CONFIG_METHOD, configMethod);
        intent.putExtra(AppContants.Extras.DATA_SERVER_NUMBER, serverNumber);
        intent.putExtra(AppContants.Extras.DATA_SERVER_STATUS, status);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        launcher.launch(intent);
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(AppContants.Extras.DEVICE_TYPE)) {
            deviceType = intent.getIntExtra(AppContants.Extras.DEVICE_TYPE, AppContants.DeviceType.DAS);
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

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            switch (deviceType) {
                case AppContants.DeviceType.DAS:
                    break;

                case AppContants.DeviceType.ADME:
                    break;

                case AppContants.DeviceType.M20:
                case AppContants.DeviceType.E40:
                case AppContants.DeviceType.VMS:
                    if (configMethod == AppContants.DataCenterConfigMethod.BASIC_CONFIG) {
                        fragment = UniversalNetDataCenterBasicConfigFragment.newInstance(serverNumber, serverStatus, projectDeviceInfo);
                    } else {
                        fragment = UniversalNetDataCenterAdvancedConfigFragment.newInstance(serverNumber, serverStatus, projectDeviceInfo);
                    }
                    break;
            }
        } else if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
            switch (deviceType) {
                case AppContants.DeviceType.DAS:
                    break;

                case AppContants.DeviceType.ADME:
                    if (configMethod == AppContants.DataCenterConfigMethod.BASIC_CONFIG) {
                        fragment = BleAdmeDataCenterBasicConfigFragment.newInstance(serverNumber, serverStatus);
                    } else {
                        fragment = BleAdmeDataCenterAdvancedConfigFragment.newInstance(serverNumber, serverStatus);
                    }
                    break;

                case AppContants.DeviceType.M20:
                    if (configMethod == AppContants.DataCenterConfigMethod.BASIC_CONFIG) {
                        fragment = BleM20DataCenterBasicConfigFragment.newInstance(serverNumber, serverStatus);
                    } else {
                        fragment = BleM20DataCenterAdvancedConfigFragment.newInstance(serverNumber, serverStatus);
                    }
                    break;
            }
        }else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            switch (deviceType) {
                case AppContants.DeviceType.E40:
                    if (configMethod == AppContants.DataCenterConfigMethod.BASIC_CONFIG) {
                        fragment = UniversalTcpDataCenterBasicConfigFragment.newInstance(serverNumber, serverStatus);
                    } else {
                        fragment = UniversalTcpDataCenterAdvancedConfigFragment.newInstance(serverNumber, serverStatus);
                    }
                    break;

                case AppContants.DeviceType.VMS:
                    if (configMethod == AppContants.DataCenterConfigMethod.BASIC_CONFIG) {
                    } else {
                        fragment = UniversalTcpDataCenterAdvancedConfigFragment.newInstance(serverNumber, serverStatus);
                    }
                    break;
            }
        }
        return fragment;
    }
}