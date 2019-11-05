package com.shmedo.mcloudapp.ui.activity.device;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.das.das.cmd.CommandResult;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.bluetooth.MdBluetoothManager;
import com.shmedo.mcloudapp.bluetooth.Message;
import com.shmedo.mcloudapp.model.Extras;

import java.util.UUID;

import butterknife.BindView;
import timber.log.Timber;

public class ADMEExecutiveAgencyConfigActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

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

    @BindView(R.id.btn_confirm_complete)
    Button btnConfirm;

    private ImageView mIvDataSettlementMethod, mIvDataResponse, mIvWaitingIntervalPerRound, mIvLogOutputMode, mIvMotorDriveAddress, mIvMotorMovementTime, mIvMotorPullUpSpeed, mIvMotorPullDownSpeed, mIvTractionLineLength, mIvHoleDepth, mIvMeasuringPitch;

    private EditText mEtWaitingIntervalPerRound, mEtMotorDriveAddress, mEtMotorMovementTime, mEtMotorPullUpSpeed, mEtMotorPullDownSpeed, mEtTractionLineLength, mEtHoleDepth, mEtMeasuringPitch;

    private SwitchButton mSvDataResponse;

    private TextView mTvDataResponse;

    private String strWaitingIntervalPerRound, strMotorDriveAddress, strMotorMovementTime, strMotorPullUpSpeed, strMotorPullDownSpeed, strTractionLineLength, strHoleDepth, strMeasuringPitch;

    private Spinner mSpDataSettlementMethod, mSpLogOutputMode;

    private ArrayAdapter<String> dataSettlementAdapter, logOutputModeAdapter;

    private MdBluetoothManager mdBluetoothManager;

    private String executiveAgencyConfigInfo;//执行机构配置指令


    public static void startActivity(Context context, String configInfo) {
        Intent intent = new Intent(context, ADMEExecutiveAgencyConfigActivity.class);
        intent.putExtra(Extras.ADME_EXECUTIVE_AGENCY_CONFIG_INFO, configInfo);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_admeexecutive_agency_config;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        initView();
        parseIntent();
        initAdapter();
        initData();
    }

    private void initView() {
        mToolbarTitle.setText("执行机构参数配置");

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

        btnConfirm.setOnClickListener(this);
    }

    private void parseIntent() {
        mdBluetoothManager = MdBluetoothManager.getInstance();

        Intent intent = getIntent();
        if (intent.getExtras().containsKey(Extras.ADME_EXECUTIVE_AGENCY_CONFIG_INFO)) {
            executiveAgencyConfigInfo = intent.getStringExtra(Extras.ADME_EXECUTIVE_AGENCY_CONFIG_INFO);
        }
    }

    private void initAdapter() {
        String[] dataSettlementData = getResources().getStringArray(R.array.adme_executive_agency_data_settlement);
//        dataSettlementAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, debugData);
        dataSettlementAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dataSettlementData);
        dataSettlementAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpDataSettlementMethod.setAdapter(dataSettlementAdapter);
        mSpDataSettlementMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String result = mSpDataSettlementMethod.getSelectedItem().toString().replace("mm", "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        String[] logOutputData = getResources().getStringArray(R.array.adme_executive_agency_log_output);
        logOutputModeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, logOutputData);
        logOutputModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpLogOutputMode.setAdapter(logOutputModeAdapter);
        mSpLogOutputMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String result = mSpLogOutputMode.getSelectedItem().toString().replace("mm", "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    private void initData() {
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
            setSwitchViewState(true);
        } else if (cmdArray[2].equals("2")) {
            setSwitchViewState(false);
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
            mEtTractionLineLength.setText(String.format("%.1f", cmdArray[9]));
            mEtHoleDepth.setText(String.format("%.1f", cmdArray[10]));

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        mEtMeasuringPitch.setText(cmdArray[11]);
    }


    private void setSwitchViewState(boolean isOpen) {
        if (isOpen) {
            mSvDataResponse.setCheckedImmediatelyNoEvent(true);
            mTvDataResponse.setClickable(true);
            mTvDataResponse.setText("已启用");
            mTvDataResponse.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            mTvDataResponse.setTextColor(getResources().getColor(R.color.white));

        } else {
            mSvDataResponse.setCheckedImmediatelyNoEvent(false);
            mTvDataResponse.setClickable(false);
            mTvDataResponse.setText("已停用");
            mTvDataResponse.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            mTvDataResponse.setTextColor(getResources().getColor(R.color.gray_807B7B));
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
                    ToastUtils.show("设备已断开连接，暂无法进行设置");
                    return;
                }
                doConfirm();
                break;
        }
    }

    private void doConfirm() {
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
            return;
        }

        if (TextUtils.isEmpty(strMotorDriveAddress)) {
            ToastUtils.show("电机驱动器地址不能为空");
            return;
        }

        if (TextUtils.isEmpty(strMotorMovementTime)) {
            ToastUtils.show("电机运动时间不能为空");
            return;
        }

        if (TextUtils.isEmpty(strMotorPullUpSpeed)) {
            ToastUtils.show("电机上拉速度不能为空");
            return;
        }

        if (TextUtils.isEmpty(strMotorPullDownSpeed)) {
            ToastUtils.show("电机下拉速度不能为空");
            return;
        }

        if (TextUtils.isEmpty(strTractionLineLength)) {
            ToastUtils.show("牵引线长不能为空");
            return;
        }

        if (TextUtils.isEmpty(strHoleDepth)) {
            ToastUtils.show("测孔深不能为空");
            return;
        }

        if (TextUtils.isEmpty(strMeasuringPitch)) {
            ToastUtils.show("测量间距不能为空");
            return;
        }

        if (Integer.parseInt(strWaitingIntervalPerRound) < 0) {
            ToastUtils.show("采集器采集间隔必须输入正数");
            return;
        }


        if (Integer.parseInt(strWaitingIntervalPerRound) < 0) {
            ToastUtils.show("每轮等待间隔必须输入正数");
            return;
        }

        int address = Integer.parseInt(strMotorDriveAddress);
        if (address < 0 || address >= 255) {
            ToastUtils.show("电机驱动器地址输入有误");
            return;
        }

        if (Integer.parseInt(strMotorMovementTime) < 0) {
            ToastUtils.show("电机运动时间必须输入正数");
            return;
        }

        if (Integer.parseInt(strMotorPullUpSpeed) < 0) {
            ToastUtils.show("电机上拉速度必须输入正数");
            return;
        }

        if (Integer.parseInt(strMotorPullDownSpeed) < 0) {
            ToastUtils.show("电机下放速度必须输入正数");
            return;
        }

        if (Double.parseDouble(strTractionLineLength) < 0) {
            ToastUtils.show("牵引线长必须输入正数");
            return;
        }

        if (Double.parseDouble(strHoleDepth) < 0) {
            ToastUtils.show("测孔深度必须输入正数");
            return;
        }

        if (Integer.parseInt(strMeasuringPitch) < 0) {
            ToastUtils.show("测量间距必须输入正数");
            return;
        }


        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("##7003,");
        if (mSpDataSettlementMethod.getSelectedItemPosition() == 0) {
            stringBuilder.append("1,");
        } else if (mSpDataSettlementMethod.getSelectedItemPosition() == 0) {
            stringBuilder.append("2,");
        }

        if (mSvDataResponse.isChecked()) {
            stringBuilder.append("1,");
        } else {
            stringBuilder.append("2,");
        }

        stringBuilder.append(strWaitingIntervalPerRound + ",");

        if (mSpLogOutputMode.getSelectedItemPosition() == 0) {
            stringBuilder.append("1,");
        } else if (mSpLogOutputMode.getSelectedItemPosition() == 1) {
            stringBuilder.append("2,");
        } else if (mSpLogOutputMode.getSelectedItemPosition() == 2) {
            stringBuilder.append("3,");
        } else if (mSpLogOutputMode.getSelectedItemPosition() == 3) {
            stringBuilder.append("4,");
        }

        stringBuilder.append(strWaitingIntervalPerRound + ",");
        stringBuilder.append(strMotorDriveAddress + ",");
        stringBuilder.append(strMotorMovementTime + ",");
        stringBuilder.append(strMotorPullUpSpeed + ",");
        stringBuilder.append(strMotorPullDownSpeed + ",");
        stringBuilder.append(strTractionLineLength + ",");
        stringBuilder.append(strHoleDepth + ",");
        stringBuilder.append(strMeasuringPitch + "\r\n");

        String cmdStr = String.valueOf(stringBuilder);
        Message msg = new Message(UUID.randomUUID().toString(), cmdStr, true);
        mdBluetoothManager.writeMessage(msg);
        Timber.d("发送执行机构参数配置指令===" + cmdStr);
//        finish();
    }
}
