package com.shmedo.mcloudapp.deviceconfig.ui.activity.e40;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40.NetE40CorsServiceFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2/26/21 <br/>
 * 描述：     E40 CORS 服务参数配置页面
 */
public class E40CORSServiceActivity extends BaseConfigFragmentContainerActivity {
    private static final String PRO_DEVICE_INFO = "com.shmedo.mcloudapp.PRO_DEVICE_INFO";

    private ProjectDeviceInfo projectDeviceInfo;

    public static void startActivity(Context context, ProjectDeviceInfo projectDeviceInfo) {
        Intent intent = new Intent(context, E40CORSServiceActivity.class);
        intent.putExtra(PRO_DEVICE_INFO, projectDeviceInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int connectWay) {
        Intent intent = new Intent(context, E40CORSServiceActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("CORS 服务");
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(PRO_DEVICE_INFO)) {
            projectDeviceInfo = intent.getParcelableExtra(PRO_DEVICE_INFO);
        }
    }

    @Override
    protected Fragment initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            fragment = NetE40CorsServiceFragment.newInstance(projectDeviceInfo);

        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
        }

        return fragment;
    }
}
