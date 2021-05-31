package com.shmedo.mcloudapp.deviceconfig.ui.activity.adme;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.BleAdmeBasicParamConfigFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme.NetAdmeBasicParamConfigFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/12/29<br/>
 * 描述：     ADME 基本参数配置页面
 */
public class AdmeBasicParamActivity extends BaseConfigFragmentContainerActivity {

    public static void startActivity(Context context, ProjectDeviceInfo projectDeviceInfo) {
        Intent intent = new Intent(context, AdmeBasicParamActivity.class);
        intent.putExtra(PRO_DEVICE_INFO, projectDeviceInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int connectWay) {
        Intent intent = new Intent(context, AdmeBasicParamActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("基础配置");
        //TODO #gh#  打开注释，可以在编辑/浏览模式间切换
//        mTvAction.setVisibility(View.VISIBLE);
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            fragment = NetAdmeBasicParamConfigFragment.newInstance(projectDeviceInfo);

        } else {
            fragment = BleAdmeBasicParamConfigFragment.newInstance();
        }

        return fragment;
    }
}