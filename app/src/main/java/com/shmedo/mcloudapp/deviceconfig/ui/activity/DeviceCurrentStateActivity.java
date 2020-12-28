package com.shmedo.mcloudapp.deviceconfig.ui.activity;

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
import com.shmedo.mcloudapp.deviceconfig.model.DevcieCurrentState;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.BleDasDeviceCurrentStateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.NetDasDeviceCurrentState;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

import butterknife.BindView;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20<br/>
 * 描述：     Das运行状态页面
 */
public class DeviceCurrentStateActivity extends BaseActivity {
    private static final String DEVICE_INFO = "device_info";
    private static final String DEVICE_CURRENT_STATE = "device_current_state";


    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    private int connectWay = AppContants.CommunicationWay.NET_PLATFORM_CONNECT;

    private Fragment fragment;

    private ProjectDeviceInfo projectDeviceInfo;

    private DevcieCurrentState devcieCurrentState;


    public static void startActivity(Context context, int connectWay) {
        Intent intent = new Intent(context, DeviceCurrentStateActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, ProjectDeviceInfo projectDeviceInfo, DevcieCurrentState devcieCurrentState) {
        Intent intent = new Intent(context, DeviceCurrentStateActivity.class);
        intent.putExtra(DEVICE_INFO, projectDeviceInfo);
        intent.putExtra(DEVICE_CURRENT_STATE, devcieCurrentState);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_device_current_state;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("运行状态");
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
            projectDeviceInfo = intent.getParcelableExtra(DEVICE_INFO);
        }

        if (intent.getExtras().containsKey(DEVICE_CURRENT_STATE)) {
            devcieCurrentState = intent.getParcelableExtra(DEVICE_CURRENT_STATE);
        }
    }

    private void initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {
            fragment = NetDasDeviceCurrentState.newInstance(projectDeviceInfo, devcieCurrentState);

        } else {
            fragment = new BleDasDeviceCurrentStateFragment();
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
