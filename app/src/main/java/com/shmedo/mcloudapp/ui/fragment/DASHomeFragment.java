package com.shmedo.mcloudapp.ui.fragment;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.das.common.BaseConfigInfo;
import com.shmedo.das.common.BreakAlarmStatusInfo;
import com.shmedo.das.common.CollectorConfigInfo;
import com.shmedo.das.common.GetAllSensorConfigInfo;
import com.shmedo.das.common.QueryOsmometerParameterInfo;
import com.shmedo.das.common.enumerate.BreakAlarmStatus;
import com.shmedo.das.common.enumerate.CollectorModel;
import com.shmedo.das.das.cmd.CommandType;
import com.shmedo.das.utils.StringUtil;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseFragment;
import com.shmedo.mcloudapp.entity.ble.BreakAlarmStatusSub;
import com.shmedo.mcloudapp.entity.ble.DeviceLockStatusSub;
import com.shmedo.mcloudapp.entity.event.BluetoothStateEvent;
import com.shmedo.mcloudapp.model.Extras;
import com.shmedo.mcloudapp.ui.activity.ConfigDASActivity;
import com.shmedo.mcloudapp.ui.activity.device.GeneralSettingActivity;
import com.shmedo.mcloudapp.ui.activity.device.MqttSettingActivity;
import com.shmedo.mcloudapp.ui.activity.device.sensor.SenSorBGKConfigActivity;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.bleutil.BlueResultParserUtil;
import com.shmedo.mcloudapp.util.page.model.SetRainAccuryPage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class DASHomeFragment extends BaseFragment {

    private static final int DEVICE_ENABLE = 0x0002;

    private static final int DEVICE_LOCK = 0x0003;

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

    @BindView(R.id.debug_model_layout)
    View debugModelLayout;

    @BindView(R.id.switch_layout)
    View switchLayout;

    @BindView(R.id.llty_rain_break_alarm)
    View rainBreakAlarmLayout;

    @BindView(R.id.llty_rain)
    View rainLayout;

    @BindView(R.id.llty_break_alarm)
    View breakAlarmLayout;

    @BindView(R.id.sensor_setting_layout)
    View sensorSettingLayout;

    private TextView mTvBluetoothConnect, mTvDeviceEnable, mTvBreakAlarm;

    private SwitchButton mSbBluetoothConnect, mSbDeviceEnable, mSbBleakAlarm;

    private Spinner mSpDebugMode, mSpSwitch, mSpRain;

    private Unbinder unbinder;

    private ConfigDASActivity configDASActivity;

    private DaoManager manager = DaoManager.getInstance();

//    private List<String> systemDataInfoList = new ArrayList<>();//项目信息列表

//    private HashMap<String, SystemDataInfo> systemDataInfoHashMap = new HashMap<>();

    private ArrayAdapter<String> debugModeAdapter;

    private ArrayAdapter<String> switchAdapter;

    private ArrayAdapter<String> rainAdapter;

    private String collectorModel = "";//采集器类型

    private String lockStatus = "";//设备锁定状态

    private StringBuilder sbcollectorSensor = new StringBuilder();//采集器上传感器配置信息


    private SetRainAccuryPage.SetRianAccuryParameter setRianAccuryParameter = new SetRainAccuryPage.SetRianAccuryParameter();
    private CollectorConfigInfo collectorConfigInfo;
    private BaseConfigInfo baseConfigInfo;
    private QueryOsmometerParameterInfo queryOsmometerParameterInfo;
    private BreakAlarmStatusInfo breakAlarmStatusInfo = new BreakAlarmStatusInfo();

    private int debugModeCheck = 0;//标志位，Avoid onItemSelected calls during initialization

    private int alarmStatusCheck = 0;//标志位，Avoid onItemSelected calls during initialization

    private int rainCheck = 0;//标志位，Avoid onItemSelected calls during initialization


    @Override
    protected int initContentView() {
        return R.layout.fragment_dashome;
    }

    @Override
    public void onResume() {
        super.onResume();
        setViewStateByConnectState(MCloudApp.isIsBluetoothDeviceConnected());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);

        getIntentData();
        initView();
        setSwitchViewListener();
        initAdapter();
        return view;
    }

    private void getIntentData() {
        Intent intent = getActivity().getIntent();
        if (intent.getExtras() != null && intent.getExtras().containsKey(Extras.CUR_DEVICE_NAME)) {
            String deviceInfo = intent.getStringExtra(Extras.CUR_DEVICE_NAME);
            String[] scanData = deviceInfo.split(",");
            mTvDeviceName.setText("物联网数据采集器");
            mTvDeviceSn.setText(scanData[1]);//设备编号
            mTvDeviceModel.setText(scanData[2]);//功能型号
            mTvSensorType.setText("");
        }
        configDASActivity = (ConfigDASActivity) getActivity();
    }


    private void initView() {
        ((TextView) bluetoothConnectLayout.findViewById(R.id.tv_config_name)).setText("蓝牙连接");
        mTvBluetoothConnect = bluetoothConnectLayout.findViewById(R.id.tv_device_state);
        mSbBluetoothConnect = bluetoothConnectLayout.findViewById(R.id.switchButton);

        ((TextView) deviceEnableLayout.findViewById(R.id.tv_config_name)).setText("设备启用状态");
        mTvDeviceEnable = deviceEnableLayout.findViewById(R.id.tv_device_state);
        mSbDeviceEnable = deviceEnableLayout.findViewById(R.id.switchButton);

        ((TextView) debugModelLayout.findViewById(R.id.tv_config_name)).setText("调试模式");
        mSpDebugMode = debugModelLayout.findViewById(R.id.spinner);

        ((TextView) switchLayout.findViewById(R.id.tv_config_name)).setText("开关量");
        mSpSwitch = switchLayout.findViewById(R.id.spinner);

        ((TextView) rainLayout.findViewById(R.id.tv_config_name)).setText("雨量计配置");
        mSpRain = rainLayout.findViewById(R.id.spinner);

        mTvBreakAlarm = breakAlarmLayout.findViewById(R.id.tv_break_alarm);
        mSbBleakAlarm = breakAlarmLayout.findViewById(R.id.sb_break_alarm);

        ((TextView) sensorSettingLayout.findViewById(R.id.tv_config_name)).setText("传感器参数配置");
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
                    configDASActivity.findAndConnectBleDevice();

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
        mSbDeviceEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    mSbDeviceEnable.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }

                if (isChecked) {
                    //发送激活DAS命令
                    configDASActivity.sendCommonCommand("##0182\r\n");
                    setSwitchViewState(true, mTvDeviceEnable, "已激活");
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
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    mSbBleakAlarm.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                if (isChecked) {
                    //发送断线报警器常开指令
                    configDASActivity.sendCommonCommand("##2271\r\n");
                    setSwitchViewState(true, mTvBreakAlarm, "常开");

                } else {
                    configDASActivity.sendCommonCommand("##2272\r\n");
                    setSwitchViewState(false, mTvBreakAlarm, "常闭");
                }
            }
        });
    }

    private void initAdapter() {
        //调试模式
        String[] debugData = getResources().getStringArray(R.array.das_debug_mode);
        debugModeAdapter = new ArrayAdapter<>(configDASActivity, android.R.layout.simple_spinner_item, debugData);
        debugModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpDebugMode.setAdapter(debugModeAdapter);
        mSpDebugMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                debugModeCheck++;
                if (debugModeCheck >= 2) {
                    String status = parent.getSelectedItem().toString();
                    String smdStr;
                    switch (status) {
                        case "初始化":
                            smdStr = "##0060\r\n";
                            break;

                        case "关闭":
                            smdStr = "##0061\r\n";
                            break;

                        case "DEBUG":
                            smdStr = "##0062\r\n";
                            break;

                        case "INFO":
                            smdStr = "##0063\r\n";
                            break;

                        default:
                            smdStr = "##0061\r\n";
                            break;
                    }
                    configDASActivity.sendCommonCommand(smdStr);
                    Timber.d("设置调试模式指令==" + smdStr);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //开关量
        String[] switchData = getResources().getStringArray(R.array.das_switch);
        switchAdapter = new ArrayAdapter<>(configDASActivity, android.R.layout.simple_spinner_item, switchData);
        switchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpSwitch.setAdapter(switchAdapter);
        mSpSwitch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                alarmStatusCheck++;
                if (alarmStatusCheck >= 2) {
                    String value = parent.getSelectedItem().toString();
                    String smdStr = "";
                    switch (value) {
                        case "关闭":
                            smdStr = "##0052\r\n";
                            rainBreakAlarmLayout.setVisibility(View.GONE);
                            break;

                        case "雨量计":
                            smdStr = "##0051\r\n";
                            rainBreakAlarmLayout.setVisibility(View.VISIBLE);
                            rainLayout.setVisibility(View.VISIBLE);
                            breakAlarmLayout.setVisibility(View.GONE);
                            break;

                        case "断线报警器":
                            smdStr = "##0053\r\n";
                            rainBreakAlarmLayout.setVisibility(View.VISIBLE);
                            rainLayout.setVisibility(View.GONE);
                            breakAlarmLayout.setVisibility(View.VISIBLE);
                            break;
                    }

                    configDASActivity.sendCommonCommand(smdStr);
                    Timber.d("设置开关量指令==" + smdStr);

                    if (value.equals("断线报警器")) {
                        configDASActivity.sendCommonCommand("##2270\r\n");
                        Timber.d("查询断线报警器参数指令==##2270");
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
                    DecimalFormat df = new DecimalFormat("0");
                    String rainResult = df.format(Double.valueOf(result) * 100);

                    String cmdStr = "##121" + rainResult + "\r\n";

                    configDASActivity.sendCommonCommand(cmdStr);
                    Timber.d("设置雨量计精度指令==" + cmdStr);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    @OnClick({R.id.sensor_setting_layout, R.id.rl_general_setting, R.id.rl_mqtt_setting})
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.tv_rain_gauge:
//                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
//                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
//                    return;
//                }
//                RainConfigActivity.startActivity(configDASActivity, setRianAccuryParameter.getRainAccury());
//                break;

            case R.id.sensor_setting_layout:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    return;
                }
                SenSorBGKConfigActivity.startActivity(configDASActivity, sbcollectorSensor.toString());
                break;

            case R.id.rl_general_setting:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    return;
                }
                GeneralSettingActivity.startActivity(configDASActivity, collectorConfigInfo, collectorModel);
                break;

            case R.id.rl_mqtt_setting:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    return;
                }
                MqttSettingActivity.startActivity(configDASActivity);
                break;
        }
    }


    /**
     * 所有配置信息处理
     */
    private void processGetAllSensorConfig(String cmdStr) {
        Timber.d("--------所有配置信息 --每次返回指令-------" + cmdStr);
        String[] strs = cmdStr.split("@@");
        if (strs == null || strs.length < 6)
            return;

        //拼接采集器接入的传感器配置信息
        sbcollectorSensor = new StringBuilder();
        for (int i = 5; i < strs.length; i++) {
            sbcollectorSensor.append(strs[i] + "&&");
        }

        GetAllSensorConfigInfo getAllSensorConfigInfo = BlueResultParserUtil.getAllBlueMessage(cmdStr);
        Timber.d("--------所有配置信息-------" + getAllSensorConfigInfo.toString());

        collectorConfigInfo = getAllSensorConfigInfo.getCollectorConfig();
        baseConfigInfo = getAllSensorConfigInfo.getBaseConfig();
        if (baseConfigInfo != null) {
            setRianAccuryParameter.setRainAccury(String.valueOf(baseConfigInfo.getRainAccuracy() / 100));
            collectorModel = baseConfigInfo.getCollectorModel().toString();
        }

        updateView();
    }

    private void updateView() {
        if (!TextUtils.isEmpty(collectorModel)) {
            CollectorModel model = CollectorModel.value(collectorModel);
            String collectorName = BlueResultParserUtil.getCollectorName(model);
            mTvSensorType.setText(collectorName);
        }

        //设备调试模式
        switch (baseConfigInfo.getDebugModel()) {
            case INITIALZE:
                mSpDebugMode.setSelection(0);
                break;

            case CLOSE:
                mSpDebugMode.setSelection(1);
                break;

            case DEBUG:
                mSpDebugMode.setSelection(2);
                break;

            case INFO:
                mSpDebugMode.setSelection(3);
                break;

            default:
                mSpDebugMode.setSelection(1);
                break;
        }

        //设备状态
        switch (baseConfigInfo.getEquipmentStatus()) {
            case STANDBY:   //待机
                mSbDeviceEnable.setCheckedImmediatelyNoEvent(false);
                setSwitchViewState(false, mTvDeviceEnable, "已待机");
                break;
            case ACTIVATION:    //激活
                mSbDeviceEnable.setCheckedImmediatelyNoEvent(true);
                setSwitchViewState(true, mTvDeviceEnable, "已激活");
                break;
        }

        //设备雨量站开关量
        switch (baseConfigInfo.getRainfallStation()) {
            case RAIN_CLOSE:
                mSpSwitch.setSelection(0);
                rainBreakAlarmLayout.setVisibility(View.GONE);
                break;

            case RAIN_OPEN://雨量站开启
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

                configDASActivity.sendCommonCommand("##2270\r\n");
                Timber.d("查询断线报警器参数指令==##2270");
                break;
        }

        String result = Double.valueOf(setRianAccuryParameter.getRainAccury()) / 100 + "mm";
        int count = rainAdapter.getCount();
        for (int i = 0; i < count; i++) {
            if (result.equals(rainAdapter.getItem(i))) {
                mSpRain.setSelection(i);
                break;
            }
        }
    }

    /**
     * 设置显示数据
     */
    private void setResultData(String cmdStr) {
        //锁定状态应答指令处理
        if (cmdStr.startsWith("$$225") && cmdStr.endsWith("\r\n")) {
            DeviceLockStatusSub deviceLockStatusSub = BlueResultParserUtil.getDeviceLockStatusInfo(cmdStr);
            lockStatus = (deviceLockStatusSub.getLockStatus() == 0) ? "unlock" : "lock";
            return;
        }

        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case RAIN_STATION://雨量计开关 0051：雨量计开启  0052：关闭   0053：断线报警器开启
                if (cmdStr.endsWith("e\r\n") || cmdStr.startsWith("$$ce\r\n")) {
                    ToastUtils.show("开关量配置错误!");
                    return;
                }
                break;
            case GET_ALL_SENSOR_CONFIG://所有配置信息 333
                processGetAllSensorConfig(cmdStr);
                break;

            case BREAK_ALARM_STATUS: //断线报警器状态 227
                if (cmdStr.endsWith("e\r\n") || cmdStr.startsWith("$$ce\r\n")) {
                    ToastUtils.show("断线报警器配置错误!");
                    return;
                }

                BreakAlarmStatusSub breakAlarmStatusSub = BlueResultParserUtil.getBreakAlarmStatus(cmdStr);
                Timber.d("--------断线报警器状态-------" + breakAlarmStatusSub.getAlarmStatus());
                breakAlarmStatusInfo.setStatus(BreakAlarmStatus.valueOf(breakAlarmStatusSub.getAlarmStatus()));
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
            case DAS_DEBUG_MODE:

                break;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getConfig(String messageEvent) {
        if (TextUtils.isEmpty(messageEvent) && !messageEvent.startsWith("$$")) {
            return;
        } else if (messageEvent.startsWith("$$005") || messageEvent.startsWith("$$333") || messageEvent.startsWith("$$227")) {
            setResultData(messageEvent);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BluetoothStateEvent bluetoothStateEvent) {
        setViewStateByConnectState(bluetoothStateEvent.isConnected);
    }


    private void setSwitchViewState(boolean isOpen, TextView textView, String content) {
        textView.setText(content);
        textView.setTextColor(isOpen ? getResources().getColor(R.color.colorPrimary) : getResources().getColor(R.color.gray_807B7B));
    }

    private void setViewStateByConnectState(boolean isConnected) {
        if (isConnected) {
            mSbBluetoothConnect.setCheckedImmediatelyNoEvent(true);
            setSwitchViewState(true, mTvBluetoothConnect, "已连接");

            mSpDebugMode.setEnabled(true);
            mSpSwitch.setEnabled(true);
            mSpRain.setEnabled(true);

        } else {
            mSbBluetoothConnect.setCheckedImmediatelyNoEvent(false);
            setSwitchViewState(false, mTvBluetoothConnect, "已断开");

            mSpDebugMode.setEnabled(false);
            mSpSwitch.setEnabled(false);
            mSpRain.setEnabled(false);
        }
    }


    /**
     * 关闭SwitchButton
     */
    public void showCloseSwitchButtonDialog(String content, final int index) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getActivity())
                .title("温馨提示：")
                .content(content)
                .contentColor(Color.parseColor("#000000"))
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
                                configDASActivity.sendCommonCommand("##0181\r\n");
                                setSwitchViewState(true, mTvDeviceEnable, "已待机");
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
                                mSbDeviceEnable.setCheckedImmediatelyNoEvent(true);
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
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @Override
    public boolean onBackPressed() {
        return false;
    }

}
