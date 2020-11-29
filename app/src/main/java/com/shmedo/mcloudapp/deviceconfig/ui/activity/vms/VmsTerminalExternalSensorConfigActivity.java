package com.shmedo.mcloudapp.deviceconfig.ui.activity.vms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.shmedo.configlibrary.iot.model.TerminalSensorInfo;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms.TcpVmsTerminalExternalSensorParamFragment;

import butterknife.BindView;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/23 <br/>
 * 描述：     Vms终端外接振弦式传感器参数配置
 */
public class VmsTerminalExternalSensorConfigActivity extends BaseActivity {
    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    private int connectWay = AppContants.CommunicationWay.NET_PLATFORM_CONNECT;

    private Fragment fragment;

    private TerminalSensorInfo sensorInfo;


    public static void startActivity(Context context, int connectWay, TerminalSensorInfo sensorInfo) {
        Intent intent = new Intent(context, VmsTerminalExternalSensorConfigActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(AppContants.Extras.SENSOR_PARAM, sensorInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.vms_terminal_external_sensor_config_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("传感器配置");
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

        if (intent.getExtras().containsKey(AppContants.Extras.SENSOR_PARAM)) {
            sensorInfo = intent.getParcelableExtra(AppContants.Extras.SENSOR_PARAM);
//            String sensorName = IOTSensorUtil.getInstance().getSensorNameByTypeNo(sensorInfo.getName());
//            mToolbarTitle.setText(sensorName);
        }
    }

    private void initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {

        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
//            if (TextUtils.isEmpty(sensorInfo.getType()) || sensorInfo.getType().trim().equals("55")) {
//                //55，多项式振弦式裂缝计
//                fragment = TcpVmsTerminalExternalPolynomialSensorFragment.newInstance(sensorInfo);
//            } else if (TextUtils.isEmpty(sensorInfo.getType()) || sensorInfo.getType().trim().equals("58")) {
//                //58 直线式钢筋计
//                fragment = TcpVmsTerminalExternalLinearSensorFragment.newInstance(sensorInfo);
//            }
            fragment = TcpVmsTerminalExternalSensorParamFragment.newInstance(sensorInfo);
        }

        if (fragment != null)
            replaceFragment(fragment);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commitAllowingStateLoss();
    }
}