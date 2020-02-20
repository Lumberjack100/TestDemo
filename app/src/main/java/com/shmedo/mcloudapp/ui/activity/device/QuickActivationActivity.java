package com.shmedo.mcloudapp.ui.activity.device;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.DeviceTypeEnum;
import com.shmedo.mcloudapp.entity.event.BluetoothStateEvent;
import com.shmedo.mcloudapp.ui.activity.MainActivity;
import com.shmedo.mcloudapp.ui.activity.ScanActivity;
import com.shmedo.mcloudapp.util.XPermissionUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class QuickActivationActivity extends BaseDeviceConnectActivity {
    public static final int REQUEST_CODE_SCAN = 0x001;

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.tv_save)
    TextView mTvSave;

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSN;

    @BindView(R.id.tv_active_state)
    TextView mTvActiveState;

    @BindView(R.id.bluetooth_switch_layout)
    View bluetoothSwitchLayout;

    private TextView tvBluetoothState;

    private SwitchButton sbBluetoothState;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, QuickActivationActivity.class);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_quick_activation;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        setSwitchViewListener();
        startScan();
    }


    private void initView() {
        isQuickActivation = true;

        ((TextView) bluetoothSwitchLayout.findViewById(R.id.tv_config_name)).setText("蓝牙连接状态：");
        tvBluetoothState = bluetoothSwitchLayout.findViewById(R.id.tv_device_state);
        sbBluetoothState = bluetoothSwitchLayout.findViewById(R.id.switchButton);
    }

    private void setSwitchViewListener() {
        //连接蓝牙开关
        sbBluetoothState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                sbBluetoothState.setCheckedImmediatelyNoEvent(!isChecked);

                //未连接时，直接打开连接
                if (isChecked) {
                    findAndConnectBleDevice();

                } else {//断开连接处理
                    if (isConfigChange) {
                        isExitMode = false;
                        showSaveDialog(getResources().getString(R.string.disconnect_bluetooth_device_save_param_warn));

                    } else {
                        disconnectDevice();
                        setViewStateByConnectState(false);
                    }
                }
            }
        });
    }


    @OnClick({R.id.back, R.id.ll_scan})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;

            case R.id.ll_scan:
                startScan();
                break;
        }
    }


    @Override
    protected void sendActivateDeviceCmd() {
        startProgressRunnable("正在发送升级指令...", 10000);
        sendCommonCommandImmediately("##12017073\r\n");
    }

    /**
     * 设置显示数据
     */
    private void setResultData(String cmdStr) {
        if (cmdStr.startsWith("$$12017073") && cmdStr.endsWith("\r\n")) {
            if (cmdStr.startsWith("$$12017073e") || cmdStr.startsWith("$$12017073ce")) {
                ToastUtils.show("发送升级指令出现错误!");
                stopProgressRunnable();
                return;
            }
            sendCommonCommandImmediately("##0182\r\n");
            return;
        }

        if (cmdStr.startsWith("$$0182") && cmdStr.endsWith("\r\n")) {
            if (cmdStr.startsWith("$$0182e") || cmdStr.startsWith("$$0182ce")) {
                ToastUtils.show("发送激活指令出现错误!");
                stopProgressRunnable();
                return;
            }

            ToastUtils.show("已激活，设备即将重启并断开连接");
            setSwitchViewState(true, mTvActiveState, "已激活");
            stopProgressRunnable();
            disconnectDevice();
            return;
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

    private void setViewStateByConnectState(boolean isConnected) {
        if (isConnected) {
            sbBluetoothState.setCheckedImmediatelyNoEvent(true);
            setSwitchViewState(true, tvBluetoothState, "已连接");

        } else {
            sbBluetoothState.setCheckedImmediatelyNoEvent(false);
            setSwitchViewState(false, tvBluetoothState, "已断开");
        }
    }

    private void setSwitchViewState(boolean isOpen, TextView textView, String content) {
        textView.setText(content);
        textView.setTextColor(isOpen ? getResources().getColor(R.color.colorPrimary) : getResources().getColor(R.color.gray_807B7B));
    }

    private void startScan() {
        XPermissionUtils.requestPermissionsResult(this, 200, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE},
                new XPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        ScanActivity.startActivityForResult(QuickActivationActivity.this, MainActivity.REQUEST_CODE_SCAN);
                    }

                    @Override
                    public void onPermissionDenied() {
                        XPermissionUtils.showRefusePermissionDialog(QuickActivationActivity.this,
                                getResources().getString(R.string.permission_request_camera_external_storage));
                    }
                });
    }

    private void scanResult(String result) {
        if (TextUtils.isEmpty(result)) {
            showTipDialog("请扫码正确的设备二维码");
            return;
        }

        if (result.contains("=")) {
            String results = result.substring(result.indexOf("=") + 1);
            scan(results);
        } else {
            scan(result);
        }
    }

    /**
     * 处理扫描结果，例如：MEDO,189150L,DAS
     */
    private void scan(String deviceInfo) {
        if (!deviceInfo.startsWith("MEDO")) {
            showTipDialog("请扫码正确的设备二维码");
            return;
        }

        String[] localData = deviceInfo.split(",");
        if (localData.length != 3) {
            showTipDialog("请扫码正确的设备二维码");
            return;
        }

        if (TextUtils.isEmpty(localData[0]) || TextUtils.isEmpty(localData[1]) || TextUtils.isEmpty(localData[2])) {
            showTipDialog("二维码信息不能为空");
            return;
        }

        if (localData[1].length() != 7) {
            showTipDialog("设备标识有误,请扫码正确的设备二维码");
            return;
        }

        if (!DeviceTypeEnum.value(localData[2])) {
            showTipDialog("此设备类型暂时不支持");
            return;
        }
        SN = localData[1];
        MCloudApp.setCurDeviceToken(localData[1]);
        MCloudApp.setCurDeviceMacAddr(null);

        if (localData[2].equals("DAS")) {
            mTvDeviceSN.setText("设备SN：" + localData[1]);
            setSwitchViewState(false, mTvActiveState, "待激活");
            findAndConnectBleDevice();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_SCAN:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        String content = data.getStringExtra(ScanActivity.CODED_CONTENT);
                        Timber.d("扫描结果为：" + content);
                        scanResult(content);
                    }
                }
                break;
        }
    }


    @Override
    public void onBackPressed() {
        disconnectDevice();
        finish();
    }
}
