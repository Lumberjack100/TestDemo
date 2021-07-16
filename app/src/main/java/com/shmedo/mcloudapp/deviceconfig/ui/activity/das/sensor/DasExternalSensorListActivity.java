package com.shmedo.mcloudapp.deviceconfig.ui.activity.das.sensor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.shmedo.configlibrary.ble.enums.CollectorModel;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.sensor.BleDasExternalDigtalSensorListListFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.das.sensor.BleDasExternalVibratingWireSensorListListFragment;

import butterknife.BindView;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20<br/>
 * 描述：     Das 扩展传感器配置主页面
 */
public class DasExternalSensorListActivity extends BaseActivity {
    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    private int connectWay = AppContants.CommunicationWay.NET_PLATFORM_CONNECT;

    private Fragment fragment;

    private String collectorModel = "";//采集器类型


    public static void startActivity(Context context, int connectWay, String collectorModel) {
        Intent intent = new Intent(context, DasExternalSensorListActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.COLLECTOR_MODE, collectorModel);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_das_external_sensor_home;
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
            connectWay = intent.getIntExtra(AppContants.Extras.COMMUNICATION_WAY, AppContants.CommunicationWay.NET_PLATFORM_CONNECT);
        }

        if (intent.getExtras().containsKey(AppContants.Extras.COLLECTOR_MODE)) {
            collectorModel = intent.getStringExtra(AppContants.Extras.COLLECTOR_MODE);
            if (CollectorModel.value(collectorModel) == CollectorModel.VW08) {//振弦式传感器
                mToolbarTitle.setText("振弦式传感器");
            } else {
                mToolbarTitle.setText("数字式传感器");
            }
        }
    }

    private void initFragment() {
        if (connectWay == AppContants.CommunicationWay.BLE_CONNECT) {
            if (CollectorModel.value(collectorModel) == CollectorModel.VW08) {//振弦式传感器
                fragment = BleDasExternalVibratingWireSensorListListFragment.newInstance(collectorModel);
            } else { //数字式传感器
                fragment = BleDasExternalDigtalSensorListListFragment.newInstance(collectorModel);
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
