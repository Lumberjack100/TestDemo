package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.NetCollectorSettingFragment;

import butterknife.BindView;

public class IOTCollectorSettingActivity extends BaseActivity {
    private static final String CONNECT_WAY = "connect_way";
    private static final String DEVICE_ID = "device_id";

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    public static final int NET_CONNECT = 0x001;//网络连接
    public static final int BLE_CONNECT = 0x002;//蓝牙连接

    private int connectWay = NET_CONNECT;

    private int deviceid;

    private int companyID;

    private Fragment fragment;


    public static void startActivity(Context context, int connectWay, int deviceid) {
        Intent intent = new Intent(context, IOTCollectorSettingActivity.class);
        intent.putExtra(CONNECT_WAY, connectWay);
        intent.putExtra(DEVICE_ID, deviceid);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_i_o_t_collector_setting;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("采集器配置");
        initUserData();
        parseIntent();
        initFragment();
    }

    private void initUserData() {
        UserInfo userInfo = MCloudApp.getCurrentUserInfo();
        if (userInfo != null && userInfo.getDepartments() != null && userInfo.getDepartments().size() > 0) {
            companyID = userInfo.getDepartments().get(0).getCompanyID();
        }
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(CONNECT_WAY)) {
            connectWay = intent.getIntExtra(CONNECT_WAY, NET_CONNECT);
        }

        if (intent.getExtras().containsKey(DEVICE_ID)) {
            deviceid = intent.getIntExtra(DEVICE_ID, -1);
        }
    }

    private void initFragment() {
        if (connectWay == NET_CONNECT) {
            fragment = NetCollectorSettingFragment.newInstance(companyID, deviceid);

        } else {

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
