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
import com.shmedo.das.das.cmd.CommandResult;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.event.BluetoothStateEvent;
import com.shmedo.mcloudapp.model.Extras;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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

    private String configInfo;


    public static void startActivity(Context context, String configInfo) {
        Intent intent = new Intent(context, CountMeterWheelActivity.class);
        intent.putExtra(Extras.ADME_COUNT_METER_WHEEL_CONFIG_INFO, configInfo);
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
        mIvBluetooth.setVisibility(View.VISIBLE);

        mEtPulsesNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtPulsesNumber.setHint("默认值：400个");
        mEtPulsesNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        mEtWheelDiameter.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEtWheelDiameter.setHint("默认值：30mm");
        mEtWheelDiameter.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        if (MCloudApp.isIsBluetoothDeviceConnected()) {
            mIvBluetooth.setImageResource(R.drawable.ic_bluetooth_connected);
        } else {
            mIvBluetooth.setImageResource(R.drawable.ic_bluetooth);
        }
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras().containsKey(Extras.ADME_COUNT_METER_WHEEL_CONFIG_INFO)) {
            configInfo = intent.getStringExtra(Extras.ADME_COUNT_METER_WHEEL_CONFIG_INFO);
        }

        if (TextUtils.isEmpty(configInfo)) {
            Timber.e("configInfo 为空或者null");
            return;
        }

        if (!configInfo.startsWith(CommandResult.COMMAND_RESULT_HEADER)) {
            Timber.e("configInfo 格式错误:" + configInfo);
            return;
        }

        String[] cmdArray = configInfo.replace("\r\n", "").split(",");
        if (cmdArray.length < 3) {
            Timber.e("configInfo 格式错误:" + configInfo);
            return;
        }

        mEtPulsesNumber.setText(cmdArray[1]);
        mEtWheelDiameter.setText(cmdArray[2]);
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
                    findAndConnectBleDevice();
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

        if (TextUtils.isEmpty(pulsesNumber)) {
            ToastUtils.show("脉冲数不能为空");
            return;
        }

        if (TextUtils.isEmpty(wheelDiameter)) {
            ToastUtils.show("轮直径不能为空");
            return;
        }

        //拼接计米轮参数指令
        StringBuilder sbCollector = new StringBuilder();
        sbCollector.append("##7023,");
        sbCollector.append(pulsesNumber + ",");
        sbCollector.append(wheelDiameter + "\r\n");

        String cmdStr = String.valueOf(sbCollector);
        sendCommonCommand(cmdStr);

        showLoadingDialog("正在发送配置指令...");
        hander.postDelayed(dismssDialogRunnable, 5000);
    }


    /**
     * 设置显示数据
     */
    private void setResultData(String cmdStr) {
        //计米轮参数配置后应答
        if (cmdStr.startsWith("$$7023") && cmdStr.endsWith("\r\n")) {
            dismissLoadingDialog();
            hander.removeCallbacks(dismssDialogRunnable);
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
