package com.shmedo.mcloudapp.deviceconfig.ui.activity.vms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.configlibrary.iot.enums.VmsAisleNumber;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms.NetVmsAisleSettingFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms.TcpVmsAisleSettingFragment;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20 <br/>
 * 描述：     Vms 网关通道控制参数配置
 */
public class VmsAisleSettingActivity extends BaseConfigFragmentContainerActivity {
    private static final String VMS_AISLE_NUMBER = "vms_aisle_number";

    private VmsAisleNumber vmsAisleNumber;

    public static void startActivity(Context context, DeviceInfo deviceInfo, VmsAisleNumber vmsAisleNumber) {
        Intent intent = new Intent(context, VmsAisleSettingActivity.class);
        intent.putExtra(PRO_DEVICE_INFO, deviceInfo);
        intent.putExtra(VMS_AISLE_NUMBER, vmsAisleNumber);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int connectWay, VmsAisleNumber vmsAisleNumber) {
        Intent intent = new Intent(context, VmsAisleSettingActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(VMS_AISLE_NUMBER, vmsAisleNumber);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (vmsAisleNumber == VmsAisleNumber.NUMBER_ONE) {
            mToolbarTitle.setText("注册通道");
        } else if (vmsAisleNumber == VmsAisleNumber.NUMBER_TWO) {
            mToolbarTitle.setText("数据通道1");
        } else if (vmsAisleNumber == VmsAisleNumber.NUMBER_THREE) {
            mToolbarTitle.setText("数据通道2");
        }
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(VMS_AISLE_NUMBER)) {
            vmsAisleNumber = (VmsAisleNumber) intent.getSerializableExtra(VMS_AISLE_NUMBER);
        }
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            fragment = NetVmsAisleSettingFragment.newInstance(deviceInfo, vmsAisleNumber);
        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            fragment = TcpVmsAisleSettingFragment.newInstance(vmsAisleNumber);
        }
        return fragment;
    }
}