package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.shmedo.configlibrary.ble.enums.CollectorModel;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.BleDASExternalSensorConfigFragment;
import com.shmedo.mcloudapp.util.bleutil.BlueResultParserUtil;

import butterknife.BindView;

public class DASExternalSensorConfigActivity extends BaseActivity {
    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    private int connectWay = AppContants.CommunicationWay.NET_CONNECT;

    private Fragment fragment;

    private String collectorModel = "";//采集器类型


    public static void startActivity(Context context, int connectWay, String collectorModel) {
        Intent intent = new Intent(context, DASExternalSensorConfigActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.COLLECTOR_MODE, collectorModel);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_d_a_s_external_sensor_config;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("扩展传感器配置");
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

        if (intent.getExtras().containsKey(AppContants.Extras.COLLECTOR_MODE)) {
            collectorModel = intent.getStringExtra(AppContants.Extras.COLLECTOR_MODE);
            String collectorName = BlueResultParserUtil.getCollectorName(CollectorModel.value(collectorModel));
            mToolbarTitle.setText(collectorName);
        }
    }

    private void initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_CONNECT) {

        } else {
            fragment = BleDASExternalSensorConfigFragment.newInstance(collectorModel);
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
