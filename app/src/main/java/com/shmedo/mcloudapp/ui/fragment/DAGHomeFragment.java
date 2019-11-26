package com.shmedo.mcloudapp.ui.fragment;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.das.common.*;
import com.shmedo.das.common.enumerate.*;
import com.shmedo.das.das.cmd.CommandManager;
import com.shmedo.das.das.cmd.CommandType;
import com.shmedo.das.utils.StringUtil;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseFragment;
import com.shmedo.mcloudapp.entity.SystemDataInfo;
import com.shmedo.mcloudapp.entity.SystemDataInfoDao;
import com.shmedo.mcloudapp.entity.ble.BreakAlarmStatusSub;
import com.shmedo.mcloudapp.entity.ble.DigitalOsmometerFunctionSub;
import com.shmedo.mcloudapp.entity.ble.RainStationSub;
import com.shmedo.mcloudapp.entity.ble.SettingRainPrecisionSub;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;
import com.shmedo.mcloudapp.entity.event.BluetoothStateEvent;
import com.shmedo.mcloudapp.model.Extras;
import com.shmedo.mcloudapp.ui.activity.ConfigDAGActivity;
import com.shmedo.mcloudapp.ui.activity.device.GeneralSettingActivity;
import com.shmedo.mcloudapp.ui.activity.device.OsmometerConfigActivity;
import com.shmedo.mcloudapp.ui.activity.device.RainConfigActivity;
import com.shmedo.mcloudapp.ui.activity.device.sensor.SenSorBGKConfigActivity;
import com.shmedo.mcloudapp.util.DaoManager;
import com.shmedo.mcloudapp.util.bleutil.BlueResultParserUtil;
import com.shmedo.mcloudapp.util.page.model.SetRainAccuryPage;
import com.shmedo.mcloudapp.util.page.model.SetRainSelectPage;
import com.shmedo.mcloudapp.views.editspinner.EditSpinner;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class DAGHomeFragment extends BaseFragment {
    private static final int BLUETOOTH_CONNECT = 0x0001;

    private static final int DEVICE_ENABLE = 0x0002;

    private static final int DEVICE_LOCK = 0x0003;

    private static final int DEBUG_MODEL = 0x0004;

    private static final int SIM_A = 0x0005;

    private static final int SIM_B = 0x0006;

    private static final int RAIN_GAUGE = 0x0007;

    private static final int OSMOMETER_CONFIG = 0x0008;

    private static final int BREAK_ALARM = 0x0009;

    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;

    @BindView(R.id.tv_device_model)
    TextView mTvDeviceModel;

    @BindView(R.id.tv_sensor_type)
    TextView mTvSensorType;

    @BindView(R.id.iv_lock)
    ImageView mIvLock;

    @BindView(R.id.tv_lock)
    TextView mTvLock;

    @BindView(R.id.editSpinner1)
    EditSpinner spinnerProjectName;

    @BindView(R.id.tv_projectName)
    TextView mTvProName;

    @BindView(R.id.bluetooth_connect_layout)
    View bluetoothConnectLayout;

    @BindView(R.id.device_enable_state_layout)
    View deviceEnableLayout;

    @BindView(R.id.device_lock_state_layout)
    View deviceLockLayout;

    @BindView(R.id.debug_model_layout)
    View debugModelLayout;

    @BindView(R.id.sim_A_layout)
    View simALayout;

    @BindView(R.id.sim_B_layout)
    View simBLayout;

    @BindView(R.id.break_alarm_layout)
    View breakAlarmLayout;

    @BindView(R.id.rain_gauge_layout)
    View rainGaugeLayout;

    @BindView(R.id.osmometer_config_layout)
    View osmometerConfigLayout;

    @BindView(R.id.sensor_setting_layout)
    View sensorSettingLayout;

    private TextView mTvBluetoothConnect, mTvDeviceEnable, mTvDeviceLock, mTvDebugMode, mTvSimA, mTvSimB, mTvOftenStatus;

    private SwitchButton mSbBluetoothConnect, mSbDeviceEnable, mSbDeviceLock, mSbDebugMode, mSbSimA, mSbSimB,
            mSbRainGauge, mSbOsmometer, mSbBleakAlarm;

    private Spinner mSpDebugMode, mSpOftenStatus;

    private Button mBtnRainGauge, mBtnOsmometer;

    private Unbinder unbinder;

    private ConfigDAGActivity configDAGActivity;

    private DaoManager manager = DaoManager.getInstance();

    private Handler hander;

    private List<String> systemDataInfoList = new ArrayList<>();//项目信息列表

    private HashMap<String, SystemDataInfo> systemDataInfoHashMap = new HashMap<>();

    private ArrayAdapter<String> debugModeAdapter;
    private ArrayAdapter<String> oftenStatusAdapter;

    private String collectorType = "";//采集器编号

    private String lockStatus = "";

    private SetRainAccuryPage.SetRianAccuryParameter setRianAccuryParameter = new SetRainAccuryPage.SetRianAccuryParameter();
    private SetRainSelectPage.SetSelectRainParameter setSelectRainParameter = new SetRainSelectPage.SetSelectRainParameter();
    private List<CollectorSensorParamsInfoSub> mCollectorParamsInfoSubList = new ArrayList<>();
    private CollectorConfigInfo collectorConfigInfo;
    private BaseConfigInfo baseConfigInfo;
    private QueryOsmometerParameterInfo queryOsmometerParameterInfo;
    private BreakAlarmStatusInfo breakAlarmStatusInfo = new BreakAlarmStatusInfo();


    private Runnable dismssDialogRunnable = new Runnable() {
        @Override
        public void run() {
            dismissLoadingDialog();
            ToastUtils.show("发送指令超时,请稍后尝试");
        }
    };

    @Override
    protected int initContentView() {
        return R.layout.fragment_daghome;
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
        queryProjectList();
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
            mTvSensorType.setText("拉线位移计");
        }
        configDAGActivity = (ConfigDAGActivity) getActivity();
        hander = new Handler();
    }


    private void initView() {
        ((TextView) bluetoothConnectLayout.findViewById(R.id.tv_config_name)).setText("蓝牙连接");
        mTvBluetoothConnect = bluetoothConnectLayout.findViewById(R.id.tv_device_state);
        mSbBluetoothConnect = bluetoothConnectLayout.findViewById(R.id.switchButton);

        ((TextView) deviceEnableLayout.findViewById(R.id.tv_config_name)).setText("设备启用状态");
        mTvDeviceEnable = deviceEnableLayout.findViewById(R.id.tv_device_state);
        mSbDeviceEnable = deviceEnableLayout.findViewById(R.id.switchButton);

        ((TextView) deviceLockLayout.findViewById(R.id.tv_config_name)).setText("设备锁定状态");
        mTvDeviceLock = deviceLockLayout.findViewById(R.id.tv_device_state);
        mSbDeviceLock = deviceLockLayout.findViewById(R.id.switchButton);

        ((TextView) debugModelLayout.findViewById(R.id.tv_config_name)).setText("调试模式");
        mTvDebugMode = debugModelLayout.findViewById(R.id.tv_device_state);
        mSbDebugMode = debugModelLayout.findViewById(R.id.switchButton);
        mSpDebugMode = debugModelLayout.findViewById(R.id.spinner);

        ((TextView) simALayout.findViewById(R.id.tv_config_name)).setText("SIM卡A功能");
        mTvSimA = simALayout.findViewById(R.id.tv_device_state);
        mSbSimA = simALayout.findViewById(R.id.switchButton);

        ((TextView) simBLayout.findViewById(R.id.tv_config_name)).setText("SIM卡B功能");
        mTvSimB = simBLayout.findViewById(R.id.tv_device_state);
        mSbSimB = simBLayout.findViewById(R.id.switchButton);

        ((TextView) rainGaugeLayout.findViewById(R.id.tv_config_name)).setText("雨量计功能");
        mBtnRainGauge = rainGaugeLayout.findViewById(R.id.btn_config);
        mSbRainGauge = rainGaugeLayout.findViewById(R.id.switchButton);

        ((TextView) osmometerConfigLayout.findViewById(R.id.tv_config_name)).setText("渗压计功能");
        mBtnOsmometer = osmometerConfigLayout.findViewById(R.id.btn_config);
        mSbOsmometer = osmometerConfigLayout.findViewById(R.id.switchButton);

        ((TextView) breakAlarmLayout.findViewById(R.id.tv_config_name)).setText("断线报警器");
        mTvOftenStatus = breakAlarmLayout.findViewById(R.id.tv_device_state);
        mSbBleakAlarm = breakAlarmLayout.findViewById(R.id.switchButton);
        mSpOftenStatus = breakAlarmLayout.findViewById(R.id.spinner);

        ((TextView) sensorSettingLayout.findViewById(R.id.tv_config_name)).setText("传感器参数配置");

        //TODO 需要查询接口确定设备所属项目
        mTvProName.setText("xxxx 项目");
        if (mTvLock.getText().equals("已锁定")) {
            mIvLock.setImageResource(R.drawable.icon_close_lock);
            mTvProName.setVisibility(View.VISIBLE);
            spinnerProjectName.setVisibility(View.GONE);

        } else if (mTvLock.getText().equals("已解锁")) {
            mIvLock.setImageResource(R.drawable.icon_open_lock);
            mTvProName.setVisibility(View.GONE);
            spinnerProjectName.setVisibility(View.VISIBLE);
        }


        mBtnRainGauge.setEnabled(false);
        mBtnOsmometer.setEnabled(false);
        mBtnRainGauge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RainConfigActivity.startActivity(configDAGActivity, setRianAccuryParameter.getRainAccury());
            }
        });

        mBtnOsmometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OsmometerConfigActivity.startActivity(configDAGActivity);
            }
        });
    }

    /**
     * switch按钮事件
     */
    private void setSwitchViewListener() {
        //连接蓝牙开关
        mSbBluetoothConnect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                //未连接时，直接打开连接
                if (isChecked) {
                    configDAGActivity.findAndConnectBleDevice();

                } else {//断开连接处理
                    if (configDAGActivity.isConfigChange) {
                        configDAGActivity.isExitMode = false;
                        configDAGActivity.showSaveDialog(getResources().getString(R.string.disconnect_bluetooth_device_save_param_warn));
                    } else {
                        configDAGActivity.disconnectDevice();
//                        MCloudApp.setIsBluetoothDeviceConnected(false);
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
                    configDAGActivity.sendCommonCommand("##0182\r\n");
                    setSwitchViewState(true, mTvDeviceEnable, "已激活");
                } else {
                    showCloseSwitchButtonDialog(getString(R.string.device_enable_state_close_warn), DEVICE_ENABLE);
                }
            }
        });

        //设备锁定状态开关
        mSbDeviceLock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    mSbDeviceLock.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                if (isChecked) {
                    //发送锁定命令
                    configDAGActivity.sendCommonCommand("##2250\r\n");
                    setSwitchViewState(true, mTvDeviceLock, "未锁定");
                } else {
                    showCloseSwitchButtonDialog(getString(R.string.device_enable_state_close_warn), DEVICE_LOCK);
                }
            }
        });

        //调试模式开关
        mSbDebugMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    mSbDebugMode.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                if (isChecked) {
                    //发送打开自动测量模式命令
                    configDAGActivity.sendCommonCommand("##70111\r\n");
                    setSwitchViewState(true, mTvDebugMode, "已启用");
                } else {
                    showCloseSwitchButtonDialog("关闭自动监测，将导致设备自动关机进入休眠状态。请确认是否关闭", DEBUG_MODEL);
                }
            }
        });

        //SimA开关
        mSbSimA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    mSbSimA.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
            }
        });

        //mSbSimB开关
        mSbSimB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    mSbSimB.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
            }
        });

        //雨量计开关
        mSbRainGauge.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    mSbRainGauge.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }
                if (isChecked) {
                    configDAGActivity.sendCommonCommand("##0051\r\n");
                    mBtnRainGauge.setEnabled(true);
                    mBtnRainGauge.setText("配置");
                } else {
                    showCloseSwitchButtonDialog("确认要关闭雨量计？", RAIN_GAUGE);
                }
            }
        });

        //渗压计开关
        mSbOsmometer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    mSbOsmometer.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }

                if (isChecked) {
                    //发送打开自动测量模式命令
                    configDAGActivity.sendCommonCommand("##4011\r\n");
                    mBtnOsmometer.setEnabled(true);
                    mBtnOsmometer.setText("配置");
                } else {
                    showCloseSwitchButtonDialog("确认要关闭渗压计？", OSMOMETER_CONFIG);
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
                    //发送打开
                    configDAGActivity.sendCommonCommand("##0053\r\n");
                    setSwitchViewState(true, mTvOftenStatus, "已启用");
                } else {
                    showCloseSwitchButtonDialog("请确认是否关闭断线报警器？", BREAK_ALARM);
                }
            }
        });
    }

    private void initAdapter() {
        spinnerProjectName.setItemData(systemDataInfoList);
        spinnerProjectName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != parent.getAdapter()) {
                    String projectName = spinnerProjectName.getText();
                    mTvProName.setText(projectName);
                }
            }
        });

        //调试模式
        String[] debugData = getResources().getStringArray(R.array.bluetooth_debug);
        debugModeAdapter = new ArrayAdapter<>(configDAGActivity, android.R.layout.simple_spinner_item, debugData);
        debugModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpDebugMode.setAdapter(debugModeAdapter);

        //断线报警器状态
        String[] alarmData = getResources().getStringArray(R.array.break_alarm_status);
        oftenStatusAdapter = new ArrayAdapter<>(configDAGActivity, android.R.layout.simple_spinner_item, alarmData);
        oftenStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpOftenStatus.setAdapter(oftenStatusAdapter);

        //禁止OnItemSelectedListener默认自动调用一次
        mSpOftenStatus.setSelection(0, true);
        mSpOftenStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String status = parent.getSelectedItem().toString();
                if (status.equals("常开")){
                    //发送断线报警器常开指令
                    configDAGActivity.sendCommonCommand("##2271\r\n");
                    Timber.i( "发送断线报警器常开指令==##2271");
                }else if (status.equals("常闭")){
                    //发送断线报警器常闭指令
                    configDAGActivity.sendCommonCommand("##2272\r\n");
                    Timber.i("发送断线报警器常闭指令==##2272");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void updateView() {
        //设备启用状态
        mSbDeviceEnable.setCheckedImmediatelyNoEvent(baseConfigInfo.getEquipmentStatus() == EquipmentStatus.ACTIVATION);
        setSwitchViewState(baseConfigInfo.getEquipmentStatus() == EquipmentStatus.ACTIVATION, mTvDeviceEnable, baseConfigInfo.getEquipmentStatus() == EquipmentStatus.ACTIVATION ? "已激活" : "待机");
        //设备锁定状态
        mSbDeviceLock.setCheckedImmediatelyNoEvent(lockStatus.equals("unlock"));
        setSwitchViewState(lockStatus.equals("unlock"), mTvDeviceLock, lockStatus.equals("unlock") ? "未锁定" : "锁定");
        //设备调试模式
        switch (baseConfigInfo.getDebugModel()) {
            case INITIALZE:
                mSbDebugMode.setCheckedImmediatelyNoEvent(true);
                break;
            case CLOSE:
                mSbDebugMode.setCheckedImmediatelyNoEvent(false);
                break;
            case INFO:
                mSbDebugMode.setCheckedImmediatelyNoEvent(true);
                mSpDebugMode.setSelection(1);
                break;
            case DEBUG:
                mSbDebugMode.setCheckedImmediatelyNoEvent(true);
                mSpDebugMode.setSelection(0);
                break;
        }

        //选择SIM卡功能
        if (baseConfigInfo.getSIMChoose() == SIMChoose.SIM2) {
            mSbSimA.setCheckedImmediatelyNoEvent(false);
            mSbSimB.setCheckedImmediatelyNoEvent(true);
        } else {
            mSbSimA.setCheckedImmediatelyNoEvent(true);
            mSbSimB.setCheckedImmediatelyNoEvent(false);
        }


        //雨量计开关
        mSbRainGauge.setCheckedImmediatelyNoEvent(baseConfigInfo.getRainfallStation() == RainfallStation.RAIN_OPEN);
        mBtnRainGauge.setEnabled(baseConfigInfo.getRainfallStation() == RainfallStation.RAIN_OPEN);
        mBtnRainGauge.setText(baseConfigInfo.getRainfallStation() == RainfallStation.RAIN_OPEN ? "配置" : "已停用");
    }


    @OnClick({R.id.iv_lock, R.id.sensor_setting_layout, R.id.rl_general_setting})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_lock:
                if (mTvLock.getText().equals("已锁定")) {
                    mTvLock.setText("已解锁");
                    mIvLock.setImageResource(R.drawable.icon_open_lock);
                    mTvProName.setVisibility(View.GONE);
                    spinnerProjectName.setVisibility(View.VISIBLE);

                } else if (mTvLock.getText().equals("已解锁")) {
                    mTvLock.setText("已锁定");
                    mIvLock.setImageResource(R.drawable.icon_close_lock);
                    mTvProName.setVisibility(View.VISIBLE);
                    spinnerProjectName.setVisibility(View.GONE);
                }
                break;

            case R.id.sensor_setting_layout:
                SenSorBGKConfigActivity.startActivity(configDAGActivity);
                break;

            case R.id.rl_general_setting:
                GeneralSettingActivity.startActivity(configDAGActivity, collectorConfigInfo, collectorType);
                break;
        }
    }


    /**
     * 设置显示数据
     */
    private void setResultData(String cmdStr) {
        //TODO  锁定状态处理

        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case SYSTEM_RUN_STATE://运行系统状态 014
                //systemRunStateSub = BlueResultParserUtil.getSystemRunState(cmdStr);
                break;

            case SETTING_RAIN_PRECISION://设置雨量计精度 121
                SettingRainPrecisionSub settingRainPrecisionSub = BlueResultParserUtil.getRainPrecisionInfo(cmdStr);
                Timber.d("--------设置雨量计精度-------" + settingRainPrecisionSub.toString());
                setRianAccuryParameter.setRainAccury(String.valueOf(settingRainPrecisionSub.getPrecision()));
                break;

            case RAIN_STATION://雨量计开关 005
                RainStationSub rainStationSub = BlueResultParserUtil.getRainStationInfo(cmdStr);
                Timber.d("--------雨量计开关状态-------" + rainStationSub.getRainStation());
                setSelectRainParameter.setRainSelect(rainStationSub.getRainStation());
                break;

            case QUERY_OSMOMETER_PARAMETER://查询数字式渗压计参数 400
                queryOsmometerParameterInfo = BlueResultParserUtil.getQueryOsmometerParameterInfo(cmdStr);
                Timber.d("--------查询数字式渗压计参数-------" + queryOsmometerParameterInfo.toString());

                //渗压计开关
                mSbOsmometer.setCheckedImmediatelyNoEvent(queryOsmometerParameterInfo.getOsmometerStatus() == OsmometerStatus.OSMOMETER_OPEN);
                mBtnOsmometer.setEnabled(queryOsmometerParameterInfo.getOsmometerStatus() == OsmometerStatus.OSMOMETER_OPEN);
                mBtnOsmometer.setText(queryOsmometerParameterInfo.getOsmometerStatus() == OsmometerStatus.OSMOMETER_OPEN ? "配置" : "已停用");
                break;

            case DIGITAL_OSMOMETER_FUNCTION://开启/关闭数字式渗压计功能 401
                DigitalOsmometerFunctionSub digitalOsmometerFunctionSub = BlueResultParserUtil.getOsmoeterFunctionInfo(cmdStr);
                Timber.d("--------开启/关闭数字式渗压计功能-------" + digitalOsmometerFunctionSub.toString());

                queryOsmometerParameterInfo.setOsmometerStatus(OsmometerStatus.valueOf(digitalOsmometerFunctionSub.getOsmometerStatus()));
                break;

            case COLLECTOR_CONFIG://获取采集器配置 100
                collectorConfigInfo = BlueResultParserUtil.getCollectorConfigInfo(cmdStr);
                Timber.d("--------获取采集器配置-------" + collectorConfigInfo.toString());

                send101Instruction(collectorConfigInfo);//发送101指令
                break;

            case COLLECTOR_CHANNEL_SENSOR_PARAMETER: //101
                CollectorSensorParamsInfoSub mCollectorParamsInfoSub = BlueResultParserUtil.setCollectorParams(cmdStr);
                Timber.d("--------101指令-------" + mCollectorParamsInfoSub.toString());
                mCollectorParamsInfoSubList.add(mCollectorParamsInfoSub);
                Timber.d("size=" + mCollectorParamsInfoSubList.size() + "--------获取XX采集器YY通道的传感器参数-------" + mCollectorParamsInfoSub.toString());
                break;

            case GET_ALL_SENSOR_CONFIG://所有配置信息 333
                GetAllSensorConfigInfo getAllSensorConfigInfo = BlueResultParserUtil.getAllBlueMessage(cmdStr);
                Timber.d("--------所有配置信息-------" + getAllSensorConfigInfo.toString());

                processGetAllSensorConfig(getAllSensorConfigInfo);
                updateView();
                break;
            case BREAK_ALARM_STATUS: //断线报警器状态 227
                BreakAlarmStatusSub breakAlarmStatusSub = BlueResultParserUtil.getBreakAlarmStatus(cmdStr);
                Timber.d("--------断线报警器状态-------" +breakAlarmStatusSub.getAlarmStatus());
                breakAlarmStatusInfo.setStatus(BreakAlarmStatus.valueOf(breakAlarmStatusSub.getAlarmStatus()));
                switch (breakAlarmStatusInfo.getStatus()){
                    case OPEN:
                        mSpOftenStatus.setSelection(0);
                        break;
                    case CLOSE:
                        mSpOftenStatus.setSelection(1);
                        break;
                }
                break;
        }
    }

    /**
     * 所有配置信息处理
     */
    private void processGetAllSensorConfig(GetAllSensorConfigInfo getAllSensorConfigInfo) {
        if (getAllSensorConfigInfo == null)
            return;

        collectorConfigInfo = getAllSensorConfigInfo.getCollectorConfig();
        baseConfigInfo = getAllSensorConfigInfo.getBaseConfig();
        if (baseConfigInfo != null) {
            switch (baseConfigInfo.getRainfallStation()) {
                case RAIN_OPEN:
                    setSelectRainParameter.setRainSelect("1");
                    break;
                case RAIN_CLOSE:
                    setSelectRainParameter.setRainSelect("2");
                    break;
                case ALARM_OPEN:
                    setSelectRainParameter.setRainSelect("3");
                    break;
            }
            setRianAccuryParameter.setRainAccury(String.valueOf(baseConfigInfo.getRainAccuracy() / 100));
            collectorType = baseConfigInfo.getCollectorModel().toString();
        }

        if (!TextUtils.isEmpty(collectorType)) {
            //根据采集器型号获取采集器配置 ##100 02
            String collectorCommand = CommandManager.getInstance().getCommand(CommandType.COLLECTOR_CONFIG, null);
            String collectorResult = collectorCommand.replace("\r\n", "") + collectorType + "\r\n";
            Timber.d("发送获取采集器配置信息指令===" + collectorResult);
            configDAGActivity.sendCommonCommand(collectorResult);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getConfig(String messageEvent) {
        if (!TextUtils.isEmpty(messageEvent) && messageEvent.startsWith("$$")) {
            setResultData(messageEvent);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BluetoothStateEvent bluetoothStateEvent) {
        setViewStateByConnectState(bluetoothStateEvent.isConnected);
    }


    /**
     * 发送101指令
     *
     * @param collectorInfoSub
     */
    private void send101Instruction(CollectorConfigInfo collectorInfoSub) {
        for (int i = 0; i < collectorInfoSub.getAccessSum(); i++) {
            //##101XXYY\r\n：获取XX采集器YY通道的传感器参数
            String collectorCommand = CommandManager.getInstance().getCommand(CommandType.COLLECTOR_CHANNEL_SENSOR_PARAMETER, null);
            String count = com.shmedo.mcloudapp.util.StringUtil.formatTwo(i);
            String collectorResult = collectorCommand.replace("\r\n", "") + collectorType + count + "\r\n";
            configDAGActivity.sendCommonCommand(collectorResult);
            Timber.d("发送101采集器配置指令===" + collectorResult);
        }
    }


    private void setSwitchViewState(boolean isOpen, TextView textView, String content) {
        textView.setText(content);
        textView.setTextColor(isOpen ? getResources().getColor(R.color.colorPrimary) : getResources().getColor(R.color.gray_807B7B));
    }

    private void setViewStateByConnectState(boolean isConnected) {
        if (isConnected) {
            mSbBluetoothConnect.setCheckedImmediatelyNoEvent(true);
            setSwitchViewState(true, mTvBluetoothConnect, "已连接");

//            mBtnEdit1.setEnabled(true);

        } else {
            mSbBluetoothConnect.setCheckedImmediatelyNoEvent(false);
            setSwitchViewState(false, mTvBluetoothConnect, "已断开");

//            sbAutoMonitorState.setCheckedImmediatelyNoEvent(false);
//            setSwitchViewState(false, tvAutoMonitorState, "已关闭");
//
//            mBtnEdit1.setEnabled(false);
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
                                configDAGActivity.sendCommonCommand("##0181\r\n");
                                setSwitchViewState(true, mTvDeviceEnable, "已待机");
                                break;

                            case DEVICE_LOCK:
                                //发送锁定命令
                                configDAGActivity.sendCommonCommand("##2251\r\n");
                                setSwitchViewState(false, mTvDeviceLock, "锁定");
                                break;

                            case DEBUG_MODEL:
                                //发送关闭测试模式命令
                                configDAGActivity.sendCommonCommand("##70122\r\n");
//                                setSwitchViewState(false, tvDebugMode, "已关闭");
                                break;

                            case RAIN_GAUGE:
                                //发送关闭雨量计命令
                                configDAGActivity.sendCommonCommand("##0052\r\n");
                                mBtnRainGauge.setEnabled(false);
                                mBtnRainGauge.setText("已停用");
                                break;

                            case OSMOMETER_CONFIG:
                                //发送关闭渗压计命令
                                configDAGActivity.sendCommonCommand("##4012\r\n");
                                mBtnOsmometer.setEnabled(false);
                                mBtnOsmometer.setText("已停用");
                                break;
                            case BREAK_ALARM:
                                configDAGActivity.sendCommonCommand("##0052\r\n");
                                setSwitchViewState(true, mTvOftenStatus, "已关闭");
                                break;
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

                            case DEVICE_LOCK:
                                mSbDeviceLock.setCheckedImmediatelyNoEvent(true);
                                break;

                            case DEBUG_MODEL:
                                mSbDebugMode.setCheckedImmediatelyNoEvent(true);
                                break;

                            case RAIN_GAUGE:
                                mSbRainGauge.setCheckedImmediatelyNoEvent(true);
                                break;

                            case OSMOMETER_CONFIG:
                                mSbOsmometer.setCheckedImmediatelyNoEvent(true);
                                break;
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    /**
     * 查询本地数据库中项目信息
     */
    private void queryProjectList() {
        List<SystemDataInfo> infoList = manager.getDaoSession().getSystemDataInfoDao().queryBuilder()
                .where(SystemDataInfoDao.Properties.Account.isNotNull(), SystemDataInfoDao.Properties.Account.eq(MCloudApp.getAccount()))
                .list();

        systemDataInfoList.clear();
        systemDataInfoHashMap.clear();
        if (null != infoList && infoList.size() > 0) {
            for (SystemDataInfo systemDataInfo : infoList) {
                systemDataInfoList.add(systemDataInfo.getProjName());
                systemDataInfoHashMap.put(systemDataInfo.getProjName(), systemDataInfo);
            }
        }
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
