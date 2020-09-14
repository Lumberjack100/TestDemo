package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.model.DevcieCurrentState;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.BleDeviceCurrentStateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.NetDeviceCurrentState;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

import butterknife.BindView;

public class DeviceCurrentStateActivity extends BaseActivity {
    private static final String CONNECT_WAY = "connect_way";
    private static final String DEVICE_INFO = "device_info";
    private static final String DEVICE_CURRENT_STATE = "device_current_state";

    public static final int NET_CONNECT = 0x001;//网络连接
    public static final int BLE_CONNECT = 0x002;//蓝牙连接

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    private int connectWay = NET_CONNECT;

    private Fragment fragment;

    private ProjectDeviceInfo projectDeviceInfo;

    private DevcieCurrentState devcieCurrentState;


    public static void startActivity(Context context, int connectWay) {
        Intent intent = new Intent(context, DeviceCurrentStateActivity.class);
        intent.putExtra(CONNECT_WAY, connectWay);
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

        if (intent.getExtras().containsKey(CONNECT_WAY)) {
            connectWay = intent.getIntExtra(CONNECT_WAY, NET_CONNECT);
        }

        if (intent.getExtras().containsKey(DEVICE_INFO)) {
            projectDeviceInfo = intent.getParcelableExtra(DEVICE_INFO);
        }

        if (intent.getExtras().containsKey(DEVICE_CURRENT_STATE)) {
            devcieCurrentState = intent.getParcelableExtra(DEVICE_CURRENT_STATE);
        }
    }

    private void initFragment() {
        if (connectWay == NET_CONNECT) {
            fragment = NetDeviceCurrentState.newInstance(projectDeviceInfo, devcieCurrentState);

        } else {
            fragment = new BleDeviceCurrentStateFragment();
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
