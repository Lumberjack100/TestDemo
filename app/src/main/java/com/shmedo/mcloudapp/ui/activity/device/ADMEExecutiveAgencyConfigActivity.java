package com.shmedo.mcloudapp.ui.activity.device;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.bluetooth.Message;
import com.shmedo.mcloudapp.ui.activity.ConfigADMEActivity;
import com.shmedo.mcloudapp.ui.fragment.DeviceFragment;
import com.shmedo.mcloudapp.util.ToastUtil;

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

    private TextView mTvDataResponse;

    private String strDataSettlementMethod, strDataResponse, strWaitingIntervalPerRound, strLogOutputMode, strMotorDriveAddress, strMotorMovementTime, strMotorPullUpSpeed, strMotorPullDownSpeed, strTractionLineLength, strHoleDepth, strMeasuringPitch;

    private Spinner mSpDataSettlementMethod, mSpLogOutputMode;

    private ArrayAdapter<String> dataAdapter;


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ADMEExecutiveAgencyConfigActivity.class);
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

        ((TextView) waitingIntervalPerRoundLayout.findViewById(R.id.itemNameTV)).setText("每轮等待间隔（min）");
        mIvWaitingIntervalPerRound = waitingIntervalPerRoundLayout.findViewById(R.id.itemTipIV);
        mEtWaitingIntervalPerRound = waitingIntervalPerRoundLayout.findViewById(R.id.itemValueET);
        mEtWaitingIntervalPerRound.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtWaitingIntervalPerRound.setHint("请输入...");

        ((TextView) logOutputModeLayout.findViewById(R.id.itemNameTV)).setText("日志输出方式");
        mIvLogOutputMode = logOutputModeLayout.findViewById(R.id.itemTipIV);
        mSpLogOutputMode = logOutputModeLayout.findViewById(R.id.spinner);

        ((TextView) motorDriveAddressLayout.findViewById(R.id.itemNameTV)).setText("电机驱动器地址");
        mIvMotorDriveAddress = motorDriveAddressLayout.findViewById(R.id.itemTipIV);
        mEtMotorDriveAddress = motorDriveAddressLayout.findViewById(R.id.itemValueET);
        mEtMotorDriveAddress.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtMotorDriveAddress.setHint("请输入...");

        ((TextView) motorMovementTimeLayout.findViewById(R.id.itemNameTV)).setText("电机运动时间（s）");
        mIvMotorMovementTime = motorMovementTimeLayout.findViewById(R.id.itemTipIV);
        mEtMotorMovementTime = motorMovementTimeLayout.findViewById(R.id.itemValueET);
        mEtMotorMovementTime.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtMotorMovementTime.setHint("请输入...");

        ((TextView) motorPullUpSpeedLayout.findViewById(R.id.itemNameTV)).setText("电机上拉速度（r/s）");
        mIvMotorPullUpSpeed = motorPullUpSpeedLayout.findViewById(R.id.itemTipIV);
        mEtMotorPullUpSpeed = motorPullUpSpeedLayout.findViewById(R.id.itemValueET);
        mEtMotorPullUpSpeed.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtMotorPullUpSpeed.setHint("请输入...");

        ((TextView) motorPullDownSpeedLayout.findViewById(R.id.itemNameTV)).setText("电机下拉速度（r/s）");
        mIvMotorPullDownSpeed = motorPullDownSpeedLayout.findViewById(R.id.itemTipIV);
        mEtMotorPullDownSpeed = motorPullDownSpeedLayout.findViewById(R.id.itemValueET);
        mEtMotorPullDownSpeed.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtMotorPullDownSpeed.setHint("请输入...");

        ((TextView) tractionLineLengthLayout.findViewById(R.id.itemNameTV)).setText("牵引线长（m）");
        mIvTractionLineLength = tractionLineLengthLayout.findViewById(R.id.itemTipIV);
        mEtTractionLineLength = tractionLineLengthLayout.findViewById(R.id.itemValueET);
        mEtTractionLineLength.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtTractionLineLength.setHint("请输入...");

        ((TextView) holeDepthLayout.findViewById(R.id.itemNameTV)).setText("测孔深（m）");
        mIvHoleDepth = holeDepthLayout.findViewById(R.id.itemTipIV);
        mEtHoleDepth = holeDepthLayout.findViewById(R.id.itemValueET);
        mEtHoleDepth.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtHoleDepth.setHint("请输入...");

        ((TextView) measuringPitchLayout.findViewById(R.id.itemNameTV)).setText("测量间距（mm）");
        mIvMeasuringPitch = measuringPitchLayout.findViewById(R.id.itemTipIV);
        mEtMeasuringPitch = measuringPitchLayout.findViewById(R.id.itemValueET);
        mEtMeasuringPitch.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtMeasuringPitch.setHint("请输入...");


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


    private void initAdapter() {
        String[] debugData = getResources().getStringArray(R.array.collector_channel);
//        dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, debugData);
        dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, debugData);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpDataSettlementMethod.setAdapter(dataAdapter);
        mSpDataSettlementMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String result = mSpDataSettlementMethod.getSelectedItem().toString().replace("mm", "");

                ToastUtil.showShortToast(result);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    private void initData() {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.data_settlement_method:
                showTipDialog(getResources().getString(R.string.collector_address));
                break;

            case R.id.data_response:
                showTipDialog(getResources().getString(R.string.collector_address));
                break;

            case R.id.waiting_interval_per_round:
                showTipDialog(getResources().getString(R.string.collector_address));
                break;

            case R.id.log_output_mode:
                showTipDialog(getResources().getString(R.string.collector_address));
                break;

            case R.id.motor_drive_address:
                showTipDialog(getResources().getString(R.string.collector_address));
                break;

            case R.id.motor_movement_time:
                showTipDialog(getResources().getString(R.string.collector_address));
                break;

            case R.id.motor_pull_up_speed:
                showTipDialog(getResources().getString(R.string.collector_address));
                break;

            case R.id.motor_pull_down_speed:
                showTipDialog(getResources().getString(R.string.collector_address));
                break;

            case R.id.traction_line_length:
                showTipDialog(getResources().getString(R.string.collector_address));
                break;

            case R.id.hole_depth:
                showTipDialog(getResources().getString(R.string.collector_address));
                break;

            case R.id.measuring_pitch:
                showTipDialog(getResources().getString(R.string.collector_address));
                break;

            case R.id.btn_confirm_complete:
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
            ToastUtil.showShortToast("每轮等待间隔不能为空");
            return;
        }

        if (TextUtils.isEmpty(strMotorDriveAddress)) {
            ToastUtil.showShortToast("电机驱动器地址不能为空");
            return;
        }

        if (TextUtils.isEmpty(strMotorMovementTime)) {
            ToastUtil.showShortToast("电机运动时间不能为空");
            return;
        }

        if (TextUtils.isEmpty(strMotorPullUpSpeed)) {
            ToastUtil.showShortToast("电机上拉速度不能为空");
            return;
        }

        if (TextUtils.isEmpty(strMotorPullDownSpeed)) {
            ToastUtil.showShortToast("电机下拉速度不能为空");
            return;
        }

        if (TextUtils.isEmpty(strTractionLineLength)) {
            ToastUtil.showShortToast("牵引线长不能为空");
            return;
        }

        if (TextUtils.isEmpty(strHoleDepth)) {
            ToastUtil.showShortToast("测孔深不能为空");
            return;
        }

        if (TextUtils.isEmpty(strMeasuringPitch)) {
            ToastUtil.showShortToast("测量间距不能为空");
            return;
        }


        int address = Integer.parseInt(strMotorDriveAddress);
        if (address <= 0 || address >= 255) {
            ToastUtil.showShortToast("电机驱动器地址输入有误");
            return;
        }


        StringBuilder stringBuilder = new StringBuilder();


        String cmdStr = String.valueOf(stringBuilder);
        if (!ConfigADMEActivity.isBlueConnected) {
            ToastUtil.showShortToast("蓝牙未连接");
            finish();
            return;
        }

        Message msg = new Message(UUID.randomUUID().toString(), cmdStr, true);
        DeviceFragment.mdBluetoothManager.writeMessage(msg);
        Timber.d("发送执行机构参数配置指令===" + cmdStr);
        finish();
    }

}
