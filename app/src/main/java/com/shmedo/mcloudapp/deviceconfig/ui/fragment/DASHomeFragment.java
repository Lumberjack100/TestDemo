package com.shmedo.mcloudapp.deviceconfig.ui.fragment;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.cmd.entity.CollectorConfigEntity;
import com.shmedo.configlibrary.ble.cmd.entity.CollectorSensorParamsEntity;
import com.shmedo.configlibrary.ble.cmd.entity.RainStationEntity;
import com.shmedo.configlibrary.ble.cmd.entity.SetRainPrecisionEntity;
import com.shmedo.configlibrary.ble.enums.BreakAlarmStatus;
import com.shmedo.configlibrary.ble.enums.CollectorModel;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.RainStation;
import com.shmedo.configlibrary.ble.model.BaseConfigInfo;
import com.shmedo.configlibrary.ble.model.BreakAlarmStatusInfo;
import com.shmedo.configlibrary.ble.model.CollectorConfigInfo;
import com.shmedo.configlibrary.ble.model.QueryOsmometerParameterInfo;
import com.shmedo.configlibrary.ble.model.SetRainPrecisionInfo;
import com.shmedo.configlibrary.ble.utils.ResultParserUtil;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.event.BluetoothConnectStateEvent;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.ConfigDASActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceAdvanceSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.sensor.CommonSensorConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.sensor.VibratingWireSensorConfigActivity;
import com.shmedo.mcloudapp.util.bleutil.BlueResultParserUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class DASHomeFragment extends BaseFragment {

    private static final int REQUEST_CODE_COLLECTOR_CONFIG = 0x0010;

    private static final int REQUEST_CODE_SENSOR_CONFIG = 0x0011;


    private static final int DEVICE_ENABLE = 0x0002;

//    private static final int SIM_A = 0x0005;

//    private static final int SIM_B = 0x0006;

//    private static final int OSMOMETER_CONFIG = 0x0008;

    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;

    @BindView(R.id.tv_device_model)
    TextView mTvDeviceModel;

    @BindView(R.id.tv_sensor_type)
    TextView mTvSensorType;

    @BindView(R.id.bluetooth_connect_layout)
    View bluetoothConnectLayout;

    @BindView(R.id.device_enable_state_layout)
    View deviceEnableLayout;

    @BindView(R.id.switch_layout)
    View switchLayout;

    @BindView(R.id.llty_rain_break_alarm)
    View rainBreakAlarmLayout;

    @BindView(R.id.llty_rain)
    View rainLayout;

    @BindView(R.id.llty_break_alarm)
    View breakAlarmLayout;

    @BindView(R.id.general_sensor_param_config_layout)
    View generalSensorParamConfigLayout;

    private TextView mTvBluetoothConnect, mTvDeviceActivation, mTvBreakAlarm;

    private SwitchButton mSbBluetoothConnect, mSbDeviceActivation, mSbBleakAlarm;

    private Spinner mSpSwitch, mSpRain;

    private ConfigDASActivity configDASActivity;

    private ArrayAdapter<String> switchAdapter;

    private ArrayAdapter<String> rainAdapter;

    private String collectorModel = "";//采集器类型

    private StringBuilder sbcollectorSensor;//采集器上传感器配置信息

    private SetRainPrecisionInfo setRainPrecisionInfo;
    private CollectorConfigInfo collectorConfigInfo;
    private BaseConfigInfo baseConfigInfo;
    private BreakAlarmStatusInfo breakAlarmStatusInfo;
    private QueryOsmometerParameterInfo queryOsmometerParameterInfo;

    private int alarmStatusCheck = 0;//标志位，Avoid onItemSelected calls during initialization

    private int rainCheck = 0;//标志位，Avoid onItemSelected calls during initialization

    private int sensorIndex = 0;//接入的传感器索引号


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_dashome;
    }

    @Override
    public void onResume() {
        super.onResume();
        setViewStateByConnectState(MCloudApp.isIsBluetoothDeviceConnected());
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getIntentData();
        setSwitchViewListener();
        initAdapter();
    }

    @Override
    protected void initView() {
        ((TextView) bluetoothConnectLayout.findViewById(R.id.tv_config_name)).setText("蓝牙连接");
        mTvBluetoothConnect = bluetoothConnectLayout.findViewById(R.id.tv_device_state);
        mSbBluetoothConnect = bluetoothConnectLayout.findViewById(R.id.switchButton);

        ((TextView) deviceEnableLayout.findViewById(R.id.tv_config_name)).setText("设备启用状态");
        mTvDeviceActivation = deviceEnableLayout.findViewById(R.id.tv_device_state);
        mSbDeviceActivation = deviceEnableLayout.findViewById(R.id.switchButton);

        ((TextView) switchLayout.findViewById(R.id.tv_config_name)).setText("开关量");
        mSpSwitch = switchLayout.findViewById(R.id.spinner);

        ((TextView) rainLayout.findViewById(R.id.tv_config_name)).setText("雨量计配置");
        mSpRain = rainLayout.findViewById(R.id.spinner);

        mTvBreakAlarm = breakAlarmLayout.findViewById(R.id.tv_break_alarm);
        mSbBleakAlarm = breakAlarmLayout.findViewById(R.id.sb_break_alarm);

        ((TextView) generalSensorParamConfigLayout.findViewById(R.id.tv_config_name)).setText("传感器参数配置");
    }

    private void getIntentData() {
        Intent intent = getActivity().getIntent();
        if (intent.getExtras() != null && intent.getExtras().containsKey(AppContants.Extras.CUR_DEVICE_NAME)) {
            String deviceInfo = intent.getStringExtra(AppContants.Extras.CUR_DEVICE_NAME);
            String[] scanData = deviceInfo.split(",");
            mTvDeviceName.setText("物联网数据采集器");
            mTvDeviceSn.setText(scanData[1]);//设备编号
            mTvDeviceModel.setText(scanData[2]);//功能型号
            mTvSensorType.setText("");
        }
        configDASActivity = (ConfigDASActivity) getActivity();
    }

    /**
     * switch按钮事件
     */
    private void setSwitchViewListener() {
        //连接蓝牙开关
        mSbBluetoothConnect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                mSbBluetoothConnect.setCheckedImmediatelyNoEvent(!isChecked);

                //未连接时，直接打开连接
                if (isChecked) {
                    configDASActivity.findAndConnectSpecificDevice();

                } else {//断开连接处理
                    if (configDASActivity.isConfigChange) {
                        configDASActivity.isExitMode = false;
                        configDASActivity.showSaveDialog(getResources().getString(R.string.disconnect_bluetooth_device_save_param_warn));
                    } else {
                        configDASActivity.disconnectDevice();
                        setViewStateByConnectState(false);
                    }
                }
            }
        });

        //设备启用状态开关
        mSbDeviceActivation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    mSbDeviceActivation.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }

                if (isChecked) {
                    //发送激活DAS命令
                    configDASActivity.setLowEnergyModel(true);
                    setSwitchViewState(true, mTvDeviceActivation, "已激活");
                } else {
                    showCloseSwitchButtonDialog(getString(R.string.device_enable_state_close_warn), DEVICE_ENABLE);
                }
            }
        });

        //断线报警器开关
        mSbBleakAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    mSbBleakAlarm.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                if (isChecked) {
                    //发送断线报警器常开指令
                    configDASActivity.setBreakAlarmStatus(BreakAlarmStatus.OPEN);
                    setSwitchViewState(true, mTvBreakAlarm, "常开");

                } else {
                    //发送断线报警器常闭指令
                    configDASActivity.setBreakAlarmStatus(BreakAlarmStatus.CLOSE);
                    setSwitchViewState(false, mTvBreakAlarm, "常闭");
                }
            }
        });
    }

    private void initAdapter() {
        //开关量
        String[] switchData = getResources().getStringArray(R.array.das_switch);
        switchAdapter = new ArrayAdapter<>(configDASActivity, android.R.layout.simple_spinner_item, switchData);
        switchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpSwitch.setAdapter(switchAdapter);
        mSpSwitch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                alarmStatusCheck++;
                if (alarmStatusCheck >= 3) {
                    String value = parent.getSelectedItem().toString();
                    RainStationEntity entity = null;
                    switch (value) {
                        case "关闭":
                            entity = new RainStationEntity(RainStation.CLOSE.toInt());
                            rainBreakAlarmLayout.setVisibility(View.GONE);
                            break;

                        case "雨量计":
                            entity = new RainStationEntity(RainStation.OPEN.toInt());
                            rainBreakAlarmLayout.setVisibility(View.VISIBLE);
                            rainLayout.setVisibility(View.VISIBLE);
                            breakAlarmLayout.setVisibility(View.GONE);
                            break;

                        case "断线报警器":
                            entity = new RainStationEntity(RainStation.ALARM_OPEN.toInt());
                            rainBreakAlarmLayout.setVisibility(View.VISIBLE);
                            rainLayout.setVisibility(View.GONE);
                            breakAlarmLayout.setVisibility(View.VISIBLE);
                            break;
                    }
                    String command = CommandManager.getInstance().getCommand(CommandType.RAIN_STATION, entity);
                    configDASActivity.sendCommonCommandImmediately(command);
                    Timber.d("设置开关量指令==%s", command);
                    if (value.equals("断线报警器")) {
                        //查询断线报警器参数
                        configDASActivity.setBreakAlarmStatus(BreakAlarmStatus.QUERY);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //雨量计精度配置
        String[] rainData = getResources().getStringArray(R.array.rain);
        rainAdapter = new ArrayAdapter<>(configDASActivity, android.R.layout.simple_spinner_item, rainData);
        rainAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpRain.setAdapter(rainAdapter);
        mSpRain.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                rainCheck++;
                if (rainCheck >= 2) {
                    String result = mSpRain.getSelectedItem().toString().replace("mm", "");
                    int precision = (int) (Double.parseDouble(result) * 100);
                    SetRainPrecisionEntity entity = new SetRainPrecisionEntity(precision);
                    String command = CommandManager.getInstance().getCommand(CommandType.SETTING_RAIN_PRECISION, entity);
                    configDASActivity.sendCommonCommandImmediately(command);
                    Timber.d("设置雨量计精度指令==%s", command);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    @OnClick({R.id.rl_mqtt_config, R.id.rl_collector_control, R.id.general_sensor_param_config_layout, R.id.rl_advanced_config, R.id.btn_save})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.general_sensor_param_config_layout:
                startSensorConfigActivity();
                break;

            case R.id.rl_mqtt_config:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }

                break;

            case R.id.rl_collector_control:
                 break;

            case R.id.rl_advanced_config:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                DeviceAdvanceSettingActivity.startActivity(getActivity(), baseConfigInfo.getWorkModel().toInt());
                break;

            case R.id.btn_save:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }
//                isExitMode = true;
                configDASActivity.saveConfigInfo();
                break;
        }
    }

    private void startSensorConfigActivity() {
        if (!MCloudApp.isIsBluetoothDeviceConnected()) {
            ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
            return;
        }
        CollectorModel model = CollectorModel.value(collectorModel);
        switch (model) {
            case VW08://同时接入多种类型传感器的采集器
                VibratingWireSensorConfigActivity.startActivityForResultByFragment(this, sbcollectorSensor.toString(), REQUEST_CODE_SENSOR_CONFIG);
                break;

            default:
                CommonSensorConfigActivity.startActivityForResultByFragment(this, sbcollectorSensor.toString(), REQUEST_CODE_SENSOR_CONFIG);
                break;
        }
    }

    /**
     * 设置显示数据
     */
    private void setResultData(String cmdStr) {
        String tempStr = cmdStr.replace("$$", "").replace("\r\n", "");
        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case BASE_CONFIG://基础配置信息 000
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("查询基础配置信息指令出错!");
                    return;
                }
                baseConfigInfo = ResultParserUtil.getEntityObject(cmdStr);
                initBaseConfigInfo();
                queryCollectorConfigInfo();
                break;

            case COLLECTOR_CONFIG://采集器配置信息 100
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("查询采集器配置信息指令出错!");
                    return;
                }
                collectorConfigInfo = ResultParserUtil.getEntityObject(cmdStr);
                // 查询传感器配置信息前,重置accessNumFlag、sbcollectorSensor参数
                sensorIndex = 0;
                sbcollectorSensor = new StringBuilder();
                if (collectorConfigInfo == null) {
                    Timber.e("采集器配置信息为空!");
                    return;
                }
                querySensorConfigInfo();
                break;

            case COLLECTOR_CHANNEL_SENSOR_PARAMETER://获取XX采集器YY通道的传感器参数 101
                sbcollectorSensor.append(cmdStr.replace("\r\n", "") + "&&");
                sensorIndex++;
                querySensorConfigInfo();
                break;

            case RAIN_STATION://雨量计开关 0051：雨量计开启  0052：关闭   0053：断线报警器开启
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("开关量配置错误!");
                    return;
                }
                break;

            case BREAK_ALARM_STATUS: //断线报警器状态 227
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("断线报警器配置错误!");
                    return;
                }
                breakAlarmStatusInfo = ResultParserUtil.getEntityObject(cmdStr);
                if (breakAlarmStatusInfo == null) {
                    Timber.e("断线报警器状态为空!");
                    return;
                }
                Timber.d("断线报警器状态: %s", breakAlarmStatusInfo.toString());
                switch (breakAlarmStatusInfo.getStatus()) {
                    case OPEN:
                        mSbBleakAlarm.setCheckedImmediatelyNoEvent(true);
                        setSwitchViewState(true, mTvBreakAlarm, "常开");
                        break;
                    case CLOSE:
                        mSbBleakAlarm.setCheckedImmediatelyNoEvent(false);
                        setSwitchViewState(false, mTvBreakAlarm, "常闭");
                        break;
                }
                break;
        }
    }

    /**
     * 处理基础配置信息
     */
    private void initBaseConfigInfo() {
        if (baseConfigInfo == null) {
            Timber.e("基础配置信息为空!");
            return;
        }

        setRainPrecisionInfo = new SetRainPrecisionInfo();
        setRainPrecisionInfo.setPrecision((double) baseConfigInfo.getRainAccuracy() / 100);
        collectorModel = baseConfigInfo.getCollectorModel().toString();

        //获取采集器类型
        if (!TextUtils.isEmpty(collectorModel)) {
            CollectorModel model = CollectorModel.value(collectorModel);
            String collectorName = BlueResultParserUtil.getCollectorName(model);
            mTvSensorType.setText(collectorName);
        }

        //设备状态
        switch (baseConfigInfo.getEquipmentStatus()) {
            case STANDBY:   //待机
                mSbDeviceActivation.setCheckedImmediatelyNoEvent(false);
                setSwitchViewState(false, mTvDeviceActivation, "已待机");
                break;
            case ACTIVATION:    //激活
                mSbDeviceActivation.setCheckedImmediatelyNoEvent(true);
                setSwitchViewState(true, mTvDeviceActivation, "已激活");
                break;
        }

        //设备雨量站开关量
        switch (baseConfigInfo.getRainStation()) {
            case CLOSE:
                mSpSwitch.setSelection(0);
                rainBreakAlarmLayout.setVisibility(View.GONE);
                break;

            case OPEN://雨量站开启
                mSpSwitch.setSelection(1);
                rainBreakAlarmLayout.setVisibility(View.VISIBLE);
                rainLayout.setVisibility(View.VISIBLE);
                breakAlarmLayout.setVisibility(View.GONE);
                break;

            case ALARM_OPEN://短线报警器开启
                mSpSwitch.setSelection(2);
                rainBreakAlarmLayout.setVisibility(View.VISIBLE);
                rainLayout.setVisibility(View.GONE);
                breakAlarmLayout.setVisibility(View.VISIBLE);

                //查询断线报警器参数
                configDASActivity.setBreakAlarmStatus(BreakAlarmStatus.QUERY);
                break;
        }
        String result = setRainPrecisionInfo.getPrecision() / 100 + "mm";
        int count = rainAdapter.getCount();
        for (int i = 0; i < count; i++) {
            if (result.equals(rainAdapter.getItem(i))) {
                mSpRain.setSelection(i);
                break;
            }
        }
    }

    /**
     * 查询采集器配置信息
     */
    private void queryCollectorConfigInfo() {
        CollectorConfigEntity collectorConfigEntity = new CollectorConfigEntity(collectorModel);
        String command = CommandManager.getInstance().getCommand(CommandType.COLLECTOR_CONFIG, collectorConfigEntity);
        configDASActivity.sendCommonCommandImmediately(command);
        Timber.d("查询采集器配置信息===%s", command);
    }

    /**
     * 查询采集器接入的传感器配置信息
     */
    private void querySensorConfigInfo() {
        if (sensorIndex >= collectorConfigInfo.getAccessSum()) {
            configDASActivity.stopProgressRunnable();
            return;
        }

        String address = StringUtil.formatStringTwo(sensorIndex + "");
        CollectorSensorParamsEntity entity = new CollectorSensorParamsEntity(collectorModel, address);
        String command = CommandManager.getInstance().getCommand(CommandType.COLLECTOR_CHANNEL_SENSOR_PARAMETER, entity);
        configDASActivity.sendCommonCommand(command);
        Timber.d("获取 %s 采集器 %s 通道的传感器参数===%s", collectorModel, address, command);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getConfig(String messageEvent) {
        if (TextUtils.isEmpty(messageEvent) || !messageEvent.startsWith("$$")) {
            return;
        }
        setResultData(messageEvent);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BluetoothConnectStateEvent bluetoothConnectStateEvent) {
        setViewStateByConnectState(bluetoothConnectStateEvent.isConnected);
    }

    private void setViewStateByConnectState(boolean isConnected) {
        mSbBluetoothConnect.setCheckedImmediatelyNoEvent(isConnected);
        setSwitchViewState(isConnected, mTvBluetoothConnect, isConnected ? "已连接" : "待连接");
        mTvDeviceActivation.setTextColor(isConnected ? getResources().getColor(R.color.colorPrimary) : getResources().getColor(R.color.gray_807B7B));
        mSpSwitch.setEnabled(isConnected);
        mSpRain.setEnabled(isConnected);
    }

    private void setSwitchViewState(boolean isOpen, TextView textView, String content) {
        textView.setText(content);
        textView.setTextColor(isOpen ? getResources().getColor(R.color.colorPrimary) : getResources().getColor(R.color.gray_807B7B));
    }

    /**
     * 关闭SwitchButton
     */
    public void showCloseSwitchButtonDialog(String content, final int index) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getActivity())
                .title("温馨提示：")
                .content(content)
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        switch (index) {
                            case DEVICE_ENABLE:
                                //发送关闭DAS命令
                                configDASActivity.setLowEnergyModel(false);
                                setSwitchViewState(false, mTvDeviceActivation, "已待机");
                                break;

//                            case OSMOMETER_CONFIG:
//                                //发送关闭渗压计命令
//                                configDASActivity.sendCommonCommand("##4012\r\n");
//                                mBtnOsmometer.setEnabled(false);
//                                mBtnOsmometer.setText("已停用");
//                                break;
                        }
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        switch (index) {
                            case DEVICE_ENABLE:
                                mSbDeviceActivation.setCheckedImmediatelyNoEvent(true);
                                break;

//                            case OSMOMETER_CONFIG:
//                                mSbOsmometer.setCheckedImmediatelyNoEvent(true);
//                                break;
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            //TODO  fragment  显示或隐藏时会触发此事件
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode != Activity.RESULT_OK)
            return;

        switch (requestCode) {
            case REQUEST_CODE_COLLECTOR_CONFIG:
                if (intent != null) {
                    collectorConfigInfo = (CollectorConfigInfo) intent.getSerializableExtra(AppContants.Extras.PARAM_CONFIG_INFO);
                    configDASActivity.isConfigChange = true;
                }
                break;

            case REQUEST_CODE_SENSOR_CONFIG:
                if (intent != null) {
                    String ss = intent.getStringExtra(AppContants.Extras.SPLICE_SENSOR_PARAMS);
                    Timber.d("更新后的传感器拼接参数:" + ss);
                    sbcollectorSensor = new StringBuilder();
                    sbcollectorSensor.append(ss);
                }
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    @Override
    public boolean onBackPressed() {
        return false;
    }

}
