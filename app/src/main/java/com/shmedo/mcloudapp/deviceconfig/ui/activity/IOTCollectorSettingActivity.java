package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.model.UserInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.BleCollectorSettingFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.NetCollectorSettingFragment;

import butterknife.BindView;

public class IOTCollectorSettingActivity extends BaseActivity {
    private static final String DEVICE_ID = "device_id";

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    private int connectWay = AppContants.CommunicationWay.NET_CONNECT;

    private int deviceid;

    private String collectorModel = "";//采集器类型

    private int companyID;

    private Fragment fragment;


    public static void startActivity(Context context, int deviceid) {
        Intent intent = new Intent(context, IOTCollectorSettingActivity.class);
        intent.putExtra(DEVICE_ID, deviceid);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int connectWay, String collectorModel) {
        Intent intent = new Intent(context, IOTCollectorSettingActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.COLLECTOR_MODE, collectorModel);
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
        companyID = MCloudApp.getCompanyID();
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

        if (intent.getExtras().containsKey(DEVICE_ID)) {
            deviceid = intent.getIntExtra(DEVICE_ID, -1);
        }

        if (intent.getExtras().containsKey(AppContants.Extras.COLLECTOR_MODE)) {
            collectorModel = intent.getStringExtra(AppContants.Extras.COLLECTOR_MODE);
        }
    }

    private void initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_CONNECT) {
            fragment = NetCollectorSettingFragment.newInstance(companyID, deviceid);
        } else {
            fragment = BleCollectorSettingFragment.newInstance(collectorModel);
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
