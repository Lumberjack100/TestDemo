package com.shmedo.mcloudapp.ui.activity.device;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.event.BluetoothStateEvent;
import com.shmedo.mcloudapp.model.Extras;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;

public class ADMEMotorControlActivity extends BaseDeviceConnectActivity {
    @BindView(R.id.back)
    ImageView mIvBack;

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.img_bluetooth)
    ImageView mIvBluetooth;

    public static void startActivity(Context context, String configInfo) {
        Intent intent = new Intent(context, ADMEMotorControlActivity.class);
        intent.putExtra(Extras.ADME_MOTOR_CONTROL_CONFIG_INFO, configInfo);
        context.startActivity(intent);
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_admemotor_control;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        mToolbarTitle.setText("参数设置");
        mIvBluetooth.setVisibility(View.VISIBLE);

//        mEtPulsesNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
//        mEtPulsesNumber.setHint("默认值：400个");
//        mEtPulsesNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});


        if (MCloudApp.isIsBluetoothDeviceConnected()) {
            mIvBluetooth.setImageResource(R.drawable.ic_bluetooth_connected);
        } else {
            mIvBluetooth.setImageResource(R.drawable.ic_bluetooth);
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
