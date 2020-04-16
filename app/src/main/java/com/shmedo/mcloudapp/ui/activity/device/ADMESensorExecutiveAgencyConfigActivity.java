package com.shmedo.mcloudapp.ui.activity.device;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.core.cmd.CommandResult;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.event.BluetoothStateEvent;
import com.shmedo.mcloudapp.interfaces.Extras;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

import butterknife.BindView;
import timber.log.Timber;

public class ADMESensorExecutiveAgencyConfigActivity extends BaseDeviceConnectActivity implements View.OnClickListener {
    @BindView(R.id.back)
    ImageView mIvBack;

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.img_bluetooth)
    ImageView mIvBluetooth;

    @BindView(R.id.collector_address_layout)
    View collectorAddressLayout;

    @BindView(R.id.collector_collect_interval_layout)
    View collectorCollectIntervalLayout;

    @BindView(R.id.collector_solution_interval_layout)
    View collectorSolutionIntervalLayout;

    @BindView(R.id.communication_module_sleep_interval_layout)
    View communicationModuleSleepIntervalLayout;

    @BindView(R.id.sensor_type_layout)
    View sensorTypeLayout;

    @BindView(R.id.sensor_address_layout)
    View sensorAddressLayout;

    @BindView(R.id.sensor_correction_value_layout)
    View sensorCorrectionValueLayout;

    @BindView(R.id.btn_confirm_complete)
    Button btnConfirm;

    private ImageView mIvCollectorAddress, mIvCollectorCollectInterval, mIvCollectorSolutionInterval, mIvCommunicationModuleSleepInterval, mIvSensorType, mIvSensorAddress, mIvSensorCorrectionValue;

    private EditText mEtCollectorAddress, mEtCollectorCollectInterval, mEtCollectorSolutionInterval, mEtCommunicationModuleSleepInterval, mEtSensorType, mEtSensorAddress, mEtSensorCorrectionValue;

    private String collectorAddress, collectorCollectInterval, collectorSolutionInterval, communicationModuleSleepInterval, sensorType, sensorAddress, sensorCorrectionValue;


    @BindView(R.id.data_settlement_method_layout)
    View dataSettlementMethodLayout;

    @BindView(R.id.data_response_layout)
    View dataResponseLayout;

    @BindView(R.id.waiting_interval_per_round_layout)
    View waitingIntervalPerRoundLayout;

    @BindView(R.id.log_output_mode_layout)
    View logOutputModeLayout;

    @BindView(R.id.motor_drive_address_layout)
    View motorDriveAddressLayout;

    @BindView(R.id.motor_movement_time_layout)
    View motorMovementTimeLayout;

    @BindView(R.id.motor_pull_up_speed_layout)
    View motorPullUpSpeedLayout;

    @BindView(R.id.motor_pull_down_speed_layout)
    View motorPullDownSpeedLayout;

    @BindView(R.id.traction_line_length_layout)
    View tractionLineLengthLayout;

    @BindView(R.id.hole_depth_layout)
    View holeDepthLayout;

    @BindView(R.id.measuring_pitch_layout)
    View measuringPitchLayout;

    private ImageView mIvDataSettlementMethod, mIvDataResponse, mIvWaitingIntervalPerRound, mIvLogOutputMode, mIvMotorDriveAddress, mIvMotorMovementTime, mIvMotorPullUpSpeed, mIvMotorPullDownSpeed, mIvTractionLineLength, mIvHoleDepth, mIvMeasuringPitch;

    private EditText mEtWaitingIntervalPerRound, mEtMotorDriveAddress, mEtMotorMovementTime, mEtMotorPullUpSpeed, mEtMotorPullDownSpeed, mEtTractionLineLength, mEtHoleDepth, mEtMeasuringPitch;

    private SwitchButton mSvDataResponse;

    private TextView mTvDataResponse;

    private String strWaitingIntervalPerRound, strMotorDriveAddress, strMotorMovementTime, strMotorPullUpSpeed, strMotorPullDownSpeed, strTractionLineLength, strHoleDepth, strMeasuringPitch;

    private Spinner mSpDataSettlementMethod, mSpLogOutputMode;

    private ArrayAdapter<String> dataSettlementAdapter, logOutputModeAdapter;

    private String dagConfigInfo;//DAG 采集器配置指令

    private String executiveAgencyConfigInfo;//执行机构配置指令


    public static void startActivity(Context context, String sensorInfo, String executiveAgencyInfo) {
        Intent intent = new Intent(context, ADMESensorExecutiveAgencyConfigActivity.class);
        intent.putExtra(Extras.ADME_SENSOR_CONFIG_INFO, sensorInfo);
        intent.putExtra(Extras.ADME_EXECUTIVE_AGENCY_CONFIG_INFO, executiveAgencyInfo);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_admesensor_executive_agency_config;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initHeadView();
        initSensorView();
        initExecutiveAgencyView();
        parseIntent();
        initAdapter();
        initSensorData();
        initExecutiveAgencyData();
    }

    private void initHeadView() {
        mToolbarTitle.setText("参数设置");
        mIvBluetooth.setVisibility(View.GONE);
        mIvBack.setOnClickListener(this);
        mIvBluetooth.setOnClickListener(this);

        if (MCloudApp.isIsBluetoothDeviceConnected()) {
            mIvBluetooth.setImageResource(R.drawable.ic_bluetooth_connected);
        } else {
            mIvBluetooth.setImageResource(R.drawable.ic_bluetooth);
        }
    }


    private void initSensorView() {
        ((TextView) collectorAddressLayout.findViewById(R.id.itemNameTV)).setText("采集器地址");
        mIvCollectorAddress = collectorAddressLayout.findViewById(R.id.itemTipIV);
        mEtCollectorAddress = collectorAddressLayout.findViewById(R.id.itemValueET);
        mEtCollectorAddress.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtCollectorAddress.setHint("请输入正数...");
        mEtCollectorAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        ((TextView) collectorCollectIntervalLayout.findViewById(R.id.itemNameTV)).setText("采集器采集间隔（s）");
        mIvCollectorCollectInterval = collectorCollectIntervalLayout.findViewById(R.id.itemTipIV);
        mEtCollectorCollectInterval = collectorCollectIntervalLayout.findViewById(R.id.itemValueET);
        mEtCollectorCollectInterval.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtCollectorCollectInterval.setHint("请输入正数...");
        mEtCollectorCollectInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        ((TextView) collectorSolutionIntervalLayout.findViewById(R.id.itemNameTV)).setText("采集器解算间隔（s）");
        mIvCollectorSolutionInterval = collectorSolutionIntervalLayout.findViewById(R.id.itemTipIV);
        mEtCollectorSolutionInterval = collectorSolutionIntervalLayout.findViewById(R.id.itemValueET);
        mEtCollectorSolutionInterval.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtCollectorSolutionInterval.setHint("请输入正数...");
        mEtCollectorSolutionInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        ((TextView) communicationModuleSleepIntervalLayout.findViewById(R.id.itemNameTV)).setText("通讯模块休眠间隔（s）");
        mIvCommunicationModuleSleepInterval = communicationModuleSleepIntervalLayout.findViewById(R.id.itemTipIV);
        mEtCommunicationModuleSleepInterval = communicationModuleSleepIntervalLayout.findViewById(R.id.itemValueET);
        mEtCommunicationModuleSleepInterval.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtCommunicationModuleSleepInterval.setHint("请输入正数...");
        mEtCommunicationModuleSleepInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtCommunicationModuleSleepInterval.setText("5");

        ((TextView) sensorTypeLayout.findViewById(R.id.itemNameTV)).setText("传感器类型");
        mIvSensorType = sensorTypeLayout.findViewById(R.id.itemTipIV);
        mEtSensorType = sensorTypeLayout.findViewById(R.id.itemValueET);
        mEtSensorType.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtSensorType.setHint("请输入正数...");
        mEtSensorType.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        ((TextView) sensorAddressLayout.findViewById(R.id.itemNameTV)).setText("传感器地址");
        mIvSensorAddress = sensorAddressLayout.findViewById(R.id.itemTipIV);
        mEtSensorAddress = sensorAddressLayout.findViewById(R.id.itemValueET);
        mEtSensorAddress.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtSensorAddress.setHint("请输入正数...");
        mEtSensorAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        ((TextView) sensorCorrectionValueLayout.findViewById(R.id.itemNameTV)).setText("传感器修正值（mm）");
        mIvSensorCorrectionValue = sensorCorrectionValueLayout.findViewById(R.id.itemTipIV);
        mEtSensorCorrectionValue = sensorCorrectionValueLayout.findViewById(R.id.itemValueET);
        mEtSensorCorrectionValue.setInputType(InputType.TYPE_CLASS_PHONE);
        mEtSensorCorrectionValue.setHint("请输入两位正小数...");
        mEtSensorCorrectionValue.setText("0.00");
        mEtSensorCorrectionValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});

        mIvCollectorAddress.setId(R.id.collector_address);
        mIvCollectorCollectInterval.setId(R.id.collector_collect_interval);
        mIvCollectorSolutionInterval.setId(R.id.collector_solution_interval);
        mIvCommunicationModuleSleepInterval.setId(R.id.communication_module_sleep_interval);
        mIvSensorType.setId(R.id.sensor_type);
        mIvSensorAddress.setId(R.id.sensor_address);
        mIvSensorCorrectionValue.setId(R.id.sensor_correction_value);

        mIvCollectorAddress.setOnClickListener(this);
        mIvCollectorCollectInterval.setOnClickListener(this);
        mIvCollectorSolutionInterval.setOnClickListener(this);
        mIvCommunicationModuleSleepInterval.setOnClickListener(this);
        mIvSensorType.setOnClickListener(this);
        mIvSensorAddress.setOnClickListener(this);
        mIvSensorCorrectionValue.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
    }

    private void initExecutiveAgencyView() {
        ((TextView) dataSettlementMethodLayout.findViewById(R.id.itemNameTV)).setText("数据结算方式");
        mIvDataSettlementMethod = dataSettlementMethodLayout.findViewById(R.id.itemTipIV);
        mSpDataSettlementMethod = dataSettlementMethodLayout.findViewById(R.id.spinner);

        ((TextView) dataResponseLayout.findViewById(R.id.itemNameTV)).setText("数据应答");
        mIvDataResponse = dataResponseLayout.findViewById(R.id.itemTipIV);
        mTvDataResponse = dataResponseLayout.findViewById(R.id.tv_switch_state);
        mSvDataResponse = dataResponseLayout.findViewById(R.id.switchButton);

        ((TextView) waitingIntervalPerRoundLayout.findViewById(R.id.itemNameTV)).setText("每轮等待间隔（min）");
        mIvWaitingIntervalPerRound = waitingIntervalPerRoundLayout.findViewById(R.id.itemTipIV);
        mEtWaitingIntervalPerRound = waitingIntervalPerRoundLayout.findViewById(R.id.itemValueET);
        mEtWaitingIntervalPerRound.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtWaitingIntervalPerRound.setHint("请输入正数...");
        mEtWaitingIntervalPerRound.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        ((TextView) logOutputModeLayout.findViewById(R.id.itemNameTV)).setText("日志输出方式");
        mIvLogOutputMode = logOutputModeLayout.findViewById(R.id.itemTipIV);
        mSpLogOutputMode = logOutputModeLayout.findViewById(R.id.spinner);

        ((TextView) motorDriveAddressLayout.findViewById(R.id.itemNameTV)).setText("电机驱动器地址");
        mIvMotorDriveAddress = motorDriveAddressLayout.findViewById(R.id.itemTipIV);
        mEtMotorDriveAddress = motorDriveAddressLayout.findViewById(R.id.itemValueET);
        mEtMotorDriveAddress.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtMotorDriveAddress.setHint("请输入正数...");
        mEtMotorDriveAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        ((TextView) motorMovementTimeLayout.findViewById(R.id.itemNameTV)).setText("电机运动时间（s）");
        mIvMotorMovementTime = motorMovementTimeLayout.findViewById(R.id.itemTipIV);
        mEtMotorMovementTime = motorMovementTimeLayout.findViewById(R.id.itemValueET);
        mEtMotorMovementTime.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtMotorMovementTime.setHint("请输入正数...");
        mEtMotorMovementTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        ((TextView) motorPullUpSpeedLayout.findViewById(R.id.itemNameTV)).setText("电机上拉速度（r/s）");
        mIvMotorPullUpSpeed = motorPullUpSpeedLayout.findViewById(R.id.itemTipIV);
        mEtMotorPullUpSpeed = motorPullUpSpeedLayout.findViewById(R.id.itemValueET);
        mEtMotorPullUpSpeed.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtMotorPullUpSpeed.setHint("请输入正数...");
        mEtMotorPullUpSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        ((TextView) motorPullDownSpeedLayout.findViewById(R.id.itemNameTV)).setText("电机下拉速度（r/s）");
        mIvMotorPullDownSpeed = motorPullDownSpeedLayout.findViewById(R.id.itemTipIV);
        mEtMotorPullDownSpeed = motorPullDownSpeedLayout.findViewById(R.id.itemValueET);
        mEtMotorPullDownSpeed.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtMotorPullDownSpeed.setHint("请输入正数...");
        mEtMotorPullDownSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        ((TextView) tractionLineLengthLayout.findViewById(R.id.itemNameTV)).setText("牵引线长（m）");
        mIvTractionLineLength = tractionLineLengthLayout.findViewById(R.id.itemTipIV);
        mEtTractionLineLength = tractionLineLengthLayout.findViewById(R.id.itemValueET);
        mEtTractionLineLength.setInputType(InputType.TYPE_CLASS_PHONE);
        mEtTractionLineLength.setHint("请输入1位正小数...");
        mEtTractionLineLength.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});

        ((TextView) holeDepthLayout.findViewById(R.id.itemNameTV)).setText("测孔深（m）");
        mIvHoleDepth = holeDepthLayout.findViewById(R.id.itemTipIV);
        mEtHoleDepth = holeDepthLayout.findViewById(R.id.itemValueET);
        mEtHoleDepth.setInputType(InputType.TYPE_CLASS_PHONE);
        mEtHoleDepth.setHint("请输入1位正小数...");
        mEtHoleDepth.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});

        ((TextView) measuringPitchLayout.findViewById(R.id.itemNameTV)).setText("测量间距（mm）");
        mIvMeasuringPitch = measuringPitchLayout.findViewById(R.id.itemTipIV);
        mEtMeasuringPitch = measuringPitchLayout.findViewById(R.id.itemValueET);
        mEtMeasuringPitch.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtMeasuringPitch.setHint("请输入正数...");
        mEtMeasuringPitch.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        mIvDataSettlementMethod.setId(R.id.data_settlement_method);
        mIvDataResponse.setId(R.id.data_response);
        mIvWaitingIntervalPerRound.setId(R.id.waiting_interval_per_round);
        mIvLogOutputMode.setId(R.id.log_output_mode);
        mIvMotorDriveAddress.setId(R.id.motor_drive_address);
        mIvMotorMovementTime.setId(R.id.motor_movement_time);
        mIvMotorPullUpSpeed.setId(R.id.motor_pull_up_speed);
        mIvMotorPullDownSpeed.setId(R.id.motor_pull_down_speed);
        mIvTractionLineLength.setId(R.id.traction_line_length);
        mIvHoleDepth.setId(R.id.hole_depth);
        mIvMeasuringPitch.setId(R.id.measuring_pitch);

        mIvDataSettlementMethod.setOnClickListener(this);
        mIvDataResponse.setOnClickListener(this);
        mIvWaitingIntervalPerRound.setOnClickListener(this);
        mIvLogOutputMode.setOnClickListener(this);
        mIvMotorDriveAddress.setOnClickListener(this);
        mIvMotorMovementTime.setOnClickListener(this);
        mIvMotorPullUpSpeed.setOnClickListener(this);
        mIvMotorPullDownSpeed.setOnClickListener(this);
        mIvTractionLineLength.setOnClickListener(this);
        mIvHoleDepth.setOnClickListener(this);
        mIvMeasuringPitch.setOnClickListener(this);

        mSvDataResponse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setSwitchViewState(isChecked, mTvDataResponse, isChecked ? "已启用" : "已停用");
            }
        });
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() != null && intent.getExtras().containsKey(Extras.ADME_SENSOR_CONFIG_INFO)) {
            dagConfigInfo = intent.getStringExtra(Extras.ADME_SENSOR_CONFIG_INFO);
        }

        if (intent.getExtras() != null && intent.getExtras().containsKey(Extras.ADME_EXECUTIVE_AGENCY_CONFIG_INFO)) {
            executiveAgencyConfigInfo = intent.getStringExtra(Extras.ADME_EXECUTIVE_AGENCY_CONFIG_INFO);
        }
    }

    private void initAdapter() {
        String[] dataSettlementData = getResources().getStringArray(R.array.adme_executive_agency_data_settlement);
        dataSettlementAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dataSettlementData);
        dataSettlementAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpDataSettlementMethod.setAdapter(dataSettlementAdapter);

        String[] logOutputData = getResources().getStringArray(R.array.adme_executive_agency_log_output);
        logOutputModeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, logOutputData);
        logOutputModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpLogOutputMode.setAdapter(logOutputModeAdapter);
    }

    private void initSensorData() {
        if (TextUtils.isEmpty(dagConfigInfo)) {
            Timber.e("dagConfigInfo 为空或者null");
            return;
        }

        if (!dagConfigInfo.startsWith(CommandResult.COMMAND_RESULT_HEADER)) {
            Timber.e("dagConfigInfo 格式错误:" + dagConfigInfo);
            return;
        }

        String[] cmdArray = dagConfigInfo.replace("\r\n", "").split(",");
        if (cmdArray.length < 8) {
            Timber.e("dagConfigInfo 格式错误:" + dagConfigInfo);
            return;
        }

        mEtCollectorAddress.setText(cmdArray[1]);
        mEtCollectorCollectInterval.setText(cmdArray[2]);
        mEtCollectorSolutionInterval.setText(cmdArray[3]);
        mEtCommunicationModuleSleepInterval.setText(cmdArray[4]);
        mEtSensorType.setText(cmdArray[5]);
        mEtSensorAddress.setText(cmdArray[6]);

        try {
            mEtSensorCorrectionValue.setText(String.format(Locale.getDefault(),"%.2f", Double.parseDouble(cmdArray[7])));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initExecutiveAgencyData() {
        if (TextUtils.isEmpty(executiveAgencyConfigInfo)) {
            Timber.e("executiveAgencyConfigInfo 为空或者null");
            return;
        }

        if (executiveAgencyConfigInfo.length() < CommandResult.RESULT_MIN_LENGTH) {
            Timber.e("executiveAgencyConfigInfo 长度过短:" + executiveAgencyConfigInfo);
            return;
        }

        if (!executiveAgencyConfigInfo.startsWith(CommandResult.COMMAND_RESULT_HEADER)) {
            Timber.e("executiveAgencyConfigInfo 格式错误:" + executiveAgencyConfigInfo);
            return;
        }

        String[] cmdArray = executiveAgencyConfigInfo.replace("\r\n", "").split(",");
        if (cmdArray.length < 12) {
            Timber.e("executiveAgencyConfigInfo 格式错误:" + executiveAgencyConfigInfo);
            return;
        }

        if (cmdArray[1].equals("1")) {
            mSpDataSettlementMethod.setSelection(0);
        } else if (cmdArray[1].equals("2")) {
            mSpDataSettlementMethod.setSelection(1);
        }

        if (cmdArray[2].equals("1")) {
            mSvDataResponse.setCheckedImmediatelyNoEvent(true);
            setSwitchViewState(true, mTvDataResponse, "已启用");

        } else if (cmdArray[2].equals("2")) {
            mSvDataResponse.setCheckedImmediatelyNoEvent(false);
            setSwitchViewState(false, mTvDataResponse, "已停用");
        }

        mEtWaitingIntervalPerRound.setText(cmdArray[3]);

        if (cmdArray[4].equals("1")) {
            mSpLogOutputMode.setSelection(0);
        } else if (cmdArray[4].equals("2")) {
            mSpLogOutputMode.setSelection(1);
        } else if (cmdArray[4].equals("3")) {
            mSpLogOutputMode.setSelection(2);
        } else if (cmdArray[4].equals("4")) {
            mSpLogOutputMode.setSelection(3);
        }

        mEtMotorDriveAddress.setText(cmdArray[5]);
        mEtMotorMovementTime.setText(cmdArray[6]);
        mEtMotorPullUpSpeed.setText(cmdArray[7]);
        mEtMotorPullDownSpeed.setText(cmdArray[8]);

        try {
            mEtTractionLineLength.setText(String.format(Locale.getDefault(),"%.1f", Double.parseDouble(cmdArray[9])));
            mEtHoleDepth.setText(String.format(Locale.getDefault(),"%.1f", Double.parseDouble(cmdArray[10])));

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        mEtMeasuringPitch.setText(cmdArray[11]);
    }

    private void setSwitchViewState(boolean isOpen, TextView textView, String content) {
        textView.setText(content);
        textView.setTextColor(isOpen ? getResources().getColor(R.color.colorPrimary) : getResources().getColor(R.color.gray_807B7B));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;

            case R.id.img_bluetooth:
                if (MCloudApp.isIsBluetoothDeviceConnected()) {
                    if (isConfigChange) {
                        isExitMode = false;
                        showSaveDialog(getResources().getString(R.string.disconnect_bluetooth_device_save_param_warn));

                    } else {
                        disconnectDevice();
                    }

                } else {
                    findAndConnectBleDevice();
                }
                break;

            case R.id.collector_address:
                showTipDialog(getResources().getString(R.string.dag_collector_addres));
                break;

            case R.id.collector_collect_interval:
                showTipDialog(getResources().getString(R.string.dag_collector_collect_interval));
                break;

            case R.id.collector_solution_interval:
                showTipDialog(getResources().getString(R.string.dag_collector_solution_interval));
                break;

            case R.id.communication_module_sleep_interval:
                showTipDialog(getResources().getString(R.string.dag_communication_module_sleep_interval));
                break;

            case R.id.sensor_type:
                showTipDialog(getResources().getString(R.string.dag_sensor_type));
                break;

            case R.id.sensor_address:
                showTipDialog(getResources().getString(R.string.dag_sensor_address));
                break;

            case R.id.sensor_correction_value:
                showTipDialog(getResources().getString(R.string.dag_sensor_correction_value));
                break;

            case R.id.data_settlement_method:
                showTipDialog(getResources().getString(R.string.adme_data_settlement_method));
                break;

            case R.id.data_response:
                showTipDialog(getResources().getString(R.string.adme_data_response));
                break;

            case R.id.waiting_interval_per_round:
                showTipDialog(getResources().getString(R.string.adme_waiting_interval_per_round));
                break;

            case R.id.log_output_mode:
                showTipDialog(getResources().getString(R.string.adme_log_output_mode));
                break;

            case R.id.motor_drive_address:
                showTipDialog(getResources().getString(R.string.adme_motor_drive_address));
                break;

            case R.id.motor_movement_time:
                showTipDialog(getResources().getString(R.string.adme_motor_movement_time));
                break;

            case R.id.motor_pull_up_speed:
                showTipDialog(getResources().getString(R.string.adme_motor_pull_up_speed));
                break;

            case R.id.motor_pull_down_speed:
                showTipDialog(getResources().getString(R.string.adme_motor_pull_down_speed));
                break;

            case R.id.traction_line_length:
                showTipDialog(getResources().getString(R.string.adme_traction_line_length));
                break;

            case R.id.hole_depth:
                showTipDialog(getResources().getString(R.string.adme_hole_depth));
                break;

            case R.id.measuring_pitch:
                showTipDialog(getResources().getString(R.string.adme_measuring_pitch));
                break;

            case R.id.btn_confirm_complete:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    return;
                }
                doConfirm();
                break;
        }
    }


    private void doConfirm() {
        if (!checkSensorInput()) {
            return;
        }

        if (!checkExecutiveAgencyInput()) {
            return;
        }

        //拼接传感器参数配置指令
        StringBuilder sbCollector = new StringBuilder();
        sbCollector.append("##7001,");
        sbCollector.append(collectorAddress + ",");
        sbCollector.append(collectorCollectInterval + ",");
        sbCollector.append(collectorSolutionInterval + ",");
        sbCollector.append(communicationModuleSleepInterval + ",");
        sbCollector.append(sensorType + ",");
        sbCollector.append(sensorAddress + ",");
        sbCollector.append(sensorCorrectionValue + "\r\n");

        //拼接执行机构参数配置指令
        StringBuilder sbExecutiveAgency = new StringBuilder();
        sbExecutiveAgency.append("##7003,");
        if (mSpDataSettlementMethod.getSelectedItemPosition() == 0) {
            sbExecutiveAgency.append("1,");
        } else {
            sbExecutiveAgency.append("2,");
        }

        if (mSvDataResponse.isChecked()) {
            sbExecutiveAgency.append("1,");
        } else {
            sbExecutiveAgency.append("2,");
        }

        sbExecutiveAgency.append(strWaitingIntervalPerRound + ",");

        if (mSpLogOutputMode.getSelectedItemPosition() == 0) {
            sbExecutiveAgency.append("1,");
        } else if (mSpLogOutputMode.getSelectedItemPosition() == 1) {
            sbExecutiveAgency.append("2,");
        } else if (mSpLogOutputMode.getSelectedItemPosition() == 2) {
            sbExecutiveAgency.append("3,");
        } else if (mSpLogOutputMode.getSelectedItemPosition() == 3) {
            sbExecutiveAgency.append("4,");
        }

        sbExecutiveAgency.append(strMotorDriveAddress + ",");
        sbExecutiveAgency.append(strMotorMovementTime + ",");
        sbExecutiveAgency.append(strMotorPullUpSpeed + ",");
        sbExecutiveAgency.append(strMotorPullDownSpeed + ",");
        sbExecutiveAgency.append(strTractionLineLength + ",");
        sbExecutiveAgency.append(strHoleDepth + ",");
        sbExecutiveAgency.append(strMeasuringPitch + "\r\n");
        executiveAgencyConfigInfo = String.valueOf(sbExecutiveAgency);

        startProgressRunnable("正在发送配置指令...", 10000);
        //先发送采集器配置指令,收到配置完成应答时再发送执行结构配置指令
        String cmdStr = String.valueOf(sbCollector);
        sendCommonCommand(cmdStr);
    }


    /**
     * 设置显示数据
     */
    private void setResultData(String cmdStr) {
        //采集器参数配置后应答
        if (cmdStr.startsWith("$$7001") && cmdStr.endsWith("\r\n")) {
            sendCommonCommand(executiveAgencyConfigInfo);
            return;
        }

        //执行机构参数配置后应答
        if (cmdStr.startsWith("$$7003") && cmdStr.endsWith("\r\n")) {
            stopProgressRunnable();
            isExitMode = true;
            showSaveDialog("是否保存设备配置参数？");
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
        mIvBluetooth.setImageResource(bluetoothStateEvent.isConnected ? R.drawable.ic_bluetooth_connected : R.drawable.ic_bluetooth);
    }


    private boolean checkSensorInput() {
        collectorAddress = mEtCollectorAddress.getText().toString().trim();
        collectorCollectInterval = mEtCollectorCollectInterval.getText().toString().trim();
        collectorSolutionInterval = mEtCollectorSolutionInterval.getText().toString().trim();
        communicationModuleSleepInterval = mEtCommunicationModuleSleepInterval.getText().toString().trim();
        sensorType = mEtSensorType.getText().toString().trim();
        sensorAddress = mEtSensorAddress.getText().toString().trim();
        sensorCorrectionValue = mEtSensorCorrectionValue.getText().toString().trim();

        if (TextUtils.isEmpty(collectorAddress)) {
            ToastUtils.show("采集器地址不能为空");
            return false;
        }

        if (TextUtils.isEmpty(collectorCollectInterval)) {
            ToastUtils.show("采集器采集间隔不能为空");
            return false;
        }

        if (TextUtils.isEmpty(collectorSolutionInterval)) {
            ToastUtils.show("采集器解算间隔不能为空");
            return false;
        }

        if (TextUtils.isEmpty(communicationModuleSleepInterval)) {
            ToastUtils.show("通讯模块休眠间隔不能为空");
            return false;
        }

        if (TextUtils.isEmpty(sensorType)) {
            ToastUtils.show("传感器类型不能为空");
            return false;
        }

        if (TextUtils.isEmpty(sensorAddress)) {
            ToastUtils.show("传感器地址不能为空");
            return false;
        }

        if (TextUtils.isEmpty(sensorCorrectionValue)) {
            ToastUtils.show("传感器修正值不能为空");
            return false;
        }

        int address = Integer.parseInt(collectorAddress);
        if (address < 0 || address >= 255) {
            ToastUtils.show("采集器地址输入有误");
            return false;
        }

        if (Integer.parseInt(collectorCollectInterval) < 0) {
            ToastUtils.show("采集器采集间隔必须输入正数");
            return false;
        }

        if (Integer.parseInt(collectorSolutionInterval) < 0) {
            ToastUtils.show("采集器解算间隔必须输入正数");
            return false;
        }

        if (Integer.parseInt(collectorSolutionInterval) < Integer.parseInt(collectorCollectInterval)) {
            ToastUtils.show("采集器解算间隔必须大于等于采集间隔");
            return false;
        }

        if (Integer.parseInt(communicationModuleSleepInterval) < 0) {
            ToastUtils.show("通讯模块休眠间隔必须输入正数");
            return false;
        }

        if (Integer.parseInt(sensorType) < 0) {
            ToastUtils.show("传感器类型编号必须输入正数");
            return false;
        }

        address = Integer.parseInt(sensorAddress);
        if (address < 0 || address >= 255) {
            ToastUtils.show("传感器地址输入有误");
            return false;
        }

        if (Double.parseDouble(sensorCorrectionValue) < 0) {
            ToastUtils.show("传感器修正值必须输入正数");
            return false;
        }

        return true;
    }


    private boolean checkExecutiveAgencyInput() {
        strWaitingIntervalPerRound = mEtWaitingIntervalPerRound.getText().toString().trim();
        strMotorDriveAddress = mEtMotorDriveAddress.getText().toString().trim();
        strMotorMovementTime = mEtMotorMovementTime.getText().toString().trim();
        strMotorPullUpSpeed = mEtMotorPullUpSpeed.getText().toString().trim();
        strMotorPullDownSpeed = mEtMotorPullDownSpeed.getText().toString().trim();
        strTractionLineLength = mEtTractionLineLength.getText().toString().trim();
        strHoleDepth = mEtHoleDepth.getText().toString().trim();
        strMeasuringPitch = mEtMeasuringPitch.getText().toString().trim();

        if (TextUtils.isEmpty(strWaitingIntervalPerRound)) {
            ToastUtils.show("每轮等待间隔不能为空");
            return false;
        }

        if (TextUtils.isEmpty(strMotorDriveAddress)) {
            ToastUtils.show("电机驱动器地址不能为空");
            return false;
        }

        if (TextUtils.isEmpty(strMotorMovementTime)) {
            ToastUtils.show("电机运动时间不能为空");
            return false;

        }

        if (TextUtils.isEmpty(strMotorPullUpSpeed)) {
            ToastUtils.show("电机上拉速度不能为空");
            return false;
        }

        if (TextUtils.isEmpty(strMotorPullDownSpeed)) {
            ToastUtils.show("电机下拉速度不能为空");
            return false;
        }

        if (TextUtils.isEmpty(strTractionLineLength)) {
            ToastUtils.show("牵引线长不能为空");
            return false;
        }

        if (TextUtils.isEmpty(strHoleDepth)) {
            ToastUtils.show("测孔深不能为空");
            return false;
        }

        if (TextUtils.isEmpty(strMeasuringPitch)) {
            ToastUtils.show("测量间距不能为空");
            return false;
        }

        if (Integer.parseInt(strWaitingIntervalPerRound) < 0) {
            ToastUtils.show("采集器采集间隔必须输入正数");
            return false;
        }


        if (Integer.parseInt(strWaitingIntervalPerRound) < 0) {
            ToastUtils.show("每轮等待间隔必须输入正数");
            return false;
        }

        int address = Integer.parseInt(strMotorDriveAddress);
        if (address < 0 || address >= 255) {
            ToastUtils.show("电机驱动器地址输入有误");
            return false;
        }

        if (Integer.parseInt(strMotorMovementTime) < 0) {
            ToastUtils.show("电机运动时间必须输入正数");
            return false;

        }

        if (Integer.parseInt(strMotorPullUpSpeed) < 0) {
            ToastUtils.show("电机上拉速度必须输入正数");
            return false;

        }

        if (Integer.parseInt(strMotorPullDownSpeed) < 0) {
            ToastUtils.show("电机下放速度必须输入正数");
            return false;
        }

        if (Double.parseDouble(strTractionLineLength) < 0) {
            ToastUtils.show("牵引线长必须输入正数");
            return false;
        }

        if (Double.parseDouble(strHoleDepth) < 0) {
            ToastUtils.show("测孔深度必须输入正数");
            return false;
        }

        if (Integer.parseInt(strMeasuringPitch) < 0) {
            ToastUtils.show("测量间距必须输入正数");
            return false;
        }

        return true;
    }


    @Override
    public void onBackPressed() {
        if (MCloudApp.isIsBluetoothDeviceConnected()) {
            if (isConfigChange) {
                isExitMode = true;
                showSaveDialog(getResources().getString(R.string.disconnect_bluetooth_device_save_param_warn));
            } else {
                finish();
            }
        } else {
            finish();
        }
    }
}
