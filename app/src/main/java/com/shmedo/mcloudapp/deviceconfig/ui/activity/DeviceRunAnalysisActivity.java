package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

import butterknife.BindView;
import butterknife.OnClick;

public class DeviceRunAnalysisActivity extends BaseActivity {
    private static final String DEVICE_INFO = "device_info";

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;

    @BindView(R.id.tv_device_model)
    TextView mTvDeviceModel;

    @BindView(R.id.tv_time)
    TextView mTvTime;

    @BindView(R.id.tv_device_communication_state_flag)
    TextView mTvDeviceCommunicationState;//通信状态(在线、离线、已连接、已断开)

    @BindView(R.id.rl_device_connect_state)
    View deviceConnectStateLayout;

    @BindView(R.id.tv_iot_card_number)
    TextView mTvIotCardNum;

    @BindView(R.id.tv_4g_signal)
    TextView mTv4gSignal;

    @BindView(R.id.tv_power)
    TextView mTvPower;

    @BindView(R.id.tv_external_voltage)
    TextView mTvExternalVoltage;

    @BindView(R.id.tv_firmware_version)
    TextView mTvFirmwareVersion;

    @BindView(R.id.tv_location)
    TextView mTvLocation;

    @BindView(R.id.tv_sensor_status)
    TextView mTvSensorStatus;

    private ProjectDeviceInfo projectDeviceInfo;

    public static void startActivity(Context context, ProjectDeviceInfo projectDeviceInfo) {
        Intent intent = new Intent(context, DeviceRunAnalysisActivity.class);
        intent.putExtra(DEVICE_INFO, projectDeviceInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_device_run_analysis;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        initView();
        parseIntent();
        setHeadInfo();
    }

    private void initView() {
        mToolbarTitle.setText("运行分析");
        mTvDeviceSn.setVisibility(View.GONE);
        deviceConnectStateLayout.setVisibility(View.GONE);
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(DEVICE_INFO)) {
            projectDeviceInfo = intent.getParcelableExtra(DEVICE_INFO);
        }
    }

    private void setHeadInfo() {
        if (projectDeviceInfo != null) {
            mTvDeviceName.setText(TextUtils.isEmpty(projectDeviceInfo.getName()) ? "" : projectDeviceInfo.getName());
            mTvDeviceModel.setText(String.format("产品型号：%s", TextUtils.isEmpty(projectDeviceInfo.getDeviceTypeName()) ? "" : projectDeviceInfo.getDeviceTypeName()));
            mTvTime.setText(String.format("传输时间：%s", TextUtils.isEmpty(projectDeviceInfo.getLastActiveTime()) ? "" : projectDeviceInfo.getLastActiveTime()));
            if (projectDeviceInfo.isOnline()) {
                mTvDeviceCommunicationState.setText("在线");
                mTvDeviceCommunicationState.setTextColor(ContextCompat.getColor(this, R.color.text_color_50E9B9));
                mTvDeviceCommunicationState.setBackgroundResource(R.drawable.bg_device_online_state_flag);
            } else {
                mTvDeviceCommunicationState.setText("离线");
                mTvDeviceCommunicationState.setTextColor(ContextCompat.getColor(this, R.color.sub_title_text_color));
                mTvDeviceCommunicationState.setBackgroundResource(R.drawable.bg_device_offline_state_flag);
            }
        }
    }


    @OnClick({R.id.iotCardLayout, R.id.signalLayout, R.id.powerLayout, R.id.externalVoltageLayout, R.id.firmwareVersionLayout, R.id.locationLayout, R.id.sensorStatusLayout})
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.iotCardLayout:
                break;

            case R.id.signalLayout:
                break;

            case R.id.powerLayout:
                break;

            case R.id.externalVoltageLayout:
                break;

            case R.id.firmwareVersionLayout:
                break;

            case R.id.locationLayout:
                break;

            case R.id.sensorStatusLayout:
                break;
        }
    }
}
