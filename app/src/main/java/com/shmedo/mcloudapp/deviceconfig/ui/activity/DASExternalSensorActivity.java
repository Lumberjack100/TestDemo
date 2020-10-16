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
import com.shmedo.mcloudapp.deviceconfig.ui.activity.sensor.ExternalDigitalSensorActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.sensor.BleDASExternalDigtalSensorFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.sensor.BleDASExternalVibratingWireSensorFragment;
import com.shmedo.mcloudapp.util.bleutil.BlueResultParserUtil;

import butterknife.BindView;

public class DASExternalSensorActivity extends BaseActivity {
    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    private int connectWay = AppContants.CommunicationWay.NET_CONNECT;

    private Fragment fragment;

    private String collectorModel = "";//采集器类型


    public static void startActivity(Context context, int connectWay, String collectorModel) {
        Intent intent = new Intent(context, DASExternalSensorActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.COLLECTOR_MODE, collectorModel);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_d_a_s_external_sensor;
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
        if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
            if (CollectorModel.value(collectorModel) == CollectorModel.VW08) {//振弦式传感器
                fragment = BleDASExternalVibratingWireSensorFragment.newInstance(collectorModel);
            } else { //数字式传感器
                fragment = BleDASExternalDigtalSensorFragment.newInstance(collectorModel);
            }
        }
        replaceFragment(fragment);
    }


    private void replaceFragment(Fragment fragment) {
        if (fragment == null)
            return;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commitAllowingStateLoss();
    }
}
