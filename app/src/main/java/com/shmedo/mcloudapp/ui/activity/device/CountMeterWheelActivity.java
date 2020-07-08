package com.shmedo.mcloudapp.ui.activity.device;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.cmd.CommandResult;
import com.dragon.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.event.BluetoothStateEvent;
import com.shmedo.mcloudapp.interfaces.Extras;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class CountMeterWheelActivity extends BaseDeviceConnectActivity {
    @BindView(R.id.back)
    ImageView mIvBack;

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.img_bluetooth)
    ImageView mIvBluetooth;

    @BindView(R.id.et_pulses_number)
    EditText mEtPulsesNumber;

    @BindView(R.id.et_wheel_diameter)
    EditText mEtWheelDiameter;

    @BindView(R.id.et_secondary_correction_factor)
    EditText mEtSecondaryCorrectionFactor;

    @BindView(R.id.et_first_correction_factor)
    EditText mEtFirstCorrectionFactor;

    @BindView(R.id.et_constant)
    EditText mEtConstant;

    @BindView(R.id.et_filter_coefficient)
    EditText mEtFilterCoefficient;

    private String countMeterParam;//计米轮参数

    private String correctionParam;//编码器修正参数


    public static void startActivity(Context context, String countMeterParam, String correctionParam) {
        Intent intent = new Intent(context, CountMeterWheelActivity.class);
        intent.putExtra(Extras.ADME_COUNT_METER_WHEEL_CONFIG_INFO, countMeterParam);
        intent.putExtra(Extras.ENCODER_CORRECTION_PARAMETERS, correctionParam);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_count_meter_wheel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        parseIntent();
    }

    private void initView() {
        mToolbarTitle.setText("参数设置");
        mIvBluetooth.setVisibility(View.GONE);

        mEtPulsesNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtPulsesNumber.setHint("默认值：400个");
        mEtPulsesNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});

        mEtWheelDiameter.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtWheelDiameter.setHint("默认值：30mm");
        mEtWheelDiameter.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});

        mEtSecondaryCorrectionFactor.setInputType(InputType.TYPE_CLASS_PHONE);
        mEtSecondaryCorrectionFactor.setHint("请输入修正参数");
        mEtSecondaryCorrectionFactor.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});

        mEtFirstCorrectionFactor.setInputType(InputType.TYPE_CLASS_PHONE);
        mEtFirstCorrectionFactor.setHint("请输入一次修正参数");
        mEtFirstCorrectionFactor.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});

        mEtConstant.setInputType(InputType.TYPE_CLASS_PHONE);
        mEtConstant.setHint("请输入常数");
        mEtConstant.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});

        mEtFilterCoefficient.setInputType(InputType.TYPE_CLASS_PHONE);
        mEtFilterCoefficient.setHint("请输入滤波器系数(0-9)");
        mEtFilterCoefficient.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});

        if (MCloudApp.isIsBluetoothDeviceConnected()) {
            mIvBluetooth.setImageResource(R.drawable.ic_bluetooth_connected);
        } else {
            mIvBluetooth.setImageResource(R.drawable.ic_bluetooth);
        }
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() != null && intent.getExtras().containsKey(Extras.ADME_COUNT_METER_WHEEL_CONFIG_INFO)) {
            countMeterParam = intent.getStringExtra(Extras.ADME_COUNT_METER_WHEEL_CONFIG_INFO);
            if (TextUtils.isEmpty(countMeterParam)) {
                Timber.e("countMeterParam 为空或者null");
                return;
            }

            if (!countMeterParam.startsWith(CommandResult.COMMAND_RESULT_HEADER)) {
                Timber.e("countMeterParam 格式错误:" + countMeterParam);
                return;
            }

            String[] cmdArray = countMeterParam.replace("\r\n", "").split(",");
            if (cmdArray.length < 3) {
                Timber.e("countMeterParam 格式错误:" + countMeterParam);
                return;
            }

            mEtPulsesNumber.setText(cmdArray[1]);
            mEtWheelDiameter.setText(cmdArray[2]);
        }

        if (intent.getExtras() != null && intent.getExtras().containsKey(Extras.ENCODER_CORRECTION_PARAMETERS)) {
            correctionParam = intent.getStringExtra(Extras.ENCODER_CORRECTION_PARAMETERS);
            if (TextUtils.isEmpty(correctionParam)) {
                Timber.e("correctionParam 为空或者null");
                return;
            }

            if (!correctionParam.startsWith(CommandResult.COMMAND_RESULT_HEADER)) {
                Timber.e("correctionParam 格式错误:" + correctionParam);
                return;
            }

            String[] cmdArray = correctionParam.replace("\r\n", "").split(",");
            if (cmdArray.length < 5) {
                Timber.e("correctionParam 格式错误:" + correctionParam);
                return;
            }

            try {
                mEtSecondaryCorrectionFactor.setText(String.format(Locale.getDefault(), "%.3f", Double.parseDouble(cmdArray[1])));
                mEtFirstCorrectionFactor.setText(String.format(Locale.getDefault(), "%.3f", Double.parseDouble(cmdArray[2])));
                mEtConstant.setText(String.format(Locale.getDefault(), "%.3f", Double.parseDouble(cmdArray[3])));
                mEtFilterCoefficient.setText(cmdArray[4]);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    @OnClick({R.id.back, R.id.img_bluetooth, R.id.btn_confirm_complete})
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
                    findAndConnectSpecificDevice();
                }
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
        String pulsesNumber = mEtPulsesNumber.getText().toString().trim();
        String wheelDiameter = mEtWheelDiameter.getText().toString().trim();
        String secondaryCorrectionFactor = mEtSecondaryCorrectionFactor.getText().toString().trim();
        String firstCorrectionFactor = mEtFirstCorrectionFactor.getText().toString().trim();
        String constant = mEtConstant.getText().toString().trim();
        String filterCoefficient = mEtFilterCoefficient.getText().toString().trim();

        if (TextUtils.isEmpty(pulsesNumber) || Integer.parseInt(secondaryCorrectionFactor) < 0) {
            ToastUtils.show("请输入正确的脉冲数");
            return;
        }
        if (TextUtils.isEmpty(wheelDiameter) || Integer.parseInt(secondaryCorrectionFactor) <= 0) {
            ToastUtils.show("请输入正确的轮直径");
            return;
        }
        if (TextUtils.isEmpty(secondaryCorrectionFactor) || Double.parseDouble(secondaryCorrectionFactor) < 0) {
            ToastUtils.show("请输入正确的二次修正参数");
            return;
        }
        if (TextUtils.isEmpty(firstCorrectionFactor) || Double.parseDouble(secondaryCorrectionFactor) < 0) {
            ToastUtils.show("请输入正确的一次修正参数");
            return;
        }
        if (TextUtils.isEmpty(constant) || Double.parseDouble(secondaryCorrectionFactor) < 0) {
            ToastUtils.show("请输入正确的常数");
            return;
        }
        if (TextUtils.isEmpty(filterCoefficient) || Integer.parseInt(secondaryCorrectionFactor) < 0) {
            ToastUtils.show("请输入正确的滤波器系数");
            return;
        }

        //拼接计米轮参数指令
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("##7023,");
        stringBuilder.append(pulsesNumber + ",");
        stringBuilder.append(wheelDiameter + "\r\n");
        String cmdStr = String.valueOf(stringBuilder);
        sendCommonCommand(cmdStr);

        //拼接编码器修正参数指令
        stringBuilder = new StringBuilder();
        stringBuilder.append("##7030,");
        stringBuilder.append(secondaryCorrectionFactor + ",");
        stringBuilder.append(firstCorrectionFactor + ",");
        stringBuilder.append(constant + ",");
        stringBuilder.append(filterCoefficient + "\r\n");
        cmdStr = String.valueOf(stringBuilder);

        errMsg = "发送指令超时,请稍后尝试";
        startProgressRunnable("正在发送配置指令...", CONFIG_PARAMS_DELAY_MILLIS);
        sendCommonCommand(cmdStr);
    }


    /**
     * 设置显示数据
     */
    private void setResultData(String cmdStr) {
        //计米轮参数配置后应答
        if (cmdStr.startsWith("$$7023") && cmdStr.endsWith("\r\n")) {
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
